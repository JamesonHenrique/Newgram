package com.jhcs.newgram.application.services;

import com.jhcs.newgram.core.domain.repositories.StorieRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Remove stories expirados (fora de destaques ha mais de 24h). Roda 1x/hora. */
@Slf4j
@Component
@RequiredArgsConstructor
public class StorieCleanupJob {

    private final StorieRepository storieRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void removerExpirados() {
        long removidos = storieRepository.deleteByDataExpiracaoBefore(LocalDateTime.now());
        if (removidos > 0) {
            log.info("Stories expirados removidos: {}", removidos);
        }
    }
}
