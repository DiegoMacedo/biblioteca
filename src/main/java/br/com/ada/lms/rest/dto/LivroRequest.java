package br.com.ada.lms.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LivroRequest(
        @NotBlank(message = "titulo é obrigatório")
        String titulo,

        @NotBlank(message = "isbn é obrigatório")
        @Size(max = 20, message = "isbn deve ter no máximo 20 caracteres")
        String isbn,

        @NotNull(message = "anoPublicacao é obrigatório")
        @Positive(message = "anoPublicacao deve ser maior que zero")
        Integer anoPublicacao) {
}
