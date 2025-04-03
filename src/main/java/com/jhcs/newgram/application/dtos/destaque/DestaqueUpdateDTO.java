package com.jhcs.newgram.application.dtos.destaque;

import jakarta.validation.constraints.Size;
import lombok.Data;


import java.util.List;

@Data
public class DestaqueUpdateDTO {
    @Size(max = 50, message = "O nome do destaque deve ter no máximo 50 caracteres")
    private String nome;

    private String descricao;

    private List<Long> storiesParaAdicionar;

    private List<Long> storiesParaRemover;
}