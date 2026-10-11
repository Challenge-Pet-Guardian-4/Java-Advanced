-- =========================================================================
-- V4: Índice Composto para Otimização de Consultas de Tarefas por Status e Prazo
-- =========================================================================

CREATE INDEX idx_tarefa_status_prazo ON tarefa(status_id_status, prazo);
