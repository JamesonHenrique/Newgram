package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Hashtag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByNome(String nome);

    boolean existsByNome(String nome);

    Page<Hashtag> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT h.nome as nome, SIZE(h.posts) as quantidadePosts FROM Hashtag h ORDER BY SIZE(h.posts) DESC")
    Page<Object[]> findHashtagsPopulares(Pageable pageable);

    @Query("SELECT DISTINCT h FROM Hashtag h JOIN h.posts p WHERE p.autor.id = :usuarioId ORDER BY SIZE(h.posts) DESC")
    List<Hashtag> findHashtagsUsadasPorUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);
    @Query("SELECT h FROM Hashtag h JOIN h.posts p WHERE p.id = :postId")
    List<Hashtag> findByPostId(@Param("postId") Long postId);

}
