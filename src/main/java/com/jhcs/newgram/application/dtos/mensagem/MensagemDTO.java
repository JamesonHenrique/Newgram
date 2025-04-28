package com.jhcs.newgram.application.dtos.mensagem;


import com.jhcs.newgram.core.domain.enums.TipoMensagem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagemDTO {
    private Long id;
    private Long conversaId;
    private Long remetenteId;
    private String remetenteNome;
    private Long destinatarioId;
    private String destinatarioNome;
    private String conteudo;
    private Date dataEnvio;
    private boolean visualizada;
    private boolean entregue;
    private TipoMensagem tipo;
    private String urlMidia;
}