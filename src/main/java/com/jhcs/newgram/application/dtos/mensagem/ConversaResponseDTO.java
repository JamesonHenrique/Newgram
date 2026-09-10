package com.jhcs.newgram.application.dtos.mensagem;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConversaResponseDTO {
    @Schema(description = "ID da conversa")
    private Long id;

    @Schema(description = "Outro participante (quem não é o autenticado)")
    private Long outroParticipanteId;

    @Schema(description = "Username do outro participante")
    private String outroParticipanteUsername;

    @Schema(description = "Nome do outro participante")
    private String outroParticipanteNome;

    @Schema(description = "Foto de perfil do outro participante")
    private String outroParticipanteFotoPerfil;

    @Schema(description = "Prévia da última mensagem")
    private String ultimaMensagem;

    @Schema(description = "Data da última atividade")
    private LocalDateTime dataAtualizacao;

    @Schema(description = "Não lidas nesta conversa")
    private long naoLidas;
}
