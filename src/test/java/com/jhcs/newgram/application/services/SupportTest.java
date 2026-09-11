package com.jhcs.newgram.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jhcs.newgram.infrastructure.exception.UnauthorizedException;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class SupportTest {

    @Test
    void limitaTamanhoDaPagina() {
        Pageable limitada = Support.safePage(PageRequest.of(0, 500));
        assertEquals(50, limitada.getPageSize());
    }

    @Test
    void paginaNegativaViraZero() {
        // PageRequest rejeita página negativa na construção; usa stub para exercitar o safePage.
        Pageable negativa = mock(Pageable.class);
        when(negativa.getPageNumber()).thenReturn(-3);
        when(negativa.getPageSize()).thenReturn(10);
        when(negativa.getSort()).thenReturn(Sort.unsorted());
        Pageable pagina = Support.safePage(negativa);
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

    @Test
    void extraiMencoesSemDuplicar() {
        assertTrue(Support.extrairMencoes(null).isEmpty());
        assertTrue(Support.extrairMencoes("sem arroba").isEmpty());
        assertEquals(
                java.util.List.of("joao.silva", "maria_99"),
                new java.util.ArrayList<>(Support.extrairMencoes("Oi @joao.silva e @maria_99, viu @joao.silva?")));
    }
}
