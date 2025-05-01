-- Índices para melhorar a performance das consultas mais comuns

-- Índices para usuários
CREATE INDEX idx_usuario_username ON usuario(username);
CREATE INDEX idx_usuario_email ON usuario(email);

-- Índices para posts
CREATE INDEX idx_post_autor ON post(autor_id);
CREATE INDEX idx_post_data_criacao ON post(data_criacao);

-- Índices para comentários
CREATE INDEX idx_comentario_post ON comentario(post_id);
CREATE INDEX idx_comentario_autor ON comentario(autor_id);

-- Índices para curtidas
CREATE INDEX idx_curtida_post ON curtida(post_id);
CREATE INDEX idx_curtida_usuario ON curtida(usuario_id);

-- Índices para seguidores
CREATE INDEX idx_seguidor_seguido ON seguidor(seguido_id);
CREATE INDEX idx_seguidor_seguidor ON seguidor(seguidor_id);

-- Índices para stories
CREATE INDEX idx_storie_autor ON storie(autor_id);
CREATE INDEX idx_storie_data_expiracao ON storie(data_expiracao);

-- Índices para notificações
CREATE INDEX idx_notificacao_destinatario ON notificacao(destinatario_id);
CREATE INDEX idx_notificacao_lida ON notificacao(lida);

-- Índices para arquivos
CREATE INDEX idx_arquivo_tipo_entidade_id ON arquivo(tipo_entidade, entidade_id);

-- Índices para hashtags
CREATE INDEX idx_hashtag_nome ON hashtag(nome);