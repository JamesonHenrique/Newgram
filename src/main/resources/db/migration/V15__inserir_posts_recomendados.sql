-- V15__inserir_posts_recomendados.sql

-- Criar dados para recomendações para o usuário 1 (Rodrigo Faro)

-- 1. Inserir hashtags que o usuário 1 já curtiu em outros posts
INSERT INTO hashtag (nome)
SELECT 'ReceitasCulinárias' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'ReceitasCulinárias');

INSERT INTO hashtag (nome)
SELECT 'DicasDeViagem' WHERE NOT EXISTS (SELECT 1 FROM hashtag WHERE nome = 'DicasDeViagem');

-- 2. Inserir posts com hashtags que o usuário 1 curte (critério 1 de recomendação)
INSERT INTO post (legenda, data_criacao, localizacao, arquivado, visibilidade, autor_id, imagem_url) VALUES
('Experimentando novas receitas hoje! #ReceitasCulinárias #Gastronomia',
 '2025-03-05 09:30:00', 'São Paulo, SP', false, 'PUBLICO', 3,'usuarios/lucianohuck/posts/lucianohuck_posts_3.webp'), -- Luciano Huck
('Férias inesquecíveis! Vocês precisam conhecer esse lugar. #DicasDeViagem #Viagem',
 '2025-03-05 12:45:00', 'Ilhas Maldivas', false, 'PUBLICO', 8,'usuarios/fatimabernards/posts/fatimabernards_posts_3.webp'); -- Fátima Bernardes

-- 3. Inserir posts de autores que o usuário 1 já curtiu posts (critério 2 de recomendação)
-- Suponha que usuário 1 já curtiu posts do usuário 4 (Ivete)
INSERT INTO post (legenda, data_criacao, localizacao, arquivado, visibilidade, autor_id, imagem_url) VALUES
('Dia especial com amigos especiais. Momentos que ficam para sempre.',
 '2025-03-06 14:20:00', 'Salvador, BA', false, 'PUBLICO', 4,'usuarios/ivetesangalo/posts/ivetesangalo_posts_4.jpg'), -- Ivete Sangalo
('Reflexões sobre a vida e carreira. Gratidão por tudo!',
 '2025-03-07 11:15:00', 'Rio de Janeiro, RJ', false, 'PUBLICO', 2,'usuarios/sabrinasato/posts/sabrinasato_posts_3.jpg'); -- Sabrina Sato

-- 4. Vincular hashtags aos posts (para critério 1)
INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 16, id FROM hashtag WHERE nome = 'ReceitasCulinárias';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 16, id FROM hashtag WHERE nome = 'Gastronomia';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 17, id FROM hashtag WHERE nome = 'DicasDeViagem';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 17, id FROM hashtag WHERE nome = 'Viagem';

-- 5. Adicionar curtidas para aumentar o score dos posts (não do usuário 1)
-- Os posts com mais curtidas e comentários terão maior pontuação na ordenação
INSERT INTO curtida (data_criacao, usuario_id, post_id, comentario_id) VALUES
-- Curtidas no post 16 (Receitas culinárias)
('2025-03-05 09:45:00', 2, 16, NULL),
('2025-03-05 09:50:00', 3, 16, NULL),
('2025-03-05 09:55:00', 4, 16, NULL),
('2025-03-05 10:00:00', 5, 16, NULL),
('2025-03-05 10:05:00', 6, 16, NULL),
('2025-03-05 10:10:00', 7, 16, NULL),
('2025-03-05 10:15:00', 8, 16, NULL),
('2025-03-05 10:20:00', 9, 16, NULL),
('2025-03-05 10:25:00', 10, 16, NULL),

-- Curtidas no post 17 (Dicas de viagem)
('2025-03-05 13:00:00', 2, 17, NULL),
('2025-03-05 13:05:00', 3, 17, NULL),
('2025-03-05 13:10:00', 4, 17, NULL),
('2025-03-05 13:15:00', 5, 17, NULL),
('2025-03-05 13:20:00', 6, 17, NULL),
('2025-03-05 13:25:00', 7, 17, NULL),
('2025-03-05 13:30:00', 9, 17, NULL),
('2025-03-05 13:35:00', 10, 17, NULL),

-- Curtidas no post 18 (Ivete)
('2025-03-06 14:30:00', 2, 18, NULL),
('2025-03-06 14:35:00', 3, 18, NULL),
('2025-03-06 14:40:00', 5, 18, NULL),
('2025-03-06 14:45:00', 6, 18, NULL),
('2025-03-06 14:50:00', 7, 18, NULL),
('2025-03-06 14:55:00', 8, 18, NULL),
('2025-03-06 15:00:00', 9, 18, NULL),
('2025-03-06 15:05:00', 10, 18, NULL),
('2025-03-06 15:10:00', 3, 18, NULL),
('2025-03-06 15:15:00', 5, 18, NULL),
('2025-03-06 15:20:00', 7, 18, NULL),
('2025-03-06 15:25:00', 9, 18, NULL),

-- Curtidas no post 19 (Sabrina)
('2025-03-07 11:30:00', 3, 19, NULL),
('2025-03-07 11:35:00', 4, 19, NULL),
('2025-03-07 11:40:00', 5, 19, NULL),
('2025-03-07 11:45:00', 6, 19, NULL),
('2025-03-07 11:50:00', 7, 19, NULL),
('2025-03-07 11:55:00', 8, 19, NULL),
('2025-03-07 12:00:00', 9, 19, NULL),
('2025-03-07 12:05:00', 10, 19, NULL);

-- 6. Adicionar comentários para aumentar ainda mais a pontuação (não do usuário 1)
INSERT INTO comentario (texto, data_criacao, autor_id, post_id, comentario_pai_id) VALUES
-- Comentários no post 16 (Receitas culinárias)
('Que delícia! Me passa essa receita?', '2025-03-05 10:30:00', 2, 16, NULL),
('Sempre quis aprender a cozinhar esse prato!', '2025-03-05 10:40:00', 4, 16, NULL),
('Ficou com uma aparência incrível!', '2025-03-05 10:50:00', 6, 16, NULL),
('Você deveria dar aulas de culinária!', '2025-03-05 11:00:00', 8, 16, NULL),
('Vou tentar fazer este fim de semana!', '2025-03-05 11:10:00', 10, 16, NULL),

-- Comentários no post 17 (Dicas de viagem)
('Esse lugar está na minha lista de desejos!', '2025-03-05 13:40:00', 3, 17, NULL),
('Quanto tempo você ficou por lá?', '2025-03-05 13:50:00', 5, 17, NULL),
('As fotos estão maravilhosas!', '2025-03-05 14:00:00', 7, 17, NULL),
('Preciso de dicas de hospedagem!', '2025-03-05 14:10:00', 9, 17, NULL),

-- Comentários no post 18 (Ivete)
('Momento especial com pessoas especiais!', '2025-03-06 15:30:00', 2, 18, NULL),
('Vocês formam um grupo incrível!', '2025-03-06 15:40:00', 3, 18, NULL),
('Amizade é tudo na vida!', '2025-03-06 15:50:00', 5, 18, NULL),
('Que energia maravilhosa nessa foto!', '2025-03-06 16:00:00', 7, 18, NULL),
('Salvador tem essa magia mesmo!', '2025-03-06 16:10:00', 9, 18, NULL),

-- Comentários no post 19 (Sabrina)
('Suas reflexões sempre são inspiradoras!', '2025-03-07 12:10:00', 3, 19, NULL),
('Você merece todo o sucesso!', '2025-03-07 12:20:00', 5, 19, NULL),
('Continue brilhando sempre!', '2025-03-07 12:30:00', 7, 19, NULL),
('Sua trajetória é um exemplo para muitos!', '2025-03-07 12:40:00', 9, 19, NULL);

-- 7. Criar dados para recomendações similares para o usuário 2 (Sabrina Sato)
-- Criar mais um post que seria recomendado, com hashtags que o usuário 2 curte
INSERT INTO post (legenda, data_criacao, localizacao, arquivado, visibilidade, autor_id) VALUES
('Nos bastidores do novo projeto! Aguardem novidades em breve. #TV #Entretenimento',
 '2025-03-08 16:30:00', 'São Paulo, SP', false, 'PUBLICO', 7); -- Marcos Mion

-- Vincular hashtags ao post
INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 20, id FROM hashtag WHERE nome = 'TV';

INSERT INTO post_hashtag (post_id, hashtag_id)
SELECT 20, id FROM hashtag WHERE nome = 'Entretenimento';

-- Adicionar curtidas e comentários para aumentar a pontuação
INSERT INTO curtida (data_criacao, usuario_id, post_id, comentario_id) VALUES
('2025-03-08 16:45:00', 1, 20, NULL),
('2025-03-08 16:50:00', 3, 20, NULL),
('2025-03-08 16:55:00', 4, 20, NULL),
('2025-03-08 17:00:00', 5, 20, NULL),
('2025-03-08 17:05:00', 6, 20, NULL),
('2025-03-08 17:10:00', 8, 20, NULL),
('2025-03-08 17:15:00', 9, 20, NULL),
('2025-03-08 17:20:00', 10, 20, NULL);

INSERT INTO comentario (texto, data_criacao, autor_id, post_id, comentario_pai_id) VALUES
('Mal posso esperar para ver!', '2025-03-08 17:30:00', 1, 20, NULL),
('Vai ser incrível como sempre!', '2025-03-08 17:40:00', 3, 20, NULL),
('Seus projetos são sempre sucesso!', '2025-03-08 17:50:00', 5, 20, NULL),
('Sou fã do seu trabalho!', '2025-03-08 18:00:00', 9, 20, NULL);