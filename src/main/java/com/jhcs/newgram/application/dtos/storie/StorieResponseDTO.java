package com.jhcs.newgram.application.dtos.storie;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class StorieResponseDTO {
    private Long id;
    private ArquivoDTO midia; // Adicionado
    private String legenda;
    private String localizacao;
    private Date dataCriacao;
    private Date dataExpiracao;
    private UsuarioSummaryDTO autor;
    private Long numeroVisualizacoes;
    private List<UsuarioSummaryDTO> usuariosMarcados;
    private String link;
    private boolean visualizadoPeloUsuario;
    private boolean destacado;
}