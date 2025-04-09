package com.jhcs.newgram.core.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails, Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String username;
    private String email;
    private String senha;
    private String bio;
    private Date dataCriacao;
    private String fotoPerfil;
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @ManyToMany(mappedBy = "seguindo")
    private List<Usuario> seguidores = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "usuario_seguindo",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "seguindo_id")
    )
    private List<Usuario> seguindo = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Salvos> postsSalvos = new ArrayList<>();

    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacao> notificacoes = new ArrayList<>();

    @OneToMany(mappedBy = "remetente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensagem> mensagensEnviadas = new ArrayList<>();

    @OneToMany(mappedBy = "destinatario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensagem> mensagensRecebidas = new ArrayList<>();

    @ManyToMany(mappedBy = "participantes")
    private List<Conversa> conversas = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private StatusUsuario statusUsuario;

    @Override
    public String getUsername() {
        return email;
    }

    public String getUsuarioName() {
        return username;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return senha;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return email;
    }
}