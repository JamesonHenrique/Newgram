package com.jhcs.newgram.application.dtos.destaque;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContagemStoriesDTO {
    @Schema(description = "Quantidade de stories no destaque", example = "5")
    private long quantidade;
}
