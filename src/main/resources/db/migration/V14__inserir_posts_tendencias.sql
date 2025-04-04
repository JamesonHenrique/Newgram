-- V14__inserir_posts_tendencias.sql

-- Ajuste para usar uma data fixa, considerando que hoje é 04/03/2025
INSERT INTO post (legenda, data_criacao, localizacao, arquivado, visibilidade, autor_id) VALUES
('Show incrível em São Paulo ontem! Obrigado a todos que compareceram. #ShowInesquecivel #FãsIncríveis',
  '2025-03-03 19:30:00', 'São Paulo, SP', false, 'PUBLICO', 6), -- Anitta
('Gravação do novo programa finalizada! Em breve vocês vão poder assistir. #NovoPrograma',
  '2025-03-03 14:15:00', 'Rio de Janeiro, RJ', false, 'PUBLICO', 1), -- Rodrigo Faro
('Dia de treino especial para o grande jogo da semana que vem! #Determinação #Foco',
  '2025-03-02 16:45:00', 'Paris, França', false, 'PUBLICO', 5), -- Neymar
('Nova música chegando! Essa colaboração vai surpreender vocês. #NovaMusica #Colaboração',
  '2025-03-03 10:20:00', 'Salvador, BA', false, 'PUBLICO', 4), -- Ivete
('Estreia do filme hoje! Não percam nas telonas de todo o Brasil. #NovoFilme #Estreia',
  '2025-03-04 09:15:00', 'São Paulo, SP', false, 'PUBLICO', 10); -- Taís

-- Inserir hashtags (se não existirem)
INSERT INTO hashtag (nome)
SELECT 'ShowInesquecivel' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'ShowInesquecivel');

INSERT INTO hashtag (nome)
SELECT 'FãsIncríveis' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'FãsIncríveis');

INSERT INTO hashtag (nome)
SELECT 'NovoPrograma' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'NovoPrograma');

INSERT INTO hashtag (nome)
SELECT 'Determinação' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'Determinação');

INSERT INTO hashtag (nome)
SELECT 'Foco' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'Foco');

INSERT INTO hashtag (nome)
SELECT 'NovaMusica' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'NovaMusica');

INSERT INTO hashtag (nome)
SELECT 'Colaboração' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'Colaboração');

INSERT INTO hashtag (nome)
SELECT 'NovoFilme' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'NovoFilme');

INSERT INTO hashtag (nome)
SELECT 'Estreia' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'Estreia');

-- Atribuir corretamente os IDs dos posts (considerando que o último post era o ID 10)
-- Relacionamento post-hashtag para os novos posts
INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 11, id FROM hashtag WHERE nome = 'ShowInesquecivel';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 11, id FROM hashtag WHERE nome = 'FãsIncríveis';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 12, id FROM hashtag WHERE nome = 'NovoPrograma';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 13, id FROM hashtag WHERE nome = 'Determinação';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 13, id FROM hashtag WHERE nome = 'Foco';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 14, id FROM hashtag WHERE nome = 'NovaMusica';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 14, id FROM hashtag WHERE nome = 'Colaboração';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 15, id FROM hashtag WHERE nome = 'NovoFilme';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 15, id FROM hashtag WHERE nome = 'Estreia';

-- Adicionar MUITAS curtidas para garantir que atenda ao critério minimoInteracoes
-- 20+ curtidas por post para garantir
INSERT INTO curtida (data_criacao, usuario_id, post_id, comentario_id) VALUES
-- Post da Anitta (11)
('2025-03-03 19:45:00', 1, 11, NULL),
('2025-03-03 19:50:00', 2, 11, NULL),
('2025-03-03 19:55:00', 3, 11, NULL),
('2025-03-03 20:00:00', 4, 11, NULL),
('2025-03-03 20:05:00', 5, 11, NULL),
('2025-03-03 20:10:00', 7, 11, NULL),
('2025-03-03 20:15:00', 8, 11, NULL),
('2025-03-03 20:20:00', 9, 11, NULL),
('2025-03-03 20:25:00', 10, 11, NULL),
('2025-03-03 20:30:00', 1, 11, NULL),
('2025-03-03 20:35:00', 2, 11, NULL),
('2025-03-03 20:40:00', 3, 11, NULL),
('2025-03-03 20:45:00', 4, 11, NULL),
('2025-03-03 20:50:00', 5, 11, NULL),
('2025-03-03 20:55:00', 7, 11, NULL),
('2025-03-03 21:00:00', 8, 11, NULL),
('2025-03-03 21:05:00', 9, 11, NULL),
('2025-03-03 21:10:00', 10, 11, NULL),
('2025-03-03 21:15:00', 1, 11, NULL),
('2025-03-03 21:20:00', 2, 11, NULL),

-- Post do Rodrigo Faro (12)
('2025-03-03 14:30:00', 2, 12, NULL),
('2025-03-03 14:35:00', 3, 12, NULL),
('2025-03-03 14:40:00', 4, 12, NULL),
('2025-03-03 14:45:00', 5, 12, NULL),
('2025-03-03 14:50:00', 6, 12, NULL),
('2025-03-03 14:55:00', 7, 12, NULL),
('2025-03-03 15:00:00', 8, 12, NULL),
('2025-03-03 15:05:00', 9, 12, NULL),
('2025-03-03 15:10:00', 10, 12, NULL),
('2025-03-03 15:15:00', 2, 12, NULL),
('2025-03-03 15:20:00', 3, 12, NULL),
('2025-03-03 15:25:00', 4, 12, NULL),
('2025-03-03 15:30:00', 5, 12, NULL),
('2025-03-03 15:35:00', 6, 12, NULL),
('2025-03-03 15:40:00', 7, 12, NULL),
('2025-03-03 15:45:00', 8, 12, NULL),
('2025-03-03 15:50:00', 9, 12, NULL),
('2025-03-03 15:55:00', 10, 12, NULL),
('2025-03-03 16:00:00', 2, 12, NULL),
('2025-03-03 16:05:00', 3, 12, NULL),

-- Post do Neymar (13)
('2025-03-02 17:00:00', 1, 13, NULL),
('2025-03-02 17:05:00', 2, 13, NULL),
('2025-03-02 17:10:00', 3, 13, NULL),
('2025-03-02 17:15:00', 4, 13, NULL),
('2025-03-02 17:20:00', 6, 13, NULL),
('2025-03-02 17:25:00', 7, 13, NULL),
('2025-03-02 17:30:00', 8, 13, NULL),
('2025-03-02 17:35:00', 9, 13, NULL),
('2025-03-02 17:40:00', 10, 13, NULL),
('2025-03-02 17:45:00', 1, 13, NULL),
('2025-03-02 17:50:00', 2, 13, NULL),
('2025-03-02 17:55:00', 3, 13, NULL),
('2025-03-02 18:00:00', 4, 13, NULL),
('2025-03-02 18:05:00', 6, 13, NULL),
('2025-03-02 18:10:00', 7, 13, NULL),
('2025-03-02 18:15:00', 8, 13, NULL),
('2025-03-02 18:20:00', 9, 13, NULL),
('2025-03-02 18:25:00', 10, 13, NULL),
('2025-03-02 18:30:00', 1, 13, NULL),
('2025-03-02 18:35:00', 2, 13, NULL),

-- Post da Ivete (14)
('2025-03-03 10:30:00', 1, 14, NULL),
('2025-03-03 10:35:00', 2, 14, NULL),
('2025-03-03 10:40:00', 3, 14, NULL),
('2025-03-03 10:45:00', 5, 14, NULL),
('2025-03-03 10:50:00', 6, 14, NULL),
('2025-03-03 10:55:00', 7, 14, NULL),
('2025-03-03 11:00:00', 8, 14, NULL),
('2025-03-03 11:05:00', 9, 14, NULL),
('2025-03-03 11:10:00', 10, 14, NULL),
('2025-03-03 11:15:00', 1, 14, NULL),
('2025-03-03 11:20:00', 2, 14, NULL),
('2025-03-03 11:25:00', 3, 14, NULL),
('2025-03-03 11:30:00', 5, 14, NULL),
('2025-03-03 11:35:00', 6, 14, NULL),
('2025-03-03 11:40:00', 7, 14, NULL),
('2025-03-03 11:45:00', 8, 14, NULL),
('2025-03-03 11:50:00', 9, 14, NULL),
('2025-03-03 11:55:00', 10, 14, NULL),
('2025-03-03 12:00:00', 1, 14, NULL),
('2025-03-03 12:05:00', 2, 14, NULL),

-- Post da Taís (15)
('2025-03-04 09:30:00', 1, 15, NULL),
('2025-03-04 09:35:00', 2, 15, NULL),
('2025-03-04 09:40:00', 3, 15, NULL),
('2025-03-04 09:45:00', 4, 15, NULL),
('2025-03-04 09:50:00', 5, 15, NULL),
('2025-03-04 09:55:00', 6, 15, NULL),
('2025-03-04 10:00:00', 7, 15, NULL),
('2025-03-04 10:05:00', 8, 15, NULL),
('2025-03-04 10:10:00', 9, 15, NULL),
('2025-03-04 10:15:00', 1, 15, NULL),
('2025-03-04 10:20:00', 2, 15, NULL),
('2025-03-04 10:25:00', 3, 15, NULL),
('2025-03-04 10:30:00', 4, 15, NULL),
('2025-03-04 10:35:00', 5, 15, NULL),
('2025-03-04 10:40:00', 6, 15, NULL),
('2025-03-04 10:45:00', 7, 15, NULL),
('2025-03-04 10:50:00', 8, 15, NULL),
('2025-03-04 10:55:00', 9, 15, NULL),
('2025-03-04 11:00:00', 1, 15, NULL),
('2025-03-04 11:05:00', 2, 15, NULL);

-- Adicionar comentários para aumentar a pontuação de engajamento
INSERT INTO comentario (texto, data_criacao, autor_id, post_id, comentario_pai_id) VALUES
-- Post da Anitta (11)
('Show incrível! Sua performance foi espetacular!', '2025-03-03 20:00:00', 2, 11, NULL),
('Estava lá e foi realmente sensacional!', '2025-03-03 20:10:00', 3, 11, NULL),
('Quando teremos mais shows desse nível?', '2025-03-03 20:20:00', 4, 11, NULL),
('Não perco o próximo por nada!', '2025-03-03 20:30:00', 5, 11, NULL),
('Amei todas as músicas! Principalmente o novo single!', '2025-03-03 20:40:00', 7, 11, NULL),
('A energia que você passa no palco é incrível!', '2025-03-03 20:50:00', 8, 11, NULL),
('Melhor show do ano!', '2025-03-03 21:00:00', 9, 11, NULL),
('Artista internacional com alma brasileira!', '2025-03-03 21:10:00', 10, 11, NULL),

-- Post do Rodrigo Faro (12)
('Mal posso esperar para assistir ao novo programa!', '2025-03-03 14:40:00', 3, 12, NULL),
('Seu carisma na TV é incomparável', '2025-03-03 14:50:00', 4, 12, NULL),
('Estou ansioso pela estreia!', '2025-03-03 15:00:00', 5, 12, NULL),
('Arrasando como sempre!', '2025-03-03 15:10:00', 6, 12, NULL),
('Você é o melhor apresentador do Brasil!', '2025-03-03 15:20:00', 7, 12, NULL),
('Sua família é linda! Parabéns pelo trabalho', '2025-03-03 15:30:00', 8, 12, NULL),
('Não perco um programa seu!', '2025-03-03 15:40:00', 9, 12, NULL),
('Sempre inovando na TV brasileira!', '2025-03-03 15:50:00', 10, 12, NULL),

-- Post do Neymar (13)
('Craque! Continue inspirando milhões', '2025-03-02 17:10:00', 1, 13, NULL),
('Determinação que inspira!', '2025-03-02 17:20:00', 2, 13, NULL),
('Melhor jogador brasileiro!', '2025-03-02 17:30:00', 3, 13, NULL),
('Estamos com você em cada jogo!', '2025-03-02 17:40:00', 4, 13, NULL),
('Ansioso para ver você brilhar no próximo jogo!', '2025-03-02 17:50:00', 6, 13, NULL),
('Você honra a camisa 10 do Brasil!', '2025-03-02 18:00:00', 7, 13, NULL),
('Exemplo para milhões de jovens!', '2025-03-02 18:10:00', 8, 13, NULL),
('Recupere-se logo! O futebol precisa do seu talento', '2025-03-02 18:20:00', 9, 13, NULL),

-- Post da Ivete (14)
('Não vejo a hora de ouvir essa música!', '2025-03-03 10:40:00', 1, 14, NULL),
('Sua voz é incomparável!', '2025-03-03 10:50:00', 2, 14, NULL),
('Rainha da música brasileira!', '2025-03-03 11:00:00', 3, 14, NULL),
('Quem será a colaboração? Estou muito curioso!', '2025-03-03 11:10:00', 5, 14, NULL),
('Suas músicas são a trilha sonora da minha vida!', '2025-03-03 11:20:00', 6, 14, NULL),
('A energia que você transmite é contagiante!', '2025-03-03 11:30:00', 7, 14, NULL),
('Mainha do axé! Sempre arrasando!', '2025-03-03 11:40:00', 8, 14, NULL),
('Não vejo a hora de dançar essa música no carnaval!', '2025-03-03 11:50:00', 9, 14, NULL),

-- Post da Taís (15)
('Você brilha em tudo que faz!', '2025-03-04 09:40:00', 1, 15, NULL),
('Atriz incrível! Seu talento é extraordinário', '2025-03-04 09:50:00', 2, 15, NULL),
('Vou assistir no primeiro dia!', '2025-03-04 10:00:00', 3, 15, NULL),
('Parabéns pelo trabalho maravilhoso!', '2025-03-04 10:10:00', 4, 15, NULL),
('Sua versatilidade como atriz é admirável!', '2025-03-04 10:20:00', 5, 15, NULL),
('Sempre representando tão bem a mulher brasileira!', '2025-03-04 10:30:00', 6, 15, NULL),
('Já comprei meu ingresso para a estreia!', '2025-03-04 10:40:00', 7, 15, NULL),
('Orgulho da nossa cultura e do nosso cinema!', '2025-03-04 10:50:00', 8, 15, NULL);