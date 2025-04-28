package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Mensagem;
import com.jhcs.newgram.core.domain.enums.TipoMensagem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByConversaIdOrderByDataEnvioAsc(Long conversaId);}