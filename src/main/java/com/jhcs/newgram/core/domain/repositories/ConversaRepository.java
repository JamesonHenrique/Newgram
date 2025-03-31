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

    @Query("SELECT c FROM Conversa c WHERE :usuario MEMBER OF c.participantes ORDER BY c.ultimaInteracao DESC")
    Page<Conversa> findByParticipante(@Param("usuario") Usuario usuario, Pageable pageable);

    @Query("SELECT c FROM Conversa c WHERE c.isGrupo = false AND SIZE(c.participantes) = 2 AND :usuario1 MEMBER OF c.participantes AND :usuario2 MEMBER OF c.participantes")
    Optional<Conversa> findConversaPrivada(@Param("usuario1") Usuario usuario1, @Param("usuario2") Usuario usuario2);

    @Query("SELECT c FROM Conversa c WHERE c.isGrupo = true AND :usuario MEMBER OF c.participantes ORDER BY c.ultimaInteracao DESC")
    List<Conversa> findGruposByParticipante(@Param("usuario") Usuario usuario);

    @Query("SELECT c FROM Conversa c WHERE c.isGrupo = false AND :usuario MEMBER OF c.participantes ORDER BY c.ultimaInteracao DESC")
    List<Conversa> findConversasPrivadasByParticipante(@Param("usuario") Usuario usuario);

    @Query("SELECT c FROM Conversa c WHERE :usuario MEMBER OF c.participantes AND c.id IN " +
            "(SELECT m.conversa.id FROM Mensagem m WHERE m.remetente.id <> :usuarioId AND m.visualizada = false GROUP BY m.conversa.id) " +
            "ORDER BY c.ultimaInteracao DESC")
    List<Conversa> findConversasComMensagensNaoLidas(@Param("usuario") Usuario usuario, @Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(m) FROM Mensagem m WHERE m.conversa.id = :conversaId AND m.remetente.id <> :usuarioId AND m.visualizada = false")
    Long countMensagensNaoLidasByConversaAndUsuario(@Param("conversaId") Long conversaId, @Param("usuarioId") Long usuarioId);

    @Query("SELECT u FROM Usuario u WHERE u.id IN (SELECT p.id FROM Conversa c JOIN c.participantes p WHERE c.id = :conversaId)")
    List<Usuario> findParticipantesByConversaId(@Param("conversaId") Long conversaId);
}