package com.jhcs.newgram.application.dtos.mensagem;

import com.jhcs.newgram.core.domain.enums.TipoMensagem;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MensagemCreateDTO {
    private String conteudo;
    private TipoMensagem tipo = TipoMensagem.TEXTO;
    private MultipartFile arquivo;
}