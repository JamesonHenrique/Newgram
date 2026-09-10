-- Inserir comentários
INSERT INTO comentario (texto, data_criacao, autor_id, post_id, comentario_pai_id) VALUES
('Arrasando como sempre!', '2025-03-20 10:15:00', 3, 1, NULL),
('Que lugar incrível! Aproveite', '2025-03-21 11:20:00', 5, 2, NULL),
('Não vejo a hora de assistir!', '2025-03-22 12:30:00', 2, 3, NULL),
('Sua voz é incrível!', '2025-03-23 13:15:00', 6, 4, NULL),
('Continue focado, craque!', '2025-03-24 14:20:00', 1, 5, NULL),
('Já quero ouvir essa música!', '2025-03-25 15:30:00', 4, 6, NULL),
('Seu programa é o melhor!', '2025-03-26 16:10:00', 8, 7, NULL),
('Família é tudo! Lindos!', '2025-03-27 17:15:00', 10, 8, NULL),
('Show incrível! Parabéns!', '2025-03-28 18:20:00', 1, 9, NULL),
('Adoro seu trabalho como atriz!', '2025-03-29 19:15:00', 2, 10, NULL);

-- Inserir curtidas (em posts)
INSERT INTO curtida (data_criacao, usuario_id, post_id, comentario_id) VALUES
('2025-03-20 10:30:00', 2, 1, NULL),
('2025-03-21 11:35:00', 3, 2, NULL),
('2025-03-22 12:40:00', 4, 3, NULL),
('2025-03-23 13:30:00', 5, 4, NULL),
('2025-03-24 14:30:00', 6, 5, NULL),
('2025-03-25 15:40:00', 7, 6, NULL),
('2025-03-26 16:20:00', 8, 7, NULL),
('2025-03-27 17:30:00', 9, 8, NULL),
('2025-03-28 18:30:00', 10, 9, NULL),
('2025-03-29 19:30:00', 1, 10, NULL);

-- Inserir curtidas (em comentários)
INSERT INTO curtida (data_criacao, usuario_id, post_id, comentario_id) VALUES
('2025-03-20 10:45:00', 1, NULL, 1),
('2025-03-21 11:40:00', 2, NULL, 2),
('2025-03-22 12:50:00', 3, NULL, 3),
('2025-03-23 13:40:00', 4, NULL, 4),
('2025-03-24 14:40:00', 5, NULL, 5);

-- Inserir salvos
INSERT INTO salvos (data_salvo, colecao, usuario_id, post_id) VALUES
('2025-03-20 11:00:00', 'Inspiração', 2, 1),
('2025-03-21 12:00:00', 'Viagens', 3, 2),
('2025-03-22 13:00:00', 'TV', 4, 3),
('2025-03-23 14:00:00', 'Música', 5, 4),
('2025-03-24 15:00:00', 'Esportes', 6, 5),
('2025-03-25 16:00:00', 'Música', 7, 6),
('2025-03-26 17:00:00', 'Entretenimento', 8, 7),
('2025-03-27 18:00:00', 'Família', 9, 8),
('2025-03-28 19:00:00', 'Shows', 10, 9),
('2025-03-29 20:00:00', 'Atuação', 1, 10);