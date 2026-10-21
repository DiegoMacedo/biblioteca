package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Livro;
import br.com.ada.lms.rest.dto.LivroRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Mesmo padrão do AutorService; aqui a regra de unicidade é pelo ISBN do livro. */
@ApplicationScoped
public class LivroService {

    private final Map<Long, Livro> livros = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;

    public LivroService(ValidadorPatch validadorPatch) {
        this.validadorPatch = validadorPatch;
        criar(new LivroRequest("Dom Casmurro", "978-85-359-0277-3", 1899));
        criar(new LivroRequest("A Hora da Estrela", "978-85-325-3042-3", 1977));
    }

    public List<Livro> listar() {
        return livros.values().stream()
                .sorted(Comparator.comparing(Livro::getId))
                .toList();
    }

    public Livro buscar(Long id) {
        Livro livro = livros.get(id);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro", id);
        }
        return livro;
    }

    public Livro criar(LivroRequest dados) {
        verificarIsbnDisponivel(dados.isbn(), null);
        Livro livro = new Livro(sequencia.incrementAndGet(), dados.titulo(), dados.isbn(), dados.anoPublicacao());
        livros.put(livro.getId(), livro);
        return livro;
    }

    public Livro atualizar(Long id, LivroRequest dados) {
        Livro livro = buscar(id);
        verificarIsbnDisponivel(dados.isbn(), id);
        livro.setTitulo(dados.titulo());
        livro.setIsbn(dados.isbn());
        livro.setAnoPublicacao(dados.anoPublicacao());
        return livro;
    }

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

    public void remover(Long id) {
        if (livros.remove(id) == null) {
            throw new RecursoNaoEncontradoException("Livro", id);
        }
    }

    private void verificarIsbnDisponivel(String isbn, Long idAtual) {
        boolean emUso = livros.values().stream()
                .anyMatch(l -> l.getIsbn().equalsIgnoreCase(isbn) && !l.getId().equals(idAtual));
        if (emUso) {
            throw new ConflitoException("Já existe um livro com o ISBN " + isbn + ".");
        }
    }
}
