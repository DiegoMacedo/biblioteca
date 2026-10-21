package br.com.ada.lms.servico;

import br.com.ada.lms.excecao.ConflitoException;
import br.com.ada.lms.excecao.RecursoNaoEncontradoException;
import br.com.ada.lms.modelo.Autor;
import br.com.ada.lms.rest.dto.AutorRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Regras de negócio + "banco de dados" em memória (um Map).
 * Os dados somem quando a aplicação reinicia; persistência real fica para as próximas aulas.
 *
 * @ApplicationScoped: o Quarkus cria UMA instância e a injeta em quem pedir.
 * ConcurrentHashMap/AtomicLong porque várias requisições podem chegar ao mesmo tempo.
 */
@ApplicationScoped
public class AutorService {

    private final Map<Long, Autor> autores = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;

    public AutorService(ValidadorPatch validadorPatch) {
        this.validadorPatch = validadorPatch;
        // alguns dados iniciais para testar o GET logo de cara
        criar(new AutorRequest("Machado de Assis", "machado@literatura.com"));
        criar(new AutorRequest("Clarice Lispector", "clarice@literatura.com"));
    }

    public List<Autor> listar() {
        return autores.values().stream()
                .sorted(Comparator.comparing(Autor::getId))
                .toList();
    }

    public Autor buscar(Long id) {
        Autor autor = autores.get(id);
        if (autor == null) {
            throw new RecursoNaoEncontradoException("Autor", id);
        }
        return autor;
    }

    public Autor criar(AutorRequest dados) {
        verificarEmailDisponivel(dados.email(), null);
        Autor autor = new Autor(sequencia.incrementAndGet(), dados.nome(), dados.email());
        autores.put(autor.getId(), autor);
        return autor;
    }

    /** PUT: substitui TODOS os campos do recurso. */
    public Autor atualizar(Long id, AutorRequest dados) {
        Autor autor = buscar(id);
        verificarEmailDisponivel(dados.email(), id);
        autor.setNome(dados.nome());
        autor.setEmail(dados.email());
        return autor;
    }

    /** PATCH: altera somente os campos enviados (os que vierem null são mantidos). */
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

    public void remover(Long id) {
        if (autores.remove(id) == null) {
            throw new RecursoNaoEncontradoException("Autor", id);
        }
    }

    private void verificarEmailDisponivel(String email, Long idAtual) {
        boolean emUso = autores.values().stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(email) && !a.getId().equals(idAtual));
        if (emUso) {
            throw new ConflitoException("Já existe um autor com o email " + email + ".");
        }
    }
}
