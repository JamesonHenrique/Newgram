package com.jhcs.newgram.application.dtos.storie;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class StorieCreateDTO {
    private MultipartFile midia; // Adicionado
    private String legenda;
    private String localizacao;
    private List<Long> usuariosMarcados;
    private String link;
    private boolean destacar;
}