package br.com.alura.service.http;

import br.com.alura.domain.Agencia;
import br.com.alura.domain.http.AgenciaHttp;
import br.com.alura.exception.AgenciaJaExistenteException;
import br.com.alura.exception.AgenciaNaoAtivaOuNaoEncontradaException;
import br.com.alura.repository.AgenciaRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AgenciaService {

    @RestClient
    SituacaoCadastralHttpService situacaoCadastralHttpService;

    private final AgenciaRepository agenciaRepository;

    public AgenciaService(AgenciaRepository agenciaRepository) {
        this.agenciaRepository = agenciaRepository;
    }


    @WithTransaction
    public Uni<Void> cadastrar(Agencia agencia) {
        Uni<AgenciaHttp> agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
        return agenciaHttp
                .onItem().ifNull().failWith(new AgenciaNaoAtivaOuNaoEncontradaException())
                .onItem().transformToUni(item -> persistirSeEstaAtiva(agencia, item));
    }

    private Uni<Void> persistirSeEstaAtiva(Agencia agencia, AgenciaHttp agenciaHttp) {
        if (!"ATIVO".equals(agenciaHttp.getSituacaoCadastral())) {
            return Uni.createFrom().failure(new AgenciaNaoAtivaOuNaoEncontradaException());
        }
        return agenciaRepository.findByCnpj(agencia.getCnpj())
                .onItem().transformToUni(existente -> {
                    if (existente != null) {
                        return Uni.createFrom().failure(new AgenciaJaExistenteException());
                    }
                    return agenciaRepository.persist(agencia).replaceWithVoid();
                });
    }

    @WithSession
    public Uni<Agencia> buscarPorId(Long idAgencia) { return agenciaRepository.findById(idAgencia); }

    public Uni<Void> deletar(Long idAgencia) {
        Log.info("Deletando agencia por id " + idAgencia);
        return agenciaRepository.deleteById(idAgencia).replaceWithVoid();
    }

    @WithSession
    public Uni<Agencia>  buscarPorCnpj(String cnpj) {
        return agenciaRepository.findByCnpj(cnpj);
    }
}
