package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginDTO {
    @Schema(description = "E-mail do usuário", example = "usuario@example.com")
    private String email;

    @Schema(description = "Senha do usuário", example = "senha123")
    private String senha;
}
