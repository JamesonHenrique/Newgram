package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "arquivo")
public class Arquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeOriginal;
    private String nomeArmazenado;
    private String tipo; // "imagem" ou "video"
    private Long tamanho;
    private Date dataUpload;
    private String caminho;
    private String contentType;

    @Enumerated(EnumType.STRING)
    private TipoEntidadeRelacionada tipoEntidade; // POST, STORIE, PERFIL, MENSAGEM, etc.

    private Long entidadeId; // ID da entidade relacionada

    public enum TipoEntidadeRelacionada {
        POST, STORIE, PERFIL, MENSAGEM, COMENTARIO
    }
}