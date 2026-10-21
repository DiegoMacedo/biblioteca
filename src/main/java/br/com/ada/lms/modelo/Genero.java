package br.com.ada.lms.modelo;

/** Gênero literário, ex.: "Romance" ou "Ficção científica". */
public class Genero {

    private Long id;
    private String nome;
    private String descricao;

    public Genero(Long id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
