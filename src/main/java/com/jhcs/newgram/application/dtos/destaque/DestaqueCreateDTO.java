package com.jhcs.newgram.application.dtos.destaque;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.util.List;

@Data
public class DestaqueCreateDTO {
    @NotBlank(message = "O nome do destaque é obrigatório")
    @Size(max = 50, message = "O nome do destaque deve ter no máximo 50 caracteres")
    private String nome;


    private List<Long> storiesIds;
}