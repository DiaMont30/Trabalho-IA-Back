# Especificação Arquitetural — Estrutura e Responsabilidades

---

## 1. Estrutura de Diretórios

```
projeto-ia/
└── src/
    ├── main/
    │   ├── java/com/plataforma/conversacional/
    │   │   ├── PlataformaConversacionalApplication.java
    │   │   ├── config/
    │   │   │   ├── WebConfig.java
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── OpenApiConfig.java
    │   │   │   ├── StorageConfig.java
    │   │   │   ├── MessagePublisherConfig.java
    │   │   │   ├── CorsConfig.java
    │   │   │   └── RestTemplateConfig.java               # ← RAG
    │   │   ├── controller/
    │   │   │   ├── MessageController.java
    │   │   │   ├── SessionController.java
    │   │   │   ├── DocumentController.java
    │   │   │   ├── HealthController.java
    │   │   │   ├── RagController.java                    # ← RAG
    │   │   │   └── WebhookController.java                # ← RAG
    │   │   ├── service/
    │   │   │   ├── MessageService.java
    │   │   │   ├── SessionService.java
    │   │   │   ├── DocumentService.java
    │   │   │   ├── HealthService.java
    │   │   │   ├── RagIngestionService.java               # ← RAG
    │   │   │   └── impl/
    │   │   │       ├── MessageServiceImpl.java
    │   │   │       ├── SessionServiceImpl.java
    │   │   │       ├── DocumentServiceImpl.java
    │   │   │       ├── HealthServiceImpl.java
    │   │   │       └── RagIngestionServiceImpl.java       # ← RAG
    │   │   ├── strategy/
    │   │   │   ├── MessageProcessingStrategy.java
    │   │   │   └── MockMessageProcessingStrategy.java
    │   │   ├── event/
    │   │   │   ├── MessageSentEvent.java
    │   │   │   ├── MessageEventPublisher.java
    │   │   │   └── DocumentIngestedEvent.java             # ← RAG
    │   │   ├── repository/
    │   │   │   ├── MessageRepository.java
    │   │   │   ├── SessionRepository.java
    │   │   │   ├── DocumentRepository.java
    │   │   │   ├── DocumentChunkRepository.java          # ← RAG
    │   │   │   ├── SourceReferenceRepository.java        # ← RAG
    │   │   │   └── PipelineJobRepository.java            # ← RAG
    │   │   ├── entity/
    │   │   │   ├── Message.java
    │   │   │   ├── Session.java
    │   │   │   ├── Document.java
    │   │   │   ├── DocumentChunk.java          # ← RAG
    │   │   │   ├── SourceReference.java        # ← RAG
    │   │   │   └── PipelineJob.java            # ← RAG
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── SendMessageRequest.java
    │   │   │   │   ├── UploadDocumentRequest.java
    │   │   │   │   └── RagQueryRequest.java              # ← RAG
    │   │   │   ├── response/
    │   │   │   │   ├── MessageResponse.java
    │   │   │   │   ├── SessionHistoryResponse.java
    │   │   │   │   ├── DocumentResponse.java
    │   │   │   │   ├── HealthResponse.java
    │   │   │   │   ├── SessionResponse.java
    │   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── RagQueryResponse.java             # ← RAG
│   │   │   │   ├── SourceDetailResponse.java          # ← RAG
│   │   │   │   ├── IngestionStatusResponse.java       # ← RAG
│   │   │   │   └── DocumentStatusResponse.java        # ← n8n
    │   │   │   └── internal/
    │   │   │       └── FileUploadData.java
    │   │   ├── mapper/
    │   │   │   ├── MessageMapper.java
    │   │   │   ├── SessionMapper.java
    │   │   │   └── DocumentMapper.java
    │   │   ├── validation/
    │   │   │   ├── AllowedFileType.java
    │   │   │   └── AllowedFileTypeValidator.java
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── InvalidFileTypeException.java
    │   │   │   ├── BusinessException.java
    │   │   │   ├── ParsingException.java                 # ← RAG
    │   │   │   ├── EmbeddingException.java               # ← RAG
│   │   │   ├── RetrievalException.java               # ← RAG
│   │   │   ├── RagProcessingException.java           # ← RAG
│   │   │   └── ConflictException.java                # ← n8n
    │   │   ├── constants/
    │   │   │   └── ApiConstants.java
    │   │   ├── util/
    │   │   │   ├── FileUtils.java
    │   │   │   └── CosineSimilarity.java                 # ← RAG
    │   │   ├── enums/
    │   │   │   ├── MessageRole.java
    │   │   │   ├── MessageStatus.java
    │   │   │   ├── SessionStatus.java
    │   │   │   ├── DocumentType.java
    │   │   │   └── PipelineStatus.java                    # ← RAG
    │   │   ├── specification/
    │   │   │   └── MessageSpecification.java
    │   │   ├── health/
    │   │   │   ├── ApplicationHealthIndicator.java
    │   │   │   └── DatabaseHealthIndicator.java
    │   │   ├── security/
    │   │   │   ├── JwtTokenProvider.java
    │   │   │   └── JwtAuthenticationFilter.java
    │   │   ├── storage/
    │   │   │   ├── FileStorageService.java
    │   │   │   ├── LocalFileStorageService.java
    │   │   │   ├── VectorStore.java                    # ← RAG
    │   │   │   └── MockVectorStore.java                # ← RAG
    │   │   ├── parser/
    │   │   │   ├── DocumentParser.java                 # ← RAG
    │   │   │   ├── TxtParser.java                      # ← RAG
    │   │   │   └── PdfParser.java                      # ← RAG
    │   │   ├── chunking/
    │   │   │   ├── ChunkingStrategy.java               # ← RAG
    │   │   │   ├── Chunk.java                          # ← RAG
    │   │   │   └── FixedSizeChunker.java               # ← RAG
    │   │   ├── embedding/
    │   │   │   ├── EmbeddingStrategy.java              # ← RAG
    │   │   │   └── OllamaEmbeddingStrategy.java        # ← RAG
    │   │   ├── retrieval/
    │   │   │   ├── Retriever.java                      # ← RAG
    │   │   │   ├── SimilarityRetriever.java            # ← RAG
    │   │   │   └── ScoredChunk.java                    # ← RAG
    │   │   ├── pipeline/
    │   │   │   ├── RagPipeline.java                    # ← RAG
    │   │   │   ├── DefaultRagPipeline.java             # ← RAG
    │   │   │   ├── RagResult.java                      # ← RAG
    │   │   │   └── SourceDetail.java                   # ← RAG
    │   │   ├── integration/
    │   │   │   ├── n8n/
│   │   │   │   ├── N8nWebhookClient.java           # ← RAG
│   │   │   │   ├── RestN8nWebhookClient.java       # ← RAG
│   │   │   │   └── N8nNotificationListener.java    # ← n8n
    │   │   │   └── webhook/
    │   │   │       └── N8nWebhookPayload.java          # ← RAG
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/
    │           ├── V1__create_session_table.sql
    │           ├── V2__create_message_table.sql
    │           ├── V3__create_document_table.sql
    │           ├── V4__create_document_chunks_table.sql        # ← RAG
    │           ├── V5__create_source_references_table.sql      # ← RAG
    │           └── V6__create_pipeline_jobs_table.sql          # ← RAG
    └── test/
        └── java/com/plataforma/conversacional/
            ├── controller/
            │   ├── MessageControllerTest.java
            │   ├── SessionControllerTest.java
            │   ├── DocumentControllerTest.java
            │   ├── HealthControllerTest.java
            │   ├── RagControllerTest.java                    # ← RAG
            │   └── WebhookControllerTest.java                # ← RAG
            ├── service/impl/
            │   ├── MessageServiceImplTest.java
            │   ├── SessionServiceImplTest.java
            │   ├── DocumentServiceImplTest.java
            │   ├── HealthServiceImplTest.java
            │   └── RagIngestionServiceImplTest.java          # ← RAG
            └── repository/
                ├── MessageRepositoryTest.java
                ├── SessionRepositoryTest.java
                ├── DocumentRepositoryTest.java
                ├── DocumentChunkRepositoryTest.java          # ← RAG
                ├── SourceReferenceRepositoryTest.java        # ← RAG
                └── PipelineJobRepositoryTest.java            # ← RAG
```

---

## 2. Responsabilidade de Cada Diretório

### config/

O que deve conter:
- Classes anotadas com @Configuration, @EnableWebSecurity, etc.
- Beans de infraestrutura: ObjectMapper, PasswordEncoder, conversores, RestTemplate.
- Configuração de CORS, OpenAPI/Swagger, segurança, storage e filas.

O que NÃO deve conter:
- Regras de negócio.
- Lógica de validação de domínio.
- Endpoints ou mapeamentos HTTP.

| Permissões | Proibições |
|---|---|
| Depender de application.yml / application.properties | Depender de Controllers, Services, Repositories |
| Importar beans do Spring Security, SpringDoc, etc. | Conter lógica condicional de negócio |

### controller/

O que deve conter:
- Classes anotadas com @RestController.
- Métodos com @GetMapping, @PostMapping, etc.
- Validação superficial via @Valid nos DTOs de request.
- Delegação imediata ao Service após conversão de parâmetros.

O que NÃO deve conter:
- Regras de negócio (if com lógica de domínio).
- Acesso a repositórios ou EntityManager.
- Uso de Entity em parâmetros ou retorno.
- Tratamento de exceções de negócio (delegar ao GlobalExceptionHandler).

| Permissões | Proibições |
|---|---|
| @Valid, @PathVariable, @RequestBody, @RequestParam, @RequestPart | Injetar Repository, EntityManager, DataSource |
| ResponseEntity<T> para personalizar resposta HTTP | Acessar HttpSession, ServletRequest para lógica de negócio |
| Delegar para Service imediatamente | Implementar validação complexa (regex, estado) |

### service/ e service/impl/

O que deve conter:
- Interfaces de serviço definindo contratos de domínio.
- Implementações concretas com lógica de negócio e orquestração.
- Uso de mappers para converter entre Entity e DTO.

O que NÃO deve conter:
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
| DTOs internos (internal/) para transporte de dados sem HTTP | — |

### strategy/

O que deve conter:
- Interface de estratégia para processamento de mensagens (MessageProcessingStrategy).
- Implementações: MockMessageProcessingStrategy (atual), futuras (LlamaStrategy, OpenAiStrategy).

O que NÃO deve conter:
- Dependência de HTTP ou Servlet.
- Acesso a repositórios ou banco de dados.

| Permissões | Proibições |
|---|---|
| DTOs de entrada/saída | Conhecer a implementação de outras estratégias |

### event/

O que deve conter:
- Classes de evento (MessageSentEvent).
- Publicador de eventos (MessageEventPublisher) que usa ApplicationEventPublisher.

O que NÃO deve conter:
- Consumidores de fila (ficarão em módulo futuro ou em config/).
- Lógica de negócio.

| Permissões | Proibições |
|---|---|
| DTOs para serialização de eventos | Acesso a repositórios |
| Anotações de serialização | Lógica condicional |

### repository/

O que deve conter:
- Interfaces que estendem JpaRepository ou JpaSpecificationExecutor.
- Métodos de consulta derivados do Spring Data JPA.
- Consultas customizadas via @Query quando necessário.

O que NÃO deve conter:
- Regras de negócio.
- Chamadas a Services.
- Lógica de transformação de dados (mappers).

| Permissões | Proibições |
|---|---|
| @Query, Pageable, Specification | Injetar Service ou Controller |
| Optional<T> como retorno | Conter if/else, loops com lógica de negócio |
| Métodos derivados: findBySessionIdOrderByCreatedAt() | Serialização ou formatação de dados |

### mapper/

O que deve conter:
- Interfaces para conversão entre Entity ↔ DTO.
- Métodos de mapeamento explícitos (MapStruct).

O que NÃO deve conter:
- Regras de negócio ou validação.
- Dependência de repositories ou services.
- Lógica condicional de transformação que seja regra de negócio.

| Permissões | Proibições |
|---|---|
| Anotações do MapStruct (@Mapper, @Mapping) | Acesso a banco de dados |
| Injeção de outros mappers (composição de mapeamentos) | Lógica de validação (se X então Y) |

### validation/

O que deve conter:
- Anotações customizadas de Jakarta Validation.
- Implementações de ConstraintValidator para validações específicas do domínio.

O que NÃO deve conter:
- Lógica de serviço ou regras que envolvam estado do banco.
- Mapeamento de entidades.

| Permissões | Proibições |
|---|---|
| Anotações e validadores reutilizáveis | Acesso a repositories ou services |

### exception/

O que deve conter:
- Classes de exceção de negócio (ResourceNotFoundException, InvalidFileTypeException, BusinessException).
- @RestControllerAdvice global (GlobalExceptionHandler) para tratamento unificado.
- Métodos handlers para cada exceção, retornando ErrorResponse.

O que NÃO deve conter:
- Regras de negócio.
- Lógica de fluxo de aplicação.

| Permissões | Proibições |
|---|---|
| @ExceptionHandler, @ControllerAdvice | Decisões que alteram fluxo de domínio |
| ResponseEntity<ErrorResponse> | Acesso a Entity, Repository |

### constants/

O que deve conter:
- Constantes públicas estáticas finais: paths de API, tamanhos máximos, formatos de data.

O que NÃO deve conter:
- Lógica executável.
- Estado mutável.

| Permissões | Proibições |
|---|---|
| public static final String API_VERSION = "/api/v1" | Métodos, construtores com lógica |

### util/

O que deve conter:
- Funções utilitárias puras e stateless: manipulação de strings, datas, arquivos.

O que NÃO deve conter:
- Dependência de Spring (beans injetados).
- Acesso a banco, HTTP, arquivos externos.

| Permissões | Proibições |
|---|---|
| Métodos static puros | @Autowired, @Component |
| Operações matemáticas, formatação | Efeitos colaterais (escrita em disco, chamadas HTTP) |

### enums/

O que deve conter:
- Enumerações de domínio: MessageRole, MessageStatus, SessionStatus, DocumentType.

O que NÃO deve conter:
- Métodos com lógica de negócio complexa.
- Dependências externas.

| Permissões | Proibições |
|---|---|
| Campos private final com descrições | Implementar interfaces de serviço |
| Métodos getters simples | Conter lógica de estado transicional |

### specification/

O que deve conter:
- Classes que implementam Specification<T> do Spring Data JPA para consultas dinâmicas.

O que NÃO deve conter:
- Regras de negócio.
- Mapeamento de DTOs.

| Permissões | Proibições |
|---|---|
| CriteriaBuilder, Predicate, Root | Referências a Services ou Controllers |

### health/

O que deve conter:
- Implementações de HealthIndicator do Spring Boot Actuator.

O que NÃO deve conter:
- Regras de negócio.
- Lógica de domínio da plataforma.

| Permissões | Proibições |
|---|---|
| Health.Builder, DataSource | Regras de negócio da aplicação |

### security/

O que deve conter:
- Classes relacionadas a autenticação/autorização: JwtTokenProvider, JwtAuthenticationFilter.

O que NÃO deve conter:
- Lógica de conversação ou mensageria.
- Entidades JPA de domínio.

| Permissões | Proibições |
|---|---|
| SecurityContextHolder, UserDetails, Authentication | Entity, Repository de domínio |

### parser/

O que deve conter:
- Interface `DocumentParser` definindo contrato de extração de texto.
- Implementações: `TxtParser` (leitura direta), `PdfParser` (Apache PDFBox).

O que NÃO deve conter:
- Lógica de chunking, embedding ou HTTP.
- Dependência de VectorStore, repositórios ou serviços de IA.

| Permissões | Proibições |
|---|---|
| byte[], InputStream, contentType | Conhecer Chunker, Embedder, n8n |
| Apache PDFBox para parsing de PDF | Regras de negócio da aplicação |

### chunking/

O que deve conter:
- Interface `ChunkingStrategy` para divisão de texto em fragmentos.
- Implementações: `FixedSizeChunker` (tamanho fixo + overlap), `RecursiveChunker` (recursivo).

O que NÃO deve conter:
- Lógica de parsing, embedding ou HTTP.
- Dependência de banco de dados ou serviços externos.

| Permissões | Proibições |
|---|---|
| String de entrada, int maxSize, int overlap | Acesso a repository, VectorStore |
| DTO interno `Chunk` (content, index) | Conhecer DocumentParser ou EmbeddingStrategy |

### embedding/

O que deve conter:
- Interface `EmbeddingStrategy` para geração de vetores semânticos.
- Implementações: `OllamaEmbeddingStrategy` (Ollama via Docker), `MockEmbeddingStrategy` (fallback).

O que NÃO deve conter:
- Lógica de parsing, chunking ou HTTP controllers.
- Regras de negócio do domínio.

| Permissões | Proibições |
|---|---|
| RestTemplate para chamada HTTP ao Ollama | Acesso a Entity, Repository |
| Configuração de timeout e modelo | Lógica de chunking ou retrieval |

### retrieval/

O que deve conter:
- Interface `Retriever` para busca de chunks relevantes.
- Implementação: `SimilarityRetriever` (embedding → vector store → scoring).
- DTO interno: `ScoredChunk` (chunk + relevanceScore).

O que NÃO deve conter:
- Lógica de geração de resposta (delegar ao pipeline).
- Dependência de HTTP controllers ou webhooks.

| Permissões | Proibições |
|---|---|
| Depender de EmbeddingStrategy interface | Conhecer n8n, webhooks, controllers |
| Depender de VectorStore interface | Regras de formatação de resposta |

### pipeline/

O que deve conter:
- Interface `RagPipeline` orquestrando retrieve → augment → generate.
- Implementação: `DefaultRagPipeline`.
- DTOs internos: `RagResult` (answer + sources), `SourceDetail`.

O que NÃO deve conter:
- Lógica de HTTP, controllers ou webhooks.
- Acesso direto a banco de dados ou repositórios.

| Permissões | Proibições |
|---|---|
| Injetar Retriever, MessageProcessingStrategy, SourceReferenceRepository | ResponseEntity, @RequestMapping |
| Spring Events (ApplicationEventPublisher) | Dependência de EmbeddingStrategy diretamente (via Retriever) |

### integration/

O que deve conter:
- Interfaces e implementações para integração com sistemas externos.
- `N8nWebhookClient`: interface para envio de eventos ao n8n.
- `RestN8nWebhookClient`: implementação via RestTemplate.

O que NÃO deve conter:
- Regras de negócio do pipeline RAG.
- Lógica de parsing, chunking ou embedding.

| Permissões | Proibições |
|---|---|
| DTOs específicos para webhook (N8nWebhookPayload) | Conhecer Entity, Repository |
| RestTemplate, URLs configuráveis | Regras de domínio do pipeline |

### storage/

O que deve conter:
- Interface FileStorageService definindo contrato de armazenamento.
- Implementações: LocalFileStorageService (atual), CloudFileStorageService (futura).
- Interface VectorStore para armazenamento de embeddings (a partir da Parte 2).

O que NÃO deve conter:
- Lógica de validação de tipo de arquivo (delegar ao Service).
- Métodos HTTP ou de rede embutidos no código.

| Permissões | Proibições |
|---|---|
| java.nio.file.Path, InputStream, MultipartFile (apenas como parâmetro) | Conter @RequestMapping, @PostMapping |
| CosineSimilarity para busca vetorial (MockVectorStore) | Regras de negócio do pipeline |

---

## 3. Mapa de Dependências entre Camadas

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
       | PostgreSQL  |
       +------------+

### Mapa Estendido — Pipeline RAG (Parte 2)

```
       +------------------+
       |   RagController  |
       |  /rag/query      |
       |  /rag/ingest     |
       +--------+---------+
                | DTO
                v
       +------------------+
       |  RagPipeline      |
       |  (Orquestrador)   |
       +--+----+----+---+
          |    |      |
          v    v      v
   +--------+ +-------+ +-----------+
   |Retriever| |Source | |Message    |
   |(busca)  | |Tracker| |Processing |
   +----+----+ +-------+ |Strategy   |
        |                 +-----------+
        v
   +---------+ +---------+
   |Vector    | |Embedding|
   |Store     | |(Ollama) |
   +---------+ +---------+

   Pipeline de Ingestão (Assíncrono):
   Document → Parser → Chunker → Embedder → VectorStore
                (TXT/PDF)  (FixedSize)  (Ollama)

   Integração n8n (externa):
   +------------------+
   |  AsyncIngestion   |
   |  Processor        |
   +--------+---------+
            | ApplicationEvent
            v
   +---------------------+
   | N8nNotification     |
   | Listener            |
   +--------+------------+
            | HTTP POST (webhook)
            v
   +------------------+
   | n8n (Docker)      |
   | workflow engine   |
   +------------------+
            | HTTP GET (status)
            v
   +------------------+
   | DocumentController |
   | /documents/{id}/   |
   | status             |
   +------------------+

   MessageServiceImpl (query RAG) → N8nWebhookClient → n8n (HTTP)
```
