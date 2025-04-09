package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "conversa")
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dataCriacao;
    private Date ultimaInteracao;
    private boolean isGrupo;
    private String nomeGrupo;


    @ManyToMany
    @JoinTable(
            name = "conversa_participante",
            joinColumns = @JoinColumn(name = "conversa_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> participantes = new ArrayList<>();

    @OneToMany(mappedBy = "conversa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensagem> mensagens = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id")
    private Usuario criador;
}