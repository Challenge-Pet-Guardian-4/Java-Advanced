-- =========================================================================
-- V4: Índice Composto para Otimização de Consultas de Tarefas por Status e Prazo
-- =========================================================================

BEGIN
    EXECUTE IMMEDIATE 'CREATE INDEX idx_tarefa_status_prazo ON tarefa(status_id_status, prazo)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE NOT IN (-955, -1408) THEN
            RAISE;
        END IF;
END;
/
