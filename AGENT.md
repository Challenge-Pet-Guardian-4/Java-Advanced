# 🐾 AGENT.md - PetGuardian API (Java-Advanced)

Guia completo e documentação técnica da arquitetura, regras de negócio, perfis de acesso (RBAC), integração mobile, endpoints, validações e fluxos do projeto **PetGuardian** (`Java-Advanced`).

---

## 🏛️ 1. Visão Geral da Arquitetura & Stack

- **Java Version:** Java 17 LTS
- **Framework:** Spring Boot 4.1.1
- **HTTP Clients:** HTTP Service Interfaces declarativas (`@HttpExchange`, `@GetExchange`) registradas via `@ImportHttpServices`.
- **Persistência Relacional & ORM:** Spring Data JPA + Hibernate (com `ddl-auto=update` para sincronização e geração contínua de schema).
- **Persistência NoSQL (Documentos):** Spring Data MongoDB (`spring-boot-starter-data-mongodb`) integrado no namespace `spring.mongodb.*` do Spring Boot 4 para conteúdos ricos e dinâmicos de aulas em Markdown (`conteudos_aula`).
- **Migrações de Banco:** Flyway (`org.flywaydb:flyway-core`, desabilitado por padrão via `spring.flyway.enabled=false` em favor da sincronização nativa das entidades pelo Hibernate).
- **Segurança:** Spring Security + OAuth2 Resource Server com tokens JWT assinados via par de chaves assimétricas RSA 2048-bit (PKCS#8).
- **Testes:** JUnit 5, Mockito e Spring Boot 4 Modular Testing (`spring-boot-starter-webmvc-test` com `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`) em perfil limpo e isolado (sem H2).
- **Cache:** Spring Starter Cache (cache em memória para lookups de Status).
- **Bancos de Dados (Persistência Híbrida / Poliglota):**
  - **Oracle Database 19c** corporativo da FIAP (`oracle.fiap.com.br:1521/orcl`) com driver oficial `ojdbc11`, dialeto Hibernate `OracleDialect` e rotinas analíticas PL/SQL (`PKG_PETGUARDIAN`).
  - **MongoDB 7+ (Railway NoSQL):** cluster NoSQL na nuvem para armazenar documentos com texto rico pedagógico, links e mídias de apoio.

---

## 👥 2. Perfis de Usuário & Controle de Acesso (RBAC)

O sistema opera com três perfis de acesso formalizados no Enum `UsuarioRole`:

| Perfil (`UsuarioRole`) | Escopo de Acesso | Recursos Bloqueados |
| :--- | :--- | :--- |
| **`COMUM`** (Tutor Gratuito) | Pets, Care Circle (Rede de Cuidado), Tarefas Diárias, Histórico Clínico, Endereço e Perfil | Trilhas, Módulos, Aulas e Assistente IA |
| **`PREMIUM`** (Tutor Assinante) | Pets, Care Circle, Tarefas, Histórico, e **estudo de Trilhas, Módulos e Aulas** (`GET` e `PATCH /aulas/{id}/concluir`) | Gestão/edição de conteúdo educacional (`POST`, `PUT`, `DELETE` em Trilhas/Módulos/Aulas) |
| **`ADMIN`** (Administrador) | Acesso administrativo total: criação, edição e exclusão de Trilhas, Módulos e Aulas via backoffice/Insomnia, além de visualização geral | Nenhum |

### 🔒 Proteção de Rotas & Regras de Acesso:
- **Rotas Educativas para Alunos (`GET /trilhas/**`, `GET /modulos/**`, `GET /aulas/**`, `PATCH /aulas/{id}/concluir`):**
  - Exclusivas para **`ROLE_PREMIUM`** e **`ROLE_ADMIN`**.
  - Usuários `COMUM` recebem **`403 Forbidden`**.
- **Rotas de Gestão Educativa (`POST`, `PUT`, `DELETE` em `/trilhas/**`, `/modulos/**`, `/aulas/**`):**
  - Exclusivas para **`ROLE_ADMIN`** (executadas via API/Insomnia por curadores/administradores).
  - Usuários `COMUM` e `PREMIUM` recebem **`403 Forbidden`**.

---

## 🔐 3. Autenticação & Integração com o Mobile (JWT RSA)

```
[ Mobile App ] --- POST /login { email, senha } ---> [ Spring Boot API ]
[ Mobile App ] <-- 200 OK { token, usuario: { role } } -- [ TokenService (RSA) ]
```

- **Fluxo Stateless:** Todas as rotas autenticadas utilizam tokens Bearer JWT no header `Authorization: Bearer <token>`.
- **Rotas Públicas (`permitAll`):**
  - `POST /login` (autenticação de tutores)
  - `POST /usuarios` (cadastro de novos tutores)
  - `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/swagger-resources/**`, `/webjars/**`
  - `/actuator/health`, `/actuator/info`, `/error`
- **Token JWT:**
  - Emissor: `petguardian-api`
  - Expiração: 1 hora a partir da emissão.
  - Claims: `role` (ex: `COMUM` ou `PREMIUM` -> convertido para `ROLE_COMUM` / `ROLE_PREMIUM`), `sub` (e-mail do usuário).
- **Consumo no Mobile:**
  - O app armazena o token em armazenamento seguro (*SecureStorage* / *KeyStore*).
  - Com base em `usuario.role`, o app libera a interface ou exibe um modal convidando o usuário a assinar o plano Premium.

---

## 🔄 4. Dois Fluxos Funcionais Completos do Sistema

### 🐾 **Fluxo 1: Cuidado Colaborativo e Rotina Diária (Acesso COMUM e PREMIUM)**
1. **Cadastro & Login:** Tutor cadastra-se em `POST /usuarios` (perfil inicial sempre `COMUM`) e faz login em `POST /login`. Caso deseje acesso aos recursos educativos exclusivos, pode realizar o upgrade via `PATCH /usuarios/{id}/upgrade-premium`.
2. **Cadastro do Pet:** Criação do animal via `POST /pets` (o tutor criador torna-se automaticamente `responsavelPrincipal = true`).
3. **Formação do Care Circle:** Tutor convida co-cuidadores pelo e-mail via `POST /pets/{petId}/cuidadores`.
4. **Ciclo de Tarefas:** Cuidadores criam tarefas de rotina (`POST /tarefas` vinculadas obrigatoriamente a um cuidador) e concluem com `PATCH /tarefas/{id}/concluir`. Caso necessário, a conclusão pode ser revertida com `PATCH /tarefas/{id}/desmarcar`.
5. **Score:** Consulta de pontos acumulados do cuidador e visualização consolidada da rede em `GET /usuarios/{id}/rede-cuidado`.

### 🎓 **Fluxo 2: Gamificação Educativa & Conteúdo Rico NoSQL (Exclusivo PREMIUM)**
1. **Acesso Protegido:** Tutor `PREMIUM` acessa as trilhas de adestramento do pet via `GET /trilhas/pet/{petId}`.
2. **Progresso de Conteúdo:** Tutor navega pelos módulos (`GET /modulos/trilha/{trilhaId}`) e acessa os metadados da aula no Oracle (`GET /aulas/{id}`).
3. **Leitura Rica no MongoDB (NoSQL):** O aplicativo consome `GET /aulas/{aulaId}/conteudo` para renderizar o texto pedagógico completo formatado em Markdown, listas de links de apoio e referências de estudo persistidas no documento NoSQL da coleção `conteudos_aula`.
4. **Conclusão de Aulas:** Conclusão de aula marcando `concluida = true` com pontuação educativa via `PATCH /aulas/{id}/concluir`.
5. **Gamificação Consolidada:** O endpoint `GET /pets/{id}/pontos` agrega em tempo real os pontos das tarefas de rotina + pontos das aulas concluídas, gerando o score total de evolução do pet.

---

## 🌐 5. Catálogo Completo de Endpoints

### 🔑 Autenticação (`/login`)
| Método | Endpoint | Request Body | Response | Descrição |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/login` | `LoginRequest` (`email`, `senha`) | `LoginResponse` (`token`, `UsuarioResponse`) | Autentica o usuário e retorna o token JWT e o perfil com `role`. |

---

### 👤 Usuários (`/usuarios`)
| Método | Endpoint | Parâmetros / Body | Response | Descrição |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/usuarios/me` | Nenhum (via JWT) | `UsuarioResponse` | Busca o perfil do usuário logado diretamente via token JWT. |
| `PUT` | `/usuarios/me` | `UsuarioRequest` | `UsuarioResponse` (200 OK) | Atualiza dados cadastrais do próprio usuário logado. |
| `DELETE`| `/usuarios/me` | Nenhum (via JWT) | 204 No Content | Remove a conta do próprio usuário logado do sistema. |
| `GET` | `/usuarios/me/rede-cuidado` | Nenhum (via JWT) | `RedeCuidadoResponse` | Retorna o Care Circle consolidado do usuário logado (pets, co-cuidadores e tarefas). |
| `PATCH`| `/usuarios/me/upgrade-premium` | Nenhum (via JWT) | `UsuarioResponse` (200 OK) | Realiza o upgrade do perfil do usuário logado de `COMUM` para `PREMIUM`. |
| `GET` | `/usuarios` | `Pageable` (`page`, `size`, `sort`) | `Page<UsuarioResponse>` | Lista usuários paginados (ordenados por nome, somente ADMIN). |
| `GET` | `/usuarios/by-email` | `@RequestParam String email` | `UsuarioResponse` | Busca usuário por e-mail exato (somente ADMIN). |
| `GET` | `/usuarios/{id}` | `@PathVariable Long id` | `UsuarioResponse` | Busca usuário por ID (ADMIN ou o próprio dono). |
| `GET` | `/usuarios/{id}/rede-cuidado` | `@PathVariable Long id` | `RedeCuidadoResponse` | Retorna o Care Circle consolidado por ID (somente ADMIN). |
| `POST` | `/usuarios` | `UsuarioRequest` | `UsuarioResponse` (201 Created) | Cadastra um novo usuário (perfil sempre `COMUM`; campos obrigatórios: `nome`, `email`, `senha`, `ddd`, `numeroTelefone` e `endereco`). |
| `PUT` | `/usuarios/{id}` | `UsuarioRequest` | `UsuarioResponse` (200 OK) | Atualiza dados cadastrais do usuário por ID. |
| `DELETE`| `/usuarios/{id}` | `@PathVariable Long id` | 204 No Content | Remove o usuário do sistema (ADMIN ou o próprio dono). |
| `PATCH`| `/usuarios/{id}/role` | `RoleUpdateRequest` (`role`) | `UsuarioResponse` (200 OK) | Altera a role do usuário para qualquer perfil (`COMUM`, `PREMIUM`, `ADMIN`). Exclusivo para administradores (`ROLE_ADMIN`). |
| `PATCH`| `/usuarios/{id}/upgrade-premium` | `@PathVariable Long id` | `UsuarioResponse` (200 OK) | Realiza o upgrade do perfil de `COMUM` para `PREMIUM` por ID. |

---

### 🐶 Pets (`/pets`)
| Método | Endpoint | Parâmetros / Body | Response | Descrição |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pets/me` | Nenhum (via JWT), `Pageable` | `Page<PetResponse>` | Lista pets associados ao usuário logado (tutor principal ou co-cuidador, otimizado por email via JOIN). |
| `GET` | `/pets/me/pontos` | Nenhum (via JWT) | `PetPontuacaoAgregadaResponse` | Retorna a pontuação agregada consolidada de todos os pets do usuário logado (tarefas + aulas) em batch único via `UsuarioPet`. |
| `GET` | `/pets/me/historico` | Nenhum (via JWT) | `List<PetHistoryResponse>` | Histórico consolidado de tarefas concluídas de todos os pets do usuário logado via JWT. |
| `GET` | `/pets` | `Pageable` (`page`, `size`, `sort`) | `Page<PetResponse>` | Lista pets com paginação (somente ADMIN). |
| `GET` | `/pets/by-usuario` | `@RequestParam Long usuarioId`, `Pageable` | `Page<PetResponse>` | Busca pets associados a um ID de usuário (somente ADMIN). |
| `GET` | `/pets/{id}` | `@PathVariable Long id` | `PetResponse` | Busca pet por ID. |
| `GET` | `/pets/{id}/historico` | `@PathVariable Long id` | `PetHistoryResponse` | Histórico consolidado de tarefas concluídas do pet por ID. |
| `GET` | `/pets/{id}/pontos` | `@PathVariable Long id` | `PetPontuacaoResponse` | Retorna a soma de pontos do pet (Tarefas + Aulas) por ID. |
| `POST` | `/pets` | `PetRequest` | `PetResponse` (201 Created) | Cria um pet e vincula o criador como `responsavelPrincipal`. |
| `PUT` | `/pets/{id}` | `PetRequest` | `PetResponse` (200 OK) | Atualiza os dados do pet e seu responsável. |
| `DELETE`| `/pets/{id}` | `@PathVariable Long id` | 204 No Content | Remove o pet do sistema. |

---

### 🤝 Care Circle - Rede de Cuidado (`/pets/{petId}`)
| Método | Endpoint | Parâmetros / Body | Response | Descrição |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/pets/{petId}/cuidadores` | `@PathVariable Long petId` | `List<CoCuidadorResponse>` | Lista todos os cuidadores vinculados ao pet (`nome`, `email`, `petId`, `nomePet`, `responsavelPrincipal`). |
| `POST` | `/pets/{petId}/cuidadores` | `CoCuidadorRequest` (`email`) | `CoCuidadorResponse` (201 Created) | Convida um co-cuidador por e-mail (autorizado pelo responsável autenticado no JWT). |
| `DELETE`| `/pets/{petId}/cuidadores` | `@RequestParam String email` | 204 No Content | Desvincula um co-cuidador pelo seu e-mail (o próprio cuidador ou o tutor principal). |
| `PATCH`| `/pets/{petId}/responsavel-principal` | `TransferirResponsabilidadeRequest` (`novoResponsavelEmail`) | 204 No Content | Transfere a titularidade de responsável principal para outro co-cuidador via e-mail. |

---

### 📋 Tarefas da Rotina (`/tarefas`)
| Método | Endpoint | Parâmetros / Body | Response | Descrição |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/tarefas/me` | `@RequestParam(defaultValue = "ALL") String status`, `Pageable` | `Page<TarefaResponse>` | Lista tarefas do cuidador logado diretamente via token JWT (otimizado via JOIN direto por email). |
| `GET` | `/tarefas/me/pontos` | Nenhum (via JWT) | `Integer` | Consulta total de pontos acumulados pelo cuidador logado via JWT. |
| `GET` | `/tarefas` | `Pageable` | `Page<TarefaResponse>` | Lista todas as tarefas (somente ADMIN). |
| `GET` | `/tarefas/by-usuario` | `@RequestParam Long usuarioId`, `@RequestParam(defaultValue = "ALL") String status`, `Pageable` | `Page<TarefaResponse>` | Lista tarefas de um cuidador por ID (somente ADMIN). |
| `GET` | `/tarefas/by-usuario/pontos` | `@RequestParam Long usuarioId` | `Integer` | Consulta pontos totais do cuidador por ID (somente ADMIN). |
| `GET` | `/tarefas/by-pet/{petId}` | `@PathVariable Long petId`, `Pageable` | `Page<TarefaResponse>` | Lista todas as tarefas vinculadas a um pet específico. |
| `GET` | `/tarefas/{id}` | `@PathVariable Long id` | `TarefaResponse` | Busca tarefa por ID (Cuidador da tarefa ou ADMIN). |
| `POST` | `/tarefas` | `TarefaRequest` | `TarefaResponse` (201 Created) | Cria nova tarefa vinculada ao cuidador autenticado com status `PENDENTE`. |
| `PUT` | `/tarefas/{id}` | `TarefaRequest` | `TarefaResponse` (200 OK) | Atualiza os dados e status da tarefa. |
| `PATCH`| `/tarefas/{id}/concluir` | Token JWT (`Authentication`) | `TarefaResponse` (200 OK) | Marca tarefa como `CONCLUIDO`, vincula executor autenticado e data de conclusão. |
| `PATCH`| `/tarefas/{id}/desmarcar` | Token JWT (`Authentication`) | `TarefaResponse` (200 OK) | Desmarca tarefa previamente concluída retornando-a ao status `PENDENTE`. |
| `DELETE`| `/tarefas/{id}` | `@PathVariable Long id` | 204 No Content | Deleta uma tarefa. |
| `GET` | `/tarefas/procedure/exportar-json` | `@RequestParam(required = false) Long statusId` | `ResponseEntity<String>` (JSON) | Executa Stored Procedure `pkg_petguardian.pr_exportar_tarefas_json` no Oracle e retorna JSON consolidado via `OUT CLOB`. |
| `GET` | `/tarefas/procedure/classificar-pontos/{pontos}` | `@PathVariable Integer pontos` | `Map<String, Object>` | Executa Stored Function `pkg_petguardian.fn_classificar_pontos` no Oracle e retorna a categoria calculada. |

---

### 🩺 Histórico Clínico e Eventos (`/historicos`)
| Método | Endpoint | Parâmetros / Body | Response | Descrição | Permissão |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/historicos` | `Pageable` (`page`, `size`, `sort`) | `Page<HistoricoResponse>` | Lista todos os registros clínicos paginados ordenados por data decrescente. | `ADMIN` |
| `GET` | `/historicos/me` | Nenhum (via JWT) | `List<HistoricoResponse>` | Prontuário médico consolidado de todos os pets do tutor autenticado no JWT. | Usuário autenticado |
| `GET` | `/historicos/pet/{petId}` | `@PathVariable Long petId` | `List<HistoricoResponse>` | Prontuário médico de eventos do pet ordenados por data decrescente. | Cuidador do pet ou `ADMIN` |
| `GET` | `/historicos/{id}` | `@PathVariable Long id` | `HistoricoResponse` | Busca evento de histórico por ID. | Cuidador do histórico ou `ADMIN` |
| `POST` | `/historicos` | `HistoricoRequest` | `HistoricoResponse` (201 Created) | Registra evento de histórico (Vacina, Consulta, Exame, etc.). | Cuidador do pet ou `ADMIN` |
| `PUT` | `/historicos/{id}` | `HistoricoRequest` | `HistoricoResponse` (200 OK) | Atualiza registro de histórico. | Cuidador do histórico e do pet ou `ADMIN` |
| `DELETE`| `/historicos/{id}` | `@PathVariable Long id` | 204 No Content | Remove registro de histórico. | Cuidador do histórico ou `ADMIN` |

---

### 🎓 Trilhas de Aprendizado (`/trilhas`) - ⭐ LEITURA: PREMIUM & ADMIN | ESCRITA: ADMIN
| Método | Endpoint | Parâmetros / Body | Response | Descrição | Permissão |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/trilhas` | `Pageable` (`page`, `size`, `sort`) | `Page<TrilhaResponse>` | Lista todas as trilhas cadastradas com paginação e ordenação por nome. | `PREMIUM`, `ADMIN` |
| `GET` | `/trilhas/me` | Nenhum (via JWT) | `List<TrilhaResponse>` | Lista todas as trilhas cadastradas de todos os pets do usuário logado. | `PREMIUM`, `ADMIN` |
| `GET` | `/trilhas/pet/{petId}` | `@PathVariable Long petId` | `List<TrilhaResponse>` | Lista trilhas cadastradas para o pet. | `PREMIUM`, `ADMIN` |
| `GET` | `/trilhas/{id}` | `@PathVariable Long id` | `TrilhaResponse` | Busca trilha por ID. | `PREMIUM`, `ADMIN` |
| `POST` | `/trilhas` | `TrilhaRequest` | `TrilhaResponse` (201 Created) | Cria nova trilha para o pet. | `ADMIN` |
| `PUT` | `/trilhas/{id}` | `TrilhaRequest` | `TrilhaResponse` (200 OK) | Atualiza trilha existente. | `ADMIN` |
| `DELETE`| `/trilhas/{id}` | `@PathVariable Long id` | 204 No Content | Deleta uma trilha e seus módulos/aulas em cascata. | `ADMIN` |

---

### 📦 Módulos das Trilhas (`/modulos`) - ⭐ LEITURA: PREMIUM & ADMIN | ESCRITA: ADMIN
| Método | Endpoint | Parâmetros / Body | Response | Descrição | Permissão |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/modulos` | `Pageable` (`page`, `size`, `sort`) | `Page<ModuloResponse>` | Lista todos os módulos com paginação e ordenação por nome. | `PREMIUM`, `ADMIN` |
| `GET` | `/modulos/trilha/{trilhaId}` | `@PathVariable Long trilhaId` | `List<ModuloResponse>` | Lista módulos pertencentes a uma trilha. | `PREMIUM`, `ADMIN` |
| `GET` | `/modulos/{id}` | `@PathVariable Long id` | `ModuloResponse` | Busca módulo por ID. | `PREMIUM`, `ADMIN` |
| `POST` | `/modulos` | `ModuloRequest` | `ModuloResponse` (201 Created) | Cria novo módulo associado a uma trilha. | `ADMIN` |
| `PUT` | `/modulos/{id}` | `ModuloRequest` | `ModuloResponse` (200 OK) | Atualiza módulo existente. | `ADMIN` |
| `DELETE`| `/modulos/{id}` | `@PathVariable Long id` | 204 No Content | Deleta módulo e suas aulas em cascata. | `ADMIN` |

---

### 📝 Aulas e Conteúdos Educativos (`/aulas`) - ⭐ LEITURA: PREMIUM & ADMIN | ESCRITA: ADMIN
| Método | Endpoint | Parâmetros / Body | Response | Descrição | Permissão |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/aulas` | `Pageable` (`page`, `size`, `sort`) | `Page<AulaResponse>` | Lista todas as aulas com paginação e ordenação por nome. | `PREMIUM`, `ADMIN` |
| `GET` | `/aulas/modulo/{moduloId}` | `@PathVariable Long moduloId` | `List<AulaResponse>` | Lista aulas pertencentes a um módulo. | `PREMIUM`, `ADMIN` |
| `GET` | `/aulas/{id}` | `@PathVariable Long id` | `AulaResponse` | Busca aula por ID. | `PREMIUM`, `ADMIN` |
| `POST` | `/aulas` | `AulaRequest` | `AulaResponse` (201 Created) | Cria nova aula (pontuação, conteúdo até 1000 caracteres, concluida = false). | `ADMIN` |
| `PUT` | `/aulas/{id}` | `AulaRequest` | `AulaResponse` (200 OK) | Atualiza aula existente. | `ADMIN` |
| `PATCH`| `/aulas/{id}/concluir` | `@PathVariable Long id` | `AulaResponse` (200 OK) | Marca aula como concluída (`concluida = true`), gerando pontos para o pet. | `PREMIUM`, `ADMIN` |
| `PATCH`| `/aulas/{id}/desmarcar` | `@PathVariable Long id` | `AulaResponse` (200 OK) | Desmarca aula concluída (`concluida = false`), estornando pontos do pet dinamicamente. | `PREMIUM`, `ADMIN` |
| `DELETE`| `/aulas/{id}` | `@PathVariable Long id` | 204 No Content | Deleta uma aula. | `ADMIN` |

---

### 🍃 Conteúdos NoSQL de Aulas no MongoDB (`/aulas`) - ⭐ LEITURA: PREMIUM & ADMIN | ESCRITA: ADMIN
| Método | Endpoint | Parâmetros / Body | Response | Descrição | Permissão |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/aulas/conteudos` | `Pageable` (`page`, `size`, `sort`) | `Page<ConteudoAulaResponse>` | Lista todos os conteúdos ricos de aulas no MongoDB com paginação. | `PREMIUM`, `ADMIN` |
| `GET` | `/aulas/{aulaId}/conteudo` | `@PathVariable Long aulaId` | `ConteudoAulaResponse` | Busca o conteúdo rico (Markdown e links de apoio) de uma aula específica por `aulaId`. | `PREMIUM`, `ADMIN` |
| `POST` | `/aulas/{aulaId}/conteudo` | `@PathVariable Long aulaId`, `ConteudoAulaRequest` | `ConteudoAulaResponse` (201 Created) | Cria documento NoSQL de conteúdo rico vinculado à aula no MongoDB. | `ADMIN` |
| `PUT` | `/aulas/{aulaId}/conteudo` | `@PathVariable Long aulaId`, `ConteudoAulaRequest` | `ConteudoAulaResponse` (200 OK) | Atualiza o documento de conteúdo rico da aula no MongoDB. | `ADMIN` |
| `DELETE`| `/aulas/{aulaId}/conteudo` | `@PathVariable Long aulaId` | 204 No Content | Remove o documento NoSQL da aula no MongoDB. | `ADMIN` |

---

### 📍 Endereços (`/enderecos`)
| Método | Endpoint | Parâmetros / Body | Response | Descrição |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/enderecos` | `Pageable` | `Page<EnderecoResponse>` | Lista endereços cadastrados. |
| `GET` | `/enderecos/{id}` | `@PathVariable Long id` | `EnderecoResponse` | Busca endereço por ID. |
| `POST` | `/enderecos` | `EnderecoRequest` | `EnderecoResponse` (201 Created) | Resolve endereço via cliente declarativo `ViaCepService` (@HttpExchange) e persiste hierarquia geográfica. |
| `PUT` | `/enderecos/{id}` | `EnderecoRequest` | `EnderecoResponse` (200 OK) | Atualiza endereço por ID. |
| `DELETE`| `/enderecos/{id}` | `@PathVariable Long id` | 204 No Content | Deleta endereço. |

---

## 🛡️ 6. Padrões de Validação no Projeto

### A. Validação de Formato e Contrato (DTOs - Bean Validation)
- `@NotBlank`, `@NotNull`: Campos obrigatórios.
- `@PastOrPresent`: Data de nascimento do Pet (`dataNasc`).
- `@FutureOrPresent`: Prazo de tarefas (`prazo`).
- `@Positive`: Pontos de tarefas e aulas.
- `@Email`: Formato do e-mail.
- `@Size(max = ...)`: Limites de tamanho de string.
- `@DddValidation` / `@DddValidator`: Valida DDD válido no Brasil (67 DDDs oficiais da Anatel).
- `@Pattern(regexp = "^\\d{5}-?\\d{3}$")`: Validação canônica de formato de CEP brasileiro de 8 dígitos.
- `@EnumValidation` / `@EnumValidator`: Valida enums dinâmicos (`PetPorte`, `EnumStatus`, `UsuarioRole`).

### B. Validação de Regras de Negócio (Domain / Service Components)
- **`TarefaService`**: Valida se o atribuído/executor é cuidador do pet e se a tarefa está apta para conclusão/desmarcação (regras encapsuladas no próprio service).
- **`UsuarioPetService`**: Valida regras de titularidade única de responsável principal, vínculo prévio e permissões de desvinculação no Care Circle (regras encapsuladas no próprio service).

### C. Tratamento Global de Erros (`GlobalExceptionHandler`)
- `400 Bad Request`: `MethodArgumentNotValidException`, `IllegalArgumentException`, `HttpMessageNotReadableException`, `DataIntegrityViolationException`.
- `401 Unauthorized`: `AuthenticationException`.
- `403 Forbidden`: `AccessDeniedException` (acesso negado para rotas Premium ou operações não autorizadas).
- `404 Not Found`: `ResourceNotFoundException`.
- `500 Internal Server Error`: Erros inesperados.

---

## 📏 7. Convenções de Código do Projeto

1. **Padrão do Professor para Busca por ID:**
   - Todo service expõe `public <Entity> findById(Long id)` delegando diretamente para um método privado auxiliar `private <Entity> find<Entity>ById(Long id)`.
   - O método privado é o responsável por consultar o repositório e lançar `ResourceNotFoundException` caso não encontre a entidade.
   - Demais métodos internos do próprio service (ex: `update`, `delete`, `concluir`) reutilizam o método privado `find<Entity>ById(id)`.
2. **Exclusão Segura com `deleteById`:**
   - Métodos `delete(Long id)` validam a existência chamando `find<Entity>ById(id)` e em seguida invocam diretamente `repository.deleteById(id)`.
3. **Desacoplamento Horizontal entre Services Irmãos:**
   - Cada service injeta diretamente os **Repositories** das entidades relacionadas de que necessita (`PetRepository`, `UsuarioRepository`, `UsuarioPetRepository`, etc.) e mantém seu próprio helper privado `findPetById` / `findUsuarioById` / verificações de titularidade (`existsByUsuarioEmailAndPetId`, `isResponsavelPrincipalPorEmail`).
   - **NÃO** injetar Services irmãos (ex: `UsuarioPetService` dentro de `PetService`, `HistoricoService` ou `TarefaService`) para prevenir horizontal coupling e ciclos de dependência circular.
4. **Sem `Locale.ROOT`:** Utilizar `.toUpperCase()` ou `.toLowerCase()` padrão.
5. **DTOs Limpos e Imutáveis:** DTOs são records puros contendo apenas campos, validações canônicas de Bean Validation (sem mensagens customizadas redundantes em anotações padrão) e conversão inicial (`toEntity(...)`). Não contêm métodos de mutação de entidades de domínio.
6. **Encapsulamento de Mutação via `aplicarEm` nos Services:** Métodos `update` nos Services orquestram dependências e encapsulam as atribuições da entidade em método privado `aplicarEm(...)` no próprio Service, mutando a entidade gerenciada com segurança.
7. **Inicialização com `@Builder.Default`:** Coleções e campos booleanos sempre inicializados.
8. **DTOs Limpos:** Records de DTO contêm apenas anotações essenciais de validação, sem `@Schema`.
9. **Sem Verificações Redundantes de Null (Proibido Null-Checks Paranoicos):** DTOs com Bean Validation (`@NotNull`, `@NotBlank`, `@Pattern`, `@DddValidation`, etc.) e entidades com `@Builder.Default` garantem a integridade dos dados na entrada. É terminantemente proibido poluir services e controllers com checagens de `!= null` e verificações defensivas em cascata desnecessárias.
10. **Sem Over-Engineering / Métodos Auxiliares Desnecessários (KISS):** Não criar métodos auxiliares, records intermediários descartáveis (como `ResolvedAddress`) ou validações encapsuladas isoladas que só são utilizadas em um único ponto e podem ser resolvidas de forma simples e direta em uma única linha.
11. **Imports no Topo (Proibido FQCN inline):** NUNCA declarar pacotes inteiros inline no meio do código (ex: `org.springframework...`, `java.time...`). SEMPRE importar a classe no topo do arquivo com `import` e usar apenas o nome da classe no corpo do código.
12. **Integrações Externas Declarativas (@HttpExchange):** Consumo de APIs externas (ex: ViaCEP) deve utilizar interfaces HTTP declarativas com `@HttpExchange` e `@GetExchange` registradas via `@ImportHttpServices`.
13. **Autorização SpEL no Care Circle:** As anotações `@PreAuthorize` no `UsuarioPetController` devem invocar diretamente `@usuarioPetService.isCuidadorDoPet` e `@usuarioPetService.isResponsavelPrincipal`, eliminando pontes indiretas por outros controllers ou services.
14. **Integridade Referencial e Cascades em `Pet`:** O mapeamento de `Pet` inclui `@OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)` para `tarefas`, `usuarioPets`, `historicos` e `trilhas`, garantindo exclusão segura e atômica do agregado sem violação de foreign keys.
15. **Entidade `UsuarioPet` & Bulk Operations:** `UsuarioPet` implementa `@EqualsAndHashCode(of = "id")` para estabilidade em coleções `Set<UsuarioPet>` baseadas no `@EmbeddedId UsuarioPetId`. Métodos `@Modifying` de atualização em lote no `UsuarioPetRepository` utilizam `(clearAutomatically = true, flushAutomatically = true)` para sincronização do cache de primeiro nível do EntityManager.
16. **Perfil Inicial de Tutores (Onboarding):** Novos usuários nascem obrigatoriamente no perfil gratuito com `@Builder.Default private UsuarioRole role = UsuarioRole.COMUM;` em `Usuario.java`. O upgrade para `PREMIUM` ocorre sob demanda via endpoint dedicado `PATCH /usuarios/{id}/upgrade-premium`.
17. **Persistência Poliglota & Segregação Estrita de Repositórios:**
    - A classe principal `PetGuardianApplication.java` adota segregação type-safe entre os módulos Spring Data:
      - `@EnableJpaRepositories(basePackageClasses = PetGuardianApplication.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ConteudoAulaRepository.class))`
      - `@EnableMongoRepositories(basePackageClasses = ConteudoAulaRepository.class)`
    - Isso impede conflitos em tempo de inicialização entre os proxies do Spring Data JPA e Spring Data MongoDB.
18. **Padrão de Documentos NoSQL (MongoDB):**
    - Documentos NoSQL residem no pacote `fiap.com.br.petguardian.trilha.aula.conteudo`.
    - Anotados com `@Document(collection = "conteudos_aula")`.
    - Contêm `@Id private String id` e `@Indexed(unique = true) private Long aulaId` para manter a integridade com o ID da aula no Oracle.
    - O `ConteudoAulaService` segue rigorosamente o padrão dos demais services do projeto com helper privado `findConteudoByAulaId(Long aulaId)` lançando `ResourceNotFoundException`.
19. **Configuração de Conectividade do MongoDB no Spring Boot 4:**
    - No Spring Boot 4.x, as propriedades de conexão com o MongoDB residem obrigatoriamente no namespace **`spring.mongodb.*`** (`host`, `port`, `username`, `password`, `database`, `authentication-database`), enquanto a criação de índices automáticos fica em `spring.data.mongodb.auto-index-creation=true`.
    - Não utilizar o prefixo legado `spring.data.mongodb.host`, que é desconsiderado pelo auto-configurador do Spring Boot 4.


