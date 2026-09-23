package br.com.alura.controller;

import br.com.alura.domain.Agencia;
import br.com.alura.service.http.AgenciaService;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/agencia")
public class AgenciaController {

    private final AgenciaService agenciaService;

    public AgenciaController(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @POST
    public Uni<Void> cadastrar(Agencia agencia) {
        return agenciaService.cadastrar(agencia);
    }
}
