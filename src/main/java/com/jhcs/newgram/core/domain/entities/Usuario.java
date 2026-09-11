package com.jhcs.newgram.core.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jhcs.newgram.core.domain.enums.Papel;
import com.jhcs.newgram.core.domain.enums.TipoConta;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String senha;

    @Column(length = 500)
    private String bio;

    /** Conta privada: posts e stories visíveis só para seguidores aceitos. */
    @Column(nullable = false)
    private boolean privado = false;

    /** Selo de verificação (critério/aprovação pela moderação). */
    @Column(nullable = false)
    private boolean verificado = false;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado = false;

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled = false;

    /** Segredo TOTP em Base32 (tratar como credencial: nunca expor). */
    @JsonIgnore
    @Column(name = "totp_secret", length = 64)
    private String totpSecret;

    /** Chave Pix para gorjetas (exibida no perfil). */
    @Column(name = "chave_pix", length = 100)
    private String chavePix;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conta", nullable = false, length = 20)
    private TipoConta tipoConta = TipoConta.PESSOAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Papel papel = Papel.USER;

    /** Hashes SHA-256 de códigos de recuperação do 2FA, separados por vírgula. */
    @JsonIgnore
    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    private String fotoPerfil;

    @JsonIgnore
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "autor")
    private List<Storie> stories = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Salvos> postsSalvos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacao> notificacoes = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private StatusUsuario statusUsuario;

    @Override
    public String getUsername() {
        return email;
    }

    public String getUsuarioName() {
        return username;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                "ROLE_" + (papel == null ? Papel.USER : papel).name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return senha;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
