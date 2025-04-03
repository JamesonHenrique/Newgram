package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Notificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    // Alterar o nome do campo na assinatura do método
    Page<Notificacao> findByDestinatarioIdOrderByDataCriacaoDesc(Long destinatarioId, Pageable pageable);

    // Alterar para usar o nome correto do campo: lida em vez de visualizada
    long countByDestinatarioIdAndLidaFalse(Long usuarioId);

    // Corrigir a query para usar os campos corretos
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.destinatario.id = :usuarioId")
    int marcarTodasComoVisualizadas(@Param("usuarioId") Long usuarioId);

    // Corrigir a query para usar dataCriacao e lida
    @Query("SELECT n FROM Notificacao n WHERE n.destinatario.id = :usuarioId AND n.lida = false ORDER BY n.dataCriacao DESC")
    Page<Notificacao> findNotificacoesNaoVisualizadas(@Param("usuarioId") Long usuarioId, Pageable pageable);}