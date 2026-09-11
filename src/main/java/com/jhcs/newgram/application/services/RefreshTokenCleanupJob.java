package com.jhcs.newgram.application.services;

import com.jhcs.newgram.core.domain.repositories.RefreshTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Purga diária de refresh tokens expirados/revogados (higiene da tabela). Roda 1x/dia. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void limparExpirados() {
        int removidos = refreshTokenRepository.limparExpiradas(LocalDateTime.now());
        if (removidos > 0) {
            log.info("Refresh tokens expirados removidos: {}", removidos);
        }
    }
}
