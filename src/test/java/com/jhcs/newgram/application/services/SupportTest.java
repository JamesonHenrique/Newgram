package com.jhcs.newgram.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class SupportTest {

    @Test
    void limitaTamanhoDaPagina() {
        Pageable limitada = Support.safePage(PageRequest.of(0, 500));
        assertEquals(50, limitada.getPageSize());
    }

    @Test
    void paginaNegativaViraZero() {
        Pageable pagina = Support.safePage(PageRequest.of(-3, 10));
        assertEquals(0, pagina.getPageNumber());
    }

    @Test
    void donoDiferenteLancaUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> Support.requireOwner(1L, 2L, "sem permissão"));
    }

    @Test
    void paginaListaEmMemoria() {
        List<Integer> itens = IntStream.range(0, 10).boxed().toList();
        Page<Integer> pagina = Support.pageOf(itens, PageRequest.of(1, 4));
        assertEquals(4, pagina.getContent().size());
        assertEquals(10, pagina.getTotalElements());
        assertTrue(pagina.getContent().contains(4));
    }

    @Test
    void limiteCruFicaEntre1e50() {
        assertEquals(1, Support.safeLimit(-5));
        assertEquals(50, Support.safeLimit(9999));
        assertEquals(5, Support.safeLimit(5));
    }
}
