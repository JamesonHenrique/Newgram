package com.jhcs.newgram.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jhcs.newgram.core.domain.enums.TipoVisibilidade;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 2200)
    private String legenda;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    private String localizacao;
    private boolean arquivado;
    private String imagemUrl;

    @Enumerated(EnumType.STRING)
    private TipoVisibilidade visibilidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Curtida> curtidas = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "post_hashtag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "hashtag_id"})
    )
    private List<Hashtag> hashtags = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "post_marcacoes",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "usuario_id"})
    )
    private List<Usuario> marcacoes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Salvos> salvosPor = new ArrayList<>();
}
