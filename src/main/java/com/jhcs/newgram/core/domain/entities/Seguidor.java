package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "seguidor")
public class Seguidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dataCriacao;
    private boolean notificacoesAtivadas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguidor_id")
    private Usuario seguidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguido_id")
    private Usuario seguido;
}