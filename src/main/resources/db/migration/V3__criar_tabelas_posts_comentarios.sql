-- Tabela de posts
CREATE TABLE post (
    id BIGSERIAL PRIMARY KEY,
    legenda TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    localizacao VARCHAR(255),
    arquivado BOOLEAN DEFAULT FALSE,
    visibilidade VARCHAR(20) NOT NULL DEFAULT 'PUBLICO',
    autor_id BIGINT NOT NULL,
    FOREIGN KEY (autor_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de relacionamento entre post e hashtags
CREATE TABLE post_hashtag (
    post_id BIGINT NOT NULL,
    hashtag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, hashtag_id),
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    FOREIGN KEY (hashtag_id) REFERENCES hashtag (id) ON DELETE CASCADE
);

-- Tabela de marcações em posts
CREATE TABLE post_marcacoes (
    post_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, usuario_id),
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de comentários
CREATE TABLE comentario (
    id BIGSERIAL PRIMARY KEY,
    texto TEXT NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    autor_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    comentario_pai_id BIGINT,
    FOREIGN KEY (autor_id) REFERENCES usuario (id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    FOREIGN KEY (comentario_pai_id) REFERENCES comentario (id) ON DELETE CASCADE
);

-- Tabela de curtidas
CREATE TABLE curtida (
    id BIGSERIAL PRIMARY KEY,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    post_id BIGINT,
    comentario_id BIGINT,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    FOREIGN KEY (comentario_id) REFERENCES comentario (id) ON DELETE CASCADE,
    CHECK (
        (post_id IS NOT NULL AND comentario_id IS NULL) OR
        (post_id IS NULL AND comentario_id IS NOT NULL)
    )
);

-- Tabela de salvos
CREATE TABLE salvos (
    id BIGSERIAL PRIMARY KEY,
    data_salvo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    colecao VARCHAR(100),
    usuario_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE,
    UNIQUE (usuario_id, post_id)
);