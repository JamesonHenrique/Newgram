package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.PushSubscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUsuarioId(Long usuarioId);

    Optional<PushSubscription> findByEndpoint(String endpoint);

    @Modifying
    @Query("DELETE FROM PushSubscription p WHERE p.endpoint = :endpoint AND p.usuario.id = :usuarioId")
    int deleteByEndpointAndUsuario(@Param("endpoint") String endpoint, @Param("usuarioId") Long usuarioId);
}
