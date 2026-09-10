package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.usuario.id = :usuarioId AND t.dataCriacao > :desde")
    long countRecentesPorUsuario(@Param("usuarioId") Long usuarioId, @Param("desde") LocalDateTime desde);

    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.usado = true WHERE t.usuario.id = :usuarioId AND t.usado = false")
    int invalidarTodosDoUsuario(@Param("usuarioId") Long usuarioId);
}
