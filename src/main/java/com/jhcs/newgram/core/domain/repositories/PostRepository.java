package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Post;
import com.jhcs.newgram.core.domain.enums.TipoVisibilidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByAutorId(Long autorId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.autor.id IN (SELECT s.seguido.id FROM Seguidor s WHERE s.seguidor.id = :usuarioId) AND p.arquivado = false ORDER BY p.dataCriacao DESC")
    Page<Post> findFeedByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.arquivado = false ORDER BY SIZE(p.curtidas) DESC")
    Page<Post> findPostsPopulares(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.hashtags h WHERE h.nome = :hashtag")
    Page<Post> findByHashtag(@Param("hashtag") String hashtag, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.localizacao LIKE %:localizacao% AND p.arquivado = false")
    Page<Post> findByLocalizacao(@Param("localizacao") String localizacao, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.autor.id = :usuarioId AND p.arquivado = false")
    Long countPostsByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT p FROM Post p WHERE p.id IN (SELECT s.post.id FROM Salvos s WHERE s.usuario.id = :usuarioId) ORDER BY p.dataCriacao DESC")
    Page<Post> findSalvosByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id IN (SELECT s.post.id FROM Salvos s WHERE s.usuario.id = :usuarioId AND s.colecao = :colecao) ORDER BY p.dataCriacao DESC")
    Page<Post> findSalvosByUsuarioIdAndColecao(@Param("usuarioId") Long usuarioId, @Param("colecao") String colecao, Pageable pageable);

    @Query(value = "SELECT p.* FROM post p " +
            "JOIN curtida c ON p.id = c.post_id " +
            "WHERE p.arquivado = false " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(c.id) DESC, p.data_criacao DESC LIMIT :limite", nativeQuery = true)
    List<Post> findTopPostsByLikes(@Param("limite") int limite);

    @Query("SELECT p FROM Post p WHERE LOWER(p.legenda) LIKE LOWER(CONCAT('%', :termo, '%')) AND p.arquivado = false")
    Page<Post> searchPostsByContent(@Param("termo") String termo, Pageable pageable);


    Page<Post> findByVisibilidadeAndArquivadoFalse(TipoVisibilidade visibilidade, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p JOIN p.hashtags h WHERE h.id = :hashtagId")
    Long countByHashtagId(@Param("hashtagId") Long hashtagId);

}