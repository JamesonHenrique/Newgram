package com.jhcs.newgram.core.domain.repositories;

import com.jhcs.newgram.core.domain.entities.Poll;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long> {

    Optional<Poll> findByPostId(Long postId);

    Optional<Poll> findByStorieId(Long storieId);
}
