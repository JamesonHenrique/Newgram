package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Salvos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalvosRepository extends JpaRepository<Salvos, Long> {

    Optional<Salvos> findByUsuarioIdAndPostId(Long usuarioId, Long postId);

    boolean existsByUsuarioIdAndPostId(Long usuarioId, Long postId);

    Page<Salvos> findByUsuarioIdOrderByDataSalvoDesc(Long usuarioId, Pageable pageable);

    Page<Salvos> findByUsuarioIdAndColecaoOrderByDataSalvoDesc(Long usuarioId, String colecao, Pageable pageable);

    @Query("SELECT DISTINCT s.colecao FROM Salvos s WHERE s.usuario.id = :usuarioId AND s.colecao IS NOT NULL")
    List<String> findColecoesByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(s) FROM Salvos s WHERE s.usuario.id = :usuarioId")
    Long countSalvosByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT COUNT(s) FROM Salvos s WHERE s.usuario.id = :usuarioId AND s.colecao = :colecao")
    Long countSalvosByUsuarioIdAndColecao(@Param("usuarioId") Long usuarioId, @Param("colecao") String colecao);

    @Modifying
    void deleteByUsuarioIdAndPostId(Long usuarioId, Long postId);
}
