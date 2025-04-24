package com.jhcs.newgram.application.dtos.storie;

import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import com.jhcs.newgram.application.dtos.usuario.UsuarioSummaryDTO;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "DTO para resposta de uma storie")
public class StorieResponseDTO {

    @Schema(description = "ID da storie")
    private Long id;

    @Schema(description = "Legenda da storie")
    private String legenda;

    @Schema(description = "Localização associada à storie")
    private String localizacao;

    @Schema(description = "Data de criação da storie")
    private Date dataCriacao;

    @Schema(description = "Data de expiração da storie")
    private Date dataExpiracao;

    @Schema(description = "Informações do autor da storie")
    private UsuarioSummaryDTO autor;

    @Schema(description = "Número de visualizações da storie")
    private Long numeroVisualizacoes;

    @Schema(description = "Lista de usuários marcados na storie")
    private List<UsuarioSummaryDTO> usuariosMarcados;

    @Schema(description = "Link associado à storie")
    private String link;

    @Schema(description = "Indica se a storie foi visualizada pelo usuário")
    private boolean visualizadoPeloUsuario;

    @Schema(description = "Indica se a storie está destacada")
    private boolean destacado;
}
