package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Mensagem;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    Optional<Mensagem> findTopByConversaIdOrderByDataCriacaoDesc(Long conversaId);

    @Query("SELECT COUNT(m) FROM Mensagem m WHERE m.conversa.id = :conversaId " +
            "AND m.remetente.id <> :usuarioId AND m.lida = false")
    long countNaoLidasPorConversa(@Param("conversaId") Long conversaId, @Param("usuarioId") Long usuarioId);

    @Query("SELECT m FROM Mensagem m WHERE m.conversa.id = :conversaId ORDER BY m.dataCriacao ASC")
    Page<Mensagem> findMensagensPorConversa(@Param("conversaId") Long conversaId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Mensagem m WHERE m.conversa.id IN " +
            "(SELECT c.id FROM Conversa c JOIN c.participantes p WHERE p.id = :usuarioId) " +
            "AND m.remetente.id <> :usuarioId AND m.lida = false")
    long countNaoLidasPorUsuario(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("UPDATE Mensagem m SET m.lida = true WHERE m.conversa.id = :conversaId " +
            "AND m.remetente.id <> :usuarioId AND m.lida = false")
    int marcarComoLidas(@Param("conversaId") Long conversaId, @Param("usuarioId") Long usuarioId);
}
