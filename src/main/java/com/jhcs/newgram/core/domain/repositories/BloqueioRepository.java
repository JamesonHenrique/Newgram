package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Bloqueio;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BloqueioRepository extends JpaRepository<Bloqueio, Long> {

    Optional<Bloqueio> findByBloqueadorIdAndBloqueadoId(Long bloqueadorId, Long bloqueadoId);

    boolean existsByBloqueadorIdAndBloqueadoId(Long bloqueadorId, Long bloqueadoId);

    /** Bloqueio em qualquer direção entre os dois usuários. */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Bloqueio b " +
            "WHERE (b.bloqueador.id = :a AND b.bloqueado.id = :b) " +
            "OR (b.bloqueador.id = :b AND b.bloqueado.id = :a)")
    boolean existsBloqueioEntre(@Param("a") Long a, @Param("b") Long b);

    @Query("SELECT b FROM Bloqueio b WHERE b.bloqueador.id = :usuarioId ORDER BY b.dataCriacao DESC")
    Page<Bloqueio> findBloqueiosPorUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Modifying
    void deleteByBloqueadorIdAndBloqueadoId(Long bloqueadorId, Long bloqueadoId);
}
