-- =========================================================================
-- V2: Carga Inicial Idempotente da Tabela STATUS (Oracle 19c)
-- =========================================================================

MERGE INTO status s
USING (
    SELECT 'PENDENTE' AS nome_status FROM dual UNION ALL
    SELECT 'CONCLUIDO' AS nome_status FROM dual UNION ALL
    SELECT 'EXPIRADO' AS nome_status FROM dual
) d ON (s.nome_status = d.nome_status)
WHEN NOT MATCHED THEN
    INSERT (nome_status) VALUES (d.nome_status);
