-- V17: moderacao (denuncias + bloqueios).

CREATE TABLE IF NOT EXISTS denuncia (
    id BIGSERIAL PRIMARY KEY,
    denunciante_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    tipo_alvo VARCHAR(20) NOT NULL,
    alvo_id BIGINT NOT NULL,
    motivo VARCHAR(100) NOT NULL,
    descricao VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTA',
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_denuncia_autor_alvo UNIQUE (denunciante_id, tipo_alvo, alvo_id)
);

CREATE INDEX IF NOT EXISTS idx_denuncia_status ON denuncia (status);
CREATE INDEX IF NOT EXISTS idx_denuncia_alvo ON denuncia (tipo_alvo, alvo_id);

CREATE TABLE IF NOT EXISTS bloqueio (
    id BIGSERIAL PRIMARY KEY,
    bloqueador_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    bloqueado_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_bloqueio_par UNIQUE (bloqueador_id, bloqueado_id),
    CONSTRAINT ck_bloqueio_diferentes CHECK (bloqueador_id <> bloqueado_id)
);

CREATE INDEX IF NOT EXISTS idx_bloqueio_bloqueador ON bloqueio (bloqueador_id);
CREATE INDEX IF NOT EXISTS idx_bloqueio_bloqueado ON bloqueio (bloqueado_id);
