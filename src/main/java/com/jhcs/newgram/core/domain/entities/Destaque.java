package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "destaque")
public class Destaque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    private String destaqueFotoDeCapaUrl;

    @ManyToMany
    @JoinTable(
            name = "destaque_storie",
            joinColumns = @JoinColumn(name = "destaque_id"),
            inverseJoinColumns = @JoinColumn(name = "storie_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"destaque_id", "storie_id"})
    )
    private List<Storie> stories = new ArrayList<>();
}
