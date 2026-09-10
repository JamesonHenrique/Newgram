package com.jhcs.newgram.application.dtos.mensagem;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MensagemResponseDTO {
    @Schema(description = "ID da mensagem")
    private Long id;

    @Schema(description = "ID da conversa")
    private Long conversaId;

    @Schema(description = "ID do remetente")
    private Long remetenteId;

    @Schema(description = "Texto")
    private String texto;

    @Schema(description = "Lida pelo destinatário")
    private boolean lida;

    @Schema(description = "Data de criação")
    private LocalDateTime dataCriacao;

    @Schema(description = "Mensagem do próprio usuário autenticado")
    private boolean minha;
}
