package br.com.alura.service.messaging;

import br.com.alura.Agencia;
import br.com.alura.domain.messaging.AgenciaMensagem;
import br.com.alura.service.http.AgenciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class RemoverAgenciaService {

    private final AgenciaService agenciaService;
    //private final ObjectMapper objectMapper;

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
                    .onItem().ifNotNull().transformToUni(agencia ->
                       agenciaService.deletar(agencia.getId())
                    ).replaceWithVoid();
        } catch (Exception e) {
            return Uni.createFrom().failure(e);
        }
    }
}
