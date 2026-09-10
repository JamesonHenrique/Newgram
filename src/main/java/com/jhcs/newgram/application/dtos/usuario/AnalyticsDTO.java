package com.jhcs.newgram.application.dtos.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnalyticsDTO {
    @Schema(description = "Visualizações dos posts nos últimos 7 dias")
    private long views7d;

    @Schema(description = "Visualizações dos posts nos últimos 30 dias")
    private long views30d;

    @Schema(description = "Contas alcançadas (distintas) nos últimos 7 dias")
    private long alcance7d;

    @Schema(description = "Total de seguidores")
    private long seguidores;

    @Schema(description = "Total de posts")
    private long posts;
}
