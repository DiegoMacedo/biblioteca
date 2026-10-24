package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
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

    public LivroService(LivroRepository repository, ValidadorPatch validadorPatch) {
        this.repository = repository;
        this.validadorPatch = validadorPatch;
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

    private void verificarIsbnDisponivel(String isbn, Long idAtual) {
        if (repository.isbnEmUso(isbn, idAtual)) {
            throw new ConflitoException("Já existe um livro com o ISBN " + isbn + ".");
        }
    }
}
