package com.jhcs.newgram.application.dtos.post;



import com.jhcs.newgram.core.domain.entities.TipoVisibilidade;
import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class PostUpdateDTO {

    @Size(max = 2200, message = "A legenda deve ter no máximo 2200 caracteres")
    private String legenda;

    private String localizacao;

    private TipoVisibilidade visibilidade;

    private List<String> hashtags;

    private List<Long> usuariosMarcados;
}