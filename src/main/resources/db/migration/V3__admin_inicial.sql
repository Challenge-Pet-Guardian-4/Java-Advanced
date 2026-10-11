-- =========================================================================
-- V3: Criação Condicional do Usuário Administrador Inicial (Oracle 19c)
-- =========================================================================
-- ATENÇÃO: Esta migration só cria o admin se o placeholder ADMIN_BCRYPT_HASH tiver
-- exatamente 60 caracteres no PRIMEIRO boot da aplicação (migrations versionadas do
-- Flyway executam uma única vez). Se a variável de ambiente não estiver definida
-- ou não tiver 60 caracteres no momento da primeira execução, a criação é ignorada.

DECLARE
    v_hash VARCHAR2(100) := '${ADMIN_BCRYPT_HASH}';
    v_existe_admin NUMBER := 0;
    v_tel_id NUMBER(19);
    v_est_id NUMBER(19);
    v_cid_id NUMBER(19);
    v_bai_id NUMBER(19);
    v_end_id NUMBER(19);
BEGIN
    IF LENGTH(v_hash) = 60 THEN
        SELECT COUNT(1) INTO v_existe_admin FROM usuario WHERE email = 'enzo.admin@petguardian.com';

        IF v_existe_admin = 0 THEN
            -- Telefone dedicado do admin (garante constraint 1:1 e índice único usuario__idx)
            INSERT INTO telefone (num_ddd, num_tel) VALUES ('11', '987654321') RETURNING id_telefone INTO v_tel_id;

            -- Estado base (São Paulo alinhado ao ViaCEP e DDL oficial)
            BEGIN
                SELECT id_estado INTO v_est_id FROM estado WHERE UPPER(nome_estado) = 'SÃO PAULO' AND ROWNUM = 1;
            EXCEPTION WHEN NO_DATA_FOUND THEN
                INSERT INTO estado (nome_estado) VALUES ('São Paulo') RETURNING id_estado INTO v_est_id;
            END;

            -- Cidade base
            BEGIN
                SELECT id_cidade INTO v_cid_id FROM cidade WHERE UPPER(nome_cidade) = 'SÃO PAULO' AND estado_id_estado = v_est_id AND ROWNUM = 1;
            EXCEPTION WHEN NO_DATA_FOUND THEN
                INSERT INTO cidade (nome_cidade, estado_id_estado) VALUES ('São Paulo', v_est_id) RETURNING id_cidade INTO v_cid_id;
            END;

            -- Bairro base
            BEGIN
                SELECT id_bairro INTO v_bai_id FROM bairro WHERE UPPER(nome_bairro) = 'BELA VISTA' AND cidade_id_cidade = v_cid_id AND ROWNUM = 1;
            EXCEPTION WHEN NO_DATA_FOUND THEN
                INSERT INTO bairro (nome_bairro, cidade_id_cidade) VALUES ('Bela Vista', v_cid_id) RETURNING id_bairro INTO v_bai_id;
            END;

            -- Endereço base
            BEGIN
                SELECT id_endereco INTO v_end_id FROM endereco WHERE cep = '01310100' AND numero = '1000' AND bairro_id_bairro = v_bai_id AND ROWNUM = 1;
            EXCEPTION WHEN NO_DATA_FOUND THEN
                INSERT INTO endereco (cep, rua, numero, bairro_id_bairro) VALUES ('01310100', 'Avenida Paulista', '1000', v_bai_id) RETURNING id_endereco INTO v_end_id;
            END;

            -- Usuário ADMIN padrão
            INSERT INTO usuario (nome, email, senha, role, telefone_id_telefone, endereco_id_endereco)
            VALUES ('Enzo Admin', 'enzo.admin@petguardian.com', v_hash, 'ADMIN', v_tel_id, v_end_id);
        END IF;
    END IF;
END;
/
