package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    Page<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(Long destinatarioId, Pageable pageable);

    long countByDestinatarioIdAndLidaFalse(Long usuarioId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.destinatario.id = :usuarioId")
    @org.springframework.transaction.annotation.Transactional
    int marcarTodasComoVisualizadas(@Param("usuarioId") Long usuarioId);

    @Query("SELECT n FROM Notificacao n WHERE n.destinatario.id = :usuarioId AND n.lida = false ORDER BY n.dataCriacao DESC")
    Page<Notificacao> findNotificacoesNaoVisualizadas(@Param("usuarioId") Long usuarioId, Pageable pageable);}