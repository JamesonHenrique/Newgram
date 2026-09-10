package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenDTO {
    @Schema(description = "Refresh token para renovação", example = "dGhpcyBpcyBhIHJlZnJlc2g...")
    @NotBlank(message = "O refresh token é obrigatório")
    private String refreshToken;
}
