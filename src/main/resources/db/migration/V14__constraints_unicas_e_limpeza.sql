-- V14: constraints únicas defensivas + remoção da tabela legada usuario_seguindo.
-- V1-V3 já criaram UNIQUE inline (email, username, seguidor, salvos, hashtag, status);
-- aqui a deduplicação é defensiva e as constraints novas usam nomes explícitos idempotentes.
-- Blacklist de refresh token fica em memória por ora: nenhuma tabela criada aqui.

-- 1. Deduplicação defensiva (mantém o menor id) --------------------------

DELETE FROM usuario a USING usuario b
WHERE a.id > b.id AND a.email = b.email;

DELETE FROM usuario a USING usuario b
WHERE a.id > b.id AND a.username = b.username;

DELETE FROM seguidor a USING seguidor b
WHERE a.id > b.id
  AND a.seguidor_id = b.seguidor_id
  AND a.seguido_id = b.seguido_id;

DELETE FROM curtida a USING curtida b
WHERE a.id > b.id
  AND a.post_id IS NOT NULL AND b.post_id IS NOT NULL
  AND a.usuario_id = b.usuario_id
  AND a.post_id = b.post_id;

DELETE FROM curtida a USING curtida b
WHERE a.id > b.id
  AND a.comentario_id IS NOT NULL AND b.comentario_id IS NOT NULL
  AND a.usuario_id = b.usuario_id
  AND a.comentario_id = b.comentario_id;

DELETE FROM salvos a USING salvos b
WHERE a.id > b.id
  AND a.usuario_id = b.usuario_id
  AND a.post_id = b.post_id;

DELETE FROM hashtag a USING hashtag b
WHERE a.id > b.id AND a.nome = b.nome;

DELETE FROM status_usuario a USING status_usuario b
WHERE a.id > b.id AND a.usuario_id = b.usuario_id;

-- 2. Tabela legada do ManyToMany Usuario.seguindo (fonte oficial: seguidor) --
DROP TABLE IF EXISTS usuario_seguindo;

-- 3. Constraints únicas nomeadas (idempotentes) ---------------------------

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_usuario_email') THEN
        ALTER TABLE usuario ADD CONSTRAINT uq_usuario_email UNIQUE (email);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_usuario_username') THEN
        ALTER TABLE usuario ADD CONSTRAINT uq_usuario_username UNIQUE (username);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_seguidor_seguidor_seguido') THEN
        ALTER TABLE seguidor ADD CONSTRAINT uq_seguidor_seguidor_seguido UNIQUE (seguidor_id, seguido_id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_salvos_usuario_post') THEN
        ALTER TABLE salvos ADD CONSTRAINT uq_salvos_usuario_post UNIQUE (usuario_id, post_id);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_hashtag_nome') THEN
        ALTER TABLE hashtag ADD CONSTRAINT uq_hashtag_nome UNIQUE (nome);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_status_usuario_usuario') THEN
        ALTER TABLE status_usuario ADD CONSTRAINT uq_status_usuario_usuario UNIQUE (usuario_id);
    END IF;
END $$;

-- Curtida: post_id/comentario_id são anuláveis com CHECK de exclusividade,
-- então a unicidade por par precisa de índice parcial (UNIQUE trata NULL como distinto).
CREATE UNIQUE INDEX IF NOT EXISTS uq_curtida_usuario_post
    ON curtida (usuario_id, post_id) WHERE post_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_curtida_usuario_comentario
    ON curtida (usuario_id, comentario_id) WHERE comentario_id IS NOT NULL;

-- post_hashtag, post_marcacoes e destaque_storie já têm PK composta (V3/V4): sem ação.

-- 4. Índices (IF NOT EXISTS; mesmos nomes da V13 onde houver sobreposição) --

CREATE INDEX IF NOT EXISTS idx_post_autor ON post (autor_id);
CREATE INDEX IF NOT EXISTS idx_post_data_criacao ON post (data_criacao);
CREATE INDEX IF NOT EXISTS idx_comentario_post ON comentario (post_id);
CREATE INDEX IF NOT EXISTS idx_comentario_autor ON comentario (autor_id);
CREATE INDEX IF NOT EXISTS idx_curtida_post ON curtida (post_id);
CREATE INDEX IF NOT EXISTS idx_curtida_usuario ON curtida (usuario_id);
CREATE INDEX IF NOT EXISTS idx_seguidor_seguido ON seguidor (seguido_id);
CREATE INDEX IF NOT EXISTS idx_seguidor_seguidor ON seguidor (seguidor_id);
CREATE INDEX IF NOT EXISTS idx_storie_autor ON storie (autor_id);
CREATE INDEX IF NOT EXISTS idx_storie_data_expiracao ON storie (data_expiracao);
CREATE INDEX IF NOT EXISTS idx_notificacao_destinatario ON notificacao (destinatario_id);
CREATE INDEX IF NOT EXISTS idx_salvos_usuario ON salvos (usuario_id);
CREATE INDEX IF NOT EXISTS idx_salvos_post ON salvos (post_id);
CREATE INDEX IF NOT EXISTS idx_status_usuario_usuario ON status_usuario (usuario_id);
