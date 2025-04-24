package com.jhcs.newgram.application.dtos.mensagem;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import com.jhcs.newgram.core.domain.enums.TipoMensagem;
import lombok.Data;

import java.util.Date;

@Data
public class MensagemResponseDTO {
    private Long id;
    private String conteudo;
    private TipoMensagem tipo;
    private UsuarioSummaryDTO remetente;
    private Date dataEnvio;
    private boolean visualizada;
    private boolean deletada;
    private boolean entregue;
}