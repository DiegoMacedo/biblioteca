package br.com.ada.lms.rest;

import br.com.ada.lms.modelo.Genero;
import br.com.ada.lms.rest.dto.GeneroRequest;
import br.com.ada.lms.servico.GeneroService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

/** CRUD de gêneros: mesmo contrato do AutorResource. */
@Path("/generos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GeneroResource {

    private final GeneroService service;

    public GeneroResource(GeneroService service) {
        this.service = service;
    }

    @GET
    public List<Genero> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public Genero buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@NotNull @Valid GeneroRequest dados, @Context UriInfo uriInfo) {
        Genero genero = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(genero.getId().toString()).build();
        return Response.created(location).entity(genero).build();
    }

    @PUT
    @Path("/{id}")
    public Genero atualizar(@PathParam("id") Long id, @NotNull @Valid GeneroRequest dados) {
        return service.atualizar(id, dados);
    }

    @PATCH
    @Path("/{id}")
    public Genero atualizarParcialmente(@PathParam("id") Long id, GeneroRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
