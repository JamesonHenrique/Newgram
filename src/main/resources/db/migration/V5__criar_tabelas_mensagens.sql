-- Tabela de conversas
CREATE TABLE conversa (
    id BIGSERIAL PRIMARY KEY,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_interacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_grupo BOOLEAN DEFAULT FALSE,
    nome_grupo VARCHAR(100),
    criador_id BIGINT,
    FOREIGN KEY (criador_id) REFERENCES usuario (id) ON DELETE SET NULL
);

-- Tabela de participantes da conversa
CREATE TABLE conversa_participante (
    conversa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (conversa_id, usuario_id),
    FOREIGN KEY (conversa_id) REFERENCES conversa (id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de mensagens
-- Tabela de mensagens
CREATE TABLE mensagem (
    id BIGSERIAL PRIMARY KEY,
    conteudo TEXT,
    data_envio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    visualizada BOOLEAN DEFAULT FALSE,
    entregue BOOLEAN DEFAULT FALSE,
    deletadaPeloRemetente BOOLEAN DEFAULT FALSE,
    tipo VARCHAR(20) NOT NULL,
    url_midia VARCHAR(255),
    remetente_id BIGINT NOT NULL,
    destinatario_id BIGINT,
    conversa_id BIGINT NOT NULL,
    FOREIGN KEY (remetente_id) REFERENCES usuario (id) ON DELETE CASCADE,
    FOREIGN KEY (destinatario_id) REFERENCES usuario (id) ON DELETE SET NULL,
    FOREIGN KEY (conversa_id) REFERENCES conversa (id) ON DELETE CASCADE
);