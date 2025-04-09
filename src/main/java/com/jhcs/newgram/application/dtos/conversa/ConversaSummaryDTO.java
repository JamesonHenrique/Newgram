package com.jhcs.newgram.application.dtos.conversa;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import java.util.Date;

@Data
public class ConversaSummaryDTO {
    private Long id;
    private String nome;
    private boolean isGrupo;
    private Date ultimaInteracao;
    private UsuarioSummaryDTO outroParticipante;
    private String ultimaMensagemTexto;

    private Long mensagensNaoLidas;
}