package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.VisualizacaoPost;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisualizacaoPostRepository extends JpaRepository<VisualizacaoPost, Long> {

    boolean existsByPostIdAndUsuarioIdAndDia(Long postId, Long usuarioId, LocalDate dia);

    @Query("SELECT COUNT(v) FROM VisualizacaoPost v WHERE v.post.id IN " +
            "(SELECT p.id FROM Post p WHERE p.autor.id = :autorId) AND v.dia >= :desde")
    long countViewsDoAutorDesde(@Param("autorId") Long autorId, @Param("desde") LocalDate desde);

    @Query("SELECT COUNT(DISTINCT v.usuario.id) FROM VisualizacaoPost v WHERE v.post.id IN " +
            "(SELECT p.id FROM Post p WHERE p.autor.id = :autorId) AND v.dia >= :desde AND v.usuario IS NOT NULL")
    long countAlcanceDoAutorDesde(@Param("autorId") Long autorId, @Param("desde") LocalDate desde);

    @Query("SELECT COUNT(v) FROM VisualizacaoPost v WHERE v.post.id = :postId AND v.dia >= :desde")
    long countViewsDoPostDesde(@Param("postId") Long postId, @Param("desde") LocalDate desde);
}
