package br.com.alura.service.http.saga;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/saga")
@RegisterRestClient(configKey = "situacao-cadastral-api")
public interface SagaHttpService {

    // Caminhos iguais aos do SagaController no banking-validation
    @PUT
    @Path("/sucesso")
    Uni<Void> fecharSagaSucesso(String id);

    @PUT
    @Path("/ignorada")
    Uni<Void> fecharSagaIgnorada(String id);

    @PUT
    @Path("/erro")
    Uni<Void> fecharSagaErro(String id);
}
