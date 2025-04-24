package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TokenDTO {
    @Schema(description = "Token de acesso do usuário", example = "eyJhbGciOiJIUzI1...")
    private String token;

    @Schema(description = "Token de atualização do usuário", example = "dGhpcyBpcyBhIHJlZnJlc2g...")
    private String refreshToken;
}
