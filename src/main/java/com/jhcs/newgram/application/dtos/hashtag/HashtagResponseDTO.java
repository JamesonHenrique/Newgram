package com.jhcs.newgram.application.dtos.hashtag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HashtagResponseDTO {
    @Schema(description = "ID da hashtag", example = "1")
    private Long id;

    @Schema(description = "Nome da hashtag", example = "#programacao")
    private String nome;

    @Schema(description = "Quantidade de posts associados à hashtag", example = "100")
    private Long quantidadePosts;
}
