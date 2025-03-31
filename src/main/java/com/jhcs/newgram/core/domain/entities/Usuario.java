package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String username;
    private String email;
    private String senha;
    private String bio;

    private Date dataCriacao;
    private boolean verificado;
    private String telefone;
    private String website;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @ManyToMany(mappedBy = "seguindo")
    private List<Usuario> seguidores = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "usuario_seguindo",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "seguindo_id")
    )
    private List<Usuario> seguindo = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Salvos> postsSalvos = new ArrayList<>();

    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacao> notificacoes = new ArrayList<>();

    @OneToMany(mappedBy = "remetente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensagem> mensagensEnviadas = new ArrayList<>();

    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensagem> mensagensRecebidas = new ArrayList<>();

    @ManyToMany(mappedBy = "participantes")
    private List<Conversa> conversas = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private StatusUsuario statusUsuario;
}