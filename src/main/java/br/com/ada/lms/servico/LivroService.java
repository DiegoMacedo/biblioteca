package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Autor;
import br.com.ada.lms.modelo.Livro;
import br.com.ada.lms.repositorio.LivroRepository;
import br.com.ada.lms.rest.dto.LivroRequest;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/** Mesmo padrão do AutorService; aqui a regra de unicidade é pelo ISBN do livro. */
@ApplicationScoped
public class LivroService {

    private final LivroRepository repository;
    private final ValidadorPatch validadorPatch;
    private final AutorService autorService;
    private final GeneroService generoService;

    public LivroService(LivroRepository repository, ValidadorPatch validadorPatch,
                        AutorService autorService, GeneroService generoService) {
        this.repository = repository;
        this.validadorPatch = validadorPatch;
        this.autorService = autorService;
        this.generoService = generoService;
    }

    public List<Livro> listar() {
        return repository.listAll(Sort.by("id"));
    }

    public Livro buscar(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro", id));
    }

    @Transactional
    public Livro criar(LivroRequest dados) {
        verificarIsbnDisponivel(dados.isbn(), null);
        Livro livro = new Livro(dados.titulo(), dados.isbn(), dados.anoPublicacao());
        repository.persist(livro);
        return livro;
    }

    @Transactional
    public Livro atualizar(Long id, LivroRequest dados) {
        Livro livro = buscar(id);
        verificarIsbnDisponivel(dados.isbn(), id);
        livro.setTitulo(dados.titulo());
        livro.setIsbn(dados.isbn());
        livro.setAnoPublicacao(dados.anoPublicacao());
        return livro;
    }

    @Transactional
    public Livro atualizarParcialmente(Long id, LivroRequest dados) {
        Livro livro = buscar(id);
        validadorPatch.validarCamposInformados(dados);
        if (dados.titulo() != null) {
            livro.setTitulo(dados.titulo());
        }
        if (dados.isbn() != null) {
            verificarIsbnDisponivel(dados.isbn(), id);
            livro.setIsbn(dados.isbn());
        }
        if (dados.anoPublicacao() != null) {
            livro.setAnoPublicacao(dados.anoPublicacao());
        }
        return livro;
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.deleteById(id)) {
            throw new RecursoNaoEncontradoException("Livro", id);
        }
    }

    /** Define (ou troca) o gênero do livro. 404 se o livro ou o gênero não existirem. */
    @Transactional
    public Livro definirGenero(Long id, Long generoId) {
        Livro livro = buscar(id);
        livro.setGenero(generoService.buscar(generoId));
        return livro;
    }

    @Transactional
    public Livro removerGenero(Long id) {
        Livro livro = buscar(id);
        livro.setGenero(null);
        return livro;
    }

    /** Adiciona o autor ao livro: o Hibernate grava a linha em livro_autor no commit. */
    @Transactional
    public Livro adicionarAutor(Long id, Long autorId) {
        Livro livro = buscar(id);
        Autor autor = autorService.buscar(autorId);
        if (livro.getAutores().contains(autor)) {
            throw new ConflitoException("Autor com id " + autorId + " já está associado ao livro " + livro.getTitulo() + ".");
        }
        livro.adicionarAutor(autor);
        return livro;
    }

    /** Remove o autor do livro; se ele não estava associado, não faz nada (idempotente, como um DELETE). */
    @Transactional
    public Livro removerAutor(Long id, Long autorId) {
        Livro livro = buscar(id);
        livro.removerAutor(autorService.buscar(autorId));
        return livro;
    }

    private void verificarIsbnDisponivel(String isbn, Long idAtual) {
        if (repository.isbnEmUso(isbn, idAtual)) {
            throw new ConflitoException("Já existe um livro com o ISBN " + isbn + ".");
        }
    }
}
