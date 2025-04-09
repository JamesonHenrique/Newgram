package com.jhcs.newgram.application.dtos.conversa;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.mensagem.MensagemResponseDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ConversaResponseDTO {
    private Long id;
    private String nome;
    private boolean isGrupo;
    private String descricao;
    private Date dataCriacao;
    private Date ultimaInteracao;
    private List<UsuarioSummaryDTO> participantes;
    private MensagemResponseDTO ultimaMensagem;

    private Long mensagensNaoLidas;
    private boolean participante;
}