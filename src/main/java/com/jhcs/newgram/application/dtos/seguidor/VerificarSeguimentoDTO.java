package com.jhcs.newgram.application.dtos.seguidor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificarSeguimentoDTO {
    @Schema(description = "Indica se o usuário autenticado segue o usuário informado", example = "true")
    private boolean seguindo;
}
