package com.jhcs.newgram.application.dtos.moderacao;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BloqueioResponseDTO {
    @Schema(description = "ID do bloqueio")
    private Long id;

    @Schema(description = "ID do usuário bloqueado")
    private Long bloqueadoId;

    @Schema(description = "Username do usuário bloqueado")
    private String bloqueadoUsername;

    @Schema(description = "Nome do usuário bloqueado")
    private String bloqueadoNome;

    @Schema(description = "Data do bloqueio")
    private LocalDateTime dataCriacao;
}
