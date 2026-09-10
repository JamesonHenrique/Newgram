package com.jhcs.newgram.application.dtos.curtida;

import io.swagger.v3.oas.annotations.media.Schema;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CurtidaResponseDTO {
    @Schema(description = "ID da curtida", example = "1")
    private Long id;

    @Schema(description = "Informações do usuário que realizou a curtida")
    private UsuarioSummaryDTO usuario;

    @Schema(description = "ID do post que foi curtido", example = "123")
    private Long postId;

    @Schema(description = "ID do comentário que foi curtido", example = "456")
    private Long comentarioId;

    @Schema(description = "Data em que a curtida foi realizada", example = "2023-01-01T12:00:00Z")
    private LocalDateTime dataCriacao;
}
