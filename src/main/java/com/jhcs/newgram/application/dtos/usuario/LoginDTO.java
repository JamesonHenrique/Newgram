package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @Schema(description = "E-mail do usuário", example = "usuario@example.com")
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @Schema(description = "Senha do usuário", example = "senha123")
    @NotBlank(message = "A senha é obrigatória")
    private String senha;
}
