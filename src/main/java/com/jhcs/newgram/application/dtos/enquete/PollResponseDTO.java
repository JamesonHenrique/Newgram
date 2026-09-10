package com.jhcs.newgram.application.dtos.enquete;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PollResponseDTO {
    @Schema(description = "ID da enquete")
    private Long id;

    @Schema(description = "Pergunta")
    private String pergunta;

    @Schema(description = "Enquete encerrada")
    private boolean encerrada;

    @Schema(description = "Total de votos")
    private long totalVotos;

    @Schema(description = "Opção votada pelo autenticado (null se não votou)")
    private Long minhaOpcaoId;

    @Schema(description = "Opções com apuração")
    private List<PollOpcaoResponseDTO> opcoes;

    @Schema(description = "Encerramento (null = sem prazo)")
    private LocalDateTime encerraEm;
}
