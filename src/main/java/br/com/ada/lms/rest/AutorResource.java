package br.com.ada.lms.rest;

import br.com.ada.lms.modelo.Autor;
import br.com.ada.lms.rest.dto.AutorRequest;
import br.com.ada.lms.servico.AutorService;
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

/**
 * CRUD de autores.
 *
 *   GET    /autores       -> 200 lista
 *   GET    /autores/{id}  -> 200 autor | 404
 *   POST   /autores       -> 201 autor criado + header Location | 400 | 409
 *   PUT    /autores/{id}  -> 200 autor atualizado (todos os campos) | 400 | 404 | 409
 *   PATCH  /autores/{id}  -> 200 autor atualizado (só campos enviados) | 400 | 404 | 409
 *   DELETE /autores/{id}  -> 204 sem corpo | 404
 *
 * Os 4xx são gerados por exceções (ApiExceptionMapper) e pela validação (@Valid).
 */
@Path("/autores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AutorResource {

    private final AutorService service;

    public AutorResource(AutorService service) {
        this.service = service;
    }

    @GET
    public List<Autor> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public Autor buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    /**
     * Aqui retornamos Response (e não Autor) porque queremos controlar o status:
     * 201 Created + header Location apontando para o novo recurso.
     */
    @POST
    public Response criar(@NotNull @Valid AutorRequest dados, @Context UriInfo uriInfo) {
        Autor autor = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(autor.getId().toString()).build();
        return Response.created(location).entity(autor).build();
    }

    @PUT
    @Path("/{id}")
    public Autor atualizar(@PathParam("id") Long id, @NotNull @Valid AutorRequest dados) {
        return service.atualizar(id, dados);
    }

    @PATCH
    @Path("/{id}")
    public Autor atualizarParcialmente(@PathParam("id") Long id, AutorRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
