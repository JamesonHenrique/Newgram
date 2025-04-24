package com.jhcs.newgram.application.dtos.pesquisa;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Data
@Schema(description = "DTO para resposta de uma pesquisa")
public class PesquisaResponseDTO {

    @Schema(description = "ID da pesquisa")
    private Long id;

    @Schema(description = "Termo pesquisado")
    private String termoPesquisado;

    @Schema(description = "Data da pesquisa")
    private Date dataPesquisa;

    @Schema(description = "ID do usuário que realizou a pesquisa")
    private Long usuarioId;
}
