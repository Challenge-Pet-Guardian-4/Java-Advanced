-- =========================================================================
-- V2: Carga Inicial Idempotente da Tabela STATUS (Oracle 19c)
-- =========================================================================

INSERT INTO status (nome_status) SELECT 'PENDENTE' FROM dual WHERE NOT EXISTS (SELECT 1 FROM status WHERE nome_status = 'PENDENTE');
INSERT INTO status (nome_status) SELECT 'CONCLUIDO' FROM dual WHERE NOT EXISTS (SELECT 1 FROM status WHERE nome_status = 'CONCLUIDO');
INSERT INTO status (nome_status) SELECT 'EXPIRADO' FROM dual WHERE NOT EXISTS (SELECT 1 FROM status WHERE nome_status = 'EXPIRADO');
