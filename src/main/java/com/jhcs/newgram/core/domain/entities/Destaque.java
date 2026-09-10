package com.jhcs.newgram.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "destaque")
public class Destaque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    private Date dataCriacao;
    private String destaqueFotoDeCapaUrl;
    @ManyToMany
    @JoinTable(
            name = "destaque_storie",
            joinColumns = @JoinColumn(name = "destaque_id"),
            inverseJoinColumns = @JoinColumn(name = "storie_id")
    )
    private List<Storie> stories = new ArrayList<>();
}