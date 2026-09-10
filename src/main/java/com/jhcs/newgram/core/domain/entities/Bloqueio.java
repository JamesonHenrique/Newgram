package com.jhcs.newgram.core.domain.entities;

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
        name = "bloqueio",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_bloqueio_par",
                columnNames = {"bloqueador_id", "bloqueado_id"}))
public class Bloqueio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloqueador_id", nullable = false)
    private Usuario bloqueador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloqueado_id", nullable = false)
    private Usuario bloqueado;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
