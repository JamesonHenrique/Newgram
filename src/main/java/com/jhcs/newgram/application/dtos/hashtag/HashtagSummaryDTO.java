package com.jhcs.newgram.application.dtos.hashtag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HashtagSummaryDTO {
    @Schema(description = "Nome da hashtag", example = "#programacao")
    private String nome;

    @Schema(description = "Quantidade de posts associados à hashtag", example = "100")
    private Long quantidadePosts;
}
