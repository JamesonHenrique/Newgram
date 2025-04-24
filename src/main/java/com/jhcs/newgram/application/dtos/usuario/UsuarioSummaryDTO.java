package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UsuarioSummaryDTO {
    @Schema(description = "ID do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nome;

    @Schema(description = "Nome de usuário único", example = "joaosilva")
    private String username;

    @Schema(description = "Indica se o usuário atual está seguindo este usuário", example = "true")
    private boolean seguindoUsuario;

    @Schema(description = "Número de seguidores do usuário", example = "100")
    private Long numeroSeguidores;

    @Schema(description = "Número de usuários que o usuário está seguindo", example = "50")
    private Long numeroSeguindo;

    @Schema(description = "Número de posts do usuário", example = "10")
    private Long numeroPosts;

    @Schema(description = "Foto de perfil do usuário em formato binário", type = "string", format = "binary")
    private byte[] fotoPerfil;
}
