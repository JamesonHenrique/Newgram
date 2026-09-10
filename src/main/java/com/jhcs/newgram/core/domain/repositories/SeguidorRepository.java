package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Seguidor;
import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.StatusSeguimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeguidorRepository extends JpaRepository<Seguidor, Long> {

    Optional<Seguidor> findBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

    boolean existsBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

    boolean existsBySeguidorIdAndSeguidoIdAndStatus(Long seguidorId, Long seguidoId, StatusSeguimento status);

    List<Seguidor> findBySeguidorId(Long seguidorId);

    List<Seguidor> findBySeguidoId(Long seguidoId);

    @Query(value = "SELECT u.* FROM usuario u " +
            "JOIN seguidor s ON u.id = s.seguido_id " +
            "WHERE s.seguidor_id = :usuarioId AND s.status = 'ACEITO' " +
            "ORDER BY RANDOM()", nativeQuery = true)
    List<Usuario> findRandomSeguidosByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Seguidor s WHERE s.seguido.id = :usuarioId AND s.status = :status")
    Long countSeguidoresByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status);

    @Query("SELECT COUNT(s) FROM Seguidor s WHERE s.seguidor.id = :usuarioId AND s.status = :status")
    Long countSeguidosByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status);

    @Modifying
    void deleteBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

    @Query("SELECT s.seguido FROM Seguidor s WHERE s.seguidor.id = :usuarioId AND s.status = :status")
    Page<Usuario> findSeguidosByUsuarioId(
            @Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status, Pageable pageable);

    @Query("SELECT s.seguidor FROM Seguidor s WHERE s.seguido.id = :usuarioId AND s.status = :status")
    Page<Usuario> findSeguidoresByUsuarioId(
            @Param("usuarioId") Long usuarioId, @Param("status") StatusSeguimento status, Pageable pageable);

    @Query("SELECT s FROM Seguidor s WHERE s.seguido.id = :usuarioId AND s.status = 'PENDENTE' ORDER BY s.dataCriacao DESC")
    Page<Seguidor> findSolicitacoesRecebidas(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Seguidor s WHERE s.seguido.id = :usuarioId AND s.status = 'PENDENTE'")
    long countSolicitacoesRecebidas(@Param("usuarioId") Long usuarioId);
}
