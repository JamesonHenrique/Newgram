-- Tabela de usuários (base para várias outras tabelas)
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    username VARCHAR(25) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    foto_perfil VARCHAR(255),
    bio TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de status do usuário
CREATE TABLE status_usuario (
    id BIGSERIAL PRIMARY KEY,
    online BOOLEAN DEFAULT FALSE,
    ultimo_acesso TIMESTAMP,
    status_personalizado VARCHAR(100),
    usuario_id BIGINT NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

-- Tabela de hashtags
CREATE TABLE hashtag (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

-- Tabela de arquivos
CREATE TABLE arquivo (
    id BIGSERIAL PRIMARY KEY,
    nome_original VARCHAR(255) NOT NULL,
    nome_armazenado VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- "imagem" ou "video"
    tamanho BIGINT NOT NULL,
    data_upload TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    caminho VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    tipo_entidade VARCHAR(20) NOT NULL, -- POST, STORIE, PERFIL, MENSAGEM, COMENTARIO
    entidade_id BIGINT NOT NULL
);