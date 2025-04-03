package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Usuario;
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

    @Query("SELECT u FROM Usuario u WHERE u.id IN (SELECT s.seguido.id FROM Seguidor s WHERE s.seguidor.id = :usuarioId)")
    Page<Usuario>  findSeguidosByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);


    @Query("SELECT u FROM Usuario u WHERE u.id IN (SELECT s.seguidor.id FROM Seguidor s WHERE s.seguido.id = :usuarioId)")
    Page<Usuario>  findSeguidoresByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);


    @Query(value = "SELECT u.* FROM usuario u " +
            "JOIN seguidor s ON u.id = s.seguido_id " +
            "WHERE s.seguidor_id = :usuarioId " +
            "ORDER BY RAND() LIMIT :limite", nativeQuery = true)
    List<Usuario> findRandomSeguidosByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("limite") int limite);

    @Query("SELECT u FROM Usuario u WHERE u.id NOT IN :idsExcluidos AND u.id <> :usuarioId ORDER BY FUNCTION('RAND') LIMIT :limite")
    List<Usuario> findSugestoesUsuarios(@Param("usuarioId") Long usuarioId, @Param("idsExcluidos") List<Long> idsExcluidos, @Param("limite") int limite);
}