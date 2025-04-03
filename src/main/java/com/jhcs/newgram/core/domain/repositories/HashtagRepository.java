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

    @Query("SELECT h FROM Hashtag h ORDER BY SIZE(h.posts) DESC")
    Page<Object[]> findHashtagsPopulares(Pageable pageable);


    // Consulta corrigida usando JOIN
    @Query("SELECT DISTINCT h FROM Hashtag h JOIN h.posts p WHERE p.autor.id = :usuarioId ORDER BY SIZE(h.posts) DESC")
    List<Hashtag> findHashtagsUsadasPorUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query(value = "SELECT h.* FROM hashtag h " +
            "JOIN post_hashtag ph ON h.id = ph.hashtag_id " +
            "JOIN post p ON ph.post_id = p.id " +
            "WHERE p.data_criacao > DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "GROUP BY h.id " +
            "ORDER BY COUNT(p.id) DESC LIMIT :limite", nativeQuery = true)
    List<Hashtag> findHashtagsTendencia(@Param("limite") int limite);
}
