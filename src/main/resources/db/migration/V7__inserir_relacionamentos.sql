-- Inserir seguidores
INSERT INTO seguidor (data_criacao, notificacoes_ativadas, seguidor_id, seguido_id) VALUES
('2025-03-11', true, 2, 1), -- Sabrina segue Rodrigo
('2025-03-11', true, 2, 5),
('2025-03-11', true, 1, 2), -- Rodrigo segue Sabrina
('2025-03-12', true, 3, 1), -- Luciano segue Rodrigo
('2025-03-12', true, 4, 1), -- Ivete segue Rodrigo
('2025-03-13', true, 1, 4), -- Rodrigo segue Ivete
('2025-03-13', true, 5, 3), -- Neymar segue Luciano
('2025-03-14', true, 6, 1), -- Anitta segue Rodrigo
('2025-03-14', true, 7, 1), -- Mion segue Rodrigo
('2025-03-15', true, 8, 1), -- Fátima segue Rodrigo
('2025-03-15', true, 9, 1), -- Gusttavo segue Rodrigo
('2025-03-12', true, 11, 12), -- Grazi segue Caio
('2025-03-12', true, 12, 11), -- Caio segue Grazi
('2025-03-13', true, 13, 11), -- Juliana segue Grazi
('2025-03-13', true, 14, 15), -- Pedro segue Jade
('2025-03-14', true, 15, 14), -- Jade segue Pedro
('2025-03-14', true, 16, 17), -- Whindersson segue Tatá
('2025-03-15', true, 17, 16), -- Tatá segue Whindersson
('2025-03-15', true, 18, 19), -- Juliette segue Gil
('2025-03-16', true, 19, 18), -- Gil segue Juliette
('2025-03-16', true, 20, 21), -- Bruna segue Gabigol
('2025-03-17', true, 21, 20), -- Gabigol segue Bruna
('2025-03-17', true, 22, 20), -- Marina segue Bruna
('2025-03-18', true, 23, 26), -- Alok segue Ludmilla
('2025-03-18', true, 24, 13), -- Paolla segue Juliana
('2025-03-19', true, 25, 16), -- Carlinhos segue Whindersson
('2025-03-19', true, 26, 23), -- Ludmilla segue Alok
('2025-03-20', true, 27, 29), -- Luan segue Wesley
('2025-03-20', true, 28, 17), -- Xuxa segue Tatá
('2025-03-21', true, 29, 27), -- Wesley segue Luan
('2025-03-21', true, 30, 24), -- Gracyanne segue Paolla
('2025-03-22', true, 5, 1),   -- Neymar segue Rodrigo
('2025-03-22', true, 10, 1),  -- Faustão segue Rodrigo
('2025-03-22', true, 11, 1),  -- Grazi segue Rodrigo
('2025-03-22', true, 12, 1),  -- Caio segue Rodrigo
('2025-03-22', true, 13, 1),  -- Juliana segue Rodrigo
('2025-03-22', true, 14, 1),  -- Pedro segue Rodrigo
('2025-03-22', true, 15, 1),  -- Jade segue Rodrigo
('2025-03-22', true, 16, 1),  -- Whindersson segue Rodrigo
('2025-03-22', true, 17, 1),  -- Tatá segue Rodrigo
('2025-03-22', true, 18, 1),  -- Juliette segue Rodrigo
('2025-03-22', true, 19, 1),  -- Gil segue Rodrigo
('2025-03-22', true, 20, 1),  -- Bruna segue Rodrigo
('2025-03-22', true, 21, 1),  -- Gabigol segue Rodrigo
('2025-03-22', true, 22, 1),  -- Marina segue Rodrigo
('2025-03-22', true, 23, 1),  -- Alok segue Rodrigo
('2025-03-22', true, 24, 1),  -- Paolla segue Rodrigo
('2025-03-22', true, 25, 1),  -- Carlinhos segue Rodrigo
('2025-03-22', true, 26, 1),  -- Ludmilla segue Rodrigo
('2025-03-22', true, 27, 1),  -- Luan segue Rodrigo
('2025-03-22', true, 28, 1),  -- Xuxa segue Rodrigo
('2025-03-22', true, 29, 1),  -- Wesley segue Rodrigo
('2025-03-22', true, 30, 1);  -- Gracyanne segue Rodrigo
INSERT INTO usuario_seguindo (usuario_id, seguindo_id) VALUES
(2, 1),
(2, 5),
(2, 8),
(1, 2), -- Rodrigo segue Sabrina
(3, 1), -- Luciano segue Rodrigo
(4, 1), -- Ivete segue Rodrigo
(1, 4), -- Rodrigo segue Ivete
(5, 3), -- Neymar segue Luciano
(6, 1), -- Anitta segue Rodrigo
(7, 1), -- Mion segue Rodrigo
(8, 1), -- Fátima segue Rodrigo
(9, 1), -- Gusttavo segue Rodrigo
(11, 12), -- Grazi segue Caio
(12, 11), -- Caio segue Grazi
(13, 11), -- Juliana segue Grazi
(14, 15), -- Pedro segue Jade
(15, 14), -- Jade segue Pedro
(16, 17), -- Whindersson segue Tatá
(17, 16), -- Tatá segue Whindersson
(18, 19), -- Juliette segue Gil
(19, 18), -- Gil segue Juliette
(20, 21), -- Bruna segue Gabigol
(21, 20), -- Gabigol segue Bruna
(22, 20), -- Marina segue Bruna
(23, 26), -- Alok segue Ludmilla
(24, 13), -- Paolla segue Juliana
(25, 16), -- Carlinhos segue Whindersson
(26, 23), -- Ludmilla segue Alok
(27, 29), -- Luan segue Wesley
(28, 17), -- Xuxa segue Tatá
(29, 27), -- Wesley segue Luan
(30, 24), -- Gracyanne segue Paolla
(5, 1),   -- Neymar segue Rodrigo
(10, 1),  -- Faustão segue Rodrigo
(11, 1),  -- Grazi segue Rodrigo
(12, 1),  -- Caio segue Rodrigo
(13, 1),  -- Juliana segue Rodrigo
(14, 1),  -- Pedro segue Rodrigo
(15, 1),  -- Jade segue Rodrigo
(16, 1),  -- Whindersson segue Rodrigo
(17, 1),  -- Tatá segue Rodrigo
(18, 1),  -- Juliette segue Rodrigo
(19, 1),  -- Gil segue Rodrigo
(20, 1),  -- Bruna segue Rodrigo
(21, 1),  -- Gabigol segue Rodrigo
(22, 1),  -- Marina segue Rodrigo
(23, 1),  -- Alok segue Rodrigo
(24, 1),  -- Paolla segue Rodrigo
(25, 1),  -- Carlinhos segue Rodrigo
(26, 1),  -- Ludmilla segue Rodrigo
(27, 1),  -- Luan segue Rodrigo
(28, 1),  -- Xuxa segue Rodrigo
(29, 1),  -- Wesley segue Rodrigo
(30, 1);  -- Gracyanne segue Rodrigo

