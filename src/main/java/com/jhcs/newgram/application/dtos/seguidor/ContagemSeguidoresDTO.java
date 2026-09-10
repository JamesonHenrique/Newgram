package com.jhcs.newgram.application.dtos.seguidor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContagemSeguidoresDTO {
    @Schema(description = "Quantidade de seguidores do usuário", example = "100")
    private long seguidores;

    @Schema(description = "Quantidade de usuários que o usuário segue", example = "50")
    private long seguidos;
}
