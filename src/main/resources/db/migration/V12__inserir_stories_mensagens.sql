-- Inserir stories
INSERT INTO storie (data_criacao, data_expiracao, destacado, autor_id) VALUES
('2025-04-01 08:00:00', '2025-04-02 08:00:00', true, 1),
('2025-04-01 09:00:00', '2025-04-02 09:00:00', false, 2),
('2025-04-01 10:00:00', '2025-04-02 10:00:00', true, 3),
('2025-04-01 11:00:00', '2025-04-02 11:00:00', false, 4),
('2025-04-01 12:00:00', '2025-04-02 12:00:00', true, 5),
('2025-04-01 13:00:00', '2025-04-02 13:00:00', false, 6),
('2025-04-01 14:00:00', '2025-04-02 14:00:00', true, 7),
('2025-04-01 15:00:00', '2025-04-02 15:00:00', false, 8),
('2025-04-01 16:00:00', '2025-04-02 16:00:00', true, 9),
('2025-04-01 17:00:00', '2025-04-02 17:00:00', false, 10);

-- Inserir visualizações de stories
INSERT INTO storie_visualizacoes (storie_id, usuario_id, data_visualizacao) VALUES
(1, 2, '2025-04-01 08:15:00'),
(1, 3, '2025-04-01 08:20:00'),
(2, 1, '2025-04-01 09:15:00'),
(3, 1, '2025-04-01 10:15:00'),
(3, 5, '2025-04-01 10:20:00'),
(4, 1, '2025-04-01 11:15:00'),
(5, 3, '2025-04-01 12:15:00'),
(6, 4, '2025-04-01 13:15:00'),
(7, 6, '2025-04-01 14:15:00'),
(8, 7, '2025-04-01 15:15:00');

-- Inserir conversas
INSERT INTO conversa (data_criacao, ultima_interacao, is_grupo, nome_grupo, criador_id) VALUES
('2025-04-05 10:00:00', '2025-04-05 10:30:00', false, NULL, 1),
('2025-04-06 11:00:00', '2025-04-06 11:30:00', false, NULL, 2),
('2025-04-07 12:00:00', '2025-04-07 12:30:00', true, 'Apresentadores de TV', 1),
('2025-04-08 13:00:00', '2025-04-08 13:30:00', false, NULL, 4),
('2025-04-09 14:00:00', '2025-04-09 14:30:00', false, NULL, 5),
('2025-04-10 15:00:00', '2025-04-10 15:30:00', false, NULL, 6),
('2025-04-11 16:00:00', '2025-04-11 16:30:00', true, 'Músicos', 4),
('2025-04-12 17:00:00', '2025-04-12 17:30:00', false, NULL, 8),
('2025-04-13 18:00:00', '2025-04-13 18:30:00', false, NULL, 9),
('2025-04-14 19:00:00', '2025-04-14 19:30:00', false, NULL, 10);

-- Inserir participantes da conversa
INSERT INTO conversa_participante (conversa_id, usuario_id) VALUES
(1, 1), (1, 2), -- Rodrigo e Sabrina
(2, 2), (2, 3), -- Sabrina e Luciano
(3, 1), (3, 2), (3, 3), (3, 7), (3, 8), -- Grupo Apresentadores
(4, 4), (4, 6), -- Ivete e Anitta
(5, 5), (5, 1), -- Neymar e Rodrigo
(6, 6), (6, 4), -- Anitta e Ivete
(7, 4), (7, 6), (7, 9), -- Grupo Músicos
(8, 8), (8, 10), -- Fátima e Taís
(9, 9), (9, 4), -- Gusttavo e Ivete
(10, 10), (10, 8); -- Taís e Fátima

-- Inserir mensagens
-- Inserir mensagens
INSERT INTO mensagem (conteudo, data_envio, visualizada, entregue, deletadaPeloRemetente, tipo, url_midia, remetente_id, destinatario_id, conversa_id) VALUES
('Oi Sabrina! Vamos gravar juntos semana que vem?', '2025-04-05 10:00:00', true, true, false, 'TEXTO', null, 1, 2, 1),
('Claro! Vai ser incrível! Já estou animada', '2025-04-05 10:15:00', true, true, false, 'TEXTO', null, 2, 1, 1),
('Luciano, podemos marcar aquela entrevista?', '2025-04-06 11:00:00', true, true, false, 'TEXTO', null, 2, 3, 2),
('Sim, claro! Minha assessoria vai entrar em contato', '2025-04-06 11:15:00', true, true, false, 'TEXTO', null, 3, 2, 2),
('Pessoal, reunião para discutir o especial de fim de ano!', '2025-04-07 12:00:00', true, true, false, 'TEXTO', null, 1, NULL, 3),
('Estarei presente!', '2025-04-07 12:15:00', true, true, false, 'TEXTO', null, 2, NULL, 3),
('Conte comigo!', '2025-04-07 12:20:00', true, true, false, 'TEXTO', null, 3, NULL, 3),
('Anitta, vamos fazer aquele feat?', '2025-04-08 13:00:00', true, true, false, 'TEXTO', null, 4, 6, 4),
('Estou super a fim! Já tenho algumas ideias', '2025-04-08 13:15:00', true, true, false, 'TEXTO', null, 6, 4, 4),
('Rodrigo, vamos marcar aquele jogo beneficente?', '2025-04-09 14:00:00', true, true, false, 'TEXTO', null, 5, 1, 5),
('Veja o rascunho da nossa música!', '2025-04-08 14:30:00', false, true, false, 'AUDIO', 'audio/feat-ivete-anitta.mp3', 4, 6, 4),
('Confira a arte do nosso clipe', '2025-04-08 15:00:00', true, true, false, 'IMAGEM', 'imagens/arte-clipe.jpg', 6, 4, 4),
('Pessoal, data do nosso show coletivo', '2025-04-07 16:00:00', true, true, false, 'TEXTO', null, 9, NULL, 7),
('Galera, compartilhando o video da última apresentação', '2025-04-07 18:30:00', false, false, false, 'VIDEO', 'videos/apresentacao.mp4', 6, NULL, 7);