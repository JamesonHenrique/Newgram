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
