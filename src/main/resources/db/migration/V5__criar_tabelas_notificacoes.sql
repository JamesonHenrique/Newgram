-- Tabela de notificações
CREATE TABLE notificacao (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    conteudo TEXT NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lida BOOLEAN DEFAULT FALSE,
    destinatario_id BIGINT NOT NULL,
    remetente_id BIGINT,
    FOREIGN KEY (destinatario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    FOREIGN KEY (remetente_id) REFERENCES usuario (id) ON DELETE SET NULL
);