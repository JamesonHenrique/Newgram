package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.EmailVerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findByUsuarioId(Long usuarioId);
}
