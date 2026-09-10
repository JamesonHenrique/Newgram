-- V15: conta privada + solicitacoes de seguimento.
-- Seguidor.status: ACEITO (padrao, comportamento atual) ou PENDENTE (conta privada).

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS privado BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE seguidor ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACEITO';

CREATE INDEX IF NOT EXISTS idx_seguidor_status ON seguidor (status);
CREATE INDEX IF NOT EXISTS idx_usuario_privado ON usuario (privado) WHERE privado = TRUE;
