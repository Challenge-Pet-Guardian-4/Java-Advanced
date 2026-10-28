# 🐾 AGENT.md — PetGuardian API (Java-Advanced)

Guia técnico definitivo da arquitetura, convenções de código, segurança, persistência poliglota e rotinas analíticas corporativas da **PetGuardian API** (`Java-Advanced`).

---

## 🏛️ 1. Arquitetura & Stack Técnica

- **Java Version:** Java 21 LTS | **Framework:** Spring Boot 4.1.1 | **Build:** Gradle 9.x
- **Persistência Relacional:** Spring Data JPA + Hibernate 7, conectado ao **Oracle Database 19c** corporativo da FIAP (`oracle.fiap.com.br:1521/orcl`). Dialeto autodetectado (`OracleDialect`), `spring.jpa.hibernate.ddl-auto=none` e migrações Flyway ativas.
- **Persistência NoSQL em Nuvem:** Spring Data MongoDB via `spring.mongodb.uri=${MONGODB_URI}` apontando para o cluster **MongoDB Atlas na Nuvem** (`petguardian.agoderb.mongodb.net`), armazenando conteúdo didático rico em Markdown e vídeos (`conteudos_aula`).
- **Segurança Stateless:** Spring Security + OAuth2 Resource Server com tokens JWT assinados via par de chaves assimétricas **RSA 2048-bit** (PKCS#8).
- **HTTP Client Declarativo:** `@HttpExchange` e `@GetExchange` com `RestClient` via `@ImportHttpServices` para integração de CEPs com o ViaCEP.
- **Cache:** Spring Cache em memória para resolução estática de domínios como `Status`.
- **Validação Declarativa:** Bean Validation em DTOs Records puros/imutáveis com anotações de domínio customizadas (`@DddValidation`, `@EnumValidation`).
- **Documentação de API:** SpringDoc OpenAPI 3 / Swagger UI 2.8.5 com prefixos visuais padronizados por autoridade (`[PÚBLICO]`, `[AUTENTICADO]`, `[CUIDADOR]`, `[PREMIUM / ADMIN]`, `[RESPONSÁVEL / ADMIN]`, `[ADMIN]`).

---

## 👥 2. Perfis de Acesso (RBAC) & Credenciais Oficiais

A autorização é governada por **Role-Based Access Control (RBAC)** em 3 níveis:

| Perfil (`UsuarioRole`) | Escopo Autorizado | Restrições |
| :--- | :--- | :--- |
| **`COMUM`** | Cadastro gratuito padrão. Gestão dos próprios Pets, Care Circle, Tarefas diárias, Histórico Clínico e Perfil próprio. | Bloqueado para Trilhas, Módulos, Aulas, Conteúdos NoSQL, Endereços e relatórios globais (`403 Forbidden`). |
| **`PREMIUM`** | Tutores assinantes. Todo o escopo de `COMUM` + visualização de Trilhas, Módulos, Aulas didáticas e conclusão de lições somando pontos ao pet. | Bloqueado para criação, edição ou deleção de conteúdos pedagógicos e infraestrutura (`403 Forbidden`). |
| **`ADMIN`** | Acesso irrestrito a todo o ecossistema: bypass de tutor em pets/tarefas, promoção de roles, CRUD pedagógico completo, gravação NoSQL no MongoDB Atlas e gestão de endereços. | Nenhuma. |

### 🔑 Credenciais Provisionadas para Testes & Correção

| Perfil (Role) | E-mail Oficial | Senha Padrão | BCrypt Hash (V3 Flyway / .env) |
|---|---|---|---|
| **ADMIN** | `enzo.admin@petguardian.com` | `123mudar` | `$2a$10$CGKqNEY043BjTqp9faNVoOodCnu3uU.rLNuuHR4XqwZTXjuShRf0G` |
| **COMUM** | `enzo.teste@petguardian.com` | `Senha123` | `$2a$10$P2N5G6U8B9...` (Usuário de controle para testes negativos de RBAC) |

- **Tokens JWT:** Emitidos via `POST /login`, válidos por 1 hora, transportando claim `sub` (e-mail) convertida em autoridades Spring Security (`ROLE_COMUM`, `ROLE_PREMIUM`, `ROLE_ADMIN`).
- **Endpoints Públicos:** `POST /login`, `POST /usuarios`, documentação Swagger (`/swagger-ui/**`, `/v3/api-docs/**`) e healthcheck Actuator (`/actuator/health`).

---

## 🗄️ 3. Integração com Stored Procedures & Functions Oracle (`PKG_PETGUARDIAN`)

A camada de persistência integra diretamente 5 rotinas empacotadas no Oracle PL/SQL corporativo através de anotações `@Procedure` e `@NamedStoredProcedureQueries` no Spring Data JPA:

| Rotina PL/SQL | Tipo | Entidade / Mapeamento | Repositório | Finalidade Corporativa |
| :--- | :---: | :--- | :--- | :--- |
| `pkg_petguardian.pr_calcular_pontuacao_pet` | Procedure | `@NamedStoredProcedureQuery("Pet.calcularPontuacaoPet")` | `PetRepository` | Calcula em tempo real pontos de tarefas concluídas, aulas didáticas e pontuação consolidada do animal. |
| `pkg_petguardian.pr_calcular_pontuacao_usuario` | Procedure | `@NamedStoredProcedureQuery("Pet.calcularPontuacaoUsuario")` | `PetRepository` | Consolida em batch único a pontuação acumulada de todos os pets e o detalhamento individual serializado via JSON nativo Oracle (`p_detalhe_pets_json`), eliminando queries N+1. |
| `pkg_petguardian.pr_obter_resumo_rede_cuidado` | Procedure | `@NamedStoredProcedureQuery("UsuarioPet.obterResumoRedeCuidado")` | `UsuarioPetRepository` | Retorna total de pets, cuidadores, tarefas pendentes, tarefas concluídas e pontos agregados da rede familiar. |
| `pkg_petguardian.fn_obter_nivel_usuario` | Function | `@Procedure("pkg_petguardian.fn_obter_nivel_usuario")` | `UsuarioPetRepository` | Retorna o nível de gamificação do tutor (`BRONZE`, `PRATA`, `OURO`) baseado no XP total acumulado. |
| `pkg_petguardian.pr_transferir_responsavel_principal` | Procedure | `@Procedure("pkg_petguardian.pr_transferir_responsavel_principal")` | `UsuarioPetRepository` | Executa a transferência atômica de tutela do tutor anterior para o novo no Care Circle (`USUARIO_PET`). |

> **Nota Técnica sobre `pr_obter_resumo_rede_cuidado` e `consolidação da Rede de Cuidado`:**  
> A procedure Oracle calcula 5 parâmetros OUT (`p_total_pets`, `p_total_cuidadores`, `p_tarefas_pendentes`, `p_tarefas_concluidas`, `p_pontos_acumulados`), os quais são estritamente mapeados na `@NamedStoredProcedureQuery` para integridade JDBC. No DTO final (`RedeCuidadoResponse`), `pets` e `cuidadores` são retornados como coleções completas detalhadas (cujos tamanhos refletem a contagem), enquanto tarefas e pontos são extraídos dos parâmetros OUT.

### ⚠️ Regra Crítica de Mapeamento para Hibernate 7
No Hibernate 7 / Spring Boot, procedures que retornam múltiplos parâmetros `OUT` exigem declaração explícita de tipos com `@StoredProcedureParameter(mode = ParameterMode.OUT, type = Integer.class)` via `@NamedStoredProcedureQueries` na entidade (`Pet` e `UsuarioPet`). Não invocar procedures com múltiplos parâmetros OUT sem esse mapeamento explícito para evitar `ParameterTypeException`.

- **Trigger de Auditoria:** `trg_audit_tarefa` é disparada automaticamente após DML em `TAREFA`, gravando valores `:OLD` e `:NEW` em `AUDITORIA_DML_TAREFA`.

---

## 🍃 4. Arquitetura Poliglota (Oracle 19c + MongoDB Atlas NoSQL)

1. **Separação de Responsabilidades:**
   - **Oracle Database 19c:** Governa entidades relacionais normalizadas em 3FN, transações financeiras/cadastrais, integridade referencial estrita e procedures de gamificação (`usuario`, `pet`, `usuario_pet`, `tarefa`, `trilha`, `modulo`, `aula`, `historico`, `endereco`).
   - **MongoDB Atlas (Nuvem):** Coleção `conteudos_aula` armazena documentos NoSQL com estruturas dinâmicas de textos longos em Markdown, metadados pedagógicos e arrays de links multimídia.
2. **Resiliência e Fallback Seguro:**
   - Os serviços `AulaService` e `TrilhaService` agregam os metadados relacionais do Oracle com o documento correspondente do MongoDB de forma tolerante a falhas (fallback gracioso caso o cluster NoSQL apresente instabilidade de rede).
3. **Controle de Escrita NoSQL:**
   - Operações de escrita (`POST`, `PUT`, `DELETE /aulas/{id}/conteudo`) são restritas com exclusividade a perfis `ADMIN`. Usuários `PREMIUM` têm acesso apenas de leitura (`GET`).

---

## 🌐 5. Mapa Canônico de Endpoints da API

### Autenticação & Usuários (`/login`, `/usuarios`)
- `POST /login`: `[PÚBLICO]` Autenticação e emissão de token JWT com claims e dados cadastrais.
- `POST /usuarios`: `[PÚBLICO]` Cadastro público de novo usuário (nasce sempre como `COMUM`).
- `GET /usuarios/me`: `[AUTENTICADO]` Detalhes cadastrais do usuário logado via JWT.
- `PUT /usuarios/me`: `[AUTENTICADO]` Atualização cadastral própria. Caso o e-mail seja modificado, emite novo token JWT no header `X-Novo-Token`.
- `DELETE /usuarios/me`: `[AUTENTICADO]` Encerramento da própria conta e remoção dos vínculos.
- `GET /usuarios/me/rede-cuidado`: `[AUTENTICADO]` Visão agregada do Care Circle do tutor (pets, co-cuidadores e resumo via Stored Procedure).
- `PATCH /usuarios/me/upgrade-premium`: `[AUTENTICADO]` Auto-promoção de perfil de `COMUM` para `PREMIUM`.
- `GET /usuarios`: `[ADMIN]` Listagem global de usuários com paginação e ordenação.
- `GET /usuarios/by-email`: `[ADMIN]` Busca de usuário por parâmetro de e-mail exato.
- `GET /usuarios/{id}`: `[ADMIN]` Detalhes de um usuário por ID (exclusivo para `ADMIN`).
- `GET /usuarios/{id}/rede-cuidado`: `[ADMIN]` Visão da rede de cuidado de qualquer usuário do sistema.
- `PUT /usuarios/{id}`: `[ADMIN]` Atualização cadastral por ID (exclusivo para `ADMIN`).
- `DELETE /usuarios/{id}`: `[ADMIN]` Remoção de conta por ID (exclusivo para `ADMIN`).
- `PATCH /usuarios/{id}/role`: `[ADMIN]` Alteração da role de qualquer usuário (`COMUM`, `PREMIUM`, `ADMIN`).
- `PATCH /usuarios/{id}/upgrade-premium`: `[ADMIN]` Promoção para `PREMIUM` por ID (exclusivo para `ADMIN`).

### Pets & Care Circle (`/pets`)
- `GET /pets/me`: `[CUIDADOR]` Lista pets associados ao usuário logado (como tutor principal ou co-cuidador).
- `GET /pets/me/pontos`: `[CUIDADOR]` Pontuação total acumulada de todos os pets do usuário via Stored Procedure Oracle.
- `POST /pets`: `[AUTENTICADO]` Cadastro de novo pet vinculando criador como responsável principal no Care Circle.
- `GET /pets/{id}`: `[CUIDADOR / ADMIN]` Buscar pet por ID.
- `GET /pets/{id}/pontos`: `[CUIDADOR / ADMIN]` Consulta pontuação consolidada do pet via Stored Procedure Oracle (`pkg_petguardian.pr_calcular_pontuacao_pet`).
- `GET /pets/{id}/detalhe`: `[CUIDADOR / ADMIN]` Ficha consolidada com pontuação, cuidadores, tarefas concluídas e prontuário.
- `PUT /pets/{id}`: `[RESPONSÁVEL / ADMIN]` Atualizar cadastro do pet.
- `DELETE /pets/{id}`: `[RESPONSÁVEL / ADMIN]` Deletar pet.
- `GET /pets`: `[ADMIN]` Listagem global de todos os pets da base com paginação.
- `GET /pets/by-usuario/{usuarioId}`: `[ADMIN]` Listar pets associados a determinado tutor.
- `GET /pets/{petId}/cuidadores`: `[CUIDADOR / ADMIN]` Listar co-cuidadores vinculados ao animal.
- `POST /pets/{petId}/cuidadores`: `[RESPONSÁVEL / ADMIN]` Convidar novo co-cuidador por e-mail.
- `DELETE /pets/{petId}/cuidadores/me`: `[CUIDADOR / ADMIN]` Desvinculação voluntária do próprio cuidador logado.
- `DELETE /pets/{petId}/cuidadores`: `[RESPONSÁVEL / ADMIN]` Desvinculação de co-cuidador por e-mail (`?email=...`).
- `PATCH /pets/{petId}/responsavel-principal`: `[RESPONSÁVEL / ADMIN]` Transferência atômica de titularidade via Stored Procedure Oracle.

### Tarefas da Rotina (`/tarefas`)
- `GET /tarefas/me`: `[CUIDADOR]` Tarefas do usuário autenticado com filtro opcional de status e auto-expiração dinâmica.
- `GET /tarefas/me/pontos`: `[CUIDADOR]` Pontos totais acumulados pelo cuidador autenticado.
- `GET /tarefas/by-pet/{petId}`: `[CUIDADOR / ADMIN]` Lista tarefas de rotina de um pet.
- `GET /tarefas/{id}`: `[CUIDADOR / ADMIN]` Buscar tarefa por ID.
- `POST /tarefas`: `[CUIDADOR / ADMIN]` Criar nova tarefa de rotina (prazo futuro obrigatório via `@Future`).
- `PUT /tarefas/{id}`: `[CUIDADOR / ADMIN]` Atualizar dados da tarefa.
- `PATCH /tarefas/{id}/concluir`: `[CUIDADOR / ADMIN]` Concluir tarefa (status `CONCLUIDO`), somando pontos e disparando trigger de auditoria.
- `PATCH /tarefas/{id}/desmarcar`: `[CUIDADOR / ADMIN]` Desmarcar tarefa concluída (retorna a `PENDENTE`), estornando pontos.
- `DELETE /tarefas/{id}`: `[CUIDADOR / ADMIN]` Excluir tarefa.
- `GET /tarefas`: `[ADMIN]` Listagem global de todas as tarefas cadastradas no sistema.
- `GET /tarefas/by-usuario/{usuarioId}`: `[ADMIN]` Listar tarefas de determinado cuidador por ID.
- `GET /tarefas/by-usuario/{usuarioId}/pontos`: `[ADMIN]` Consultar pontos acumulados de tarefas por usuário.

### Histórico Clínico de Saúde (`/historicos`)
- `GET /historicos/me`: `[CUIDADOR]` Histórico agregado de todos os pets do cuidador logado.
- `GET /historicos/pet/{petId}`: `[CUIDADOR / ADMIN]` Prontuário médico ordenado por data mais recente.
- `GET /historicos/{id}`: `[CUIDADOR / ADMIN]` Buscar evento de saúde por ID.
- `POST /historicos`: `[CUIDADOR / ADMIN]` Registrar vacina, consulta, exame ou cirurgia.
- `PUT /historicos/{id}`: `[CUIDADOR / ADMIN]` Atualizar registro médico.
- `DELETE /historicos/{id}`: `[CUIDADOR / ADMIN]` Excluir registro clínico.
- `GET /historicos`: `[ADMIN]` Listagem global de prontuários com paginação.

### Trilhas Educativas, Módulos & Aulas (`/trilhas`, `/modulos`, `/aulas`)
- `GET /trilhas/me`: `[PREMIUM / ADMIN]` Trilhas, módulos e aulas completas dos pets do tutor logado em árvore unificada.
- `GET /trilhas` | `GET /trilhas/{id}`: `[PREMIUM / ADMIN]` Consulta de trilhas pedagógicas.
- `POST /trilhas` | `PUT /trilhas/{id}` | `DELETE /trilhas/{id}`: `[ADMIN]` Gestão do ciclo de vida da trilha.
- `GET /modulos` | `GET /modulos/{id}` | `GET /modulos/trilha/{trilhaId}`: `[PREMIUM / ADMIN]` Consulta de módulos temáticos.
- `POST /modulos` | `PUT /modulos/{id}` | `DELETE /modulos/{id}`: `[ADMIN]` Gestão do ciclo de vida de módulos.
- `GET /aulas` | `GET /aulas/{id}` | `GET /aulas/modulo/{moduloId}`: `[PREMIUM / ADMIN]` Consulta de aulas didáticas.
- `PATCH /aulas/{id}/concluir`: `[PREMIUM / ADMIN]` Concluir lição e somar pontuação educacional ao score do animal.
- `PATCH /aulas/{id}/desmarcar`: `[PREMIUM / ADMIN]` Desmarcar lição e estornar pontos do animal.
- `POST /aulas` | `PUT /aulas/{id}` | `DELETE /aulas/{id}`: `[ADMIN]` Gestão do ciclo de vida de aulas.

### Conteúdos NoSQL no MongoDB Atlas (`/aulas`)
- `GET /aulas/{aulaId}/conteudo`: `[PREMIUM / ADMIN]` Obter texto didático rico em Markdown e vídeos do cluster MongoDB Atlas.
- `GET /aulas/conteudos`: `[ADMIN]` Listagem de todos os conteúdos NoSQL do cluster com paginação.
- `POST /aulas/{aulaId}/conteudo`: `[ADMIN]` Inserir documento NoSQL no cluster MongoDB Atlas para a aula.
- `PUT /aulas/{aulaId}/conteudo`: `[ADMIN]` Atualizar documento NoSQL no MongoDB Atlas.
- `DELETE /aulas/{aulaId}/conteudo`: `[ADMIN]` Deletar documento NoSQL do MongoDB Atlas.

### Endereços com Integração ViaCEP (`/enderecos`)
- `GET /enderecos`: `[ADMIN]` Listagem global de endereços normalizados.
- `GET /enderecos/{id}`: `[ADMIN]` Buscar endereço por ID.
- `POST /enderecos`: `[ADMIN]` Criar endereço com resolução automática via ViaCEP (body: `{"cep": "01310-200", "numero": "1578"}`).
- `PUT /enderecos/{id}`: `[ADMIN]` Atualizar número ou CEP com nova resolução.
- `DELETE /enderecos/{id}`: `[ADMIN]` Remover endereço por ID.

---

## 📏 6. Convenções de Código & Diretrizes Arquiteturais

1. **Top-Level Imports Obrigatórios:** Proibido uso de FQCN inline. Todos os tipos devem ser importados no topo do arquivo.
2. **Tipagem Estrita (Zero `any`):** Garantir tipagem forte em contratos de API, DTOs e entidades.
3. **DTOs Puros (Records):** DTOs de entrada e saída devem ser `record` imutáveis contendo validações declarativas (`@NotBlank`, `@NotNull`, `@Pattern`, `@Positive`, `@Future`) e métodos de conversão estáticos ou `toEntity()`.
4. **Sem Null-Checks Redundantes:** Confiar nas validações do Bean Validation no pipeline HTTP e nas constraints relacionais (`NOT NULL`) do Oracle. Realizar verificações apenas onde `null` for estado de negócio plausível.
5. **Desacoplamento Horizontal:** Services injetam apenas os **Repositories** necessários das entidades vizinhas, nunca Services irmãos (prevenção de dependência circular).
6. **Segregação de Repositórios Poliglotas:**
   - `@EnableJpaRepositories` gerencia as entidades relacionais Oracle (`fiap.com.br.petguardian.*`).
   - `@EnableMongoRepositories` gerencia exclusivamente os documentos NoSQL (`fiap.com.br.petguardian.trilha.aula.conteudo`).
   - Configuração de URI via `spring.mongodb.uri=${MONGODB_URI}`.
7. **Tratamento Global de Erros (`GlobalExceptionHandler`):**
   - `400`: `MethodArgumentNotValidException`, `IllegalArgumentException`.
   - `401`: `AuthenticationException`.
   - `403`: `AccessDeniedException` / `AuthorizationDeniedException`.
   - `404`: `ResourceNotFoundException`.
   - `409`: `DataIntegrityViolationException` (violações de chave única/integridade relacional como `ORA-00001`).
8. **Flyway Versionado & Idempotente:**
   - `V1__criar_tabelas.sql`: DDL relacional em 3FN compatível com Oracle 19c.
   - `V2__carga_inicial_status.sql`: Status de domínio fixos (`PENDENTE=1`, `CONCLUIDO=2`, `EXPIRADO=3`).
   - `V3__admin_inicial.sql`: Usuário administrador inicial (`enzo.admin@petguardian.com`) parametrizado via `${ADMIN_BCRYPT_HASH}`.
   - `V4__pkg_petguardian.sql`: Migração que compila a package e triggers de auditoria PL/SQL (`PKG_PETGUARDIAN`).
9. **Auditoria Automatizada RBAC e Integração:**
   - Suíte de validação formal automatizada no repositório `Compliance-Quality-Assurance-Tests` (Postman Collection) e 292 testes JUnit 5 (MockMvc, Services, GlobalExceptionHandler, Validadores e Spring Security Test) com 99.53% de cobertura JaCoCo, cobrindo CRUDs de Admin, procedures Oracle, resiliência NoSQL/ViaCEP e barreiras negativas de HTTP 403.
