package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Mensagem;
import com.jhcs.newgram.core.domain.entities.TipoMensagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByConversaIdOrderByDataEnvioAsc(Long conversaId);

    Page<Mensagem> findByConversaIdOrderByDataEnvioDesc(Long conversaId, Pageable pageable);

    @Query("SELECT m FROM Mensagem m WHERE m.conversa.id = :conversaId AND m.dataEnvio > :ultimaData ORDER BY m.dataEnvio ASC")
    List<Mensagem> findNovasMensagens(@Param("conversaId") Long conversaId, @Param("ultimaData") Date ultimaData);

    @Query("SELECT COUNT(m) FROM Mensagem m WHERE m.conversa.id = :conversaId AND m.remetente.id <> :usuarioId AND m.visualizada = false")
    Long countMensagensNaoVisualizadas(@Param("conversaId") Long conversaId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("UPDATE Mensagem m SET m.visualizada = true WHERE m.conversa.id = :conversaId AND m.remetente.id <> :usuarioId")
    void marcarMensagensComoVisualizadas(@Param("conversaId") Long conversaId, @Param("usuarioId") Long usuarioId);

    @Query("SELECT m FROM Mensagem m WHERE m.conversa.id = :conversaId AND m.tipo = :tipo ORDER BY m.dataEnvio DESC")
    Page<Mensagem> findByConversaIdAndTipo(@Param("conversaId") Long conversaId, @Param("tipo") TipoMensagem tipo, Pageable pageable);

    @Query("SELECT m FROM Mensagem m WHERE m.conversa.id IN :conversasIds AND m.id IN (SELECT MAX(m2.id) FROM Mensagem m2 WHERE m2.conversa.id IN :conversasIds GROUP BY m2.conversa.id) ORDER BY m.dataEnvio DESC")
    List<Mensagem> findUltimasMensagensPorConversas(@Param("conversasIds") List<Long> conversasIds);
}