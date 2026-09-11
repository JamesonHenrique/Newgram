package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(String jti);

    @Query("SELECT t FROM RefreshToken t WHERE t.usuario.id = :usuarioId AND t.revogado = false AND t.expiracao > :agora ORDER BY t.dataCriacao DESC")
    List<RefreshToken> findSessoesAtivas(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revogado = true WHERE t.usuario.id = :usuarioId AND t.revogado = false")
    int revogarTodasDoUsuario(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiracao <= :agora OR t.revogado = true")
    int limparExpiradas(@Param("agora") LocalDateTime agora);
}
