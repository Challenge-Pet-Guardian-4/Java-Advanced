# PetGuardian API

> **Challenge FIAP - Java Advanced (Spring Boot)**
>
> Plataforma corporativa para gestão da saúde e rotina de cuidados do pet em família sob a **Arquitetura Pet-Centric**, com persistência relacional e rotinas analíticas corporativas em **Oracle Database 19c (PL/SQL)**.

<p>
  <img src="https://img.shields.io/badge/Java-17_LTS-007396?logo=openjdk&logoColor=white" alt="Java 17 LTS" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.1.1" />
  <img src="https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white" alt="Gradle" />
  <img src="https://img.shields.io/badge/Database-Oracle_19c-F80000?logo=oracle&logoColor=white" alt="Oracle Database 19c" />
  <img src="https://img.shields.io/badge/Security-JWT_RSA_(RBAC)-F80000?logo=jsonwebtokens&logoColor=white" alt="JWT RSA" />
  <img src="https://img.shields.io/badge/Docs-OpenAPI_3_Swagger-85EA2D?logo=swagger&logoColor=black" alt="Swagger" />
</p>

| Link Rápido | URL |
|---|---|
| **Repositório GitHub** | https://github.com/Challenge-Pet-Guardian-4/Java-Advanced |
| **API em Produção (Railway)** | https://java-advanced-production-35ab.up.railway.app |
| **Swagger UI Interativo (Produção)** | https://java-advanced-production-35ab.up.railway.app/swagger-ui/index.html |
| **OpenAPI Docs JSON (Produção)** | https://java-advanced-production-35ab.up.railway.app/v3/api-docs |
| **Actuator Health (Produção)** | https://java-advanced-production-35ab.up.railway.app/actuator/health |
| **Swagger UI (Local)** | http://localhost:8080/swagger-ui/index.html |
| **Arquivo de Coleção Insomnia** | [/docs/Insomnia_2026-05-21.yaml](./docs/Insomnia_2026-05-21.yaml) |

## 🔗 Vídeo de Demonstração

[Vídeo de Demonstração](https://youtu.be/HAolZI8EX0M)

---

## Integrantes

| Nome | RM | Turma | GitHub | LinkedIn |
| :--- | :---: | :---: | :--- | :--- |
| **Enzo Okuizumi** | **561432** | 2TDSPG | [EnzoOkuizumiFiap](https://github.com/EnzoOkuizumiFiap) | [Enzo Okuizumi](https://www.linkedin.com/in/enzo-okuizumi-b60292256/) |
| **Gustavo Okada** | **563428** | 2TDSPG | [Gdev3356](https://github.com/Gdev3356) | [Gustavo Okada](https://www.linkedin.com/in/gustavo-okada-53a3b8359/) |
| **Lucas Barros Gouveia** | **566422** | 2TDSPG | [LuzBGouveia](https://github.com/LuzBGouveia) | [Lucas Barros Gouveia](https://www.linkedin.com/in/lucas-barros-gouveia-09b147355/) |
| **Luna de Carvalho Guimarães** | **562290** | 2TDSPG | [lunaguima](https://github.com/lunaguima) | [Luna M. Guimarães](https://www.linkedin.com/in/luna-m-guimar%C3%A3es-1850ab173/) |
| **Milton Marcelino** | **564836** | 2TDSPG | [MiltonMarcelino](https://github.com/MiltonMarcelino) | [Milton Marcelino](http://linkedin.com/in/milton-marcelino-250298142) |

---

## Sobre o Projeto

O **PetGuardian** é uma API REST corporativa desenvolvida em **Spring Boot** para o ecossistema FIAP Challenge (Mentoria Clyvo 2026), estruturada sob a **Arquitetura Pet-Centric**. A plataforma resolve o desafio contemporâneo da divisão de cuidados com animais de estimação entre membros de uma família ou cuidadores compartilhados, unindo governança colaborativa, gamificação com pontuação de bem-estar e trilhas de aprendizagem.

### 🌟 Pilares da Arquitetura Pet-Centric
- **Ecossistema Centrado no Animal:** O pet não é um atributo secundário do usuário, mas a entidade nuclear (`pet`), acumulando histórico clínico, tarefas da rotina diária e pontuação própria de bem-estar.
- **Rede de Cuidado Familiar (Care Circle):** Um animal pode possuir múltiplos cuidadores com distinção de autoridade: um **Tutor Principal** (com autoridade sobre deleção e gestão de co-cuidadores) e múltiplos **Co-cuidadores** colaborativos com visibilidade em tempo real.
- **Gamificação de Bem-Estar em Dupla Camada:**
  - `pontos_tarefa`: Conquistados pelos cuidadores ao executar cuidados essenciais de rotina (alimentar, medicar, passear, higienizar).
  - `pontos_aula`: Conquistados ao finalizar lições educativas de adestramento e boas práticas, agregando pontos diretamente ao score de bem-estar do pet.
- **Ciclo de Vida Auditável:** Transições de status automatizadas (`PENDENTE`, `CONCLUIDO`, `EXPIRADO`), com suporte nativo a desmarcação imediata e auditoria de conclusões.

---

## ☁️ Arquitetura e Integração em Nuvem

A API **PetGuardian** opera conectada diretamente à infraestrutura corporativa **Oracle Database 19c** da FIAP:

* **Microsserviço da Aplicação (`Java-Advanced`):** Container Spring Boot 4.1.1 (Java 17 LTS / Gradle) com pipeline de entrega contínua vinculado ao GitHub, operando com status `Online` no Railway e em ambiente local.
* **Banco de Dados Corporativo (`Oracle 19c`):** Instância gerenciada Oracle Database (`oracle.fiap.com.br:1521/orcl`), garantindo integridade transacional estrita, persistência das tabelas relacionais e execução do pacote PL/SQL `PKG_PETGUARDIAN`.
* **Conectividade OJDBC11:** Pool de conexões otimizado com o driver oficial Oracle (`ojdbc11`) e dialeto Hibernate `OracleDialect`.

![Arquitetura de Deploy no Railway](docs/Railway.png)

---

## Modelagem Lógica e Relacional do Banco de Dados

### Modelo Lógico 
![Modelo Lógico](docs/Logical.png)

### Modelo Relacional
![Modelo Relacional](docs/Relational.png)

---

## Arquitetura

```
src/main/java/fiap/com/br/petguardian/
├── auth/                # Autenticação stateless, SecurityConfig (RBAC), Tokens JWT com chaves RSA
├── config/              # OpenAPI/Swagger, beans globais e RestClient
├── exception/           # Tratamento centralizado de exceções (GlobalExceptionHandler)
├── validation/          # Validadores customizados de Bean Validation (DDD oficial Anatel, enums dinâmicos)
│
├── usuario/             # Gestão de usuários/tutores, perfis RBAC e visão agregada da rede
├── usuariopet/          # Relação N:N Usuário x Pet (Care Circle, vínculos, transferência de tutela)
├── pet/                 # Núcleo do pet, raças, score consolidado e histórico compartilhado
│   ├── raca/            # Catálogo e autocriação de raças
│   └── historico/       # Prontuário clínico de saúde (vacinas)
│
├── tarefa/              # Rotina gamificada de cuidados, ciclo de vida e cálculo de pontos
│   └── status/          # Status de domínio (PENDENTE, CONCLUIDO, EXPIRADO)
│
├── trilha/              # Trilhas educativas de adestramento (RBAC: PREMIUM / ADMIN)
│   ├── modulo/          # Módulos temáticos da trilha
│   └── aula/            # Aulas com conteúdo didático e pontuação educacional
│
├── endereco/            # Endereço integrado declarativamente ao ViaCEP via @HttpExchange
│   ├── bairro/          # Normalização de bairros
│   ├── cidade/          # Normalização de municípios
│   └── estado/          # Normalização de estados federativos
│
└── telefone/            # Telefones de contato normalizados por DDD e número
```

---

## Tecnologias Utilizadas

| Tecnologia | Versão | Finalidade |
|---|---|---|
| **Java** | 17 LTS | Linguagem oficial do ecossistema corporativo |
| **Spring Boot** | 4.1.1 | Framework base para microsserviços REST corporativos |
| **Spring Data JPA / Hibernate** | Integrado | Mapeamento Objeto-Relacional (ORM) e consultas dinâmicas otimizadas via OracleDialect |
| **Oracle Database** | 19c (FIAP Cloud) | Sistema Gerenciador de Banco de Dados Relacional corporativo e execução de PL/SQL (`PKG_PETGUARDIAN`) |
| **Oracle JDBC (OJDBC11)** | 11.x | Driver oficial de conectividade de alto desempenho com o Oracle Database |
| **Spring JdbcTemplate** | Integrado | Invocação performática de procedures e functions nativas do Oracle Database |
| **Flyway Migration** | 10.x | Suporte a migrações versionadas com `flyway-database-oracle` |
| **Spring Security & OAuth2** | Integrado | Segurança stateless e Resource Server com validação de tokens JWT |
| **Nimbus JOSE + JWT** | Integrado | Criptografia assimétrica RSA (2048-bit) para assinatura e decodificação de tokens |
| **Spring Validation** | Integrado | Bean Validation declarativo em DTOs Records puros/imutáveis (@DddValidation, @EnumValidation, @Pattern, etc.) |
| **SpringDoc OpenAPI 3** | 2.8.5 | Geração automática de documentação e console interativo Swagger UI |
| **HTTP Service Interfaces** | Integrado | Cliente declarativo (`@HttpExchange`) para consumo assíncrono/síncrono do ViaCEP |
| **Spring Boot Actuator** | Integrado | Observabilidade com métricas e healthcheck de infraestrutura |
| **Lombok** | Integrado | Redução de código boilerplate |
| **Gradle** | 8.x | Gerenciamento determinístico de dependências e build |

---

## 🔐 Spring Security, RBAC & Credenciais de Avaliação

O sistema adota segurança corporativa stateless com autenticação JWT e par de chaves assimétricas **RSA 2048-bit** (`private_key.pem` e `public_key.pem`). A autorização é controlada por **Role-Based Access Control (RBAC)** em 3 níveis hierárquicos: `COMUM`, `PREMIUM` e `ADMIN`.

### 🛡️ Matriz de Permissões por Perfil

| Recurso / Rota | Método HTTP | `COMUM` | `PREMIUM` | `ADMIN` | Comportamento em Caso de Violação |
|---|---|:---:|:---:|:---:|---|
| `/login` | `POST` | 🔓 Livre | 🔓 Livre | 🔓 Livre | Rota pública para obtenção do token JWT |
| `/usuarios` (Cadastro) | `POST` | 🔓 Livre | 🔓 Livre | 🔓 Livre | Rota pública de cadastro (perfil atribuído sempre como `COMUM`) |
| `/usuarios/{id}/upgrade-premium` | `PATCH` | ✅ Próprio | ✅ Próprio | ✅ Permitido | `403 Forbidden` se chamado por outro usuário |
| `/usuarios/{id}/role` | `PATCH` | ❌ Bloqueado | ❌ Bloqueado | ✅ Permitido | `403 Forbidden` (exclusivo para `ADMIN` alterar role) |
| `/usuarios`, `/usuarios/by-*` (Listagens) | `GET` | ❌ Bloqueado | ❌ Bloqueado | ✅ Permitido | `403 Forbidden` (consultas gerais exclusivas para `ADMIN`) |
| `/usuarios/{id}`, `/usuarios/{id}/rede-cuidado` | `GET`, `PUT`, `DELETE` | ✅ Próprio | ✅ Próprio | ✅ Permitido | `403 Forbidden` se tentar acessar/alterar outro usuário |
| `/pets/**`, `/tarefas/**`, `/historicos/**`, `/enderecos/**` | Vários | 🔒 Cuidador | 🔒 Cuidador | ✅ Permitido | `403 Forbidden` se não pertencer ao Care Circle do pet |
| `/trilhas`, `/modulos`, `/aulas` | `GET` | ❌ Bloqueado | ✅ Permitido | ✅ Permitido | `403 Forbidden` para tutores comuns |
| `/aulas/*/concluir`, `/aulas/*/desmarcar` | `PATCH` | ❌ Bloqueado | ✅ Permitido | ✅ Permitido | `403 Forbidden` para tutores comuns |
| `/trilhas/**`, `/modulos/**`, `/aulas/**` (Gestão/CRUD) | `POST`, `PUT`, `DELETE` | ❌ Bloqueado | ❌ Bloqueado | ✅ Permitido | `403 Forbidden` para tutores comuns e premium |

### 🔑 Credenciais Pré-Cadastradas para Teste

Para agilizar a correção e os testes da banca avaliadora, os seguintes usuários já se encontram provisionados com tokens válidos e senhas criptografadas via **BCrypt**:

| Perfil (Role) | E-mail | Senha | Finalidade de Teste |
|---|---|---|---|
| **ADMIN** | `enzo.admin@petguardian.com` | `Admin@123456` | Acesso completo a todo o sistema, incluindo criação e deleção de trilhas, módulos e aulas educativas. |
| **PREMIUM** | `carolina.cuidadora@petguardian.com` | `User@123456` | Gestão de pets, tarefas da família e consumo/conclusão de aulas e trilhas educativas de adestramento. |
| **COMUM** | *(Qualquer usuário com role `COMUM`)* | *(Cadastrado em `/usuarios`)* | Gestão de rotinas e pets. Ao tentar acessar `/trilhas`, a API retorna `403 Forbidden`. |

---

## 🗃️ Modelagem do Schema & Migrações de Banco

O schema relacional completo do ecossistema é mantido diretamente no **Oracle Database** corporativo da FIAP (`oracle.fiap.com.br`), garantindo integridade referencial rigorosa, sequences, índices de performance e o empacotamento do pacote PL/SQL `PKG_PETGUARDIAN`.

A modelagem contempla 15 tabelas relacionais (`usuario`, `pet`, `raca`, `usuario_pet`, `tarefa`, `status`, `historico`, `trilha`, `modulo`, `aula`, `endereco`, `bairro`, `cidade`, `estado`, `telefone`), além de tabelas de auditoria:

- **Script DDL & Packages:** Disponível em [`Database-Advanced/sprint4_pkg_petguardian.sql`](../Database-Advanced/sprint4_pkg_petguardian.sql), com criação idempotente de tabelas, triggers de auditoria, constraints e procedures.
- **Suporte ao Flyway:** A aplicação inclui a biblioteca `org.flywaydb:flyway-database-oracle` no `build.gradle`. Por padrão, na base gerenciada da FIAP onde o schema já se encontra provisionado pela equipe, a execução automática permanece desabilitada (`spring.flyway.enabled=false`), permitindo validação das entidades via JPA (`spring.jpa.hibernate.ddl-auto=none`).

Configurações ativas no `application.properties`:
```properties
spring.datasource.url=${ORACLE_URL}
spring.datasource.username=${ORACLE_USER}
spring.datasource.password=${ORACLE_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=${SPRING_FLYWAY_ENABLED:false}
```

---

## 🚀 Fluxos Completos do Sistema (Além de CRUD)

O sistema implementa múltiplos fluxos transacionais e analíticos de ponta a ponta com regras de negócio corporativas complexas:

### 1. Fluxo de Gamificação e Ciclo de Vida da Rotina
1. **Criação de Tarefa:** O tutor cria uma rotina (`/tarefas`) vinculando pet e responsável. O sistema valida se o usuário pertence à rede de cuidado do animal e inicializa com status `PENDENTE`.
2. **Auto-Expiração Inteligente:** Ao listar tarefas, o método `expirarTarefasPendentesAtrasadas()` avalia o `prazo` contra o relógio do servidor (`LocalDateTime.now()`) e transiciona tarefas atrasadas para `EXPIRADO` de forma automática.
3. **Conclusão e Gamificação:** O cuidador conclui a tarefa via `PATCH /tarefas/{id}/concluir` autenticado via JWT. O sistema credita imediatamente os pontos ao cuidador (`calcularPontosTotaisUsuario`) e soma ao score de bem-estar do pet.
4. **Desmarcação Resiliente:** Se houver necessidade de cancelamento ou correção operacional, o endpoint `PATCH /tarefas/{id}/desmarcar` (autenticado via JWT) valida a titularidade do cuidador no Care Circle, remove os pontos acumulados e retorna a tarefa para `PENDENTE`.

### 2. Fluxo de Governança Familiar Pet-Centric (Care Circle)
1. **Titularidade Automática no Nascimento do Pet:** Ao cadastrar um pet (`POST /pets`), o tutor criador é registrado imediatamente na tabela `usuario_pet` com a flag `responsavel_principal = true`.
2. **Convite de Co-cuidadores:** O responsável principal convida familiares ou cuidadores (`POST /pets/{petId}/cuidadores`) informando o e-mail do convidado.
3. **Visão Agregada da Família:** O endpoint `GET /usuarios/{id}/rede-cuidado` consolida todos os animais sob responsabilidade do usuário, a lista completa de co-cuidadores de cada animal e as tarefas pendentes/concluídas da semana.
4. **Transferência de Responsabilidade Principal:** Caso a guarda ou tutela principal mude, o endpoint `PATCH /pets/{petId}/responsavel-principal` valida se a solicitação partiu do titular atual e transfere atomicamente os privilégios administrativos para o novo cuidador.

### 3. Fluxo de Trilhas Educativas com Restrição RBAC
1. **Curadoria de Conteúdo (ADMIN):** Administradores criam trilhas (`POST /trilhas`), módulos (`POST /modulos`) e lições (`POST /aulas`).
2. **Acesso Exclusivo (PREMIUM/ADMIN):** Tutores com perfil `PREMIUM` acessam as lições educativas de adestramento e boas práticas. Tutores `COMUM` recebem `403 Forbidden`.
3. **Pontuação Educacional do Pet:** Ao finalizar lições (`PATCH /aulas/{id}/concluir`), a pontuação educacional é calculada e integrada ao score consolidado do animal consultado em `GET /pets/{id}/pontos`.

### 4. Fluxo de Integração Declarativa de Endereço via ViaCEP
1. **Consumo sem Boilerplate:** Utilizando HTTP Service Interfaces (`@HttpExchange`), o serviço `ViaCepService` consome a API do ViaCEP (`https://viacep.com.br/ws/{cep}/json`).
2. **Normalização Automática de Entidades:** O `EnderecoService` decompõe a resposta, garantindo a normalização e reaproveitamento de `Bairro`, `Cidade` e `Estado` no Oracle Database sem duplicidades.

### 5. Fluxo de Processamento PL/SQL no Oracle Database (Stored Procedures & Functions)
1. **Exportação de Rotinas via Stored Procedure (`pkg_petguardian.pr_exportar_tarefas_json`):** Disparado via `GET /tarefas/procedure/exportar-json` (com parâmetro opcional `statusId`), executa a procedure PL/SQL corporativa que serializa e agrega as rotinas de cuidado diretamente no motor do Oracle Database, devolvendo o documento consolidado via parâmetro `OUT CLOB`.
2. **Classificação de Gamificação via Stored Function (`pkg_petguardian.fn_classificar_pontos`):** Disparado via `GET /tarefas/procedure/classificar-pontos/{pontos}`, executa a função de categorização diretamente no banco Oracle, retornando dinamicamente a medalha de cuidado (`BRONZE`, `PRATA`, `OURO` ou `DIAMANTE (MASTER)`).

---

## 📡 Catálogo Completo de Endpoints

### 1. Autenticação (`/login`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `POST` | `/login` | Autentica com e-mail/senha e emite token JWT assinado via RSA com dados do perfil | Pública |

*Exemplo de Request:*
```json
{
  "email": "enzo.admin@petguardian.com",
  "senha": "Admin@123456"
}
```

*Exemplo de Response (200 OK):*
```json
{
  "token": "eyJhbGciOiJSUzI1NiJ9...",
  "user": {
    "id": 1,
    "nome": "Enzo Administrador",
    "email": "enzo.admin@petguardian.com",
    "role": "ADMIN",
    "ddd": "11",
    "numeroTelefone": "987654321",
    "enderecos": []
  }
}
```

---

### 2. Usuários (`/usuarios`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/usuarios/me` | Obter detalhes do próprio usuário autenticado via JWT | Autenticado |
| `PUT` | `/usuarios/me` | Atualizar dados cadastrais do próprio usuário autenticado via JWT | Autenticado |
| `GET` | `/usuarios/me/rede-cuidado` | Visão agregada da rede de cuidado do próprio usuário autenticado via JWT | Autenticado |
| `PATCH` | `/usuarios/me/upgrade-premium` | Realizar upgrade do perfil do usuário autenticado de `COMUM` para `PREMIUM` | Autenticado |
| `GET` | `/usuarios` | Listar usuários cadastrados com paginação (`?page=0&size=10&sort=nome,asc`) | `ADMIN` |
| `GET` | `/usuarios/by-email` | Buscar usuário por e-mail exato (`?email=...`) | `ADMIN` |
| `GET` | `/usuarios/{id}` | Obter detalhes de um usuário por ID | Dono da conta ou `ADMIN` |
| `GET` | `/usuarios/{id}/rede-cuidado` | Visão agregada da rede de cuidado (pets vinculados, co-cuidadores e rotinas) | Dono da conta ou `ADMIN` |
| `POST` | `/usuarios` | Cadastrar novo tutor/usuário (perfil nasce sempre como `COMUM`; endereço validado via ViaCEP) | Pública |
| `PUT` | `/usuarios/{id}` | Atualizar dados cadastrais do usuário (requer todos os dados do `UsuarioRequest`; a `role` não é alterada) | Dono da conta ou `ADMIN` |
| `DELETE` | `/usuarios/{id}` | Remover usuário | Dono da conta ou `ADMIN` |
| `PATCH` | `/usuarios/{id}/role` | Alterar role do usuário para qualquer perfil (`COMUM`, `PREMIUM`, `ADMIN`) - body: `{"role": "ADMIN"}` | `ADMIN` |
| `PATCH` | `/usuarios/{id}/upgrade-premium` | Realizar upgrade do perfil de `COMUM` para `PREMIUM` (simulação de adesão ao plano) | Dono da conta ou `ADMIN` |

---

### 3. Pets (`/pets`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/pets/me` | Listar pets associados ao usuário logado como tutor ou co-cuidador | Autenticado |
| `GET` | `/pets` | Listar todos os pets do sistema com paginação | `ADMIN` |
| `GET` | `/pets/by-usuario` | Listar todos os pets vinculados ao usuário (`?usuarioId=1`) como titular ou co-cuidador | `ADMIN` |
| `GET` | `/pets/{id}` | Buscar pet por ID | Autenticado |
| `GET` | `/pets/{id}/historico` | Histórico compartilhado consolidado de tarefas concluídas de um pet | Cuidador do pet ou `ADMIN` |
| `GET` | `/pets/{id}/pontos` | Score total consolidado (Tarefas de rotina + Aulas educativas) | Cuidador do pet ou `ADMIN` |
| `POST` | `/pets` | Cadastrar pet e vincular criador automaticamente como responsável principal | Autenticado |
| `PUT` | `/pets/{id}` | Atualizar dados do pet (autorizado apenas para o responsável principal ou `ADMIN`) | Responsável principal ou `ADMIN` |
| `DELETE` | `/pets/{id}` | Remover pet | Responsável principal ou `ADMIN` |

---

### 4. Care Circle & Co-cuidadores (`/pets/{petId}`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/pets/{petId}/cuidadores` | Listar todos os cuidadores e tutores vinculados ao pet | Cuidador do pet ou `ADMIN` |
| `POST` | `/pets/{petId}/cuidadores` | Convidar co-cuidador por e-mail (body: `{"email": "..."}`) | Responsável principal ou `ADMIN` |
| `DELETE` | `/pets/{petId}/cuidadores` | Desvincular co-cuidador do animal por e-mail (`?email=...`) via JWT | Próprio cuidador, Responsável ou `ADMIN` |
| `PATCH` | `/pets/{petId}/responsavel-principal` | Transferir a titularidade de responsável principal para outro co-cuidador (via JWT) | Responsável principal atual ou `ADMIN` |

---

### 5. Tarefas da Rotina (`/tarefas`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/tarefas/me` | Listar tarefas do cuidador autenticado via JWT com auto-expiração dinâmica | Autenticado |
| `GET` | `/tarefas/me/pontos` | Consultar pontos totais de tarefas do cuidador logado via JWT | Autenticado |
| `GET` | `/tarefas` | Listar todas as tarefas com auto-expiração automática de atrasadas | `ADMIN` |
| `GET` | `/tarefas/by-usuario` | Listar tarefas do cuidador com filtro opcional (`?usuarioId=1&status=ALL\|PENDENTE...`) | `ADMIN` |
| `GET` | `/tarefas/by-pet/{petId}` | Listar todas as tarefas da rotina de um pet | Cuidador do pet ou `ADMIN` |
| `GET` | `/tarefas/{id}` | Buscar tarefa por ID | Cuidador da tarefa ou `ADMIN` |
| `GET` | `/tarefas/by-usuario/pontos` | Obter total de pontos acumulados pelo cuidador (`?usuarioId=1`) | `ADMIN` |
| `POST` | `/tarefas` | Criar nova rotina de cuidado vinculada ao cuidador autenticado no JWT | Cuidador do pet ou `ADMIN` |
| `PUT` | `/tarefas/{id}` | Atualizar dados e status da tarefa | Cuidador da tarefa ou `ADMIN` |
| `PATCH` | `/tarefas/{id}/concluir` | Concluir tarefa via JWT (marca executor logado e credita pontos de bem-estar) | Cuidador da tarefa ou `ADMIN` |
| `PATCH` | `/tarefas/{id}/desmarcar` | Desmarcar tarefa concluída via JWT (retorna para `PENDENTE` e estorna pontos) | Cuidador da tarefa ou `ADMIN` |
| `DELETE` | `/tarefas/{id}` | Excluir tarefa | Cuidador da tarefa ou `ADMIN` |
| `GET` | `/tarefas/procedure/exportar-json` | Exportar tarefas em JSON via Stored Procedure Oracle (`pkg_petguardian.pr_exportar_tarefas_json`) | Autenticado |
| `GET` | `/tarefas/procedure/classificar-pontos/{pontos}` | Classificar pontos via Stored Function Oracle (`pkg_petguardian.fn_classificar_pontos`) | Autenticado |

---

### 6. Histórico Clínico de Saúde (`/historicos`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/historicos` | Listar registros clínicos com paginação | `ADMIN` |
| `GET` | `/historicos/pet/{petId}` | Prontuário médico de eventos do pet ordenados por data | Cuidador do pet ou `ADMIN` |
| `GET` | `/historicos/{id}` | Obter detalhes do registro de saúde por ID | Cuidador do histórico ou `ADMIN` |
| `POST` | `/historicos` | Registrar vacina, consulta, exame ou cirurgia | Cuidador do pet ou `ADMIN` |
| `PUT` | `/historicos/{id}` | Atualizar registro de histórico clínico | Cuidador do histórico e do pet ou `ADMIN` |
| `DELETE` | `/historicos/{id}` | Excluir registro de histórico clínico | Cuidador do histórico ou `ADMIN` |

---

### 7. Trilhas Educativas (`/trilhas`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/trilhas` | Listar todas as trilhas disponíveis | `PREMIUM`, `ADMIN` |
| `GET` | `/trilhas/pet/{petId}` | Listar trilhas atribuídas a um pet | `PREMIUM`, `ADMIN` |
| `GET` | `/trilhas/{id}` | Buscar trilha por ID | `PREMIUM`, `ADMIN` |
| `POST` | `/trilhas` | Criar nova trilha educativa | `ADMIN` |
| `PUT` | `/trilhas/{id}` | Atualizar trilha existente | `ADMIN` |
| `DELETE` | `/trilhas/{id}` | Excluir trilha e cascatear remoção para módulos e aulas | `ADMIN` |

---

### 8. Módulos das Trilhas (`/modulos`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/modulos` | Listar todos os módulos educativos | `PREMIUM`, `ADMIN` |
| `GET` | `/modulos/trilha/{trilhaId}` | Listar módulos de uma trilha específica | `PREMIUM`, `ADMIN` |
| `GET` | `/modulos/{id}` | Buscar módulo por ID | `PREMIUM`, `ADMIN` |
| `POST` | `/modulos` | Criar módulo associado a uma trilha | `ADMIN` |
| `PUT` | `/modulos/{id}` | Atualizar módulo existente | `ADMIN` |
| `DELETE` | `/modulos/{id}` | Excluir módulo e suas aulas | `ADMIN` |

---

### 9. Aulas Educativas (`/aulas`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/aulas` | Listar todas as aulas | `PREMIUM`, `ADMIN` |
| `GET` | `/aulas/modulo/{moduloId}` | Listar lições associadas a um módulo | `PREMIUM`, `ADMIN` |
| `GET` | `/aulas/{id}` | Buscar lição por ID | `PREMIUM`, `ADMIN` |
| `POST` | `/aulas` | Criar lição com pontuação didática | `ADMIN` |
| `PUT` | `/aulas/{id}` | Atualizar lição | `ADMIN` |
| `PATCH` | `/aulas/{id}/concluir` | Marcar aula como concluída e somar pontos ao score do pet | `PREMIUM`, `ADMIN` |
| `PATCH` | `/aulas/{id}/desmarcar` | Desmarcar aula e estornar pontos educacionais do pet | `PREMIUM`, `ADMIN` |
| `DELETE` | `/aulas/{id}` | Deletar aula | `ADMIN` |

---

### 10. Endereços (`/enderecos`)
| Método | Endpoint | Descrição | Permissão |
|---|---|---|---|
| `GET` | `/enderecos` | Listar endereços cadastrados | Autenticado |
| `GET` | `/enderecos/{id}` | Buscar endereço por ID | Autenticado |
| `POST` | `/enderecos` | Criar endereço com consulta automática ao ViaCEP (body: `{"cep": "01310-000", "numero": "100"}`) | Autenticado |
| `PUT` | `/enderecos/{id}` | Atualizar endereço | Autenticado |
| `DELETE` | `/enderecos/{id}` | Remover endereço | Autenticado |

---

## 💻 Como Executar

### Pré-requisitos
- **Java 17 LTS** instalado e configurado no `JAVA_HOME`.
- **Git** para clonagem do repositório.
- Conectividade de rede com o servidor Oracle da FIAP (`oracle.fiap.com.br:1521`).

---

### Configuração de Ambientes (`.env`)

Seguindo as melhores práticas de segurança do **OWASP**, a aplicação **não embute credenciais de produção no código-fonte**. As credenciais de conexão ao Oracle Database corporativo da FIAP são injetadas estritamente via variáveis de ambiente configuradas no arquivo `.env` na pasta `Java-Advanced`.

Copie o template `.env.example` para `.env`:

```bash
cp .env.example .env
```

Parâmetros ativos no `.env`:

```env
ORACLE_URL=jdbc:oracle:thin:@//oracle.fiap.com.br:1521/orcl
ORACLE_USER=RM561432
ORACLE_PASSWORD=sua_senha_aqui
PORT=8080
```

---

### Execução da Aplicação

Com as variáveis configuradas no `.env`:

```bash
# No Linux / macOS:
./gradlew bootRun

# No Windows (PowerShell / CMD):
.\gradlew.bat bootRun
```
*(Ou execute a classe principal `PetGuardianApplication.java` diretamente pela sua IDE favorita).*

---

## 🗄️ Procedimentos & Funções Corporativas em Oracle Database (PL/SQL)

No escopo corporativo de **Mastering Relational and Non-Relational Database**, a API PetGuardian adota o **Oracle Database 19c** como base de dados relacional oficial e centralizada para persistência de dados, integridade referencial, e execução de rotinas analíticas em lote e classificação de gamificação empacotadas no pacote PL/SQL `PKG_PETGUARDIAN`:

### 1. Configuração de Variáveis de Ambiente (.env)
As credenciais e a URL de conexão não são versionadas no código-fonte. Configure o arquivo `.env` a partir do template `.env.example`:

```properties
ORACLE_HOST=oracle.fiap.com.br
ORACLE_PORT=1521
ORACLE_SERVICE=orcl
ORACLE_USER=RM561432
ORACLE_PASSWORD=sua_senha_aqui
```

> **Para execução local ou pelo professor:**  
> A string de conexão utilizada é:  
> `jdbc:oracle:thin:@//oracle.fiap.com.br:1521/orcl`  
> Usuário: `RM561432`  

### 2. Endpoints de Invocação Direta de Procedures
As rotinas empacotadas são disparadas diretamente pelos endpoints REST de tarefas:

* **Exportação JSON via Stored Procedure (`pkg_petguardian.pr_exportar_tarefas_json`):**
  - **Método / Rota:** `GET /tarefas/procedure/exportar-json`
  - **Parâmetros Opcionais:** `?statusId=1` (1 = PENDENTE, 2 = CONCLUIDO, 3 = EXPIRADO)
  - **Operação:** Dispara a procedure que serializa as tarefas no banco de dados e retorna o documento JSON consolidado via parâmetro `OUT CLOB`.

* **Classificação de Pontos via Stored Function (`pkg_petguardian.fn_classificar_pontos`):**
  - **Método / Rota:** `GET /tarefas/procedure/classificar-pontos/{pontos}`
  - **Exemplo:** `GET /tarefas/procedure/classificar-pontos/85`
  - **Operação:** Executa a regra corporativa de gamificação diretamente no motor do Oracle (`SELECT pkg_petguardian.fn_classificar_pontos(:pontos) FROM DUAL`), retornando `BRONZE`, `PRATA`, `OURO` ou `DIAMANTE (MASTER)`.

---

## 🧪 Testes Automatizados e Script E2E

### 1. Testes Unitários e de Integração (JUnit 5 + Mockito + Spring Security Test)
Para rodar toda a suíte de testes de autenticação, validações, serviços e controllers:

```bash
./gradlew test
```

Os relatórios detalhados de execução dos testes são gerados em:
`build/reports/tests/test/index.html`

---

### 2. Script de Validação E2E e Seed Automático (`seed-railway.ps1`)
O repositório inclui um script em PowerShell completo, idempotente e automatizado que valida o ciclo de vida completo da API em produção ou em localhost:

```powershell
# Executar contra a API em produção (Railway):
powershell -ExecutionPolicy Bypass -File .\seed-railway.ps1

# Ou executar contra o servidor local:
powershell -ExecutionPolicy Bypass -File .\seed-railway.ps1 -BaseUrl "http://localhost:8080"
```

**O que o script executa e valida:**
1. Healthcheck do Actuator (`/actuator/health`).
2. Cadastro dos usuários `ADMIN` e `PREMIUM` com CEPs reais validados no ViaCEP.
3. Autenticação JWT (`/login`) e extração do Bearer Token.
4. Cadastro de 3 pets com raças normalizadas.
5. Vínculo de co-cuidadores no Care Circle.
6. Criação de tarefas futuras, execução de `PATCH /concluir` e verificação da soma de pontos.
7. Cadastro de eventos no prontuário de saúde (`/historicos`).
8. Criação de trilhas, módulos e lições educativas com verificação das restrições RBAC.

---

## ⚠️ Tratamento de Erros

A API possui interceptador global (`@RestControllerAdvice` em `GlobalExceptionHandler`) que padroniza os erros nos formatos:

### Formato 1: Erros de Validação de Campos (`400 Bad Request`)
Disparado por falhas no Bean Validation (`@NotBlank`, `@NotNull`, `@Pattern`, `@DddValidation`, `@EnumValidation`, etc.):
```json
{
  "erros": [
    {
      "campo": "email",
      "mensagem": "deve ser um endereço de e-mail bem formado"
    },
    {
      "campo": "nome",
      "mensagem": "não deve estar em branco"
    }
  ]
}
```

### Formato 2: Erros de Domínio, Não Encontrado e Segurança
Disparado por `ResourceNotFoundException` (`404`), `IllegalArgumentException` (`400`), `AuthenticationException` (`401`), `AccessDeniedException` (`403`) ou `Exception` genérica (`500`):
```json
{
  "timestamp": "2026-09-11T19:50:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Voce nao possui permissao para este recurso.",
  "path": "/trilhas"
}
```
