package com.jhcs.newgram.application.dtos.usuario;

import java.util.Date;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import lombok.Data;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String username;
    private String email;
    private String bio;
    private String fotoPerfilUrl;
    private Date dataCadastro;
    private Long numeroSeguidores;
    private Long numeroSeguindo;
    private Long numeroPosts;
    private boolean seguindoUsuario;
}