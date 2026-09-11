package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/** Sessão de refresh token (revogável, expira com o refresh). Guarda só o jti. */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 64)
    private String jti;

    @Column(nullable = false)
    private LocalDateTime expiracao;

    @Column(nullable = false)
    private boolean revogado = false;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public boolean expirado(LocalDateTime agora) {
        return revogado || expiracao == null || !expiracao.isAfter(agora);
    }
}
