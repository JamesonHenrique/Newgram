package com.jhcs.newgram.application.dtos.usuario;
import com.jhcs.newgram.application.dtos.arquivo.ArquivoDTO;
import lombok.Data;

@Data
public class UsuarioSummaryDTO {
    private Long id;
    private String nome;
    private String username;
    private boolean seguindoUsuario;
    private Long numeroSeguidores;
    private Long numeroSeguindo;
    private Long numeroPosts;
    private byte[] fotoPerfil;

}