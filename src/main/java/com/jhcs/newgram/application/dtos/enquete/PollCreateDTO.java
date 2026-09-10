package com.jhcs.newgram.application.dtos.enquete;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PollCreateDTO {
    @Schema(description = "Pergunta (máx. 300 caracteres)")
    @NotBlank(message = "Pergunta é obrigatória")
    @Size(max = 300, message = "Pergunta deve ter no máximo 300 caracteres")
    private String pergunta;

    @Schema(description = "ID do post (ou storieId, nunca os dois)")
    private Long postId;

    @Schema(description = "ID do storie (ou postId, nunca os dois)")
    private Long storieId;

    @Schema(description = "Opções (2 a 4)")
    @Size(min = 2, max = 4, message = "Enquete precisa de 2 a 4 opções")
    private List<@NotBlank(message = "Opção não pode ser vazia") @Size(max = 100) String> opcoes;

    @Schema(description = "Encerramento opcional (futuro)")
    private LocalDateTime encerraEm;
}
