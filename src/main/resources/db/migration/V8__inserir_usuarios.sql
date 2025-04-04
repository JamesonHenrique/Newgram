-- Inserir usuários
INSERT INTO usuario (nome, username, email, senha, bio, data_criacao) VALUES
('Rodrigo Faro', 'rodrigofaro', 'rodrigo@exemplo.com', '$2a$10$up1Fz5FymSble4vxQsPDf.3qLiu18limnVW5d5GMW/BpRXgyarOQW', 'Apresentador e empresário', '2025-03-01'),
('Sabrina Sato', 'sabrinasato', 'sabrina@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Apresentadora, ex-BBB e empresária', '2025-03-02'),
('Luciano Huck', 'lucianohuck', 'luciano@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Apresentador e empreendedor social', '2025-03-03'),
('Ivete Sangalo', 'ivetesangalo', 'ivete@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Cantora e apresentadora', '2025-03-04'),
('Neymar Jr', 'neymarjr', 'neymar@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Jogador de futebol', '2025-03-05'),
('Anitta', 'anitta', 'anitta@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Cantora, empresária e apresentadora', '2025-03-06'),
('Marcos Mion', 'marcosmion', 'mion@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Apresentador e ator', '2025-03-07'),
('Fátima Bernardes', 'fatimabernards', 'fatima@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Jornalista e apresentadora', '2025-03-08'),
('Gusttavo Lima', 'gusttavolima', 'gusttavo@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Cantor sertanejo e empresário', '2025-03-09'),
('Taís Araújo', 'taisaraujo', 'tais@exemplo.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Atriz e apresentadora', '2025-03-10');

-- Inserir status de usuário
INSERT INTO status_usuario (online, ultimo_acesso, status_personalizado, usuario_id) VALUES
(true, '2025-03-15 10:00:00', 'Gravando programa', 1),
(true, '2025-03-15 08:30:00', 'Em Tokyo', 2),
(false, '2025-03-14 09:45:00', 'Preparando o Domingão', 3),
(true, '2025-03-15 11:20:00', 'Show hoje!', 4),
(false, '2025-03-14 07:15:00', 'Treino de manhã', 5),
(true, '2025-03-15 14:00:00', 'Lançamento dia 20', 6),
(true, '2025-03-15 15:30:00', 'Caldeirão fervendo', 7),
(false, '2025-03-14 16:45:00', 'Descansando', 8),
(true, '2025-03-15 13:10:00', 'Ensaio para turnê', 9),
(true, '2025-03-15 12:00:00', 'Gravando novela', 10);