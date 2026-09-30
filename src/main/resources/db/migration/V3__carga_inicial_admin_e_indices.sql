-- =========================================================================
-- Migration V3: Carga inicial de usuários de teste e índices de performance
-- =========================================================================

-- Inserir Estado base se não existir
INSERT INTO estado (nome_estado)
SELECT 'São Paulo'
WHERE NOT EXISTS (SELECT 1 FROM estado WHERE nome_estado = 'São Paulo');

-- Inserir Cidade base se não existir
INSERT INTO cidade (nome_cidade, estado_id_estado)
SELECT 'São Paulo', (SELECT id_estado FROM estado WHERE nome_estado = 'São Paulo' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM cidade WHERE nome_cidade = 'São Paulo');

-- Inserir Bairro base se não existir
INSERT INTO bairro (nome_bairro, cidade_id_cidade)
SELECT 'Bela Vista', (SELECT id_cidade FROM cidade WHERE nome_cidade = 'São Paulo' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM bairro WHERE nome_bairro = 'Bela Vista');

-- Inserir Endereço base se não existir
INSERT INTO endereco (cep, numero, rua, bairro_id_bairro)
SELECT '01310100', '1000', 'Avenida Paulista', (SELECT id_bairro FROM bairro WHERE nome_bairro = 'Bela Vista' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM endereco WHERE cep = '01310100' AND numero = '1000');

-- Inserir Telefone base se não existir
INSERT INTO telefone (num_ddd, num_tel)
SELECT '11', '987654321'
WHERE NOT EXISTS (SELECT 1 FROM telefone WHERE num_ddd = '11' AND num_tel = '987654321');

-- Inserir Usuário ADMIN padrão (enzo.admin@petguardian.com / Admin@123456)
INSERT INTO usuario (nome, email, senha, role, telefone_id_telefone, endereco_id_endereco)
SELECT 'Enzo Admin', 'enzo.admin@petguardian.com', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KUOYTa', 'ADMIN',
       (SELECT id_telefone FROM telefone WHERE num_ddd = '11' AND num_tel = '987654321' LIMIT 1),
       (SELECT id_endereco FROM endereco WHERE cep = '01310100' AND numero = '1000' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'enzo.admin@petguardian.com');

-- Inserir Usuário PREMIUM padrão (carolina.cuidadora@petguardian.com / User@123456)
INSERT INTO usuario (nome, email, senha, role, telefone_id_telefone, endereco_id_endereco)
SELECT 'Carolina Cuidadora', 'carolina.cuidadora@petguardian.com', '$2a$10$iN6z1gC3I9eO9wVz1U6v0eC4gYxI3sC2B8zX9qR2aE5tL7uJ3bW8K', 'PREMIUM',
       (SELECT id_telefone FROM telefone WHERE num_ddd = '11' AND num_tel = '987654321' LIMIT 1),
       (SELECT id_endereco FROM endereco WHERE cep = '01310100' AND numero = '1000' LIMIT 1)
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'carolina.cuidadora@petguardian.com');

-- Índices de performance para otimização de consultas frequentes e chaves estrangeiras
CREATE INDEX IF NOT EXISTS idx_tarefa_usuario_status ON tarefa (usuario_id_usuario, status_id_status);
CREATE INDEX IF NOT EXISTS idx_tarefa_pet_prazo ON tarefa (pet_id_pet, prazo);
CREATE INDEX IF NOT EXISTS idx_usuario_pet_pet ON usuario_pet (pet_id_pet);
CREATE INDEX IF NOT EXISTS idx_historico_pet_data ON historico (pet_id_pet, data_hist DESC);
CREATE INDEX IF NOT EXISTS idx_pet_nome ON pet (nome);
