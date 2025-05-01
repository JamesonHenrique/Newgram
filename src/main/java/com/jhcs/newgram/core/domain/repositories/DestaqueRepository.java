package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Destaque;
import com.jhcs.newgram.core.domain.entities.Storie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DestaqueRepository extends JpaRepository<Destaque, Long> {

    List<Destaque> findByUsuarioIdOrderByNome(Long usuarioId);

    @Query("SELECT d FROM Destaque d LEFT JOIN FETCH d.stories WHERE d.id = :destaqueId")
    Destaque findByIdWithStories(@Param("destaqueId") Long destaqueId);

    @Query("SELECT COUNT(s) FROM Destaque d JOIN d.stories s WHERE d.id = :destaqueId")
    Long countStoriesByDestaqueId(@Param("destaqueId") Long destaqueId);

    @Query("SELECT d FROM Destaque d LEFT JOIN FETCH d.stories WHERE d.usuario.id = :usuarioId")
    List<Destaque> findByUsuarioIdWithStories(@Param("usuarioId") Long usuarioId);
    @Query("SELECT s FROM Destaque d JOIN d.stories s WHERE d.id = :destaqueId ORDER BY s.dataCriacao DESC")
    List<Storie> findStoriesByDestaqueId(@Param("destaqueId") Long destaqueId);
}