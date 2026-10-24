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
}
