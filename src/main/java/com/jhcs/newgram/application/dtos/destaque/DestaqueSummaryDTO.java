package com.jhcs.newgram.application.dtos.destaque;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DestaqueSummaryDTO {
    @Schema(description = "ID do destaque", example = "1")
    private Long id;

    @Schema(description = "Nome do destaque", example = "Destaque Principal")
    private String nome;

    @Schema(description = "Data de criação do destaque", example = "2023-01-01T12:00:00Z")
    private LocalDateTime dataCriacao;

    @Schema(description = "Quantidade de stories associadas ao destaque", example = "5")
    private Integer quantidadeStories;
    @Schema(description = "URL da foto de capa do destaque", example = "foto.jpg")
    private String destaqueFotoDeCapaUrl;
}
