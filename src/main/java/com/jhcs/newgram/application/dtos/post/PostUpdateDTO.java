package com.jhcs.newgram.application.dtos.post;

import com.jhcs.newgram.core.domain.enums.TipoVisibilidade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "DTO para atualização de um post")
public class PostUpdateDTO {

    @Schema(description = "Legenda do post (máximo de 2200 caracteres)")
    @Size(max = 2200, message = "A legenda deve ter no máximo 2200 caracteres")
    private String legenda;

    @Schema(description = "Localização associada ao post")
    private String localizacao;

    @Schema(description = "Visibilidade do post")
    private TipoVisibilidade visibilidade;

    @Schema(description = "Lista de hashtags associadas ao post")
    private List<String> hashtags;

    @Schema(description = "Lista de IDs dos usuários marcados no post")
    private List<Long> usuariosMarcados;
}
