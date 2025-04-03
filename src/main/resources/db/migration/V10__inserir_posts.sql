-- Inserir hashtags
INSERT INTO hashtag (nome) VALUES
('Entretenimento'),
('Música'),
('Esporte'),
('TV'),
('Cinema'),
('Gastronomia'),
('Viagem'),
('Família'),
('Moda'),
('Fitness');

-- Inserir posts
INSERT INTO post (legenda, data_criacao, localizacao, arquivado, visibilidade, autor_id) VALUES
('Mais um dia de gravação! Adoro meu trabalho! 📺', '2025-03-20 10:00:00', 'São Paulo, SP', false, 'PUBLICO', 1),
('Dia de gravação especial no Japão! 🇯🇵', '2025-03-21 11:00:00', 'Tokyo, Japão', false, 'PUBLICO', 2),
('Preparativos finais para o programa de domingo. Vai ser incrível!', '2025-03-22 12:00:00', 'Rio de Janeiro, RJ', false, 'PUBLICO', 3),
('Ensaio para o show de hoje! Energia total! 🎵', '2025-03-23 13:00:00', 'Salvador, BA', false, 'PUBLICO', 4),
('Treino matinal. Foco, força e determinação! ⚽', '2025-03-24 14:00:00', 'Paris, França', false, 'PUBLICO', 5),
('Gravando meu novo hit! Em breve novidades 🎶', '2025-03-25 15:00:00', 'Rio de Janeiro, RJ', false, 'PUBLICO', 6),
('Bastidores do Caldeirão! Sábado tem programa especial!', '2025-03-26 16:00:00', 'São Paulo, SP', false, 'PUBLICO', 7),
('Momentos de descanso com a família são essenciais', '2025-03-27 17:00:00', 'Rio de Janeiro, RJ', false, 'PUBLICO', 8),
('Show incrível ontem! Obrigado a todos que vieram! 🎸', '2025-03-28 18:00:00', 'Goiânia, GO', false, 'PUBLICO', 9),
('Gravações intensas hoje. Amo meu trabalho! 🎬', '2025-03-29 19:00:00', 'Rio de Janeiro, RJ', false, 'PUBLICO', 10);

-- Relacionamento post-hashtag
INSERT INTO post_hashtag (post_id, hashtag_id) VALUES
(1, 4), -- TV
(1, 1), -- Entretenimento
(2, 7), -- Viagem
(2, 1), -- Entretenimento
(3, 4), -- TV
(4, 2), -- Música
(5, 3), -- Esporte
(5, 10), -- Fitness
(6, 2), -- Música
(7, 4), -- TV
(8, 8), -- Família
(9, 2), -- Música
(10, 5); -- Cinema

-- Marcações em posts
INSERT INTO post_marcacoes (post_id, usuario_id) VALUES
(1, 2), -- Rodrigo marcou Sabrina
(2, 1), -- Sabrina marcou Rodrigo
(4, 6), -- Ivete marcou Anitta
(6, 4), -- Anitta marcou Ivete
(9, 4); -- Gusttavo marcou Ivete