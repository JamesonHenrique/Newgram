package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "mensagem")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conteudo;
    private Date dataEnvio;
    private boolean visualizada;

    @Enumerated(EnumType.STRING)
    private TipoMensagem tipo;

    private Integer duracaoAudio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id")
    private Usuario remetente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id")
    private Conversa conversa;

    public boolean isEntregue() {
        return true;
    }

    public void marcarComoVisualizada() {
        this.visualizada = true;
    }
}