package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Autor;
import br.com.ada.lms.repositorio.AutorRepository;
import br.com.ada.lms.repositorio.LivroRepository;
import br.com.ada.lms.rest.dto.AutorRequest;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Regras de negócio. Os dados agora ficam no PostgreSQL: quem conversa com o banco é o AutorRepository.
 *
 * @Transactional: tudo que o método faz no banco é confirmado (commit) junto no final,
 * ou desfeito (rollback) se uma exceção for lançada. Obrigatório para gravar/alterar/remover.
 *
 * Repare que no PUT/PATCH não chamamos nenhum "update": a entidade buscada está "gerenciada"
 * pelo Hibernate, que detecta os setters e gera o UPDATE sozinho no commit (dirty checking).
 */
@ApplicationScoped
public class AutorService {

    private final AutorRepository repository;
    private final ValidadorPatch validadorPatch;
    private final LivroRepository livroRepository;

    public AutorService(AutorRepository repository, ValidadorPatch validadorPatch, LivroRepository livroRepository) {
        this.repository = repository;
        this.validadorPatch = validadorPatch;
        this.livroRepository = livroRepository;
    }

    public List<Autor> listar() {
        return repository.listAll(Sort.by("id"));
    }

    public Autor buscar(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Autor", id));
    }

    @Transactional
    public Autor criar(AutorRequest dados) {
        verificarEmailDisponivel(dados.email(), null);
        Autor autor = new Autor(dados.nome(), dados.email());
        repository.persist(autor); // INSERT; o id é preenchido pelo banco
        return autor;
    }

    /** PUT: substitui TODOS os campos do recurso. */
    @Transactional
    public Autor atualizar(Long id, AutorRequest dados) {
        Autor autor = buscar(id);
        verificarEmailDisponivel(dados.email(), id);
        autor.setNome(dados.nome());
        autor.setEmail(dados.email());
        return autor;
    }

    /** PATCH: altera somente os campos enviados (os que vierem null são mantidos). */
    @Transactional
    public Autor atualizarParcialmente(Long id, AutorRequest dados) {
        Autor autor = buscar(id);
        validadorPatch.validarCamposInformados(dados);
        if (dados.nome() != null) {
            autor.setNome(dados.nome());
        }
        if (dados.email() != null) {
            verificarEmailDisponivel(dados.email(), id);
            autor.setEmail(dados.email());
        }
        return autor;
    }

    @Transactional
    public void remover(Long id) {
        // sem esta checagem o banco recusaria o DELETE (chave estrangeira) e a API devolveria 500
        if (livroRepository.existeComAutor(id)) {
            throw new ConflitoException("Autor com id " + id + " possui livros cadastrados; remova o autor dos livros antes de removê-lo.");
        }
        if (!repository.deleteById(id)) {
            throw new RecursoNaoEncontradoException("Autor", id);
        }
    }

    private void verificarEmailDisponivel(String email, Long idAtual) {
        if (repository.emailEmUso(email, idAtual)) {
            throw new ConflitoException("Já existe um autor com o email " + email + ".");
        }
    }
}
