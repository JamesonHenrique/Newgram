package com.jhcs.newgram.application.dtos.conversa;

import io.swagger.v3.oas.annotations.media.Schema;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import java.util.Date;

@Data
public class ConversaSummaryDTO {
    @Schema(description = "ID da conversa", example = "1")
    private Long id;

    @Schema(description = "Nome da conversa", example = "Grupo de Estudos")
    private String nome;

    @Schema(description = "Indica se a conversa é um grupo", example = "true")
    private boolean isGrupo;

    @Schema(description = "Data da última interação na conversa", example = "2023-01-02T15:30:00Z")
    private Date ultimaInteracao;

    @Schema(description = "Informações do outro participante, caso não seja um grupo")
    private UsuarioSummaryDTO outroParticipante;

    @Schema(description = "Texto da última mensagem enviada", example = "Olá, tudo bem?")
    private String ultimaMensagemTexto;

    @Schema(description = "Número de mensagens não lidas", example = "5")
    private Long mensagensNaoLidas;
}
