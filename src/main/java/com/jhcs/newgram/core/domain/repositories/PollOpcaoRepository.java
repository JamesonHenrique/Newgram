package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.PollOpcao;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOpcaoRepository extends JpaRepository<PollOpcao, Long> {

    List<PollOpcao> findByEnqueteIdOrderByOrdemAsc(Long enqueteId);
}
