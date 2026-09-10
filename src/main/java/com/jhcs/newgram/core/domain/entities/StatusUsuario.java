package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "status_usuario", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id"})
})
public class StatusUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private boolean online;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    private String statusPersonalizado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;
}
