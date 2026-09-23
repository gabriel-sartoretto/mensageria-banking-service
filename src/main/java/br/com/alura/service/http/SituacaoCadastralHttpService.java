package br.com.alura.service.http;

import br.com.alura.domain.http.AgenciaHttp;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "situacao-cadastral-api")
@Path("/situacao-cadastral")
public interface SituacaoCadastralHttpService {

    @GET
    @Path("{cnpj}")
    Uni<AgenciaHttp> buscarPorCnpj(@PathParam("cnpj") String cnpj);
}
