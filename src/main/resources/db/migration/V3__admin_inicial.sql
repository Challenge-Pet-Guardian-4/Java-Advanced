-- =========================================================================
-- V3: Criação Condicional do Usuário Administrador Inicial (Oracle 19c)
-- =========================================================================
-- ATENÇÃO: Esta migration só cria o admin se o placeholder ADMIN_BCRYPT_HASH tiver
-- exatamente 60 caracteres no PRIMEIRO boot da aplicação (migrations versionadas do
-- Flyway executam uma única vez). Se a variável de ambiente não estiver definida
-- ou não tiver 60 caracteres no momento da primeira execução, a criação é ignorada.

DECLARE
    v_hash VARCHAR2(100) := '${ADMIN_BCRYPT_HASH}';
    v_tel_id NUMBER(19);
    v_est_id NUMBER(19);
    v_cid_id NUMBER(19);
    v_bai_id NUMBER(19);
    v_end_id NUMBER(19);
BEGIN
    IF LENGTH(v_hash) = 60 THEN
        -- Telefone base
        BEGIN
            SELECT id_telefone INTO v_tel_id FROM telefone WHERE num_ddd = '11' AND num_tel = '999999999' AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            INSERT INTO telefone (num_ddd, num_tel) VALUES ('11', '999999999') RETURNING id_telefone INTO v_tel_id;
        END;

        -- Estado base
        BEGIN
            SELECT id_estado INTO v_est_id FROM estado WHERE nome_estado = 'SP' AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            INSERT INTO estado (nome_estado) VALUES ('SP') RETURNING id_estado INTO v_est_id;
        END;

        -- Cidade base
        BEGIN
            SELECT id_cidade INTO v_cid_id FROM cidade WHERE nome_cidade = 'São Paulo' AND estado_id_estado = v_est_id AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            INSERT INTO cidade (nome_cidade, estado_id_estado) VALUES ('São Paulo', v_est_id) RETURNING id_cidade INTO v_cid_id;
        END;

        -- Bairro base
        BEGIN
            SELECT id_bairro INTO v_bai_id FROM bairro WHERE nome_bairro = 'Centro' AND cidade_id_cidade = v_cid_id AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            INSERT INTO bairro (nome_bairro, cidade_id_cidade) VALUES ('Centro', v_cid_id) RETURNING id_bairro INTO v_bai_id;
        END;

        -- Endereço base
        BEGIN
            SELECT id_endereco INTO v_end_id FROM endereco WHERE cep = '01001000' AND numero = '1' AND bairro_id_bairro = v_bai_id AND ROWNUM = 1;
        EXCEPTION WHEN NO_DATA_FOUND THEN
            INSERT INTO endereco (cep, rua, numero, bairro_id_bairro) VALUES ('01001000', 'Praça da Sé', '1', v_bai_id) RETURNING id_endereco INTO v_end_id;
        END;

        -- Usuário ADMIN
        MERGE INTO usuario u
        USING (SELECT 'admin@petguardian.com' AS email FROM dual) d
        ON (u.email = d.email)
        WHEN NOT MATCHED THEN
            INSERT (nome, email, senha, role, telefone_id_telefone, endereco_id_endereco)
            VALUES ('Administrador', 'admin@petguardian.com', v_hash, 'ADMIN', v_tel_id, v_end_id);
    END IF;
END;
/
