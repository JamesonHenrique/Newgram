package com.jhcs.newgram.application.dtos.usuario;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class UsuarioCreateDTO {
    private String nome;
    private String username;
    private String email;
    private String senha;
    private String bio;
    private String confirmacaoSenha;



}