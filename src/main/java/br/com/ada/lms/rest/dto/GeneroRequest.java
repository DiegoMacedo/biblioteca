package br.com.ada.lms.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneroRequest(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        @Size(max = 500, message = "descricao deve ter no máximo 500 caracteres")
        String descricao) {
}
