package br.com.ada.lms.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidade de domínio que representa um livro do acervo.
 * Ex.: "Dom Casmurro", ISBN 978-85-359-0277-3, publicado em 1899.
 */
@Entity
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id gerado pelo banco (coluna identity)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "ano_publicacao", nullable = false)
    private Integer anoPublicacao;

    /** Gênero principal: vários livros podem ter o mesmo gênero (coluna genero_id na tabela livro). */
    @ManyToOne
    @JoinColumn(name = "genero_id")
    private Genero genero;

    /**
     * Autores do livro: um autor pode ter vários livros (tabela de junção livro_autor).
     * EAGER porque o Jackson serializa o livro depois que a transação já fechou (evita LazyInitializationException).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "livro_autor",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id"))
    private List<Autor> autores = new ArrayList<>();

    /** Construtor vazio exigido pelo JPA (o Hibernate cria o objeto e depois preenche os campos). */
    protected Livro() {
    }

    public Livro(String titulo, String isbn, Integer anoPublicacao) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(Integer anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void adicionarAutor(Autor autor) {
        if (!autores.contains(autor)) {
            autores.add(autor);
        }
    }

    public void removerAutor(Autor autor) {
        autores.remove(autor);
    }
}
