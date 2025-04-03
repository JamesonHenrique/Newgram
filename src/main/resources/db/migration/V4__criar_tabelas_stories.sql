-- Tabela de stories
CREATE TABLE storie (
    id BIGSERIAL PRIMARY KEY,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP NOT NULL,
    destacado BOOLEAN DEFAULT FALSE,
    autor_id BIGINT NOT NULL,
    FOREIGN KEY (autor_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de visualizações de stories
CREATE TABLE storie_visualizacoes (
    storie_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    data_visualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (storie_id, usuario_id),
    FOREIGN KEY (storie_id) REFERENCES storie (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de marcações em stories
CREATE TABLE storie_marcacoes (
    storie_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (storie_id, usuario_id),
    FOREIGN KEY (storie_id) REFERENCES storie (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de destaques
CREATE TABLE destaque (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de relacionamento entre destaque e stories
CREATE TABLE destaque_storie (
    destaque_id BIGINT NOT NULL,
    storie_id BIGINT NOT NULL,
    PRIMARY KEY (destaque_id, storie_id),
    FOREIGN KEY (destaque_id) REFERENCES destaque (id) ON DELETE CASCADE,
    FOREIGN KEY (storie_id) REFERENCES storie (id) ON DELETE CASCADE
);