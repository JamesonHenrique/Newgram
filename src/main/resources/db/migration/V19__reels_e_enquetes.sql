-- V19: reels (tipo de midia) + enquetes em posts/stories.

ALTER TABLE post ADD COLUMN IF NOT EXISTS tipo_midia VARCHAR(10) NOT NULL DEFAULT 'IMAGEM';

CREATE TABLE IF NOT EXISTS enquete (
    id BIGSERIAL PRIMARY KEY,
    pergunta VARCHAR(300) NOT NULL,
    autor_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    post_id BIGINT REFERENCES post (id) ON DELETE CASCADE,
    storie_id BIGINT REFERENCES storie (id) ON DELETE CASCADE,
    encerra_em TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_enquete_alvo CHECK (
        (post_id IS NOT NULL AND storie_id IS NULL)
        OR (post_id IS NULL AND storie_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_enquete_post ON enquete (post_id);
CREATE INDEX IF NOT EXISTS idx_enquete_storie ON enquete (storie_id);

CREATE TABLE IF NOT EXISTS enquete_opcao (
    id BIGSERIAL PRIMARY KEY,
    enquete_id BIGINT NOT NULL REFERENCES enquete (id) ON DELETE CASCADE,
    texto VARCHAR(100) NOT NULL,
    ordem INT NOT NULL
);

CREATE TABLE IF NOT EXISTS enquete_voto (
    id BIGSERIAL PRIMARY KEY,
    enquete_id BIGINT NOT NULL REFERENCES enquete (id) ON DELETE CASCADE,
    opcao_id BIGINT NOT NULL REFERENCES enquete_opcao (id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_enquete_voto UNIQUE (enquete_id, usuario_id)
);

CREATE INDEX IF NOT EXISTS idx_enquete_voto_opcao ON enquete_voto (opcao_id);
