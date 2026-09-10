package com.jhcs.newgram.application.dtos.salvos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalvosResponseDTO {
    @Schema(description = "ID do item salvo", example = "1")
    private Long id;

    @Schema(description = "ID do post salvo", example = "10")
    private Long postId;

    @Schema(description = "ID do usuário que salvou o post", example = "5")
    private Long usuarioId;

    @Schema(description = "Data em que o post foi salvo", example = "2023-01-01T12:00:00Z")
    private LocalDateTime dataSalvo;

    @Schema(description = "Coleção onde o post foi salvo", example = "Favoritos")
    private String colecao;

    @Schema(description = "URL da imagem do post salvo", example = "https://example.com/imagem.jpg")
    private String imagemPostUrl;

    @Schema(description = "Legenda do post salvo", example = "Uma bela paisagem")
    private String legendaPost;
}
