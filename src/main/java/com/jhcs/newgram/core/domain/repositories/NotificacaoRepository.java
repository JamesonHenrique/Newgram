package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    Page<Notificacao> findByDestinatarioIdOrderByDataNotificacaoDesc(Long destinatarioId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notificacao n WHERE n.destinatario.id = :usuarioId AND n.visualizada = false")
    Long countNotificacoesNaoVisualizadasByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("UPDATE Notificacao n SET n.visualizada = true WHERE n.destinatario.id = :usuarioId")
    void marcarTodasComoVisualizadas(@Param("usuarioId") Long usuarioId);

    @Query("SELECT n FROM Notificacao n WHERE n.destinatario.id = :usuarioId AND n.visualizada = false ORDER BY n.dataNotificacao DESC")
    Page<Notificacao> findNotificacoesNaoVisualizadas(@Param("usuarioId") Long usuarioId, Pageable pageable);
}