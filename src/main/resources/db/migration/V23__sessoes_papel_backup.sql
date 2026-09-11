-- V23: sessoes persistentes, papel admin e backup codes do 2FA.

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS papel VARCHAR(10) NOT NULL DEFAULT 'USER';
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS backup_codes TEXT;

CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    jti VARCHAR(64) NOT NULL UNIQUE,
    expiracao TIMESTAMP NOT NULL,
    revogado BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_usuario ON refresh_token (usuario_id);

-- Promova o primeiro admin manualmente:
-- UPDATE usuario SET papel = 'ADMIN' WHERE email = 'voce@example.com';
