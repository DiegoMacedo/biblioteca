package br.com.ada.lms.modelo;

/**
 * Entidade de domínio que representa um livro do acervo.
 * Ex.: "Dom Casmurro", ISBN 978-85-359-0277-3, publicado em 1899.
 */
public class Livro {

    private Long id;
    private String titulo;
    private String isbn;
    private Integer anoPublicacao;

    public Livro(Long id, String titulo, String isbn, Integer anoPublicacao) {
        this.id = id;
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
