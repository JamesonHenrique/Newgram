package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {

    List<Arquivo> findByTipoEntidadeAndEntidadeId(Arquivo.TipoEntidadeRelacionada tipoEntidade, Long entidadeId);

    @Query("SELECT a FROM Arquivo a WHERE a.tipoEntidade = :tipoEntidade AND a.entidadeId = :entidadeId AND a.tipo = :tipoArquivo")
    List<Arquivo> findByTipoEntidadeAndEntidadeIdAndTipoArquivo(
            @Param("tipoEntidade") Arquivo.TipoEntidadeRelacionada tipoEntidade,
            @Param("entidadeId") Long entidadeId,
            @Param("tipoArquivo") String tipoArquivo);

    void deleteByTipoEntidadeAndEntidadeId(Arquivo.TipoEntidadeRelacionada tipoEntidade, Long entidadeId);
}