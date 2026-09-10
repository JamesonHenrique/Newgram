-- V22: visualizacoes de post (analytics do criador).

CREATE TABLE IF NOT EXISTS visualizacao_post (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    usuario_id BIGINT REFERENCES usuario (id) ON DELETE SET NULL,
    dia DATE NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_view_post_usuario_dia UNIQUE (post_id, usuario_id, dia)
);

CREATE INDEX IF NOT EXISTS idx_view_post_dia ON visualizacao_post (post_id, dia);
