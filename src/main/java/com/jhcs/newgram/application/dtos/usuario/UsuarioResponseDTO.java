package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioResponseDTO {
    @Schema(description = "ID do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário", example = "João Silva")
    private String nome;

    @Schema(description = "Nome de usuário único", example = "joaosilva")
    private String username;

    @Schema(description = "E-mail do usuário", example = "joao@example.com")
    private String email;

    @Schema(description = "Biografia do usuário", example = "Desenvolvedor Java")
    private String bio;

    @Schema(description = "URL da foto de perfil do usuário", example = "https://example.com/foto.jpg")
    private String fotoPerfilUrl;

    @Schema(description = "Data de cadastro do usuário", example = "2023-01-01T12:00:00Z")
    private LocalDateTime dataCadastro;

    @Schema(description = "Número de seguidores do usuário", example = "100")
    private Long numeroSeguidores;

    @Schema(description = "Número de usuários que o usuário está seguindo", example = "50")
    private Long numeroSeguindo;

    @Schema(description = "Número de posts do usuário", example = "10")
    private Long numeroPosts;
    @Schema(description = "Foto de perfil do usuário em formato binário", type = "string", format = "binary")
    private String fotoPerfil;

    @Schema(description = "Indica se o usuário atual está seguindo este usuário", example = "true")
    private boolean seguindoUsuario;

    @Schema(description = "Indica se a conta é privada", example = "false")
    private boolean privado;
}
