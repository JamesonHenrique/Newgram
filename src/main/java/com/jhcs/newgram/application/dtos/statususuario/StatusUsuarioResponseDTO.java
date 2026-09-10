package com.jhcs.newgram.application.dtos.statususuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class StatusUsuarioResponseDTO {
    @Schema(description = "ID do status do usuário", example = "1")
    private Long id;

    @Schema(description = "ID do usuário", example = "5")
    private Long usuarioId;

    @Schema(description = "Indica se o usuário está online", example = "true")
    private boolean online;

    @Schema(description = "Data do último acesso do usuário", example = "2023-01-01T12:00:00Z")
    private Date ultimoAcesso;

    @Schema(description = "Status personalizado do usuário", example = "Trabalhando em um projeto")
    private String statusPersonalizado;

    @Schema(description = "Username do usuário", example = "joaosilva")
    private String username;
}
