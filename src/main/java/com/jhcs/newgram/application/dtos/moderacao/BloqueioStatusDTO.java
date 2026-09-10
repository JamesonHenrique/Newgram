package com.jhcs.newgram.application.dtos.moderacao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BloqueioStatusDTO {
    @Schema(description = "Indica se o usuário autenticado bloqueou o informado", example = "false")
    private boolean bloqueado;
}
