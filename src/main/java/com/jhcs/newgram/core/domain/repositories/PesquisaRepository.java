package com.jhcs.newgram.core.domain.repositories;
import com.jhcs.newgram.core.domain.entities.Pesquisa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PesquisaRepository extends JpaRepository<Pesquisa, Long> {

    Page<Pesquisa> findByUsuarioIdOrderByDataPesquisaDesc(Long usuarioId, Pageable pageable);

    @Query("SELECT DISTINCT p.termoPesquisado FROM Pesquisa p WHERE p.usuario.id = :usuarioId ORDER BY p.dataPesquisa DESC")
    List<String> findTermosPesquisadosRecentes(@Param("usuarioId") Long usuarioId, Pageable pageable);

    @Query("SELECT p.termoPesquisado, COUNT(p) as contagem FROM Pesquisa p WHERE p.usuario.id = :usuarioId GROUP BY p.termoPesquisado ORDER BY contagem DESC")
    List<Object[]> findTermosPesquisadosFrequentes(@Param("usuarioId") Long usuarioId, Pageable pageable);

    void deleteByUsuarioIdAndTermoPesquisado(Long usuarioId, String termoPesquisado);
}