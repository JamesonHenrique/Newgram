package com.jhcs.newgram.application.dtos.enquete;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PollVotoDTO {
    @Schema(description = "ID da opção votada", example = "7")
    @NotNull(message = "Opção é obrigatória")
    private Long opcaoId;
}
