package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.StatusUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface StatusUsuarioRepository extends JpaRepository<StatusUsuario, Long> {

    Optional<StatusUsuario> findByUsuarioId(Long usuarioId);

    @Query("SELECT s FROM StatusUsuario s WHERE s.online = true AND s.usuario.id IN :usuariosIds")
    List<StatusUsuario> findUsuariosOnline(@Param("usuariosIds") List<Long> usuariosIds);

    @Query("SELECT s FROM StatusUsuario s WHERE s.ultimoAcesso > :data AND s.usuario.id IN :usuariosIds")
    List<StatusUsuario> findUsuariosRecentementeAtivos(@Param("data") Date data, @Param("usuariosIds") List<Long> usuariosIds);

    @Query("SELECT s.usuario.id FROM StatusUsuario s WHERE s.online = true")
    List<Long> findAllUsuariosOnlineIds();
}