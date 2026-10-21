package br.com.ada.lms.modelo;

/**
 * Entidade de domínio que representa um autor de livros.
 * O Jackson usa os getters para gerar o JSON de resposta:
 * {"id": 1, "nome": "...", "email": "..."}
 */
public class Autor {

    private Long id;
    private String nome;
    private String email;

    public Autor(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
