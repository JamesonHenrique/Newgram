package com.jhcs.newgram.core.domain.entities;

import com.jhcs.newgram.core.domain.enums.StatusSeguimento;
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
        name = "seguidor",
        uniqueConstraints = @UniqueConstraint(name = "uq_seguidor_seguido", columnNames = {"seguidor_id", "seguido_id"}))
public class Seguidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    private boolean notificacoesAtivadas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSeguimento status = StatusSeguimento.ACEITO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguidor_id", nullable = false)
    private Usuario seguidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seguido_id", nullable = false)
    private Usuario seguido;
}
