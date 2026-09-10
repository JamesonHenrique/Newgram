package com.jhcs.newgram.application.dtos.moderacao;

import com.jhcs.newgram.core.domain.enums.AlvoDenuncia;
import com.jhcs.newgram.core.domain.enums.StatusDenuncia;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DenunciaResponseDTO {
    @Schema(description = "ID da denúncia")
    private Long id;

    @Schema(description = "Tipo do alvo")
    private AlvoDenuncia tipoAlvo;

    @Schema(description = "ID do alvo")
    private Long alvoId;

    @Schema(description = "Motivo")
    private String motivo;

    @Schema(description = "Status")
    private StatusDenuncia status;

    @Schema(description = "Data de criação")
    private LocalDateTime dataCriacao;
}
