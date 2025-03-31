package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "storie")
public class Storie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dataCriacao;
    private Date dataExpiracao;
    private boolean destacado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @ManyToMany
    @JoinTable(
            name = "storie_visualizacoes",
            joinColumns = @JoinColumn(name = "storie_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> visualizadoPor = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "storie_marcacoes",
            joinColumns = @JoinColumn(name = "storie_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> marcacoes = new ArrayList<>();
}