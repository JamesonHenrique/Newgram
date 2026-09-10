package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Curtida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface CurtidaRepository extends JpaRepository<Curtida, Long> {

    boolean existsByUsuarioIdAndPostId(Long usuarioId, Long postId);

    boolean existsByUsuarioIdAndComentarioId(Long usuarioId, Long comentarioId);

    @Modifying
    void deleteByUsuarioIdAndPostId(Long usuarioId, Long postId);

    @Modifying
    void deleteByUsuarioIdAndComentarioId(Long usuarioId, Long comentarioId);

    Long countByPostId(Long postId);

    Long countByComentarioId(Long comentarioId);


    Page<Curtida> findByPostIdOrderByDataCriacaoDesc(Long postId, Pageable pageable);
    Page<Curtida> findByComentarioIdOrderByDataCriacaoDesc(Long comentarioId, Pageable pageable);
}
