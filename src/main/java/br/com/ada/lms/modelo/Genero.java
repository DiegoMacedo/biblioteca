package br.com.ada.lms.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Gênero literário, ex.: "Romance" ou "Ficção científica". */
@Entity
public class Genero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id gerado pelo banco (coluna identity)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column(length = 500)
    private String descricao;

    /** Construtor vazio exigido pelo JPA (o Hibernate cria o objeto e depois preenche os campos). */
    protected Genero() {
    }

    public Genero(String nome, String descricao) {
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
