package com.jhcs.newgram.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate-limit simples por IP+rota (janela fixa de 1 min) para endpoints
 * sensíveis de auth. In-memory: por instância (multi-instância exige
 * Redis/Bucket4j — trocar sem mudar callers).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private record Limite(int maxPorMinuto) {
    }

    private static final Map<String, Limite> LIMITES = Map.of(
            "/auth/login", new Limite(10),
            "/auth/recuperar-senha", new Limite(5),
            "/auth/redefinir-senha", new Limite(5),
            "/auth/2fa/verificar", new Limite(10));

    private static final class Janela {
        final AtomicLong inicio = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger contador = new AtomicInteger(0);
    }

    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !LIMITES.containsKey(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Limite limite = LIMITES.get(request.getServletPath());
        String chave = request.getServletPath() + "|" + ip(request);
        long agora = System.currentTimeMillis();

        Janela janela = janelas.computeIfAbsent(chave, k -> new Janela());
        synchronized (janela) {
            if (agora - janela.inicio.get() > 60_000) {
                janela.inicio.set(agora);
                janela.contador.set(0);
            }
            if (janela.contador.incrementAndGet() > limite.maxPorMinuto()) {
                responder429(response, request);
                return;
            }
        }
        // Evita crescimento infinito do mapa (limpeza oportunista).
        if (janelas.size() > 50_000) {
            janelas.clear();
        }
        chain.doFilter(request, response);
    }

    private static String ip(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void responder429(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> error = Map.of(
                "status", 429,
                "message", "Muitas tentativas. Aguarde um minuto.",
                "path", request.getRequestURI(),
                "timestamp", LocalDateTime.now().toString());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
