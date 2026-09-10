-- V18: mensagens diretas 1:1 (polling; sem WebSocket).

CREATE TABLE IF NOT EXISTS conversa (
    id BIGSERIAL PRIMARY KEY,
    chave_participantes VARCHAR(50) NOT NULL UNIQUE,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_atualizacao TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conversa_participante (
    conversa_id BIGINT NOT NULL REFERENCES conversa (id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT uq_conversa_participante UNIQUE (conversa_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_conversa_participante_usuario ON conversa_participante (usuario_id);

CREATE TABLE IF NOT EXISTS mensagem (
    id BIGSERIAL PRIMARY KEY,
    conversa_id BIGINT NOT NULL REFERENCES conversa (id) ON DELETE CASCADE,
    remetente_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    texto VARCHAR(1000) NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_mensagem_conversa ON mensagem (conversa_id, data_criacao);
