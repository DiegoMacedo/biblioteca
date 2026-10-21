package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Genero;
import br.com.ada.lms.rest.dto.GeneroRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Mesmo padrão do AutorService; aqui a regra de unicidade é pelo nome do gênero. */
@ApplicationScoped
public class GeneroService {

    private final Map<Long, Genero> generos = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;

    public GeneroService(ValidadorPatch validadorPatch) {
        this.validadorPatch = validadorPatch;
        criar(new GeneroRequest("Romance", "Narrativas ficcionais centradas em relações e conflitos humanos"));
        criar(new GeneroRequest("Ficção científica", "Histórias sobre tecnologia, espaço e futuros imaginados"));
    }

    public List<Genero> listar() {
        return generos.values().stream()
                .sorted(Comparator.comparing(Genero::getId))
                .toList();
    }

    public Genero buscar(Long id) {
        Genero genero = generos.get(id);
        if (genero == null) {
            throw new RecursoNaoEncontradoException("Gênero", id);
        }
        return genero;
    }

    public Genero criar(GeneroRequest dados) {
        verificarNomeDisponivel(dados.nome(), null);
        Genero genero = new Genero(sequencia.incrementAndGet(), dados.nome(), dados.descricao());
        generos.put(genero.getId(), genero);
        return genero;
    }

    public Genero atualizar(Long id, GeneroRequest dados) {
        Genero genero = buscar(id);
        verificarNomeDisponivel(dados.nome(), id);
        genero.setNome(dados.nome());
        genero.setDescricao(dados.descricao());
        return genero;
    }

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

    public void remover(Long id) {
        if (generos.remove(id) == null) {
            throw new RecursoNaoEncontradoException("Gênero", id);
        }
    }

    private void verificarNomeDisponivel(String nome, Long idAtual) {
        boolean emUso = generos.values().stream()
                .anyMatch(g -> g.getNome().equalsIgnoreCase(nome) && !g.getId().equals(idAtual));
        if (emUso) {
            throw new ConflitoException("Já existe um gênero com o nome " + nome + ".");
        }
    }
}
