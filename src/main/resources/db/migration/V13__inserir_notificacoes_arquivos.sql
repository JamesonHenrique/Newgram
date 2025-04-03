-- Inserir notificações
INSERT INTO notificacao (tipo, conteudo, data_criacao, lida, destinatario_id, remetente_id) VALUES
('CURTIDA', 'curtiu sua publicação', '2025-04-20 10:00:00', true, 1, 2),
('COMENTARIO', 'comentou: "Arrasando como sempre!"', '2025-04-20 10:15:00', true, 1, 3),
('SEGUIDOR', 'começou a seguir você', '2025-04-21 11:00:00', false, 1, 4),
('MENSAGEM', 'enviou uma mensagem para você', '2025-04-22 12:00:00', true, 2, 1),
('CURTIDA', 'curtiu seu comentário', '2025-04-23 13:00:00', false, 3, 1),
('MARCACAO', 'marcou você em uma publicação', '2025-04-24 14:00:00', true, 4, 6),
('COMENTARIO', 'comentou: "Sua voz é incrível!"', '2025-04-25 15:00:00', false, 4, 6),
('SEGUIDOR', 'começou a seguir você', '2025-04-26 16:00:00', true, 5, 3),
('CURTIDA', 'curtiu sua publicação', '2025-04-27 17:00:00', false, 6, 7),
('MENSAGEM', 'enviou uma mensagem para você', '2025-04-28 18:00:00', true, 10, 8);

-- Inserir arquivos
INSERT INTO arquivo (nome_original, nome_armazenado, tipo, tamanho, data_upload, caminho, content_type, tipo_entidade, entidade_id) VALUES
('perfil_rodrigo.jpg', 'user1_profile_123456.jpg', 'imagem', 1024000, '2025-03-01 10:00:00', '/uploads/imagens/perfis/', 'image/jpeg', 'PERFIL', 1),
('perfil_sabrina.jpg', 'user2_profile_123457.jpg', 'imagem', 1024000, '2025-03-02 11:00:00', '/uploads/imagens/perfis/', 'image/jpeg', 'PERFIL', 2),
('post_estudio.jpg', 'post1_image_123458.jpg', 'imagem', 2048000, '2025-03-20 10:00:00', '/uploads/imagens/posts/', 'image/jpeg', 'POST', 1),
('post_japao.jpg', 'post2_image_123459.jpg', 'imagem', 2048000, '2025-03-21 11:00:00', '/uploads/imagens/posts/', 'image/jpeg', 'POST', 2),
('post_preparativos.jpg', 'post3_image_123460.jpg', 'imagem', 2048000, '2025-03-22 12:00:00', '/uploads/imagens/posts/', 'image/jpeg', 'POST', 3),
('storie_bastidores.mp4', 'storie1_video_123461.mp4', 'video', 5120000, '2025-04-01 08:00:00', '/uploads/videos/stories/', 'video/mp4', 'STORIE', 1),
('storie_ensaio.jpg', 'storie4_image_123462.jpg', 'imagem', 1536000, '2025-04-01 11:00:00', '/uploads/imagens/stories/', 'image/jpeg', 'STORIE', 4),
('audio_mensagem.mp3', 'message_audio_123463.mp3', 'audio', 3072000, '2025-04-10 15:15:00', '/uploads/audio/mensagens/', 'audio/mpeg', 'MENSAGEM', 10),
('comentario_foto.jpg', 'comment_image_123464.jpg', 'imagem', 1024000, '2025-03-20 10:15:00', '/uploads/imagens/comentarios/', 'image/jpeg', 'COMENTARIO', 1),
('post_show.jpg', 'post9_image_123465.jpg', 'imagem', 2048000, '2025-03-28 18:00:00', '/uploads/imagens/posts/', 'image/jpeg', 'POST', 9);