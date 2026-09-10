package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Post;
import com.jhcs.newgram.core.domain.enums.TipoVisibilidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByAutorId(Long autorId, Pageable pageable);

    @Query(value = "SELECT p.* FROM post p " +
            "LEFT JOIN (" +
            "SELECT s.seguido_id FROM seguidor s WHERE s.seguidor_id = :usuarioId" +
            ") f ON p.autor_id = f.seguido_id " +
            "WHERE p.arquivado = false " +
            "ORDER BY " +
            "CASE WHEN f.seguido_id IS NOT NULL THEN 1 ELSE 0 END DESC, " +  // Prioritize followed content
            "(SELECT COUNT(*) FROM curtida c WHERE c.post_id = p.id) * 0.6 + " +  // Weight by popularity
            "(SELECT COUNT(*) FROM comentario cm WHERE cm.post_id = p.id) * 0.4 DESC, " +
            "p.data_criacao DESC",
            countQuery = "SELECT COUNT(*) FROM post p WHERE p.arquivado = false",
            nativeQuery = true)
    Page<Post> findFeedByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);
    @Query("SELECT p FROM Post p WHERE p.arquivado = false ORDER BY SIZE(p.curtidas) DESC")
    Page<Post> findPostsPopulares(Pageable pageable);
    @Query(value = "SELECT p.* FROM post p " +
            "WHERE p.arquivado = false " +
            "AND p.data_criacao > :dataCorte " +
            "AND (SELECT COUNT(*) FROM curtida c WHERE c.post_id = p.id) >= :minimoInteracoes " +
            "ORDER BY ((SELECT COUNT(*) FROM curtida c WHERE c.post_id = p.id) + " +
            "(SELECT COUNT(*) FROM comentario cm WHERE cm.post_id = p.id)) DESC, " +
            "p.data_criacao DESC",
            countQuery = "SELECT COUNT(*) FROM post p " +
                    "WHERE p.arquivado = false " +
                    "AND p.data_criacao > :dataCorte " +
                    "AND (SELECT COUNT(*) FROM curtida c WHERE c.post_id = p.id) >= :minimoInteracoes",
            nativeQuery = true)
    Page<Post> findPostsTendencias(
            @Param("dataCorte") LocalDateTime dataCorte,
            @Param("minimoInteracoes") int minimoInteracoes,
            Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM post p " +
            "WHERE p.arquivado = false " +
            "AND p.data_criacao > :dataCorte " +
            "AND (SELECT COUNT(*) FROM curtida c WHERE c.post_id = p.id) >= :minimoInteracoes",
            nativeQuery = true)
    long countPostsTendencias(
            @Param("dataCorte") LocalDateTime dataCorte,
            @Param("minimoInteracoes") int minimoInteracoes);
    @Query("SELECT p FROM Post p " +
            "WHERE p.autor.id IN (SELECT s.seguido.id FROM Seguidor s WHERE s.seguidor.id = :usuarioId) " +
            "AND p.arquivado = false AND SIZE(p.curtidas) > 0 " +
            "ORDER BY SIZE(p.curtidas) * 0.7 + SIZE(p.comentarios) * 0.3 DESC, p.dataCriacao DESC")
    Page<Post> findPopularPostsFromFollowing(@Param("usuarioId") Long usuarioId, Pageable pageable);
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
            "ORDER BY COUNT(c.id) DESC, p.data_criacao DESC",
            countQuery = "SELECT COUNT(DISTINCT p.id) FROM post p " +
                    "JOIN curtida c ON p.id = c.post_id " +
                    "WHERE p.arquivado = false",
            nativeQuery = true)
    Page<Post> findTopPostsByLikes(Pageable pageable);

    @Deprecated
    default Page<Post> searchPostsByContent(String termo, Pageable pageable) {
        return buscarPostsPorLegenda(termo, pageable);
    }


    Page<Post> findByVisibilidadeAndArquivadoFalse(TipoVisibilidade visibilidade, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p JOIN p.hashtags h WHERE h.id = :hashtagId")
    Long countByHashtagId(@Param("hashtagId") Long hashtagId);
    @Query("SELECT p FROM Post p WHERE LOWER(p.legenda) LIKE LOWER(CONCAT('%', :termo, '%')) AND p.arquivado = false")
    Page<Post> buscarPostsPorLegenda(@Param("termo") String termo, Pageable pageable);
    @Query(value = "SELECT p.* FROM post p " +
            "WHERE p.arquivado = false " +
            "AND p.autor_id != :usuarioId " +
            "AND p.id NOT IN (SELECT c.post_id FROM curtida c WHERE c.usuario_id = :usuarioId) " +
            "AND p.id NOT IN (SELECT cm.post_id FROM comentario cm WHERE cm.autor_id = :usuarioId) " +
            "AND (" +
            "  EXISTS (SELECT 1 FROM post_hashtag ph JOIN hashtag h ON ph.hashtag_id = h.id " +
            "          WHERE ph.post_id = p.id AND h.id IN (" +
            "            SELECT ph2.hashtag_id FROM post_hashtag ph2 " +
            "            JOIN curtida c ON ph2.post_id = c.post_id " +
            "            WHERE c.usuario_id = :usuarioId" +
            "          )) " +
            "  OR p.autor_id IN (" +
            "    SELECT DISTINCT p3.autor_id FROM post p3 " +
            "    JOIN curtida c ON p3.id = c.post_id " +
            "    WHERE c.usuario_id = :usuarioId" +
            "  ) " +
            "  OR EXISTS (SELECT 1 FROM post_hashtag ph JOIN hashtag h ON ph.hashtag_id = h.id " +
            "             WHERE ph.post_id = p.id AND h.id IN (" +
            "               SELECT ph3.hashtag_id FROM post_hashtag ph3 " +
            "               JOIN comentario cm ON ph3.post_id = cm.post_id " +
            "               WHERE cm.autor_id = :usuarioId" +
            "             ))" +
            ") " +
            "ORDER BY " +
            "  (CASE WHEN EXISTS (SELECT 1 FROM post_hashtag ph JOIN hashtag h ON ph.hashtag_id = h.id " +
            "                     WHERE ph.post_id = p.id AND h.id IN (" +
            "                       SELECT ph2.hashtag_id FROM post_hashtag ph2 " +
            "                       JOIN curtida c ON ph2.post_id = c.post_id " +
            "                       WHERE c.usuario_id = :usuarioId" +
            "                     )) THEN 3 ELSE 0 END) + " +
            "  (CASE WHEN p.autor_id IN (" +
            "    SELECT DISTINCT p3.autor_id FROM post p3 " +
            "    JOIN curtida c ON p3.id = c.post_id " +
            "    WHERE c.usuario_id = :usuarioId" +
            "  ) THEN 2 ELSE 0 END) + " +
            "  (SELECT COUNT(*) FROM curtida c WHERE c.post_id = p.id) * 0.5 + " +
            "  (SELECT COUNT(*) FROM comentario cm WHERE cm.post_id = p.id) * 0.3 DESC, " +
            "  p.data_criacao DESC",
            countQuery = "SELECT COUNT(*) FROM post p " +
                    "WHERE p.arquivado = false " +
                    "AND p.autor_id != :usuarioId " +
                    "AND p.id NOT IN (SELECT c.post_id FROM curtida c WHERE c.usuario_id = :usuarioId) " +
                    "AND p.id NOT IN (SELECT cm.post_id FROM comentario cm WHERE cm.autor_id = :usuarioId) " +
                    "AND (" +
                    "  EXISTS (SELECT 1 FROM post_hashtag ph JOIN hashtag h ON ph.hashtag_id = h.id " +
                    "          WHERE ph.post_id = p.id AND h.id IN (" +
                    "            SELECT ph2.hashtag_id FROM post_hashtag ph2 " +
                    "            JOIN curtida c ON ph2.post_id = c.post_id " +
                    "            WHERE c.usuario_id = :usuarioId" +
                    "          )) " +
                    "  OR p.autor_id IN (" +
                    "    SELECT DISTINCT p3.autor_id FROM post p3 " +
                    "    JOIN curtida c ON p3.id = c.post_id " +
                    "    WHERE c.usuario_id = :usuarioId" +
                    "  ) " +
                    "  OR EXISTS (SELECT 1 FROM post_hashtag ph JOIN hashtag h ON ph.hashtag_id = h.id " +
                    "             WHERE ph.post_id = p.id AND h.id IN (" +
                    "               SELECT ph3.hashtag_id FROM post_hashtag ph3 " +
                    "               JOIN comentario cm ON ph3.post_id = cm.post_id " +
                    "               WHERE cm.autor_id = :usuarioId" +
                    "             ))" +
                    ")",
            nativeQuery = true)
    Page<Post> buscarPostsRecomendadosParaUsuario(
            @Param("usuarioId") Long usuarioId,
            Pageable pageable);
}
