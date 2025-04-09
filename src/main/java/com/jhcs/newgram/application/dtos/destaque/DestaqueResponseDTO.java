package com.jhcs.newgram.application.dtos.destaque;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.storie.StorieResponseDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DestaqueResponseDTO {
    private Long id;
    private String nome;
    private Date dataCriacao;
    private Long usuarioId;
    private String usernameUsuario;

    private Integer quantidadeStories;
    private List<StorieResponseDTO> stories;
}