package com.jhcs.newgram.application.services;

import com.jhcs.newgram.core.domain.entities.Usuario;
import com.jhcs.newgram.core.domain.enums.StatusSeguimento;
import com.jhcs.newgram.core.domain.repositories.SeguidorRepository;
import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

/**
 * Utilidades compartilhadas pelos services: paginação defensiva,
 * checagem de dono e paginação de listas em memória.
 */
public final class Support {

    public static final int MAX_PAGE_SIZE = 50;

    private Support() {
    }

    /** Limita size a {@code maxSize} e garante page >= 0, preservando o sort do pageable. */
    public static Pageable safePage(Pageable pageable, int maxSize) {
        int size = Math.min(pageable.getPageSize(), maxSize);
        if (size <= 0) {
            size = Math.min(20, maxSize);
        }
        int page = Math.max(pageable.getPageNumber(), 0);
        Sort sort = pageable.getSort() != null ? pageable.getSort() : Sort.unsorted();
        return PageRequest.of(page, size, sort);
    }

    /** {@link #safePage(Pageable, int)} com limite padrão de 50. */
    public static Pageable safePage(Pageable pageable) {
        return safePage(pageable, MAX_PAGE_SIZE);
    }

    /** {@link #safePage(Pageable, int)} com limite padrão de 50 e sort explícito. */
    public static Pageable safePage(Pageable pageable, Sort sort) {
        return safePage(pageable, sort, MAX_PAGE_SIZE);
    }

    /** {@link #safePage(Pageable, int)} com sort explícito. */
    public static Pageable safePage(Pageable pageable, Sort sort, int maxSize) {
        int size = Math.min(pageable.getPageSize(), maxSize);
        if (size <= 0) {
            size = Math.min(20, maxSize);
        }
        int page = Math.max(pageable.getPageNumber(), 0);
        return PageRequest.of(page, size, sort != null ? sort : Sort.unsorted());
    }

    /** Lança {@link UnauthorizedException} se {@code logadoId} não for o dono. */
    public static void requireOwner(Long donoId, Long logadoId, String msg) {
        if (donoId == null || logadoId == null || !donoId.equals(logadoId)) {
            throw new UnauthorizedException(msg);
        }
    }

    /** Pagina uma lista em memória (para repos que retornam {@link List}). */
    public static <T> Page<T> pageOf(List<T> lista, Pageable pageable) {
        if (lista == null) {
            lista = Collections.emptyList();
        }
        Pageable safe = safePage(pageable);
        int total = lista.size();
        int from = (int) Math.min(safe.getOffset(), total);
        int to = Math.min(from + safe.getPageSize(), total);
        return new PageImpl<>(lista.subList(from, to), safe, total);
    }

    /** Garante limite 1..50 para parâmetros de limite crus (ex.: "limite" em query nativa). */
    public static int safeLimit(int limite) {
        return Math.min(Math.max(limite, 1), MAX_PAGE_SIZE);
    }

    /**
     * Conteúdo visível? Dono sempre vê; conta pública todos veem;
     * conta privada só seguidor aceito.
     */
    public static boolean conteudoVisivelPara(
            Usuario alvo, Long viewerId, SeguidorRepository seguidorRepository) {
        if (alvo == null) {
            return false;
        }
        if (!alvo.isPrivado()) {
            return true;
        }
        if (viewerId != null && viewerId.equals(alvo.getId())) {
            return true;
        }
        return viewerId != null
                && seguidorRepository.existsBySeguidorIdAndSeguidoIdAndStatus(
                        viewerId, alvo.getId(), StatusSeguimento.ACEITO);
    }
}
