package com.jhcs.newgram.application.dtos.conversa;

import io.swagger.v3.oas.annotations.media.Schema;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ConversaResponseDTO {
    @Schema(description = "ID da conversa", example = "1")
    private Long id;

    @Schema(description = "Nome da conversa", example = "Grupo de Estudos")
    private String nome;

    @Schema(description = "Indica se a conversa é um grupo", example = "true")
    private boolean isGrupo;

    @Schema(description = "Descrição da conversa", example = "Grupo para discutir assuntos de estudo.")
    private String descricao;

    @Schema(description = "Data de criação da conversa", example = "2023-01-01T12:00:00Z")
    private Date dataCriacao;

    @Schema(description = "Data da última interação na conversa", example = "2023-01-02T15:30:00Z")
    private Date ultimaInteracao;

    @Schema(description = "Lista de participantes da conversa")
    private List<UsuarioSummaryDTO> participantes;

    @Schema(description = "Última mensagem enviada na conversa")
    private MensagemResponseDTO ultimaMensagem;

    @Schema(description = "Número de mensagens não lidas", example = "5")
    private Long mensagensNaoLidas;

    @Schema(description = "Indica se o usuário atual é participante da conversa", example = "true")
    private boolean participante;
}
