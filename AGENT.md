# 🐾 AGENT.md — PetGuardian API (Java-Advanced)

Guia técnico de referência da arquitetura, convenções de código, segurança e rotinas de banco da **PetGuardian API** (`Java-Advanced`).

---

## 🏛️ 1. Arquitetura & Stack Técnica

- **Java Version:** Java 17 LTS | **Framework:** Spring Boot 4.1.1 | **Build:** Gradle 8.x
- **Persistência Relacional:** Spring Data JPA + Hibernate (`OracleDialect`), conectado ao **Oracle Database 19c** corporativo da FIAP (`oracle.fiap.com.br:1521/orcl`).
- **Persistência NoSQL:** Spring Data MongoDB (`spring.mongodb.*`) para conteúdo pedagógico rico de aulas em Markdown (`conteudos_aula`).
- **Segurança:** Spring Security + OAuth2 Resource Server com tokens JWT assinados via par de chaves assimétricas RSA 2048-bit (PKCS#8).
- **HTTP Client Declarativo:** `@HttpExchange` e `@GetExchange` com `RestClient` via `@ImportHttpServices` para integração com ViaCEP.
- **Cache:** Spring Cache em memória para resolução estática de `Status`.
- **Validação:** Bean Validation declarativo em DTOs (Records) com validadores customizados (`@DddValidation`, `@EnumValidation`).

---

## 👥 2. Perfis de Acesso (RBAC) & Segurança

| Perfil (`UsuarioRole`) | Escopo Autorizado | Restrições |
| :--- | :--- | :--- |
| **`COMUM`** | Cadastro gratuito padrão. Acesso a Pets, Care Circle, Tarefas, Histórico e Perfil próprio. | Bloqueado para Trilhas, Módulos e Aulas (`403 Forbidden`). |
| **`PREMIUM`** | Tutores pagantes. Todo o escopo de `COMUM` + visualização e conclusão de Trilhas, Módulos e Aulas. | Bloqueado para criação/edição/deleção de conteúdos pedagógicos. |
| **`ADMIN`** | Acesso irrestrito a todas as entidades, relatórios e CRUD completo de Trilhas/Módulos/Aulas. | Nenhuma. |

- **Tokens JWT:** Emitidos via `POST /login`, válidos por 1 hora, transportando as claims `sub` (e-mail) e `role` (`ROLE_COMUM`, `ROLE_PREMIUM`, `ROLE_ADMIN`).
- **Endpoints Públicos:** `POST /login`, `POST /usuarios`, documentação Swagger/OpenAPI (`/v3/api-docs/**`, `/swagger-ui/**`), Actuator (`/actuator/health`).

---

## 🗄️ 3. Integração com Stored Procedures Oracle (`PKG_PETGUARDIAN`)

A camada de persistência executa 3 Stored Procedures empacotadas no Oracle PL/SQL através de anotações `@Procedure` do Spring Data JPA:

| Stored Procedure | Repositório | Endpoint Acionador | Descrição de Negócio |
| :--- | :--- | :--- | :--- |
| `pkg_petguardian.pr_calcular_pontuacao_pet` | `PetRepository` | `GET /pets/{id}/pontos` | Retorna tarefas, aulas e total de pontos do animal por ID. |
| `pkg_petguardian.pr_calcular_pontuacao_usuario` | `PetRepository` | `GET /pets/me/pontos` e `GET /usuarios/me/rede-cuidado` | Consolida em batch único a pontuação de todos os pets sob tutela do usuário logado. |
| `pkg_petguardian.pr_transferir_responsavel_principal` | `UsuarioPetRepository` | `PATCH /pets/{id}/responsavel-principal` | Executa a troca atômica do responsável principal no Care Circle (`USUARIO_PET`). |

- **Trigger de Auditoria:** `trg_audit_tarefa` é disparada automaticamente após DML em `TAREFA`, gravando valores `:OLD` e `:NEW` em `AUDITORIA_DML_TAREFA`.

---

## 🌐 4. Mapa Canônico de Endpoints da API

### Autenticação & Usuários (`/login`, `/usuarios`)
- `POST /login`: Autenticação e geração de token JWT.
- `POST /usuarios`: Cadastro público de tutor (perfil inicial `COMUM`).
- `GET /usuarios/me`: Perfil do usuário logado via JWT.
- `PUT /usuarios/me`: Atualização cadastral própria.
- `DELETE /usuarios/me`: Encerramento da própria conta.
- `GET /usuarios/me/rede-cuidado`: Visão consolidada da rede (pets, co-cuidadores, tarefas da semana e pontuação via procedure).
- `PATCH /usuarios/me/upgrade-premium`: Upgrade de perfil de `COMUM` para `PREMIUM`.
- `GET /usuarios` | `GET /usuarios/by-email` | `PATCH /usuarios/{id}/role`: Ações administrativas (`ADMIN`).

### Pets & Care Circle (`/pets`)
- `GET /pets/me`: Pets vinculados ao usuário logado (tutor principal ou co-cuidador).
- `GET /pets/me/pontos`: Pontuação agregada dos pets do usuário via Stored Procedure.
- `GET /pets/me/historico`: Tarefas concluídas de todos os pets do usuário logado.
- `GET /pets/{id}` | `POST /pets` | `PUT /pets/{id}` | `DELETE /pets/{id}`: Gestão do animal (criador torna-se responsável principal).
- `GET /pets/{id}/pontos`: Pontuação individual do pet via Stored Procedure.
- `GET /pets/{petId}/cuidadores`: Lista co-cuidadores vinculados ao animal.
- `POST /pets/{petId}/cuidadores`: Convite de co-cuidador por e-mail.
- `DELETE /pets/{petId}/cuidadores/me`: Desvinculação do próprio cuidador autenticado do Care Circle via JWT.
- `DELETE /pets/{petId}/cuidadores?email=...`: Desvinculação de co-cuidador por e-mail (responsável principal ou o próprio cuidador).
- `PATCH /pets/{petId}/responsavel-principal`: Transferência atômica de titularidade via Stored Procedure.

### Tarefas da Rotina (`/tarefas`)
- `GET /tarefas/me`: Tarefas do cuidador logado com auto-expiração dinâmica de vencidas.
- `GET /tarefas/me/pontos`: Pontos acumulados de rotina do cuidador autenticado.
- `GET /tarefas/by-pet/{petId}`: Lista tarefas de rotina de um pet.
- `GET /tarefas/{id}` | `POST /tarefas` | `PUT /tarefas/{id}` | `DELETE /tarefas/{id}`: CRUD da rotina.
- `PATCH /tarefas/{id}/concluir`: Conclusão de tarefa via JPA, creditando pontos e disparando trigger de auditoria.
- `PATCH /tarefas/{id}/desmarcar`: Estorno de tarefa concluída para `PENDENTE` e reversão de pontos.

### Histórico Clínico de Saúde (`/historicos`)
- `GET /historicos/me`: Prontuário médico de todos os pets do usuário autenticado.
- `GET /historicos/pet/{petId}`: Prontuário do animal ordenado por data.
- `GET /historicos/{id}` | `POST /historicos` | `PUT /historicos/{id}` | `DELETE /historicos/{id}`: Gestão de eventos de saúde.

### Trilhas Educativas, Módulos & Aulas (`/trilhas`, `/modulos`, `/aulas`) — *PREMIUM & ADMIN*
- `GET /trilhas/me` | `GET /trilhas/pet/{petId}` | `GET /trilhas/{id}`: Trilhas de adestramento do animal.
- `GET /modulos/trilha/{trilhaId}` | `GET /modulos/{id}`: Módulos pedagógicos.
- `GET /aulas/modulo/{moduloId}` | `GET /aulas/{id}`: Metadados relacionais da aula no Oracle.
- `PATCH /aulas/{id}/concluir` | `PATCH /aulas/{id}/desmarcar`: Conclusão ou estorno de lição somando pontos ao pet.
- `GET /aulas/{aulaId}/conteudo`: Conteúdo rico em Markdown e links de apoio persistido no MongoDB NoSQL.
- *Operações de escrita (`POST`, `PUT`, `DELETE` em trilhas/módulos/aulas/conteúdos): Exclusivas de `ADMIN`.*

### Endereços (`/enderecos`)
- `GET /enderecos` | `GET /enderecos/{id}` | `POST /enderecos` | `PUT /enderecos/{id}` | `DELETE /enderecos/{id}`: Resolução via ViaCEP e persistência normalizada em 3FN (`Bairro`, `Cidade`, `Estado`).

---

## 📏 5. Convenções de Código & Diretrizes Arquiteturais

1. **Top-Level Imports (Proibido FQCN Inline):** NUNCA usar pacotes completos no corpo do arquivo. Importar no topo via `import`.
2. **DTOs Limpos e Imutáveis (Records):** DTOs são `record` puros contendo apenas Bean Validation formal (`@NotBlank`, `@NotNull`, `@Pattern`, `@Positive`, etc.) e método de mapeamento `toEntity()`.
3. **Sem Null-Checks Paranoicos:** Validações de entrada em DTOs garantem integridade dos dados. Não espalhar `!= null` defensivos desnecessários em services.
4. **Padrão de Busca por ID no Service:**
   ```java
   public Entity findById(Long id) {
       return findEntityById(id);
   }
   private Entity findEntityById(Long id) {
       return repository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Entity com id " + id + " nao encontrada."));
   }
   ```
5. **Desacoplamento Horizontal:** Services injetam apenas os **Repositories** necessários das entidades vizinhas, nunca Services irmãos (prevenção de dependência circular).
6. **Segregação Poliglota nos Repositórios:**
   - `@EnableJpaRepositories` exclui o pacote/classe de repositórios do Mongo.
   - `@EnableMongoRepositories` aponta estritamente para `ConteudoAulaRepository`.
   - Propriedades do MongoDB no Spring Boot 4 utilizam o prefixo canônico `spring.mongodb.*`.
7. **Tratamento Global de Erros (`GlobalExceptionHandler`):**
   - `400`: `MethodArgumentNotValidException`, `IllegalArgumentException`.
   - `401`: `AuthenticationException`.
   - `403`: `AccessDeniedException`.
   - `404`: `ResourceNotFoundException`.
