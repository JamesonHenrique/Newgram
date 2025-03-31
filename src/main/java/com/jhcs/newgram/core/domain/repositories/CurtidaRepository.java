package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Curtida;
import com.jhcs.newgram.core.domain.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CurtidaRepository extends JpaRepository<Curtida, Long> {

    boolean existsByUsuarioIdAndPostId(Long usuarioId, Long postId);

    boolean existsByUsuarioIdAndComentarioId(Long usuarioId, Long comentarioId);

    void deleteByUsuarioIdAndPostId(Long usuarioId, Long postId);

    void deleteByUsuarioIdAndComentarioId(Long usuarioId, Long comentarioId);

    Long countByPostId(Long postId);

    Long countByComentarioId(Long comentarioId);

    // Método corrigido com @Query explícita
    @Query("SELECT c.usuario FROM Curtida c WHERE c.post.id = :postId")
    Page<Usuario> findUsuariosByPostId(@Param("postId") Long postId, Pageable pageable);
}