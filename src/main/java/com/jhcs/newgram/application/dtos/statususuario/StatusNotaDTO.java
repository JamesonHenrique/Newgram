package com.jhcs.newgram.application.dtos.statususuario;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StatusNotaDTO {
    @Schema(description = "ID do usuário")
    private Long usuarioId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nome")
    private String nome;

    @Schema(description = "Foto de perfil (URL)")
    private String fotoPerfil;

    @Schema(description = "Texto da nota (máx. 60 caracteres)")
    private String nota;

    @Schema(description = "Último acesso")
    private LocalDateTime ultimoAcesso;
}
