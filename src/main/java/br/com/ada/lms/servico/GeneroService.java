package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Genero;
import br.com.ada.lms.repositorio.GeneroRepository;
import br.com.ada.lms.repositorio.LivroRepository;
import br.com.ada.lms.rest.dto.GeneroRequest;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

/** Mesmo padrão do AutorService; aqui a regra de unicidade é pelo nome do gênero. */
@ApplicationScoped
public class GeneroService {

    private final GeneroRepository repository;
    private final ValidadorPatch validadorPatch;
    private final LivroRepository livroRepository;

    public GeneroService(GeneroRepository repository, ValidadorPatch validadorPatch, LivroRepository livroRepository) {
        this.repository = repository;
        this.validadorPatch = validadorPatch;
        this.livroRepository = livroRepository;
    }

    public List<Genero> listar() {
        return repository.listAll(Sort.by("id"));
    }

    public Genero buscar(Long id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Gênero", id));
    }

    @Transactional
    public Genero criar(GeneroRequest dados) {
        verificarNomeDisponivel(dados.nome(), null);
        Genero genero = new Genero(dados.nome(), dados.descricao());
        repository.persist(genero);
        return genero;
    }

    @Transactional
    public Genero atualizar(Long id, GeneroRequest dados) {
        Genero genero = buscar(id);
        verificarNomeDisponivel(dados.nome(), id);
        genero.setNome(dados.nome());
        genero.setDescricao(dados.descricao());
        return genero;
    }

    @Transactional
    public Genero atualizarParcialmente(Long id, GeneroRequest dados) {
        Genero genero = buscar(id);
        validadorPatch.validarCamposInformados(dados);
        if (dados.nome() != null) {
            verificarNomeDisponivel(dados.nome(), id);
            genero.setNome(dados.nome());
        }
        if (dados.descricao() != null) {
            genero.setDescricao(dados.descricao());
        }
        return genero;
    }

    @Transactional
    public void remover(Long id) {
        // sem esta checagem o banco recusaria o DELETE (chave estrangeira) e a API devolveria 500
        if (livroRepository.existeComGenero(id)) {
            throw new ConflitoException("Gênero com id " + id + " possui livros cadastrados; troque o gênero dos livros antes de removê-lo.");
        }
        if (!repository.deleteById(id)) {
            throw new RecursoNaoEncontradoException("Gênero", id);
        }
    }

    private void verificarNomeDisponivel(String nome, Long idAtual) {
        if (repository.nomeEmUso(nome, idAtual)) {
            throw new ConflitoException("Já existe um gênero com o nome " + nome + ".");
        }
    }
}
