-- Inserir seguidores
INSERT INTO seguidor (data_criacao, notificacoes_ativadas, seguidor_id, seguido_id) VALUES
('2025-03-11', true, 2, 1), -- Sabrina segue Rodrigo
('2025-03-11', true, 1, 2), -- Rodrigo segue Sabrina
('2025-03-12', true, 3, 1), -- Luciano segue Rodrigo
('2025-03-12', true, 4, 1), -- Ivete segue Rodrigo
('2025-03-13', true, 1, 4), -- Rodrigo segue Ivete
('2025-03-13', true, 5, 3), -- Neymar segue Luciano
('2025-03-14', true, 6, 1), -- Anitta segue Rodrigo
('2025-03-14', true, 7, 1), -- Mion segue Rodrigo
('2025-03-15', true, 8, 1), -- Fátima segue Rodrigo
('2025-03-15', true, 9, 1); -- Gusttavo segue Rodrigo

-- Inserir relacionamento de seguindo
INSERT INTO usuario_seguindo (usuario_id, seguindo_id) VALUES
(2, 1), -- Sabrina segue Rodrigo
(1, 2), -- Rodrigo segue Sabrina
(3, 1), -- Luciano segue Rodrigo
(4, 1), -- Ivete segue Rodrigo
(1, 4), -- Rodrigo segue Ivete
(5, 3), -- Neymar segue Luciano
(6, 1), -- Anitta segue Rodrigo
(7, 1), -- Mion segue Rodrigo
(8, 1), -- Fátima segue Rodrigo
(9, 1); -- Gusttavo segue Rodrigo

-- Inserir pesquisas
INSERT INTO pesquisa (termo_pesquisado, data_pesquisa, usuario_id) VALUES
('próximos shows', '2025-03-20 10:15:00', 1),
('receitas saudáveis', '2025-03-21 11:20:00', 2),
('ideias programa', '2025-03-22 12:30:00', 3),
('exercícios vocais', '2025-03-23 13:40:00', 4),
('treino funcional', '2025-03-24 14:50:00', 5),
('produtores música', '2025-03-25 15:00:00', 6),
('entrevistas', '2025-03-26 16:10:00', 7),
('dicas apresentação', '2025-03-27 17:20:00', 8),
('instrumentos musicais', '2025-03-28 18:30:00', 9),
('técnicas de atuação', '2025-03-29 19:40:00', 10);