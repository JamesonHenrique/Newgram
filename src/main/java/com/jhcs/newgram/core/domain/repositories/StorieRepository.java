package com.jhcs.newgram.core.domain.repositories;
import com.jhcs.newgram.core.domain.entities.Storie;
import com.jhcs.newgram.core.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StorieRepository extends JpaRepository<Storie, Long> {

    List<Storie> findByAutorIdAndDataExpiracaoAfterOrderByDataCriacaoDesc(Long autorId, LocalDateTime agora);

    @Query("SELECT s FROM Storie s WHERE s.autor.id IN (SELECT seg.seguido.id FROM Seguidor seg WHERE seg.seguidor.id = :usuarioId AND seg.status = 'ACEITO') AND s.dataExpiracao > :agora ORDER BY s.dataCriacao DESC")
    List<Storie> findStoriesDeSeguidosAtivos(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);

    @Query("SELECT s FROM Storie s WHERE s.autor.id = :autorId AND s.destacado = true ORDER BY s.dataCriacao DESC")
    List<Storie> findStoriesDestacados(@Param("autorId") Long autorId);

    @Query("SELECT DISTINCT s.autor FROM Storie s WHERE s.autor.id IN (SELECT seg.seguido.id FROM Seguidor seg WHERE seg.seguidor.id = :usuarioId AND seg.status = 'ACEITO') AND s.dataExpiracao > :agora")
    List<Usuario> findUsuariosComStoriesAtivos(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);

    @Query("SELECT COUNT(s) FROM Storie s WHERE s.autor.id = :usuarioId AND s.dataExpiracao > :agora")
    Long countStoriesAtivosByUsuarioId(@Param("usuarioId") Long usuarioId, @Param("agora") LocalDateTime agora);

    @Modifying
    long deleteByDataExpiracaoBefore(LocalDateTime agora);
}