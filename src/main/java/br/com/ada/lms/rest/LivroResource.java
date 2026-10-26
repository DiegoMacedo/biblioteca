package br.com.ada.lms.rest;

import br.com.ada.lms.modelo.Livro;
import br.com.ada.lms.rest.dto.LivroRequest;
import br.com.ada.lms.servico.LivroService;
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

/** CRUD de livros: mesmo contrato do AutorResource. */
@Path("/livros")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LivroResource {

    private final LivroService service;

    public LivroResource(LivroService service) {
        this.service = service;
    }

    @GET
    public List<Livro> listar() {
        return service.listar();
    }

    @GET
    @Path("/{id}")
    public Livro buscar(@PathParam("id") Long id) {
        return service.buscar(id);
    }

    @POST
    public Response criar(@NotNull @Valid LivroRequest dados, @Context UriInfo uriInfo) {
        Livro livro = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(livro.getId().toString()).build();
        return Response.created(location).entity(livro).build();
    }

    @PUT
    @Path("/{id}")
    public Livro atualizar(@PathParam("id") Long id, @NotNull @Valid LivroRequest dados) {
        return service.atualizar(id, dados);
    }

    @PATCH
    @Path("/{id}")
    public Livro atualizarParcialmente(@PathParam("id") Long id, LivroRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    @DELETE
    @Path("/{id}")
    public Response remover(@PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }

    // ---------- Gênero principal ----------

    /** PUT: define (ou troca) o gênero do livro. Repetir a chamada dá o mesmo resultado (idempotente). */
    @PUT
    @Path("/{id}/genero/{generoId}")
    @Consumes(MediaType.WILDCARD) // sem corpo: aceita requisição sem Content-Type (senão 415)
    public Livro definirGenero(@PathParam("id") Long id, @PathParam("generoId") Long generoId) {
        return service.definirGenero(id, generoId);
    }

    @DELETE
    @Path("/{id}/genero")
    public Response removerGenero(@PathParam("id") Long id) {
        service.removerGenero(id);
        return Response.noContent().build();
    }

    // ---------- Autores do livro ----------

    /** POST: adiciona o autor (409 se ele já estiver associado). */
    @POST
    @Path("/{id}/autores/{autorId}")
    @Consumes(MediaType.WILDCARD) // sem corpo: aceita requisição sem Content-Type (senão 415)
    public Livro adicionarAutor(@PathParam("id") Long id, @PathParam("autorId") Long autorId) {
        return service.adicionarAutor(id, autorId);
    }

    @DELETE
    @Path("/{id}/autores/{autorId}")
    public Response removerAutor(@PathParam("id") Long id, @PathParam("autorId") Long autorId) {
        service.removerAutor(id, autorId);
        return Response.noContent().build();
    }
}
