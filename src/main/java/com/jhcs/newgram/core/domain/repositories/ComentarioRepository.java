package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByPostIdOrderByDataCriacaoAsc(Long postId);

    Page<Comentario> findByPostId(Long postId, Pageable pageable);

    List<Comentario> findByComentarioPaiIdOrderByDataCriacaoAsc(Long comentarioPaiId);

    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.post.id = :postId")
    Long countByPostId(@Param("postId") Long postId);

    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId AND c.comentarioPai IS NULL ORDER BY c.dataCriacao DESC")
    List<Comentario> findComentariosPrincipaisByPostId(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comentario c WHERE c.autor.id = :usuarioId ORDER BY c.dataCriacao DESC")
    Page<Comentario> findByAutorId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.comentarioPai.id = :comentarioId")
    Long countRespostasByComentarioId(@Param("comentarioId") Long comentarioId);
}