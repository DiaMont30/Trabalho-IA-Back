# Documento de Especificação do Sistema — Plataforma Conversacional

---

## 1. Visão Geral da Arquitetura

A arquitetura adotada é uma **variante estrita do MVC (Model-View-Controller) implementada sobre Spring Boot**, com camadas adicionais de isolamento inspiradas nos princípios da **Clean Architecture** e **Hexagonal Architecture**.

### Stack Tecnológica

| Componente | Versão / Tecnologia |
|---|---|
| Linguagem | Java 17.0.x LTS |
| Framework | Spring Boot 3.x |
| Persistência | Spring Data JPA / Hibernate |
| Banco de Dados | MySQL 8.x |
| Build | Maven 3.9+ |
| Migrations | Flyway |
| Mapeamento DTO/Entity | MapStruct |
| Validação | Jakarta Validation + Hibernate Validator |
| Documentação API | SpringDoc OpenAPI 3 |
| Logs | Logback + json-logback |
| Testes | JUnit 5 + Mockito + AssertJ |

### Camadas e Comunicação

`
[HTTP] → Controller → DTO → Service (interface) → ServiceImpl → Repository → Entity → MySQL
                              ↕                              ↕
                           Mapper                        Mapper (inverso)
`

### Justificativas Técnicas

| Decisão | Justificativa |
|---|---|
| MVC estrito | Alinhamento com o ecossistema Spring Boot, facilitando adoção pela equipe e aproveitando convenções do framework |
| Services como interface + impl | Permite polimorfismo, testes com mocks e preparação para múltiplas estratégias (ex: IA mock → LLM real) |
| DTOs como única via de comunicação externa | Isola o domínio de contratos HTTP, permitindo que serviços sejam reutilizados em outros contextos (fila, websocket, etc.) |
| Mapper explícito | Evita vazamento de Entity para camadas superiores e vice-versa |
| Strategy Pattern para mensagens | Prepara o terreno para diferentes provedores de IA sem modificar o fluxo principal |
| Event Publisher para mensagens | Prepara integração futura com RabbitMQ/Kafka sem modificar a lógica de negócio |

### Fluxo Arquitetural Genérico

`
Cliente HTTP
    ↓ (JSON / Multipart)
Controller          [Adaptador HTTP puro — sem regras]
    ↓ (DTO)
Service Interface   [Contrato de domínio — sem HTTP]
    ↓
ServiceImpl         [Lógica de negócio — sem Entity exposta]
    ↓ (Entity)
Repository           [Persistência — sem regras]
    ↓
MySQL
`

---

## 2. Princípios Arquiteturais

### 2.1 SOLID — Aplicação Concreta

| Princípio | Aplicação no Projeto |
|---|---|
| **S** — Single Responsibility | Cada classe possui exatamente um motivo para mudar. Controller só roteia. Service só orquestra regras. Repository só persiste. |
| **O** — Open/Closed | Services são abertos para extensão (novas impls) e fechados para modificação via interfaces. Estratégias de IA seguem o mesmo padrão. |
| **L** — Liskov Substitution | Qualquer implementação de MessageService pode substituir a interface sem quebrar o consumidor (Controller). |
| **I** — Interface Segregation | Interfaces de serviço são coesas e específicas. Não existe um CrudService genérico. Cada domínio tem seu contrato. |
| **D** — Dependency Inversion | Controller depende de abstrações (MessageService), não de implementações concretas (MessageServiceImpl). Repositories idem. |

### 2.2 Separation of Concerns — Fronteiras Rígidas

`
┌──────────────────────────────────────────────────────────────┐
│                      LAYER DE APRESENTAÇÃO                   │
│  controller/*, dto/request, dto/response                     │
│  Conhece: HTTP, JSON, DTOs                                   │
│  Não conhece: Entity, ServiceImpl, Repository                │
├──────────────────────────────────────────────────────────────┤
│                      LAYER DE DOMÍNIO                        │
│  service/*, service/impl/*, strategy/*, mapper/*             │
│  Conhece: Entity, DTOs, Repository interfaces                │
│  Não conhece: HTTP, ResponseEntity, MultipartFile, Servlet   │
├──────────────────────────────────────────────────────────────┤
│                      LAYER DE PERSISTÊNCIA                   │
│  repository/*, entity/*, specification/*                     │
│  Conhece: JPA, MySQL, Entity                                 │
│  Não conhece: DTOs, regras de negócio, HTTP                  │
└──────────────────────────────────────────────────────────────┘
`

### 2.3 Clean Architecture — Adaptação

A Clean Architecture prega que o domínio está no centro e não depende de nada externo. Adaptamos este conceito ao Spring Boot da seguinte forma:

- **Entidades** (entity/) são o núcleo: contêm apenas atributos, relacionamentos JPA e anotações de mapeamento. Sem regras de negócio.
- **Casos de Uso** (service/) orquestram o fluxo e contêm regras. Dependem de abstrações de repositório, nunca de detalhes de infraestrutura.
- **Adaptadores** (controller/, repository/, storage/) traduzem entre o mundo externo e o domínio.
- **Frameworks** (Spring, MySQL) estão na camada mais externa.

### 2.4 Spec-Driven Development (SDD)

Este documento é a **fonte única de verdade**. Todo código gerado — seja por equipe humana ou por IA — deve derivar exclusivamente das especificações aqui contidas. Nenhuma decisão de implementação deve contrariar os contratos, responsabilidades e restrições definidos a seguir.
---

## 3. Estrutura de Diretórios

```
projeto-ia/
└── src/
    ├── main/
    │   ├── java/com/plataforma/conversacional/
    │   │   ├── PlataformaConversacionalApplication.java
    │   │   │
    │   │   ├── config/
    │   │   │   ├── WebConfig.java
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── OpenApiConfig.java
    │   │   │   ├── StorageConfig.java
    │   │   │   ├── MessagePublisherConfig.java
    │   │   │   └── CorsConfig.java
    │   │   │
    │   │   ├── controller/
    │   │   │   ├── MessageController.java
    │   │   │   ├── SessionController.java
    │   │   │   ├── DocumentController.java
    │   │   │   └── HealthController.java
    │   │   │
    │   │   ├── service/
    │   │   │   ├── MessageService.java
    │   │   │   ├── SessionService.java
    │   │   │   ├── DocumentService.java
    │   │   │   ├── HealthService.java
    │   │   │   └── impl/
    │   │   │       ├── MessageServiceImpl.java
    │   │   │       ├── SessionServiceImpl.java
    │   │   │       ├── DocumentServiceImpl.java
    │   │   │       └── HealthServiceImpl.java
    │   │   │
    │   │   ├── strategy/
    │   │   │   └── MessageProcessingStrategy.java
    │   │   │
    │   │   ├── event/
    │   │   │   ├── MessageSentEvent.java
    │   │   │   └── MessageEventPublisher.java
    │   │   │
    │   │   ├── repository/
    │   │   │   ├── MessageRepository.java
    │   │   │   ├── SessionRepository.java
    │   │   │   └── DocumentRepository.java
    │   │   │
    │   │   ├── entity/
    │   │   │   ├── Message.java
    │   │   │   ├── Session.java
    │   │   │   └── Document.java
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── SendMessageRequest.java
    │   │   │   │   └── UploadDocumentRequest.java
    │   │   │   └── response/
    │   │   │       ├── MessageResponse.java
    │   │   │       ├── SessionHistoryResponse.java
    │   │   │       ├── DocumentResponse.java
    │   │   │       ├── HealthResponse.java
    │   │   │       ├── SessionResponse.java
    │   │   │       └── ErrorResponse.java
    │   │   │
    │   │   ├── mapper/
    │   │   │   ├── MessageMapper.java
    │   │   │   ├── SessionMapper.java
    │   │   │   └── DocumentMapper.java
    │   │   │
    │   │   ├── validation/
    │   │   │   ├── AllowedFileType.java
    │   │   │   └── AllowedFileTypeValidator.java
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── InvalidFileTypeException.java
    │   │   │   └── BusinessException.java
    │   │   │
    │   │   ├── constants/
    │   │   │   └── ApiConstants.java
    │   │   │
    │   │   ├── util/
    │   │   │   └── FileUtils.java
    │   │   │
    │   │   ├── enums/
    │   │   │   ├── MessageRole.java
    │   │   │   ├── MessageStatus.java
    │   │   │   ├── SessionStatus.java
    │   │   │   └── DocumentType.java
    │   │   │
    │   │   ├── specification/
    │   │   │   └── MessageSpecification.java
    │   │   │
    │   │   ├── health/
    │   │   │   ├── ApplicationHealthIndicator.java
    │   │   │   └── DatabaseHealthIndicator.java
    │   │   │
    │   │   ├── security/
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   └── JwtAuthenticationFilter.java
    │   │   │
    │   │   └── storage/
    │   │       ├── FileStorageService.java
    │   │       └── LocalFileStorageService.java
    │   │
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/
    │           ├── V1__create_session_table.sql
    │           ├── V2__create_message_table.sql
    │           └── V3__create_document_table.sql
    │
    └── test/
        └── java/com/plataforma/conversacional/
            ├── controller/
            │   ├── MessageControllerTest.java
            │   ├── SessionControllerTest.java
            │   ├── DocumentControllerTest.java
            │   └── HealthControllerTest.java
            ├── service/
            │   └── impl/
            │       ├── MessageServiceImplTest.java
            │       ├── SessionServiceImplTest.java
            │       ├── DocumentServiceImplTest.java
            │       └── HealthServiceImplTest.java
            └── repository/
                ├── MessageRepositoryTest.java
                ├── SessionRepositoryTest.java
                └── DocumentRepositoryTest.java
```
---

## 4. Responsabilidade de Cada Diretório

### config/

**O que deve conter:**
- Classes anotadas com @Configuration, @EnableWebSecurity, etc.
- Beans de infraestrutura: ObjectMapper, PasswordEncoder, conversores, RestTemplate.
- Configuração de CORS, OpenAPI/Swagger, segurança, storage e filas.

**O que NÃO deve conter:**
- Regras de negócio.
- Lógica de validação de domínio.
- Endpoints ou mapeamentos HTTP.

| Permissões | Proibições |
|---|---|
| Depender de application.yml / application.properties | Depender de Controllers, Services, Repositories |
| Importar beans do Spring Security, SpringDoc, etc. | Conter lógica condicional de negócio |

---

### controller/

**O que deve conter:**
- Classes anotadas com @RestController.
- Métodos com @GetMapping, @PostMapping, etc.
- Validação superficial via @Valid nos DTOs de request.
- Delegação imediata ao Service após conversão de parâmetros.

**O que NÃO deve conter:**
- Regras de negócio (if com lógica de domínio).
- Acesso a repositórios ou EntityManager.
- Uso de Entity em parâmetros ou retorno.
- Tratamento de exceções de negócio (delegar ao GlobalExceptionHandler).

| Permissões | Proibições |
|---|---|
| @Valid, @PathVariable, @RequestBody, @RequestParam, @RequestPart | Injetar Repository, EntityManager, DataSource |
| ResponseEntity<T> para personalizar resposta HTTP | Acessar HttpSession, ServletRequest para lógica de negócio |
| Delegar para Service imediatamente | Implementar validação complexa (regex, estado) |

---

### service/ e service/impl/

**O que deve conter:**
- Interfaces de serviço definindo contratos de domínio.
- Implementações concretas com lógica de negócio e orquestração.
- Uso de mappers para converter entre Entity e DTO.

**O que NÃO deve conter:**
- Referências a HttpServletRequest, HttpServletResponse, ResponseEntity.
- Parâmetros do tipo MultipartFile.
- Anotações ou imports do pacote jakarta.servlet.http.
- Código de serialização/deserialização JSON.

| Permissões | Proibições |
|---|---|
| Injetar Repository (interfaces), Mapper, outros Services | Injetar Controller |
| @Transactional para controle transacional | ResponseEntity, HttpStatus, @RequestMapping |
| Lançar exceções de negócio (BusinessException) | Capturar exceções HTTP |
| Spring Events (ApplicationEventPublisher) | @CrossOrigin, @ResponseStatus |

---

### strategy/

**O que deve conter:**
- Interface de estratégia para processamento de mensagens (ex: MessageProcessingStrategy).
- Implementações futuras: MockMessageStrategy, LlamaStrategy, OpenAiStrategy.

**O que NÃO deve conter:**
- Dependência de HTTP ou Servlet.
- Implementação concreta inicial que não seja a mock.

| Permissões | Proibições |
|---|---|
| DTOs de entrada/saída definidos no pacote dto/ | Conhecer a implementação de outras estratégias |

---

### event/

**O que deve conter:**
- Classes de evento (ex: MessageSentEvent).
- Publicador de eventos (ex: MessageEventPublisher) que usa ApplicationEventPublisher.

**O que NÃO deve conter:**
- Consumidores de fila (ficarão em módulo futuro ou em config/).
- Lógica de negócio.

| Permissões | Proibições |
|---|---|
| DTOs para serialização de eventos | Acesso a repositórios |
| Anotações de serialização (@Serializable) | Lógica condicional |

---

### repository/

**O que deve conter:**
- Interfaces que estendem JpaRepository ou JpaSpecificationExecutor.
- Métodos de consulta derivados do Spring Data JPA.
- Consultas customizadas via @Query quando necessário.

**O que NÃO deve conter:**
- Regras de negócio.
- Chamadas a Services.
- Lógica de transformação de dados (mappers).

| Permissões | Proibições |
|---|---|
| @Query, Pageable, Specification | Injetar Service ou Controller |
| Optional<T> como retorno | Conter if/else, loops com lógica de negócio |
| Métodos derivados: findBySessionIdOrderByCreatedAt() | Serialização ou formatação de dados |

---

### entity/

**O que deve conter:**
- Classes anotadas com @Entity, @Table.
- Mapeamento JPA completo: @Id, @GeneratedValue, @Column, @ManyToOne, @OneToMany.
- Atributos com tipos Java padrão, enums, relacionamentos.

**O que NÃO deve conter:**
- Anotações de validação de domínio complexas (ex: regex de conteúdo).
- Métodos com lógica de negócio.
- Dependência de outros layers (Service, Controller).

| Permissões | Proibições |
|---|---|
| @PrePersist, @PreUpdate para timestamps automáticos | Herdar de classes de Service ou DTO |
| @Enumerated(EnumType.STRING) | Conter referências a jakarta.servlet.http |
| @Lob para conteúdo longo | Conter @JsonIgnore ou anotações de serialização |

---

### dto/request/ e dto/response/

**O que deve conter:**
- Records ou classes simples com @NotBlank, @Size, @NotNull, etc.
- DTOs de requisição: dados que entram via HTTP.
- DTOs de resposta: dados que saem via HTTP.

**O que NÃO deve conter:**
- Anotações JPA (@Entity, @Column).
- Lógica de negócio ou métodos de transformação complexa.
- Dependência de pacotes de infraestrutura além de Jakarta Validation.

| Permissões | Proibições |
|---|---|
| @JsonProperty, @JsonFormat (anotações Jackson leves) | @Entity, @Table, @Column |
| Jakarta Validation (@NotBlank, @Email, @Pattern) | Conter regras de negócio |
| jakarta.validation.constraints.* | Métodos que executam consultas ou cálculos |

---

### mapper/

**O que deve conter:**
- Interfaces ou classes para conversão entre Entity ↔ DTO.
- Métodos de mapeamento explícitos (MapStruct recomendado ou manual).

**O que NÃO deve conter:**
- Regras de negócio ou validação.
- Dependência de repositories ou services.
- Lógica condicional de transformação que seja regra de negócio.

| Permissões | Proibições |
|---|---|
| Anotações do MapStruct (@Mapper, @Mapping) | Acesso a banco de dados |
| Injeção de outros mappers (composição de mapeamentos) | Lógica de validação (se X então Y) |

---

### validation/

**O que deve conter:**
- Anotações customizadas de Jakarta Validation.
- Implementações de ConstraintValidator para validações específicas do domínio.

**O que NÃO deve conter:**
- Lógica de serviço ou regras que envolvam estado do banco.
- Mapeamento de entidades.

| Permissões | Proibições |
|---|---|
| Anotações e validadores reutilizáveis | Acesso a repositories ou services |

---

### exception/

**O que deve conter:**
- Classes de exceção de negócio (ResourceNotFoundException, InvalidFileTypeException, BusinessException).
- @RestControllerAdvice global (GlobalExceptionHandler) para tratamento unificado.
- Métodos handlers para cada exceção, retornando ErrorResponse.

**O que NÃO deve conter:**
- Regras de negócio.
- Lógica de fluxo de aplicação.

| Permissões | Proibições |
|---|---|
| @ExceptionHandler, @ControllerAdvice | Decisões que alteram fluxo de domínio |
| ResponseEntity<ErrorResponse> | Acesso a Entity, Repository |

---

### constants/

**O que deve conter:**
- Constantes públicas estáticas finais: paths de API, tamanhos máximos, formatos de data.

**O que NÃO deve conter:**
- Lógica executável.
- Estado mutável.

| Permissões | Proibições |
|---|---|
| public static final String API_VERSION = "/api/v1" | Métodos, construtores com lógica |

---

### util/

**O que deve conter:**
- Funções utilitárias puras e stateless: manipulação de strings, datas, arquivos.

**O que NÃO deve conter:**
- Dependência de Spring (beans injetados).
- Acesso a banco, HTTP, arquivos externos.

| Permissões | Proibições |
|---|---|
| Métodos static puros | @Autowired, @Component |
| Operações matemáticas, formatação | Efeitos colaterais (escrita em disco, chamadas HTTP) |

---

### enums/

**O que deve conter:**
- Enumerações de domínio: MessageRole (USER, ASSISTANT, SYSTEM), MessageStatus (SENT, RECEIVED, FAILED), SessionStatus (ACTIVE, CLOSED), DocumentType (PDF, TXT).

**O que NÃO deve conter:**
- Métodos com lógica de negócio complexa.
- Dependências externas.

| Permissões | Proibições |
|---|---|
| Campos private final com descrições | Implementar interfaces de serviço |
| Métodos getters simples | Conter lógica de estado transicional |

---

### specification/

**O que deve conter:**
- Classes que implementam Specification<T> do Spring Data JPA para consultas dinâmicas.

**O que NÃO deve conter:**
- Regras de negócio.
- Mapeamento de DTOs.

| Permissões | Proibições |
|---|---|
| CriteriaBuilder, Predicate, Root | Referências a Services ou Controllers |

---

### health/

**O que deve conter:**
- Implementações de HealthIndicator do Spring Boot Actuator.

**O que NÃO deve conter:**
- Regras de negócio.
- Lógica de domínio da plataforma.

| Permissões | Proibições |
|---|---|
| Health.Builder, DataSource, RedisTemplate | Regras de negócio da aplicação |

---

### security/

**O que deve conter:**
- Classes relacionadas a autenticação/autorização: JwtTokenProvider, JwtAuthenticationFilter.

**O que NÃO deve conter:**
- Lógica de conversação ou mensageria.
- Entidades JPA.

| Permissões | Proibições |
|---|---|
| SecurityContextHolder, UserDetails, Authentication | Entity, Repository de domínio |

---

### storage/

**O que deve conter:**
- Interface FileStorageService definindo contrato de armazenamento.
- Implementações: LocalFileStorageService (inicial), CloudFileStorageService (futura).

**O que NÃO deve conter:**
- Lógica de validação de tipo de arquivo (delegar ao Service).
- Métodos HTTP ou de rede embutidos no código.

| Permissões | Proibições |
|---|---|
| java.nio.file.Path, InputStream, MultipartFile (apenas como parâmetro de método) | Conter @RequestMapping, @PostMapping |
---

## 5. Casos de Uso

### 5.1 Enviar Mensagem

```
[Front-end]
    │ POST /api/v1/sessions/{sessionId}/messages
    │ Content-Type: application/json
    ▼
[MessageController]
    │ 1. Valida @Valid no SendMessageRequest
    │ 2. Extrai sessionId do PathVariable
    │ 3. Chama messageService.sendMessage(sessionId, request)
    ▼
[MessageServiceImpl]
    │ 1. Busca Session por ID (lança ResourceNotFoundException se não existir)
    │ 2. Cria entidade Message com role=USER, status=SENT
    │ 3. Salva Message no banco via MessageRepository
    │ 4. Aplica estratégia de processamento (MessageProcessingStrategy)
    │ 5. Cria entidade Message com role=ASSISTANT, status=RECEIVED
    │ 6. Salva resposta no banco
    │ 7. Publica MessageSentEvent (preparação para RabbitMQ/Kafka)
    │ 8. Converte para MessageResponse via MessageMapper
    ▼
[MessageRepository] → [MySQL]
    ▼
[MessageController]
    │ Retorna ResponseEntity.ok(messageResponse)
    ▼
[Front-end] ← 200 OK
```

### 5.2 Recuperar Histórico

```
[Front-end]
    │ GET /api/v1/sessions/{sessionId}/messages
    ▼
[SessionController]
    │ 1. Extrai sessionId
    │ 2. Chama sessionService.getHistory(sessionId, page, size)
    ▼
[SessionServiceImpl]
    │ 1. Verifica existência da Session
    │ 2. Busca mensagens paginadas via MessageRepository
    │ 3. Converte para SessionHistoryResponse via SessionMapper
    ▼
[MessageRepository] → [MySQL]
    ▼
[SessionController]
    │ Retorna ResponseEntity.ok(historyResponse)
    ▼
[Front-end] ← 200 OK
```

### 5.3 Upload de Documento

```
[Front-end]
    │ POST /api/v1/documents/upload
    │ Content-Type: multipart/form-data
    ▼
[DocumentController]
    │ 1. Recebe MultipartFile
    │ 2. Valida @Valid
    │ 3. Chama documentService.storeDocument(file, sessionId)
    ▼
[DocumentServiceImpl]
    │ 1. Valida tipo do arquivo
    │ 2. Gera nome único
    │ 3. Chama fileStorageService.store(file, uniqueName)
    │ 4. Cria entidade Document
    │ 5. Salva via DocumentRepository
    │ 6. Converte para DocumentResponse
    ▼
[FileStorageService] → [LocalFileStorageService]
[DocumentRepository] → [MySQL]
    ▼
[DocumentController]
    │ Retorna ResponseEntity.created().body(documentResponse)
    ▼
[Front-end] ← 201 Created
```

### 5.4 Health Check

```
[Cliente/Probe]
    │ GET /api/v1/health
    ▼
[HealthController]
    │ 1. Chama healthService.check()
    ▼
[HealthServiceImpl]
    │ 1. Agrega status dos HealthIndicators
    │ 2. Retorna HealthResponse
    ▼
[HealthIndicators (Spring Actuator)]
    ▼
[HealthController]
    │ Retorna ResponseEntity.ok(healthResponse)
    ▼
[Cliente] ← 200 OK
```
---

## 6. Contratos da API

Base URL: /api/v1

### 6.1 Mensageria

| Método | Endpoint | DTO Entrada | DTO Saída | Descrição | Erros |
|---|---|---|---|---|---|
| POST | /sessions/{sessionId}/messages | SendMessageRequest | MessageResponse | Envia mensagem e obtém resposta mockada | 404, 400, 500 |
| GET | /sessions/{sessionId}/messages | page, size | SessionHistoryResponse | Recupera histórico paginado | 404, 400 |

### 6.2 Sessões

| Método | Endpoint | DTO Entrada | DTO Saída | Descrição | Erros |
|---|---|---|---|---|---|
| POST | /sessions | — | SessionResponse | Cria nova sessão | 500 |
| GET | /sessions/{sessionId} | — | SessionResponse | Detalhes da sessão | 404 |

### 6.3 Documentos

| Método | Endpoint | DTO Entrada | DTO Saída | Descrição | Erros |
|---|---|---|---|---|---|
| POST | /documents/upload | UploadDocumentRequest (Multipart) | DocumentResponse | Upload PDF/TXT | 400, 413, 500 |
| GET | /documents/{documentId} | — | DocumentResponse | Metadados do documento | 404 |

### 6.4 Health Check

| Método | Endpoint | DTO Entrada | DTO Saída | Descrição | Erros |
|---|---|---|---|---|---|
| GET | /health | — | HealthResponse | Disponibilidade da aplicação | 503 |

### 6.5 DTOs Detalhados

#### SendMessageRequest
```json
{
  "content": "string (obrigatório, 1-5000 caracteres)",
  "sessionId": "UUID"
}
```

#### MessageResponse
```json
{
  "id": "UUID",
  "sessionId": "UUID",
  "content": "string",
  "role": "USER | ASSISTANT | SYSTEM",
  "status": "SENT | RECEIVED | FAILED",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

#### SessionHistoryResponse
```json
{
  "sessionId": "UUID",
  "messages": ["MessageResponse"],
  "page": "int",
  "totalPages": "int",
  "totalElements": "long",
  "hasNext": "boolean"
}
```

#### SessionResponse
```json
{
  "id": "UUID",
  "title": "string | null",
  "status": "ACTIVE | CLOSED | ARCHIVED",
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

#### UploadDocumentRequest
```
file: MultipartFile (PDF ou TXT, máx 10MB)
sessionId: UUID (opcional)
```

#### DocumentResponse
```json
{
  "id": "UUID",
  "fileName": "string",
  "originalName": "string",
  "type": "PDF | TXT",
  "size": "long",
  "storagePath": "string",
  "sessionId": "UUID | null",
  "uploadedAt": "ISO-8601"
}
```

#### HealthResponse
```json
{
  "status": "UP | DOWN | DEGRADED",
  "database": "UP | DOWN",
  "timestamp": "ISO-8601",
  "version": "string"
}
```

#### ErrorResponse
```json
{
  "status": "int",
  "error": "string",
  "message": "string",
  "timestamp": "ISO-8601",
  "path": "string",
  "validationErrors": ["string"] | null
}
```

---

## 7. Modelo Conceitual do Domínio

### 7.1 Entidades e Relacionamentos

```
+-----------------------------------------------------------+
|                          SESSION                            |
+-----------------------------------------------------------+
| id                  : UUID (PK)                             |
| title               : String (opcional)                     |
| status              : SessionStatus                        |
| createdAt           : LocalDateTime                         |
| updatedAt           : LocalDateTime                         |
| closedAt            : LocalDateTime (nullable)              |
+-----------------------------------------------------------+
| Relacionamentos: 1 --- N → Message, 1 --- N → Document    |
+-----------------------------------------------------------+

+-----------------------------------------------------------+
|                          MESSAGE                            |
+-----------------------------------------------------------+
| id                  : UUID (PK)                             |
| sessionId           : UUID (FK → Session.id)               |
| content             : TEXT (Lob)                            |
| role                : MessageRole                          |
| status              : MessageStatus                        |
| metadata            : JSON (opcional)                      |
| createdAt           : LocalDateTime                         |
| updatedAt           : LocalDateTime                         |
+-----------------------------------------------------------+
| Relacionamentos: N --- 1 → Session                        |
+-----------------------------------------------------------+

+-----------------------------------------------------------+
|                          DOCUMENT                           |
+-----------------------------------------------------------+
| id                  : UUID (PK)                             |
| sessionId           : UUID (FK → Session.id, nullable)      |
| originalName        : String                                |
| storageFileName     : String (único)                        |
| storagePath         : String                                |
| type                : DocumentType (PDF, TXT)               |
| size                : Long (bytes)                          |
| contentType         : String (MIME type)                    |
| uploadedAt          : LocalDateTime                         |
+-----------------------------------------------------------+
| Relacionamentos: N --- 1 → Session (opcional)             |
+-----------------------------------------------------------+
```

### 7.2 Cardinalidades

| Origem | Destino | Tipo | Obrigatório |
|---|---|---|---|
| Session | Message | 1 → N | Sim |
| Message | Session | N → 1 | Sim |
| Session | Document | 1 → N | Não |
| Document | Session | N → 1 | Não |

### 7.3 Índices

- idx_message_session_id em Message.sessionId
- idx_message_created_at em Message.createdAt
- idx_document_session_id em Document.sessionId
- uk_document_storage_file_name em Document.storageFileName

### 7.4 Design de Domínio

- **IDs UUID**: evita exposição de sequenciais, facilita migração futura.
- **Session como raiz agregada**: toda mensagem pertence a uma sessão.
- **role enum**: USER (front-end), ASSISTANT (sistema/IA), SYSTEM (eventos).
- **metadata JSON**: preparação para tokens de LLM, confidence scores.
- **Document dissociável**: upload antes de vincular à sessão.
- **Lob para content**: suporta mensagens longas.

---

## 8. Requisitos Não Funcionais

### 8.1 Escalabilidade
- Camada de serviço stateless para múltiplas instâncias horizontais.
- Paginação obrigatória em consultas de histórico.
- Redis, RabbitMQ/Kafka atrás de interfaces.

### 8.2 Extensibilidade
- Novos provedores de IA via MessageProcessingStrategy.
- Novos storages implementando FileStorageService.
- Eventos de domínio permitem novos consumers.

### 8.3 Manutenibilidade
- Cobertura de testes mínima de 80%.
- Nomenclatura consistente em todo o projeto.
- Código em inglês; comentários só para contexto de domínio.

### 8.4 Observabilidade
- Logs estruturados em JSON (ELK/OpenSearch).
- Métricas via Spring Actuator + Micrometer.
- Health check por dependência individual.
- Tracing preparado para OpenTelemetry.

### 8.5 Segurança
- Validação em duas camadas: DTO (Jakarta) + Service (regras).
- Upload: validar magic bytes, não confiar em extensão.
- JWT preparado desde o início (SecurityFilter + JwtTokenProvider).

### 8.6 Testabilidade
- Services com interfaces → Mockito.
- Controllers com @WebMvcTest.
- Repositories com @DataJpaTest.
- Integração com @SpringBootTest.

---

## 9. Diretrizes de Implementação

### 9.1 Práticas Obrigatórias

1. **Todo Service deve ter uma interface.** Nenhuma injeção em concreta.
2. **Todo Controller deve receber DTO e retornar DTO.** Proibido Entity.
3. **Todo DTO de request deve usar Jakarta Validation** com @Valid no Controller.
4. **Toda exceção de negócio** como BusinessException tratada pelo GlobalExceptionHandler.
5. **Toda escrita deve ser @Transactional.**
6. **Toda consulta de coleção deve ser paginada** (Pageable).
7. **Toda injeção via construtor.** Proibido @Autowired em campos.
8. **Todo mapper explícito.** Proibido mapeamento manual em Service.
9. **Migrações Flyway imutáveis.** Novas alterações = novas migrações.
10. **Configurações externalizadas** em application-<profile>.yml.

### 9.2 Práticas Proibidas

1. Controller com regra de negócio.
2. Service com dependência HTTP (jakarta.servlet.http.*).
3. Repository com regra de negócio.
4. Entity com @JsonIgnore ou anotações de serialização.
5. DTO com anotações JPA.
6. Injeção de EntityManager em Service.
7. @Data do Lombok em Entity JPA.
8. Strings mágicas no código.
9. Classes com mais de 200 linhas.
10. Métodos com mais de 30 linhas.
11. Comentários desnecessários.
12. Dependências cíclicas.

### 9.3 Convenções de Nomenclatura

| Elemento | Convenção | Exemplo |
|---|---|---|
| Controller | <Entidade>Controller | MessageController |
| Service (interface) | <Entidade>Service | MessageService |
| Service (impl) | <Entidade>ServiceImpl | MessageServiceImpl |
| Repository | <Entidade>Repository | MessageRepository |
| Entity | <Entidade> | Message |
| Request DTO | <Acao>Request | SendMessageRequest |
| Response DTO | <Entidade>Response | MessageResponse |
| Mapper | <Entidade>Mapper | MessageMapper |
| Exception | <Descricao>Exception | ResourceNotFoundException |
| Enum | <Descricao> | MessageRole |

---

## Anexo: Mapa de Dependências entre Camadas

```
                        +--------------+
                        |   Front-end   |
                        +------+-------+
                               | HTTP
                               v
                    +------------------+
                    |    Controller     |
                    |  (Adaptador HTTP) |
                    +------+-----------+
                           | DTO
                           v
                    +------------------+
                    |   Service (intf)  |
                    +------+-----------+
                           |
                    +------+-----------+
                    |  ServiceImpl      |
                    +--+----+----+---+
                       |    |      |
              +--------+    |      +--------+
              v             v              v
       +------------+ +----------+ +--------------+
       |  Mapper     | |Strategy  | |  Event        |
       | (EntityDTO) | | (padrão) | |  Publisher    |
       +------------+ +----------+ +------+-------+
              |                           |
              v                           v
       +------------+          +------------------+
       | Repository  |          | RabbitMQ/Kafka    |
       | (JPA)      |          | (futuro)         |
       +------+-----+          +------------------+
              |
              v
       +------------+
       |   MySQL     |
       +------------+
```

---

*Este documento constitui a especificação arquitetural de referência para o desenvolvimento da Plataforma Conversacional. Toda implementação deve derivar exclusivamente dos contratos, responsabilidades e restrições aqui definidos.*
