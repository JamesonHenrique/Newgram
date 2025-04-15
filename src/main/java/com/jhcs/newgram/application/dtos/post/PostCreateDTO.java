package com.jhcs.newgram.application.dtos.post;

import com.jhcs.newgram.core.domain.enums.TipoVisibilidade;
import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class PostCreateDTO {
    @Size(max = 2200, message = "A legenda deve ter no máximo 2200 caracteres")
    private String legenda;
    private String localizacao;
    private TipoVisibilidade visibilidade = TipoVisibilidade.PUBLICO;
    private List<String> hashtags;


}