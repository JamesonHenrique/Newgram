package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Conversa 1:1 identificada pela chave ordenada dos participantes. */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "conversa")
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** "menorId_maiorId": garante uma conversa por par (unique). */
    @Column(name = "chave_participantes", nullable = false, unique = true, length = 50)
    private String chaveParticipantes;

    @ManyToMany
    @JoinTable(
            name = "conversa_participante",
            joinColumns = @JoinColumn(name = "conversa_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"conversa_id", "usuario_id"}))
    private Set<Usuario> participantes = new HashSet<>();

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public static String chavePara(Long a, Long b) {
        return Math.min(a, b) + "_" + Math.max(a, b);
    }
}
