package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Denuncia;
import com.jhcs.newgram.core.domain.enums.AlvoDenuncia;
import com.jhcs.newgram.core.domain.enums.StatusDenuncia;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    Optional<Denuncia> findByDenuncianteIdAndTipoAlvoAndAlvoId(Long denuncianteId, AlvoDenuncia tipoAlvo, Long alvoId);

    Page<Denuncia> findByDenuncianteIdOrderByDataCriacaoDesc(Long denuncianteId, Pageable pageable);

    Page<Denuncia> findByStatusOrderByDataCriacaoDesc(StatusDenuncia status, Pageable pageable);
}
