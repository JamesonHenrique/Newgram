package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.PollVoto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PollVotoRepository extends JpaRepository<PollVoto, Long> {

    Optional<PollVoto> findByEnqueteIdAndUsuarioId(Long enqueteId, Long usuarioId);

    boolean existsByEnqueteIdAndUsuarioId(Long enqueteId, Long usuarioId);

    @Query("SELECT COUNT(v) FROM PollVoto v WHERE v.opcao.id = :opcaoId")
    long countByOpcaoId(@Param("opcaoId") Long opcaoId);

    @Query("SELECT COUNT(v) FROM PollVoto v WHERE v.enquete.id = :enqueteId")
    long countByEnqueteId(@Param("enqueteId") Long enqueteId);
}
