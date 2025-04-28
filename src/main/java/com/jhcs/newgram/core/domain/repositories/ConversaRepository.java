package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Conversa;
import com.jhcs.newgram.core.domain.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    @Query("SELECT c FROM Conversa c JOIN c.participantes p WHERE p.id IN (:usuario1Id, :usuario2Id) " +
            "GROUP BY c HAVING COUNT(DISTINCT p.id) = 2 AND c.isGrupo = false")
    Optional<Conversa> findByParticipantesIds(@Param("usuario1Id") Long usuario1Id,
                                              @Param("usuario2Id") Long usuario2Id);

    @Query("SELECT DISTINCT c FROM Conversa c JOIN c.participantes p WHERE p.id = :usuarioId")
    List<Conversa> findByParticipanteId(@Param("usuarioId") Long usuarioId);
}