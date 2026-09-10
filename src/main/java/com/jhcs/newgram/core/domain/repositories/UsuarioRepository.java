package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.StatusSeguimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :termo, '%'))")
    Page<Usuario> buscarUsuarios(@Param("termo") String termo, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE u.id IN (SELECT s.seguido.id FROM Seguidor s WHERE s.seguidor.id = :usuarioId AND s.status = :status)")
    Page<Usuario> findSeguidosByUsuarioId(
            @Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE u.id IN (SELECT s.seguidor.id FROM Seguidor s WHERE s.seguido.id = :usuarioId AND s.status = :status)")
    Page<Usuario> findSeguidoresByUsuarioId(
            @Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status, Pageable pageable);

    @Query("SELECT u, COUNT(s) as commonFollowers FROM Usuario u " +
            "JOIN Seguidor s ON u.id = s.seguido.id " +
            "WHERE s.status = :status " +
            "AND s.seguidor.id IN (SELECT seg.seguido.id FROM Seguidor seg WHERE seg.seguidor.id = :usuarioId AND seg.status = :status) " +
            "AND u.id <> :usuarioId " +
            "GROUP BY u.id " +
            "ORDER BY commonFollowers DESC")
    Page<Object[]> findUsuariosPorAmigosEmComum(
            @Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status, Pageable pageable);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN Seguidor s ON u.id = s.seguido.id AND s.status = :status " +
            "WHERE u.id NOT IN (SELECT s.seguido.id FROM Seguidor s WHERE s.seguidor.id = :usuarioId) " +
            "AND u.id <> :usuarioId " +
            "GROUP BY u.id " +
            "ORDER BY COUNT(s) DESC, u.dataCriacao DESC")
    Page<Usuario> findSugestoesUsuarios(
            @Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status, Pageable pageable);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN Seguidor s ON u.id = s.seguido.id AND s.status = :status " +
            "WHERE u.id NOT IN (SELECT s.seguido.id FROM Seguidor s WHERE s.seguidor.id = :usuarioId) " +
            "AND u.id <> :usuarioId " +
            "GROUP BY u.id " +
            "ORDER BY COUNT(s) DESC")
    Page<Usuario> findUsuariosMaisFamosos(
            Pageable pageable, @Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status);

    @Query("SELECT u FROM Usuario u WHERE u.id = :publicId")
    Optional<Usuario> findByPublicId(@Param("publicId") Long publicId);
}