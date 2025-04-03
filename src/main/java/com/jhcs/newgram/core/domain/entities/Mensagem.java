package com.jhcs.newgram.core.domain.entities;

import com.jhcs.newgram.core.domain.enums.TipoMensagem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mensagem")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversa_id", nullable = false)
    private Conversa conversa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    private boolean deletadaPeloRemetente;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_envio", nullable = false)
    private Date dataEnvio;

    @Column(nullable = false)
    private boolean visualizada;
    private boolean entregue;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMensagem tipo;

    @Column(name = "url_midia")
    private String urlMidia;
    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @PrePersist
    protected void onCreate() {
        dataEnvio = new Date();
        visualizada = false;
    }




}