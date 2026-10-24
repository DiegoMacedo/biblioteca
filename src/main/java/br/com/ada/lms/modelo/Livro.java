package br.com.ada.lms.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Entidade de domínio que representa um livro do acervo.
 * Ex.: "Dom Casmurro", ISBN 978-85-359-0277-3, publicado em 1899.
 */
@Entity
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id gerado pelo banco (coluna identity)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "ano_publicacao", nullable = false)
    private Integer anoPublicacao;

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
}
