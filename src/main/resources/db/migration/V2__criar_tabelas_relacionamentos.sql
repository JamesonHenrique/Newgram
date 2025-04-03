-- Tabela de relacionamento de seguidores
CREATE TABLE seguidor (
    id BIGSERIAL PRIMARY KEY,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notificacoes_ativadas BOOLEAN DEFAULT TRUE,
    seguidor_id BIGINT NOT NULL,
    seguido_id BIGINT NOT NULL,
    FOREIGN KEY (seguidor_id) REFERENCES usuario (id) ON DELETE CASCADE,
    FOREIGN KEY (seguido_id) REFERENCES usuario (id) ON DELETE CASCADE,
    UNIQUE (seguidor_id, seguido_id)
);

-- Tabela auxiliar para relacionamento muitos-para-muitos entre usuários (seguindo)
CREATE TABLE usuario_seguindo (
    usuario_id BIGINT NOT NULL,
    seguindo_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, seguindo_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    FOREIGN KEY (seguindo_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de pesquisas
CREATE TABLE pesquisa (
    id BIGSERIAL PRIMARY KEY,
    termo_pesquisado VARCHAR(255) NOT NULL,
    data_pesquisa TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);