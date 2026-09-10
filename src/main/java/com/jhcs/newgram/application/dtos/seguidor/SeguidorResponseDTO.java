package com.jhcs.newgram.application.dtos.seguidor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeguidorResponseDTO {
    @Schema(description = "ID da relação de seguidor", example = "1")
    private Long id;

    @Schema(description = "Data de criação da relação de seguidor", example = "2023-01-01T12:00:00Z")
    private LocalDateTime dataCriacao;

    @Schema(description = "Indica se as notificações estão ativadas", example = "true")
    private boolean notificacoesAtivadas;

    @Schema(description = "ID do seguidor", example = "5")
    private Long seguidorId;

    @Schema(description = "Username do seguidor", example = "joaosilva")
    private String seguidorUsername;

    @Schema(description = "Nome do seguidor", example = "João Silva")
    private String seguidorNome;

    @Schema(description = "URL da foto de perfil do seguidor", example = "https://example.com/foto.jpg")
    private String seguidorFotoPerfil;

    @Schema(description = "Indica se o seguidor é verificado", example = "true")
    private boolean seguidorVerificado;

    @Schema(description = "ID do usuário seguido", example = "10")
    private Long seguidoId;

    @Schema(description = "Username do usuário seguido", example = "mariasilva")
    private String seguidoUsername;

    @Schema(description = "Nome do usuário seguido", example = "Maria Silva")
    private String seguidoNome;

    @Schema(description = "URL da foto de perfil do usuário seguido", example = "https://example.com/foto2.jpg")
    private String seguidoFotoPerfil;

    @Schema(description = "Indica se o usuário seguido é verificado", example = "true")
    private boolean seguidoVerificado;
}
