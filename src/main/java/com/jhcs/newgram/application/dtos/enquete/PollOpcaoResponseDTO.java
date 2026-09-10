package com.jhcs.newgram.application.dtos.enquete;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PollOpcaoResponseDTO {
    @Schema(description = "ID da opção")
    private Long id;

    @Schema(description = "Texto da opção")
    private String texto;

    @Schema(description = "Votos nesta opção")
    private long votos;

    @Schema(description = "Percentual (0-100)")
    private double percentual;
}
