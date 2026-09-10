package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Conversa;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    Optional<Conversa> findByChaveParticipantes(String chaveParticipantes);

    @Query("SELECT c FROM Conversa c JOIN c.participantes p WHERE p.id = :usuarioId ORDER BY c.dataAtualizacao DESC")
    Page<Conversa> findConversasPorUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);
}
