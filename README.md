# PetGuardian API

> **Challenge FIAP - Java Advanced (Spring Boot)**
>
> Plataforma corporativa para gestão da saúde e rotina de cuidados do pet em família sob a **Arquitetura Pet-Centric**, com persistência relacional e rotinas analíticas corporativas em **Oracle Database 19c (PL/SQL)** e persistência NoSQL orientada a documentos no **MongoDB Atlas na Nuvem**.

<p>
  <img src="https://img.shields.io/badge/Java-17_LTS-007396?logo=openjdk&logoColor=white" alt="Java 17 LTS" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.1.1" />
  <img src="https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white" alt="Gradle" />
  <img src="https://img.shields.io/badge/Database-Oracle_19c-F80000?logo=oracle&logoColor=white" alt="Oracle Database 19c" />
  <img src="https://img.shields.io/badge/NoSQL-MongoDB_Atlas-47A248?logo=mongodb&logoColor=white" alt="MongoDB Atlas" />
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

O **PetGuardian** é uma API REST corporativa desenvolvida em **Spring Boot** para o ecossistema FIAP Challenge (Mentoria Clyvo 2026), estruturada sob a **Arquitetura Pet-Centric**. A plataforma resolve o desafio contemporâneo da divisão de cuidados com animais de estimação entre membros de uma família ou cuidadores compartilhados, unindo governança colaborativa, gamificação com pontuação de bem-estar, persistência poliglota e trilhas de aprendizagem.

### 🌟 Pilares da Arquitetura Pet-Centric
- **Ecossistema Centrado no Animal:** O pet não é um atributo secundário do usuário, mas a entidade nuclear (`pet`), acumulando histórico clínico, tarefas da rotina diária e pontuação própria de bem-estar.
- **Rede de Cuidado Familiar (Care Circle):** Um animal pode possuir múltiplos cuidadores com distinção de autoridade: um **Tutor Principal** (com autoridade sobre deleção e gestão de co-cuidadores) e múltiplos **Co-cuidadores** colaborativos com visibilidade em tempo real.
- **Gamificação de Bem-Estar em Dupla Camada:**
  - `pontos_tarefa`: Conquistados pelos cuidadores ao executar cuidados essenciais de rotina (alimentar, medicar, passear, higienizar).
  - `pontos_aula`: Conquistados ao finalizar lições educativas de adestramento e boas práticas, agregando pontos diretamente ao score de bem-estar do pet.
- **Persistência Poliglota Integrada:** Dados relacionais em 3FN e Stored Procedures no **Oracle 19c** corporativo da FIAP + Documentos didáticos flexíveis em Markdown com vídeos no **MongoDB Atlas na Nuvem**.
- **Ciclo de Vida Auditável:** Transições de status automatizadas (`PENDENTE`, `CONCLUIDO`, `EXPIRADO`), com suporte nativo a desmarcação imediata e auditoria de conclusões via Trigger PL/SQL.

---

## ☁️ Arquitetura e Integração em Nuvem

A API **PetGuardian** opera conectada diretamente a uma infraestrutura poliglota corporativa:

* **Microsserviço da Aplicação (`Java-Advanced`):** Container Spring Boot 4.1.1 (Java 17 LTS / Gradle) com pipeline de entrega contínua vinculado ao GitHub, operando no Railway e em ambiente local.
* **Banco de Dados Relacional Corporativo (`Oracle 19c`):** Instância gerenciada Oracle Database (`oracle.fiap.com.br:1521/orcl`), garantindo integridade transacional estrita, persistência das tabelas relacionais em 3FN e execução do pacote PL/SQL `PKG_PETGUARDIAN`.
* **Banco de Dados NoSQL em Nuvem (`MongoDB Atlas`):** Cluster NoSQL em nuvem (`petguardian.agoderb.mongodb.net`), responsável pela coleção `conteudos_aula`, armazenando textos pedagógicos extensos em Markdown, metadados didáticos e links multimídia.
* **Conectividade OJDBC11:** Pool de conexões otimizado com o driver oficial Oracle (`ojdbc11`) e dialeto Hibernate autodetectado.

![Arquitetura de Deploy no Railway](docs/Railway.png)

---

## Modelagem Lógica e Relacional do Banco de Dados

### Modelo Lógico 
![Modelo Lógico](docs/Logical.png)

### Modelo Relacional
![Modelo Relacional](docs/Relational.png)

---

## Arquitetura de Pacotes

```
src/main/java/fiap/com/br/petguardian/
├── auth/                # Autenticação stateless, SecurityConfig (RBAC), Tokens JWT com chaves RSA
├── config/              # OpenAPI/Swagger, beans globais e RestClient
├── exception/           # Tratamento centralizado de exceções (GlobalExceptionHandler)
├── validation/          # Validadores customizados de Bean Validation (DDD oficial Anatel, enums dinâmicos)
│
├── usuario/             # Gestão de usuários/tutores, perfis RBAC e visão agregada da rede
├── usuariopet/          # Relação N:N Usuário x Pet (Care Circle, vínculos, transferência de tutela, procedures)
├── pet/                 # Núcleo do pet, raças, score consolidado e histórico compartilhado
│   ├── raca/            # Catálogo e autocriação de raças
│   └── historico/       # Prontuário clínico de saúde (vacinas, consultas, exames)
│
├── tarefa/              # Rotina gamificada de cuidados, ciclo de vida e cálculo de pontos
│   └── status/          # Status de domínio (PENDENTE, CONCLUIDO, EXPIRADO)
│
├── trilha/              # Trilhas educativas de adestramento (RBAC: PREMIUM / ADMIN)
│   ├── modulo/          # Módulos temáticos da trilha
│   └── aula/            # Aulas com metadados relacionais no Oracle e conteúdo didático no MongoDB Atlas
│       └── conteudo/    # Persistência NoSQL (MongoDB Atlas) de documentos Markdown enriquecidos
│
├── endereco/            # Endereço integrado declarativamente ao ViaCEP via @HttpExchange (RBAC: ADMIN)
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
| **Spring Data JPA / Hibernate 7** | Integrado | Mapeamento Objeto-Relacional (ORM) e consultas dinâmicas otimizadas com `@NamedStoredProcedureQueries` |
| **Spring Data MongoDB** | Integrado | Persistência NoSQL orientada a documentos para conteúdos didáticos ricos em Markdown (`conteudos_aula`) |
| **Oracle Database** | 19c (FIAP Cloud) | Sistema Gerenciador de Banco de Dados Relacional corporativo e execução de PL/SQL (`PKG_PETGUARDIAN`) |
| **MongoDB Atlas** | 7+ (Cloud NoSQL) | Cluster gerenciado de alta performance para armazenamento de textos longos pedagógicos |
| **Oracle JDBC (OJDBC11)** | 11.x | Driver oficial de conectividade de alto desempenho com o Oracle Database |
| **Flyway Migration** | 10.x | Suporte a migrações versionadas com `flyway-database-oracle` e scripts idempotentes |
| **Spring Security & OAuth2** | Integrado | Segurança stateless e Resource Server com validação de tokens JWT |
| **Nimbus JOSE + JWT** | Integrado | Criptografia assimétrica RSA (2048-bit) para assinatura e decodificação de tokens |
| **Spring Validation** | Integrado | Bean Validation declarativo em DTOs Records puros/imutáveis (`@DddValidation`, `@EnumValidation`, `@Pattern`, etc.) |
| **SpringDoc OpenAPI 3** | 2.8.5 | Geração automática de documentação e console interativo Swagger UI com prefixos visuais de roles |
| **HTTP Service Interfaces** | Integrado | Cliente declarativo (`@HttpExchange`) para consumo do ViaCEP |
| **Spring Boot Actuator** | Integrado | Observabilidade com métricas e healthcheck de infraestrutura |
| **Lombok** | Integrado | Redução de código boilerplate |
| **Gradle** | 8.x | Gerenciamento determinístico de dependências e build |

---

## 🔐 Spring Security, RBAC & Credenciais de Avaliação

O sistema adota segurança corporativa stateless com autenticação JWT e par de chaves assimétricas **RSA 2048-bit** (`private_key.pem` e `public_key.pem`). A autorização é governada por **Role-Based Access Control (RBAC)** em 3 níveis hierárquicos: `COMUM`, `PREMIUM` e `ADMIN`.

### 🛡️ Matriz de Permissões por Perfil

| Recurso / Rota | Método HTTP | `COMUM` | `PREMIUM` | `ADMIN` | Comportamento em Caso de Violação |
|---|---|:---:|:---:|:---:|---|
| `/login` | `POST` | 🔓 Livre | 🔓 Livre | 🔓 Livre | Rota pública para obtenção do token JWT |
| `/usuarios` (Cadastro) | `POST` | 🔓 Livre | 🔓 Livre | 🔓 Livre | Rota pública de cadastro (perfil atribuído sempre como `COMUM`) |
| `/usuarios/{id}/upgrade-premium` | `PATCH` | ✅ Próprio | ✅ Próprio | ✅ Permitido | `403 Forbidden` se chamado por outro usuário |
| `/usuarios/{id}/role` | `PATCH` | ❌ Bloqueado | ❌ Bloqueado | ✅ Permitido | `403 Forbidden` (exclusivo para `ADMIN` alterar role) |
| `/usuarios`, `/usuarios/by-*` (Listagens) | `GET` | ❌ Bloqueado | ❌ Bloqueado | ✅ Permitido | `403 Forbidden` (consultas gerais exclusivas para `ADMIN`) |
| `/usuarios/{id}`, `/usuarios/{id}/rede-cuidado` | `GET`, `PUT`, `DELETE` | ✅ Próprio | ✅ Próprio | ✅ Permitido | `403 Forbidden` se tentar acessar/alterar outro usuário |
| `/pets/**`, `/tarefas/**`, `/historicos/**` | Vários | 🔒 Cuidador | 🔒 Cuidador | ✅ Permitido | `403 Forbidden` se não pertencer ao Care Circle do pet |
| `/enderecos/**` (Infraestrutura) | `GET`, `POST`, `PUT`, `DELETE` | ❌ Bloqueado | ❌ Bloqueado | ✅ Permitido | `403 Forbidden` (gestão exclusiva de `ADMIN`) |
| `/trilhas`, `/modulos`, `/aulas` | `GET` | ❌ Bloqueado | ✅ Permitido | ✅ Permitido | `403 Forbidden` para tutores comuns |
| `/aulas/*/concluir`, `/aulas/*/desmarcar` | `PATCH` | ❌ Bloqueado | ✅ Permitido | ✅ Permitido | `403 Forbidden` para tutores comuns |
| `/trilhas/**`, `/modulos/**`, `/aulas/**` (Gestão/CRUD) | `POST`, `PUT`, `DELETE` | ❌ Bloqueado | ❌ Bloqueado | ✅ Permitido | `403 Forbidden` para tutores comuns e premium |
| `/aulas/{id}/conteudo` (MongoDB Atlas) | `GET` | ❌ Bloqueado | ✅ Permitido | ✅ Permitido | `403 Forbidden` para tutores comuns |
| `/aulas/{id}/conteudo`, `/aulas/conteudos` | `POST`, `PUT`, `DELETE`, `GET (all)` | ❌ Bloqueado | ❌ Bloqueado | ✅ Permitido | `403 Forbidden` para tutores comuns e premium |

### 🔑 Credenciais Provisionadas para Avaliação

| Perfil (Role) | E-mail Oficial | Senha Padrão | BCrypt Hash (V3 Flyway / .env) | Finalidade do Perfil |
|---|---|---|---|---|
| **ADMIN** | `enzo.admin@petguardian.com` | `123mudar` | `$2a$10$CGKqNEY043BjTqp9faNVoOodCnu3uU.rLNuuHR4XqwZTXjuShRf0G` | Acesso irrestrito a todo o sistema, promoção de roles, CRUD pedagógico completo, escrita no MongoDB Atlas e gestão de endereços. |
| **PREMIUM** | `carolina.cuidadora@petguardian.com` | `User@123456` | Hash registrado na base | Gestão de pets, tarefas e consumo/conclusão de lições pedagógicas gamificadas. |
| **COMUM** | `enzo.teste@petguardian.com` | `Senha123` | Hash registrado na base | Usuário de controle para validação de segurança RBAC (recebe `403 Forbidden` em trilhas e recursos de admin). |

---

## 🗃️ Modelagem do Schema & Migrações de Banco

O schema relacional completo do ecossistema é mantido diretamente no **Oracle Database** corporativo da FIAP (`oracle.fiap.com.br`), garantindo integridade referencial rigorosa, sequences, índices de performance e o empacotamento do pacote PL/SQL `PKG_PETGUARDIAN`.

A modelagem contempla 15 tabelas relacionais (`usuario`, `pet`, `raca`, `usuario_pet`, `tarefa`, `status`, `historico`, `trilha`, `modulo`, `aula`, `endereco`, `bairro`, `cidade`, `estado`, `telefone`), além da coleção `conteudos_aula` no MongoDB Atlas:

- **Script DDL & Packages:** Disponível em [`src/main/resources/db/migration/R__pkg_petguardian.sql`](./src/main/resources/db/migration/R__pkg_petguardian.sql), com criação idempotente de procedures, triggers de auditoria e validações.
- **Suporte ao Flyway:** A aplicação inclui `org.flywaydb:flyway-database-oracle` no `build.gradle` com migrações versionadas (`V1__...` a `V4__...`) e repetíveis (`R__...`). A execução automática é habilitada (`spring.flyway.enabled=true`, baseline 1), garantindo evolução segura e DDL gerenciado (`spring.jpa.hibernate.ddl-auto=none`).

Configurações ativas no `application.properties`:
```properties
# Oracle Database 19c (FIAP Cloud)
spring.datasource.url=${ORACLE_URL:jdbc:oracle:thin:@//oracle.fiap.com.br:1521/orcl}
spring.datasource.username=${ORACLE_USER:RM561432}
spring.datasource.password=${ORACLE_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:none}

# Flyway Migrations (Oracle 19c)
spring.flyway.enabled=${SPRING_FLYWAY_ENABLED:true}
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1
spring.flyway.placeholders.ADMIN_BCRYPT_HASH=${ADMIN_BCRYPT_HASH:$2a$10$CGKqNEY043BjTqp9faNVoOodCnu3uU.rLNuuHR4XqwZTXjuShRf0G}

# MongoDB Atlas na Nuvem (Conteúdos NoSQL)
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/petguardian}
```

---

## 🚀 Fluxos Completos do Sistema (Além de CRUD)

O sistema implementa múltiplos fluxos transacionais e analíticos de ponta a ponta com regras de negócio corporativas complexas:

### 1. Fluxo de Gamificação e Ciclo de Vida da Rotina
1. **Criação de Tarefa:** O tutor cria uma rotina (`/tarefas`) vinculando pet e responsável. O sistema valida se o usuário pertence à rede de cuidado do animal e inicializa com status `PENDENTE`.
2. **Auto-Expiração Inteligente:** Ao listar ou buscar tarefas, o método `expirarTarefasPendentesAtrasadas()` avalia o `prazo` contra o relógio do servidor (`LocalDateTime.now()`) e transiciona tarefas pendentes atrasadas para `EXPIRADO` diretamente no banco de dados via Spring Data JPA.
3. **Conclusão e Gamificação:** O cuidador conclui a tarefa via `PATCH /tarefas/{id}/concluir` autenticado via JWT. O sistema credita imediatamente os pontos ao cuidador (`calcularPontosTotaisUsuario`) e soma ao score de bem-estar do pet.
4. **Desmarcação Resiliente:** Se houver necessidade de cancelamento ou correção operacional, o endpoint `PATCH /tarefas/{id}/desmarcar` (autenticado via JWT) valida a titularidade do cuidador no Care Circle, remove os pontos acumulados e retorna a tarefa para `PENDENTE`.

### 2. Fluxo de Governança Familiar Pet-Centric (Care Circle)
1. **Titularidade Automática no Nascimento do Pet:** Ao cadastrar um pet (`POST /pets`), o tutor criador é registrado imediatamente na tabela `usuario_pet` com a flag `responsavel_principal = true`.
2. **Convite de Co-cuidadores:** O responsável principal convida familiares ou cuidadores (`POST /pets/{petId}/cuidadores`) informando o e-mail do convidado.
3. **Visão Agregada da Família:** O endpoint `GET /usuarios/me/rede-cuidado` (ou `GET /usuarios/{id}/rede-cuidado` para `ADMIN`) consolida todos os animais sob responsabilidade do usuário, a lista completa de co-cuidadores de cada animal e as tarefas pendentes/concluídas da semana via Stored Procedure Oracle.
4. **Transferência de Responsabilidade Principal:** Caso a tutela principal mude, o endpoint `PATCH /pets/{petId}/responsavel-principal` valida se a solicitação partiu do titular atual e transfere atomicamente os privilégios administrativos via Stored Procedure PL/SQL.

### 3. Fluxo de Trilhas Educativas com Persistência Poliglota (Oracle + MongoDB Atlas)
1. **Curadoria Relacional (ADMIN):** Administradores criam trilhas (`POST /trilhas`), módulos (`POST /modulos`) e lições (`POST /aulas`) no Oracle 19c.
2. **Armazenamento de Conteúdo Didático Rico (ADMIN):** O conteúdo longo em Markdown com links de apoio audiovisual é persistido no cluster MongoDB Atlas na nuvem (`POST /aulas/{id}/conteudo`).
3. **Acesso Exclusivo (PREMIUM/ADMIN):** Tutores com perfil `PREMIUM` acessam lições e conteúdos pedagógicos. Tutores `COMUM` recebem `403 Forbidden`.
4. **Pontuação Educacional do Pet:** Ao finalizar lições (`PATCH /aulas/{id}/concluir`), a pontuação educacional é calculada e integrada ao score consolidado do animal consultado em `GET /pets/{id}/pontos`.

### 4. Fluxo de Integração Declarativa de Endereço via ViaCEP
1. **Consumo sem Boilerplate:** Utilizando HTTP Service Interfaces (`@HttpExchange`), o serviço `ViaCepService` consome a API do ViaCEP (`https://viacep.com.br/ws/{cep}/json`).
2. **Normalização Automática de Entidades:** O `EnderecoService` decompõe a resposta, garantindo a normalização e reaproveitamento de `Bairro`, `Cidade` e `Estado` no Oracle Database sem duplicidades.

### 5. Fluxo de Processamento PL/SQL no Oracle Database (Stored Procedures Tipadas)
1. **Cálculo de Pontuação via Stored Procedures:** Disparado pelos endpoints canônicos de pontos (`/pets/me/pontos` e `/pets/{id}/pontos`), executa as rotinas empacotadas do Oracle (`pr_calcular_pontuacao_pet` e `pr_calcular_pontuacao_usuario`) mapeadas via `@NamedStoredProcedureQueries` no Hibernate 7.
2. **Relatório Analítico de Rede de Cuidado:** Disparado por `GET /usuarios/me/rede-cuidado` e `GET /usuarios/{id}/rede-cuidado`, executa `pkg_petguardian.pr_obter_resumo_rede_cuidado`.
3. **Transferência Atômica de Titularidade:** Disparado por `PATCH /pets/{petId}/responsavel-principal`, executa `pkg_petguardian.pr_transferir_responsavel_principal` garantindo consistência na troca do titular principal do animal.

---

## 📡 Catálogo Canônico de Endpoints

Os endpoints utilizam os seguintes prefixos visuais padronizados no Swagger UI:
- `[PÚBLICO]`: Acesso sem necessidade de autenticação.
- `[AUTENTICADO]`: Requer qualquer token JWT válido.
- `[CUIDADOR]`: Requer token JWT de tutor pertencente ao Care Circle do animal.
- `[PREMIUM / ADMIN]`: Requer perfil `ROLE_PREMIUM` ou `ROLE_ADMIN`.
- `[RESPONSÁVEL / ADMIN]`: Requer ser o responsável principal pelo pet ou `ROLE_ADMIN`.
- `[ADMIN]`: Requer perfil `ROLE_ADMIN`.

### 1. Autenticação (`/login`)
| Método | Endpoint | Resumo Swagger | Permissão |
|---|---|---|---|
| `POST` | `/login` | `[PÚBLICO]` Autenticar usuário e gerar token JWT | Pública |

*Exemplo de Request:*
```json
{
  "email": "enzo.admin@petguardian.com",
  "senha": "123mudar"
}
```

---

### 2. Usuários (`/usuarios`)
| Método | Endpoint | Resumo Swagger | Permissão |
|---|---|---|---|
| `POST` | `/usuarios` | `[PÚBLICO]` Cadastrar novo usuário | Pública |
| `GET` | `/usuarios/me` | `[AUTENTICADO]` Visualizar dados cadastrais do próprio usuário logado | Autenticado |
| `PUT` | `/usuarios/me` | `[AUTENTICADO]` Atualizar dados cadastrais do próprio usuário autenticado | Autenticado |
| `DELETE` | `/usuarios/me` | `[AUTENTICADO]` Encerrar conta do usuário autenticado | Autenticado |
| `GET` | `/usuarios/me/rede-cuidado` | `[AUTENTICADO]` Visualizar rede de cuidado consolidada do usuário | Autenticado |
| `PATCH` | `/usuarios/me/upgrade-premium` | `[AUTENTICADO]` Realizar upgrade do usuário autenticado para PREMIUM | Autenticado |
| `GET` | `/usuarios` | `[ADMIN]` Listar todos os usuários com paginação e ordenação | `ADMIN` |
| `GET` | `/usuarios/by-email` | `[ADMIN]` Buscar usuário por email | `ADMIN` |
| `GET` | `/usuarios/{id}` | `[AUTENTICADO]` Buscar usuário por ID | Próprio ou `ADMIN` |
| `GET` | `/usuarios/{id}/rede-cuidado` | `[ADMIN]` Visualizar rede de cuidado do usuário por ID | `ADMIN` |
| `PUT` | `/usuarios/{id}` | `[AUTENTICADO]` Atualizar usuário por ID | Próprio ou `ADMIN` |
| `DELETE` | `/usuarios/{id}` | `[AUTENTICADO]` Deletar usuário por ID | Próprio ou `ADMIN` |
| `PATCH` | `/usuarios/{id}/role` | `[ADMIN]` Alterar role do usuário | `ADMIN` |
| `PATCH` | `/usuarios/{id}/upgrade-premium` | `[AUTENTICADO]` Realizar upgrade de um usuário para PREMIUM | Próprio ou `ADMIN` |

---

### 3. Pets & Care Circle (`/pets`)
| Método | Endpoint | Resumo Swagger | Permissão |
|---|---|---|---|
| `GET` | `/pets/me` | `[CUIDADOR]` Listar pets associados ao usuário autenticado | `CUIDADOR` |
| `GET` | `/pets/me/pontos` | `[CUIDADOR]` Consultar pontuação total acumulada de todos os pets via Procedure Oracle | `CUIDADOR` |
| `POST` | `/pets` | `[AUTENTICADO]` Criar pet associado ao usuário autenticado como responsável | Autenticado |
| `GET` | `/pets/{id}` | `[CUIDADOR / ADMIN]` Buscar pet por ID | Cuidador ou `ADMIN` |
| `GET` | `/pets/{id}/pontos` | `[CUIDADOR / ADMIN]` Consultar pontuação total acumulada pelo pet via Procedure Oracle | Cuidador ou `ADMIN` |
| `GET` | `/pets/{id}/detalhe` | `[CUIDADOR / ADMIN]` Obter ficha consolidada do pet com pontuação, tarefas e prontuário | Cuidador ou `ADMIN` |
| `PUT` | `/pets/{id}` | `[RESPONSÁVEL / ADMIN]` Atualizar pet | Responsável ou `ADMIN` |
| `DELETE` | `/pets/{id}` | `[RESPONSÁVEL / ADMIN]` Deletar pet | Responsável ou `ADMIN` |
| `GET` | `/pets` | `[ADMIN]` Listar todos os pets com paginação e ordenação | `ADMIN` |
| `GET` | `/pets/by-usuario/{usuarioId}` | `[ADMIN]` Buscar pets associados a um usuário | `ADMIN` |
| `GET` | `/pets/{petId}/cuidadores` | `[CUIDADOR / ADMIN]` Listar cuidadores associados ao pet | Cuidador ou `ADMIN` |
| `POST` | `/pets/{petId}/cuidadores` | `[RESPONSÁVEL / ADMIN]` Adicionar cuidador ao pet por e-mail | Responsável ou `ADMIN` |
| `DELETE` | `/pets/{petId}/cuidadores/me` | `[CUIDADOR / ADMIN]` Remover o próprio vínculo de cuidador do pet | Cuidador ou `ADMIN` |
| `DELETE` | `/pets/{petId}/cuidadores` | `[RESPONSÁVEL / ADMIN]` Remover cuidador do pet por e-mail | Responsável ou `ADMIN` |
| `PATCH` | `/pets/{petId}/responsavel-principal` | `[RESPONSÁVEL / ADMIN]` Transferir responsabilidade principal do pet via Procedure Oracle | Responsável ou `ADMIN` |

---

### 4. Tarefas da Rotina (`/tarefas`)
| Método | Endpoint | Resumo Swagger | Permissão |
|---|---|---|---|
| `GET` | `/tarefas/me` | `[CUIDADOR]` Listar tarefas do cuidador autenticado com filtro opcional | `CUIDADOR` |
| `GET` | `/tarefas/me/pontos` | `[CUIDADOR]` Consultar pontos totais acumulados pelo cuidador autenticado | `CUIDADOR` |
| `GET` | `/tarefas/by-pet/{petId}` | `[CUIDADOR / ADMIN]` Listar todas as tarefas de um pet específico | Cuidador ou `ADMIN` |
| `GET` | `/tarefas/{id}` | `[CUIDADOR / ADMIN]` Buscar tarefa por ID | Cuidador ou `ADMIN` |
| `POST` | `/tarefas` | `[CUIDADOR / ADMIN]` Criar tarefa | Cuidador ou `ADMIN` |
| `PUT` | `/tarefas/{id}` | `[CUIDADOR / ADMIN]` Atualizar tarefa | Cuidador ou `ADMIN` |
| `PATCH` | `/tarefas/{id}/concluir` | `[CUIDADOR / ADMIN]` Concluir tarefa | Cuidador ou `ADMIN` |
| `PATCH` | `/tarefas/{id}/desmarcar` | `[CUIDADOR / ADMIN]` Desmarcar tarefa concluída retornando a PENDENTE | Cuidador ou `ADMIN` |
| `DELETE` | `/tarefas/{id}` | `[CUIDADOR / ADMIN]` Deletar tarefa | Cuidador ou `ADMIN` |
| `GET` | `/tarefas` | `[ADMIN]` Listar todas as tarefas com paginação e ordenação | `ADMIN` |
| `GET` | `/tarefas/by-usuario/{usuarioId}` | `[ADMIN]` Listar tarefas do cuidador com filtro opcional | `ADMIN` |
| `GET` | `/tarefas/by-usuario/{usuarioId}/pontos` | `[ADMIN]` Consultar pontos totais acumulados pelo cuidador | `ADMIN` |

---

### 5. Histórico Clínico de Saúde (`/historicos`)
| Método | Endpoint | Resumo Swagger | Permissão |
|---|---|---|---|
| `GET` | `/historicos/me` | `[CUIDADOR]` Listar histórico agregado de todos os pets do cuidador autenticado | `CUIDADOR` |
| `GET` | `/historicos/pet/{petId}` | `[CUIDADOR / ADMIN]` Listar histórico de eventos/saúde de um pet por data mais recente | Cuidador ou `ADMIN` |
| `GET` | `/historicos/{id}` | `[CUIDADOR / ADMIN]` Buscar registro de histórico por ID | Cuidador ou `ADMIN` |
| `POST` | `/historicos` | `[CUIDADOR / ADMIN]` Criar novo registro de histórico para um pet | Cuidador ou `ADMIN` |
| `PUT` | `/historicos/{id}` | `[CUIDADOR / ADMIN]` Atualizar registro de histórico por ID | Cuidador ou `ADMIN` |
| `DELETE` | `/historicos/{id}` | `[CUIDADOR / ADMIN]` Deletar registro de histórico por ID | Cuidador ou `ADMIN` |
| `GET` | `/historicos` | `[ADMIN]` Listar todos os registros de histórico com paginação e ordenação | `ADMIN` |

---

### 6. Trilhas Educativas, Módulos & Aulas (`/trilhas`, `/modulos`, `/aulas`)
| Método | Endpoint | Resumo Swagger | Permissão |
|---|---|---|---|
| `GET` | `/trilhas/me` | `[PREMIUM / ADMIN]` Listar todas as trilhas, módulos e aulas dos pets do usuário autenticado | `PREMIUM`, `ADMIN` |
| `GET` | `/trilhas` | `[PREMIUM / ADMIN]` Listar todas as trilhas cadastradas | `PREMIUM`, `ADMIN` |
| `GET` | `/trilhas/{id}` | `[PREMIUM / ADMIN]` Buscar trilha por ID | `PREMIUM`, `ADMIN` |
| `POST` | `/trilhas` | `[ADMIN]` Criar nova trilha | `ADMIN` |
| `PUT` | `/trilhas/{id}` | `[ADMIN]` Atualizar trilha existente | `ADMIN` |
| `DELETE` | `/trilhas/{id}` | `[ADMIN]` Deletar trilha por ID | `ADMIN` |
| `GET` | `/modulos` | `[PREMIUM / ADMIN]` Listar todos os módulos cadastrados | `PREMIUM`, `ADMIN` |
| `GET` | `/modulos/trilha/{trilhaId}` | `[PREMIUM / ADMIN]` Listar módulos por ID da trilha | `PREMIUM`, `ADMIN` |
| `GET` | `/modulos/{id}` | `[PREMIUM / ADMIN]` Buscar módulo por ID | `PREMIUM`, `ADMIN` |
| `POST` | `/modulos` | `[ADMIN]` Criar novo módulo | `ADMIN` |
| `PUT` | `/modulos/{id}` | `[ADMIN]` Atualizar módulo existente | `ADMIN` |
| `DELETE` | `/modulos/{id}` | `[ADMIN]` Deletar módulo por ID | `ADMIN` |
| `GET` | `/aulas` | `[PREMIUM / ADMIN]` Listar todas as aulas | `PREMIUM`, `ADMIN` |
| `GET` | `/aulas/modulo/{moduloId}` | `[PREMIUM / ADMIN]` Listar aulas por ID do módulo | `PREMIUM`, `ADMIN` |
| `GET` | `/aulas/{id}` | `[PREMIUM / ADMIN]` Buscar aula por ID | `PREMIUM`, `ADMIN` |
| `PATCH` | `/aulas/{id}/concluir` | `[PREMIUM / ADMIN]` Marcar aula como concluída e pontuar pet | `PREMIUM`, `ADMIN` |
| `PATCH` | `/aulas/{id}/desmarcar` | `[PREMIUM / ADMIN]` Desmarcar aula e estornar pontuação | `PREMIUM`, `ADMIN` |
| `POST` | `/aulas` | `[ADMIN]` Criar nova aula | `ADMIN` |
| `PUT` | `/aulas/{id}` | `[ADMIN]` Atualizar aula existente | `ADMIN` |
| `DELETE` | `/aulas/{id}` | `[ADMIN]` Deletar aula por ID | `ADMIN` |

---

### 7. Conteúdos NoSQL no MongoDB Atlas (`/aulas`)
| Método | Endpoint | Resumo Swagger | Permissão |
|---|---|---|---|
| `GET` | `/aulas/{aulaId}/conteudo` | `[PREMIUM / ADMIN]` Buscar conteúdo didático rico de uma aula no MongoDB Atlas | `PREMIUM`, `ADMIN` |
| `GET` | `/aulas/conteudos` | `[ADMIN]` Listar todos os conteúdos pedagógicos NoSQL com paginação | `ADMIN` |
| `POST` | `/aulas/{aulaId}/conteudo` | `[ADMIN]` Inserir documento de conteúdo didático no MongoDB Atlas | `ADMIN` |
| `PUT` | `/aulas/{aulaId}/conteudo` | `[ADMIN]` Atualizar documento de conteúdo didático no MongoDB Atlas | `ADMIN` |
| `DELETE` | `/aulas/{aulaId}/conteudo` | `[ADMIN]` Deletar conteúdo didático no MongoDB Atlas | `ADMIN` |

---

### 8. Endereços com Integração ViaCEP (`/enderecos`)
| Método | Endpoint | Resumo Swagger | Permissão |
|---|---|---|---|
| `GET` | `/enderecos` | `[ADMIN]` Listar todos os endereços com paginação e ordenação | `ADMIN` |
| `GET` | `/enderecos/{id}` | `[ADMIN]` Buscar endereço por ID | `ADMIN` |
| `POST` | `/enderecos` | `[ADMIN]` Criar endereço com integração automática ao ViaCEP | `ADMIN` |
| `PUT` | `/enderecos/{id}` | `[ADMIN]` Atualizar endereço por ID | `ADMIN` |
| `DELETE` | `/enderecos/{id}` | `[ADMIN]` Deletar endereço por ID | `ADMIN` |

---

## 💻 Como Executar

### Pré-requisitos
- **Java 17 LTS** instalado e configurado no `JAVA_HOME`.
- **Git** para clonagem do repositório.
- Conectividade de rede com o servidor Oracle da FIAP (`oracle.fiap.com.br:1521`) e MongoDB Atlas na nuvem.

---

### Configuração de Ambientes (`.env`)

Seguindo as melhores práticas do **OWASP**, as credenciais não são versionadas no código-fonte. Copie o template `.env.example` para `.env`:

```bash
cp .env.example .env
```

Parâmetros ativos no `.env`:

```env
# Oracle Database 19c Corporativo (FIAP Cloud)
ORACLE_URL=jdbc:oracle:thin:@//oracle.fiap.com.br:1521/orcl
ORACLE_USER=RM561432
ORACLE_PASSWORD=sua_senha_aqui
JPA_DDL_AUTO=none

# Flyway Migrations (Oracle 19c)
SPRING_FLYWAY_ENABLED=true
ADMIN_BCRYPT_HASH=$2a$10$CGKqNEY043BjTqp9faNVoOodCnu3uU.rLNuuHR4XqwZTXjuShRf0G

# MongoDB Atlas na Nuvem (Conteúdo Didático NoSQL)
MONGODB_URI=mongodb+srv://enzookuizumifiap_db_user:PAEdhlPTE5QRMzjC@petguardian.agoderb.mongodb.net/petguardian

# Servidor
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

## 🧪 Testes Automatizados e Auditoria de Segurança

### 1. Testes Unitários e de Integração (JUnit 5 + Mockito + Spring Security Test)
Para executar a suíte completa de testes unitários:

```bash
./gradlew test
```

### 2. Auditoria Automatizada de Permissões de ADMIN & RBAC
Para auditar a autorização de todas as rotas de ponta a ponta e comprovar que o perfil ADMIN tem permissões reais de criação/edição/deleção, e que o perfil COMUM é bloqueado com `403 Forbidden`:

```powershell
powershell -ExecutionPolicy Bypass -File .\scratch\test_admin_endpoints.ps1
```

- **Resultado da Auditoria:** 45 / 45 testes aprovados (100% de sucesso).
- **Relatório Técnico Consolidado:** Consulte o arquivo [relatorio_auditoria_permissoes_admin.md](./relatorio_auditoria_permissoes_admin.md) para evidências completas da matriz de testes.

---

## ⚠️ Tratamento Global de Erros

A API possui interceptador global (`@RestControllerAdvice` em `GlobalExceptionHandler`) que padroniza os erros:

### Formato 1: Erros de Validação de Campos (`400 Bad Request`)
Disparado por falhas no Bean Validation (`@NotBlank`, `@NotNull`, `@Pattern`, `@DddValidation`, `@EnumValidation`, `@Positive`, `@Future`):
```json
{
  "erros": [
    {
      "campo": "prazo",
      "mensagem": "O prazo da tarefa deve ser uma data e hora futura."
    },
    {
      "campo": "pontosTarefa",
      "mensagem": "Pontos da tarefa devem ser maiores que zero."
    }
  ]
}
```

### Formato 2: Erros de Domínio e Segurança (`401`, `403`, `404`, `409`)
Disparado por `ResourceNotFoundException` (`404`), `AuthenticationException` (`401`), `AuthorizationDeniedException` (`403`) ou `DataIntegrityViolationException` (`409`):
```json
{
  "timestamp": "2026-10-11T18:22:30.021Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Acesso negado para a requisicao /trilhas",
  "path": "/trilhas"
}
```
