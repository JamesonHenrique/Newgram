package com.jhcs.newgram.core.domain.entities;

import com.jhcs.newgram.core.domain.enums.AlvoDenuncia;
import com.jhcs.newgram.core.domain.enums.StatusDenuncia;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "denuncia",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_denuncia_autor_alvo",
                columnNames = {"denunciante_id", "tipo_alvo", "alvo_id"}))
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "denunciante_id", nullable = false)
    private Usuario denunciante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alvo", nullable = false, length = 20)
    private AlvoDenuncia tipoAlvo;

    @Column(name = "alvo_id", nullable = false)
    private Long alvoId;

    @Column(nullable = false, length = 100)
    private String motivo;

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDenuncia status = StatusDenuncia.ABERTA;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
