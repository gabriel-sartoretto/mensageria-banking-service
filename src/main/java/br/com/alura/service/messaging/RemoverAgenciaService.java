package br.com.alura.service.messaging;

import br.com.alura.Agencia;
import br.com.alura.domain.messaging.AgenciaMensagem;
import br.com.alura.service.http.AgenciaService;
import br.com.alura.service.http.saga.SagaHttpService;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.function.Function;

@ApplicationScoped
public class RemoverAgenciaService {

    private final AgenciaService agenciaService;
    //private final ObjectMapper objectMapper;

    @RestClient
    SagaHttpService sagaHttpService;

    public RemoverAgenciaService(
            AgenciaService agenciaService
            //ObjectMapper objectMapper
    ) {
        this.agenciaService = agenciaService;
        //this.objectMapper = new ObjectMapper();
    }

    @WithTransaction
    @Incoming("remover-agencia-channel")
    public Uni<Void> consumirMensagem(Agencia mensagem) {
        try{
            //Usar o ObjectMapper quando for Json
            //AgenciaMensagem agenciaMensagem = objectMapper.readValue(mensagem, AgenciaMensagem.class);
            AgenciaMensagem agenciaMensagem = new AgenciaMensagem(
                    1,
                    mensagem.getNome(),
                    mensagem.getRazaoSocial(),
                    mensagem.getCnpj(),
                    mensagem.getSituacaoCadastral()
            );
            return agenciaService.buscarPorCnpj(agenciaMensagem.getCnpj())
                    .onItem().transformToUni(agencia -> {
                        if (agencia == null) {
                            // Agência já não existe (ex.: reenvio do resync depois de uma remoção cujo
                            // fechamento falhou): nada a remover, a saga é fechada como IGNORED
                            // para não se confundir com uma remoção de fato (COMPLETED)
                            return fecharSaga(mensagem, sagaHttpService::fecharSagaIgnorada);
                        }
                        Uni<Void> remocao = agencia.getNome().contains("ERRO")
                                // Simulação de falha para testar o fluxo de erro da saga
                                ? Uni.createFrom().failure(new IllegalStateException("Falha simulada ao remover agência"))
                                : agenciaService.deletar(agencia.getId());

                        // Qualquer falha na remoção (simulada ou real) fecha a saga como ERROR,
                        // senão ela ficaria OPEN e seria reenviada pelo resync para sempre
                        return remocao
                                .onItemOrFailure().transformToUni((ok, falha) -> falha == null
                                        ? fecharSaga(mensagem, sagaHttpService::fecharSagaSucesso)
                                        : fecharSaga(mensagem, sagaHttpService::fecharSagaErro));
                    });
        } catch (Exception e) {
            return Uni.createFrom().failure(e);
        }
    }

    private Uni<Void> fecharSaga(Agencia mensagem, Function<String, Uni<Void>> fechamento) {
        // Mensagens antigas (antes do campo sagaId no schema) chegam sem id: não há saga para fechar
        return mensagem.getSagaId() == null
                ? Uni.createFrom().voidItem()
                : fechamento.apply(mensagem.getSagaId());
    }
}
