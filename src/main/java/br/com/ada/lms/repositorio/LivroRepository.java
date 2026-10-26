package br.com.ada.lms.repositorio;

import br.com.ada.lms.modelo.Livro;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LivroRepository implements PanacheRepository<Livro> {

    /** Existe OUTRO livro (id diferente de idAtual) com este ISBN? idAtual = null no cadastro. */
    public boolean isbnEmUso(String isbn, Long idAtual) {
        if (idAtual == null) {
            return count("lower(isbn) = lower(?1)", isbn) > 0;
        }
        return count("lower(isbn) = lower(?1) and id <> ?2", isbn, idAtual) > 0;
    }

    /** Algum livro usa este gênero? Usado para impedir remover um gênero ainda referenciado. */
    public boolean existeComGenero(Long generoId) {
        return count("genero.id", generoId) > 0;
    }

    /** Algum livro tem este autor? "join l.autores a" percorre a tabela de junção livro_autor. */
    public boolean existeComAutor(Long autorId) {
        return count("select count(l) from Livro l join l.autores a where a.id = ?1", autorId) > 0;
    }
}
