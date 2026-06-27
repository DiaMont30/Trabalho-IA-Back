# Implementações Pendentes — Passo a Passo

> Este documento reúne o plano de implementação para todas as pendências do projeto.
> Cada seção descreve um conjunto de tarefas ordenadas, prontas para serem executadas.

---

## Pendências — Estado Atual

| #   | Pendência                                                                 | Prioridade | Status          |
| --- | ------------------------------------------------------------------------- | ---------- | --------------- |
| 1   | Autenticação JWT completa (Fase 2)                                        | Alta       | ✅ Concluída    |
| 2   | Testes unitários e de integração                                          | Alta       | ✅ Concluída    |
| 3   | Implementar MessageSpecification (consultas dinâmicas)                    | Média      | ✅ Concluída    |
| 4   | Implementar Event Listeners para MessageSentEvent                         | Média      | ✅ Concluída    |
| 5   | Paginação no endpoint GET /sessions                                       | Baixa      | ✅ Concluída    |
| 6   | Validação por magic bytes em upload                                       | Média      | ✅ Concluída    |
| 7   | Implementar config classes stub (Web, Storage, OpenApi, MessagePublisher) | Baixa      | ✅ iniciada |
| 8   | Logging estruturado JSON (logback-spring.xml)                             | Média      | ✅ Concluída     |
| 9   | **Pipeline RAG — Parte 2 (16 etapas)**                                    | **Alta**   | 🟡 Em andamento (14/16) |

---

## [1] Autenticação JWT — Fase 2

> **Documento de referência:** `docs/implementacao-autenticacao.md`

### Pré-requisitos

- [x] Adicionar dependências JJWT no pom.xml
- [x] Adicionar propriedades JWT no application.yml

### Implementação

- [x] Criar migration V4\_\_create_users_table.sql
- [x] Criar entidade User em entity/
- [x] Criar UserRepository
- [x] Criar CustomUserDetailsService em security/
- [x] Implementar JwtTokenProvider (substituir skeletons)
- [x] Implementar JwtAuthenticationFilter (substituir skeleton)
- [x] Criar DTOs: LoginRequest, RegisterRequest, AuthResponse
- [x] Criar AuthController
- [x] Atualizar SecurityConfig com rotas protegidas

---

## [2] Testes

> **Cobertura mínima:** 80%

### Controllers (@WebMvcTest)

- [x] SessionControllerTest
- [x] MessageControllerTest
- [x] DocumentControllerTest
- [x] HealthControllerTest

### Services (@ExtendWith(MockitoExtension.class))

- [x] SessionServiceImplTest
- [x] MessageServiceImplTest
- [x] DocumentServiceImplTest
- [x] HealthServiceImplTest

### Repositories (@DataJpaTest)

- [x] SessionRepositoryTest
- [x] MessageRepositoryTest
- [x] DocumentRepositoryTest

---

## [3] MessageSpecification

- [x] Implementar `bySessionId(Long sessionId)` com CriteriaBuilder
- [x] Integrar com MessageRepository (JpaSpecificationExecutor já estendido)

---

## [4] Event Listeners

- [x] Criar consumer para MessageSentEvent
- [x] Definir ação: logging, notificação, ou preparação para fila

---

## [5] Paginação — GET /sessions

- [x] Adicionar parâmetros page/size no endpoint
- [x] Implementar no SessionServiceImpl com Pageable

---

## [6] Validação de Upload — Magic Bytes

- [x] Implementar verificação de magic bytes para PDF (%PDF) e TXT
- [x] Atualizar AllowedFileTypeValidator ou DocumentServiceImpl

---

## [7] Config Classes

- [x] Implementar WebConfig (configurar ObjectMapper com date format)
- [x] Implementar StorageConfig (bean Path para upload-dir)
- [x] Implementar OpenApiConfig (OpenAPI bean com info/contact)
- [x] Implementar MessagePublisherConfig (thread pool + @EnableAsync)

---

## [8] Logging Estruturado

- [x] Criar logback-spring.xml em src/main/resources/
- [x] Configurar appender JSON (LogstashEncoder)
- [x] Configurar níveis por pacote (dev | prod | default)

---

## [9] Pipeline RAG — Parte 2: Inteligência em Ação

> **Documentos de referência:** `docs/spec-dominio.md`, `docs/spec-api.md`, `docs/spec-arquitetura.md`, `docs/spec-casos-de-uso.md`, `docs/spec-nao-funcionais.md`
>
> **Stack:** Ollama (Docker) para embeddings locais, VectorStore abstrata, PostgreSQL para chunks
>
> **Ordem:** As etapas devem ser executadas sequencialmente. Cada etapa depende da anterior.

---

### Etapa 9.1 — Docker Compose + Config Ollama

**Objetivo:** Disponibilizar Ollama localmente via Docker para geração de embeddings.

**Tarefas:**

- [x] Criar `docker-compose.yml` na raiz do projeto:

```yaml
version: "3.8"
services:
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    command: serve
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:11434/api/tags"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  ollama_data:
```

- [x] Adicionar script `docker-init.sh` para baixar modelo padrão:

  ```bash
  docker compose up -d
  docker compose exec ollama ollama pull nomic-embed-text
  ```

- [x] Adicionar propriedades no `application.yml`:
  ```yaml
  app:
    rag:
      ollama:
        url: http://localhost:11434
        model: nomic-embed-text
        timeout: 5000
      chunking:
        max-size: 512
        overlap: 64
      retrieval:
        top-k: 5
        min-score: 0.7
  ```

**Arquivos:** `docker-compose.yml`, `docker-init.sh`, `src/main/resources/application.yml`

---

### Etapa 9.2 — Migrations Flyway (V5, V6, V7)

**Objetivo:** Criar as tabelas do pipeline RAG.

**Tarefas:**

- [x] Criar `V5__create_document_chunks_table.sql`:

```sql
CREATE TABLE document_chunks (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    chunk_index INT NOT NULL,
    embedding TEXT,  -- JSON array armazenado como string (preparação para pgvector futuro)
    metadata JSON,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_chunk_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    UNIQUE(document_id, chunk_index)
);

CREATE INDEX idx_chunks_document_id ON document_chunks(document_id);
```

- [x] Criar `V6__create_source_references_table.sql`:

```sql
CREATE TABLE source_references (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    chunk_id BIGINT NOT NULL,
    relevance_score DOUBLE PRECISION NOT NULL,
    excerpt VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_source_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_source_chunk FOREIGN KEY (chunk_id) REFERENCES document_chunks(id) ON DELETE CASCADE
);

CREATE INDEX idx_sources_message_id ON source_references(message_id);
```

- [x] Criar `V7__create_pipeline_jobs_table.sql`:

```sql
CREATE TYPE pipeline_status AS ENUM ('QUEUED', 'PARSING', 'CHUNKING', 'EMBEDDING', 'READY', 'FAILED');

CREATE TABLE pipeline_jobs (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    status pipeline_status NOT NULL DEFAULT 'QUEUED',
    chunks_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    CONSTRAINT fk_job_document FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);

CREATE INDEX idx_jobs_document_id ON pipeline_jobs(document_id);
CREATE INDEX idx_jobs_status ON pipeline_jobs(status);
```

**Arquivos:** `src/main/resources/db/migration/V5__create_document_chunks_table.sql`, `V6__create_source_references_table.sql`, `V7__create_pipeline_jobs_table.sql`

---

### Etapa 9.3 — Entidades do Pipeline RAG

**Objetivo:** Mapear as novas tabelas em entidades JPA.

**Tarefas:**

- [x] Criar `entity/DocumentChunk.java` — id, document (ManyToOne), content, chunkIndex, embedding (String JSON), metadata (String JSON), createdAt
- [x] Criar `entity/SourceReference.java` — id, message (ManyToOne), chunk (ManyToOne), relevanceScore, excerpt, createdAt
- [x] Criar `entity/PipelineJob.java` — id, document (ManyToOne), status (PipelineStatus), chunksCount, errorMessage, createdAt, completedAt
- [x] Criar `enums/PipelineStatus.java` — QUEUED, PARSING, CHUNKING, EMBEDDING, READY, FAILED

**Arquivos:** `entity/DocumentChunk.java`, `entity/SourceReference.java`, `entity/PipelineJob.java`, `enums/PipelineStatus.java`

---

### Etapa 9.4 — VectorStore Interface + Mock Implementação

**Objetivo:** Abstrair o armazenamento e busca de embeddings.

**Tarefas:**

- [x] Criar `storage/VectorStore.java` — interface com métodos:
  - `void storeChunk(DocumentChunk chunk, float[] embedding)`
  - `void storeChunks(List<DocumentChunk> chunks, List<float[]> embeddings)`
  - `List<ScoredChunk> searchSimilar(float[] queryEmbedding, int topK, double minScore)`
  - `void deleteByDocumentId(Long documentId)`
- [x] Criar DTO interno `retrieval/ScoredChunk.java` — chunk, score
- [x] Criar `storage/MockVectorStore.java` — implementação em memória com ConcurrentHashMap e cálculo de similaridade por cosseno
- [x] Criar `util/CosineSimilarity.java` — método utilitário estático para cálculo de similaridade

**Arquivos:** `storage/VectorStore.java`, `storage/MockVectorStore.java`, `util/CosineSimilarity.java`, `retrieval/ScoredChunk.java`

---

### Etapa 9.5 — Parser Strategy (Extração de Texto)

**Objetivo:** Extrair texto bruto de documentos PDF e TXT.

**Tarefas:**

- [x] Criar `parsing/DocumentParser.java` — interface:
  ```java
  String parse(byte[] content, String contentType);
  ```
- [x] Criar `parsing/TxtParser.java` — `@Component`, new String(content, StandardCharsets.UTF_8)
- [x] Criar `parsing/PdfParser.java` — `@Component`, usar Apache PDFBox para extrair texto
- [x] Adicionar dependência no pom.xml:
  ```xml
  <dependency>
      <groupId>org.apache.pdfbox</groupId>
      <artifactId>pdfbox</artifactId>
      <version>3.0.1</version>
  </dependency>
  ```
- [x] Criar `exception/ParsingException.java` — RuntimeException para falhas de parsing

**Arquivos:** `parsing/DocumentParser.java`, `parsing/TxtParser.java`, `parsing/PdfParser.java`, `exception/ParsingException.java`, `pom.xml`

---

### Etapa 9.6 — Chunking Strategy (Divisão de Texto)

**Objetivo:** Dividir texto extraído em fragmentos para indexação.

**Tarefas:**

- [x] Criar `chunking/ChunkingStrategy.java` — interface:
  ```java
  List<Chunk> chunk(String text);
  ```
- [x] Criar DTO interno `chunking/Chunk.java` — `String content`, `int index`
- [x] Criar `chunking/FixedSizeChunker.java` — `@Component`, divide em tamanho fixo com overlap configurável via `@Value("${app.rag.chunking.max-size}")` e `@Value("${app.rag.chunking.overlap}")`
- [x] Criar `chunking/RecursiveChunker.java` — `@Component` (opcional, fallback), divide recursivamente por parágrafos → frases → caracteres

**Arquivos:** `chunking/ChunkingStrategy.java`, `chunking/Chunk.java`, `chunking/FixedSizeChunker.java`

---

### Etapa 9.7 — Embedding Strategy (Ollama)

**Objetivo:** Gerar embeddings via Ollama rodando em Docker.

**Tarefas:**

- [x] Criar `embedding/EmbeddingStrategy.java` — interface:
  ```java
  float[] embed(String text);
  List<float[]> embedBatch(List<String> texts);
  int getDimension();
  ```
- [x] Criar `embedding/OllamaEmbeddingStrategy.java` — `@Component`, faz POST para `http://localhost:11434/api/embeddings` com RestTemplate
- [x] Configurar RestTemplate com timeout de 5s
- [x] Tratar resposta do Ollama: `{"embedding": [0.1, 0.2, ...]}`
- [x] Criar `exception/EmbeddingException.java` — RuntimeException para falhas de embedding

**Arquivos:** `embedding/EmbeddingStrategy.java`, `embedding/OllamaEmbeddingStrategy.java`, `exception/EmbeddingException.java`, `config/RestTemplateConfig.java` (bean RestTemplate)

---

### Etapa 9.8 — Retriever (Busca por Similaridade)

**Objetivo:** Buscar chunks relevantes dada uma query.

**Tarefas:**

- [x] Criar `retrieval/Retriever.java` — interface:
  ```java
  List<ScoredChunk> retrieve(String query, int topK);
  ```
- [x] Criar `retrieval/SimilarityRetriever.java` — `@Service`, injeta EmbeddingStrategy + VectorStore:
  1. Gera embedding da query via EmbeddingStrategy
  2. Busca chunks similares via VectorStore.searchSimilar()
  3. Ordena por score descendente
  4. Retorna topK resultados

**Arquivos:** `retrieval/Retriever.java`, `retrieval/SimilarityRetriever.java`

---

### Etapa 9.9 — Pipeline RAG Orchestrator

**Objetivo:** Orquestrar o fluxo completo de retrieve → augment → generate.

**Tarefas:**

- [x] Criar `pipeline/RagPipeline.java` — interface:
  ```java
  RagResult execute(String query, Long sessionId);
  ```
- [x] Criar DTO `pipeline/RagResult.java` — `String answer`, `List<SourceReference> sources`
- [x] Criar DTO `pipeline/SourceDetail.java` — `Long chunkId`, `Long documentId`, `String documentName`, `String excerpt`, `double relevanceScore`
- [x] Criar `pipeline/DefaultRagPipeline.java` — `@Service`, injeta Retriever + MessageProcessingStrategy:
  1. Retrieve: busca chunks relevantes (topK=5, minScore=0.7)
  2. Augment: monta contexto string concatenando chunks
  3. Generate: chama MessageProcessingStrategy.process(context + "\n\nPergunta: " + query)
  4. Track: cria SourceReference para cada chunk usado
  5. Retorna RagResult com resposta + fontes
- [x] Criar `exception/RagProcessingException.java` — RuntimeException com status code 422

**Arquivos:** `pipeline/RagPipeline.java`, `pipeline/DefaultRagPipeline.java`, `pipeline/RagResult.java`, `pipeline/SourceDetail.java`, `exception/RagProcessingException.java`

---

### Etapa 9.10 — Serviço de Ingestão de Documentos

**Objetivo:** Orquestrar parse → chunk → embed → store para cada documento.

**Tarefas:**

- [x] Criar `service/RagIngestionService.java` — interface:
  ```java
  IngestionStatusResponse ingestDocument(Long documentId);
  IngestionStatusResponse getStatus(Long jobId);
  ```
- [x] Criar `service/impl/RagIngestionServiceImpl.java` — `@Service`, `@Async`:
  1. Busca Document do repositório + conteúdo do arquivo via FileStorageService
  2. Cria PipelineJob com status QUEUED
  3. Atualiza status → PARSING → extrai texto via DocumentParser
  4. Atualiza status → CHUNKING → divide texto via ChunkingStrategy
  5. Atualiza status → EMBEDDING → gera embeddings via EmbeddingStrategy
  6. Armazena chunks + embeddings via VectorStore
  7. Atualiza status → READY
- [x] Criar `event/DocumentIngestedEvent.java` — evento de domínio
- [x] Configurar `@EnableAsync` na aplicação (já existente em MessagePublisherConfig)

**Arquivos:** `service/RagIngestionService.java`, `service/impl/RagIngestionServiceImpl.java`, `event/DocumentIngestedEvent.java`

---

### Etapa 9.11 — Controllers + DTOs da API RAG

**Objetivo:** Expor endpoints REST para query RAG e ingestão.

**Tarefas:**

- [x] Criar DTO `dto/request/RagQueryRequest.java`:
  ```java
  public record RagQueryRequest(
      @NotBlank @Size(min = 1, max = 5000) String query,
      @NotNull Long sessionId
  ) {}
  ```
- [x] Criar DTO `dto/response/RagQueryResponse.java`:
  ```java
  public record RagQueryResponse(
      String answer,
      List<SourceDetailResponse> sources
  ) {}
  ```
- [x] Criar DTO `dto/response/SourceDetailResponse.java`:
  ```java
  public record SourceDetailResponse(
      Long documentId,
      String documentName,
      String excerpt,
      double relevanceScore
  ) {}
  ```
- [x] Criar DTO `dto/response/IngestionStatusResponse.java`:
  ```java
  public record IngestionStatusResponse(
      Long jobId,
      Long documentId,
      String status,
      Integer chunksCount,
      String errorMessage
  ) {}
  ```
- [x] Criar `controller/RagController.java` — `@RestController`:

| Método | Endpoint                            | Request                | Response                   | Erros         |
| ------ | ----------------------------------- | ---------------------- | -------------------------- | ------------- |
| POST   | `/api/v1/rag/query`                 | RagQueryRequest (JSON) | RagQueryResponse           | 400, 404, 422 |
| POST   | `/api/v1/rag/ingest/{documentId}`   | —                      | IngestionStatusResponse    | 400, 404      |
| GET    | `/api/v1/rag/ingest/{jobId}/status` | —                      | IngestionStatusResponse    | 404           |
| GET    | `/api/v1/rag/sources/{messageId}`   | —                      | List<SourceDetailResponse> | 404           |

- [x] Adicionar constantes no `ApiConstants.java`:
  ```java
  public static final String RAG_PATH = "/rag";
  public static final String QUERY_PATH = "/query";
  public static final String INGEST_PATH = "/ingest";
  public static final String SOURCES_PATH = "/sources";
  ```

**Arquivos:** `dto/request/RagQueryRequest.java`, `dto/response/RagQueryResponse.java`, `dto/response/SourceDetailResponse.java`, `dto/response/IngestionStatusResponse.java`, `controller/RagController.java`, `constants/ApiConstants.java`

---

### Etapa 9.12 — Integração n8n (Webhooks)

**Objetivo:** Preparar webhooks para orquestração externa via n8n.

**Tarefas:**

- [x] Criar `integration/n8n/N8nWebhookClient.java` — interface:
  ```java
  void notifyQueryCompleted(RagQueryResponse response, Long sessionId);
  void notifyIngestionCompleted(Long documentId, String status);
  ```
- [x] Criar `integration/n8n/RestN8nWebhookClient.java` — `@Component`, POST para URL configurada
- [x] Adicionar propriedades no `application.yml`:
  ```yaml
  app:
    n8n:
      webhook-url: ${N8N_WEBHOOK_URL:http://localhost:5678/webhook/rag}
      enabled: false
  ```
- [x] Criar DTO `dto/webhook/N8nWebhookPayload.java` — eventType, payload (Map), timestamp
- [x] Criar `controller/WebhookController.java` — recebe respostas do n8n:
  - `POST /api/v1/webhooks/n8n/rag-response` — processa resposta assíncrona do n8n
- [x] Atualizar `MessageSentEvent` para incluir tipo RAG vs simples

**Arquivos:** `integration/n8n/N8nWebhookClient.java`, `integration/n8n/RestN8nWebhookClient.java`, `dto/webhook/N8nWebhookPayload.java`, `controller/WebhookController.java`

---

### Etapa 9.13 — Tratamento de Erros do Pipeline

**Objetivo:** Garantir que erros do pipeline retornem respostas HTTP adequadas.

**Tarefas:**

- [ ] Adicionar handlers no `GlobalExceptionHandler.java`:

| Exceção                | HTTP Status               | error                         |
| ---------------------- | ------------------------- | ----------------------------- |
| RagProcessingException | 422 UNPROCESSABLE_ENTITY  | RAG Processing Failed         |
| ParsingException       | 422 UNPROCESSABLE_ENTITY  | Document Parsing Failed       |
| EmbeddingException     | 502 BAD_GATEWAY           | Embedding Service Unavailable |
| RetrievalException     | 500 INTERNAL_SERVER_ERROR | Retrieval Failed              |

- [ ] Criar `exception/RetrievalException.java`
- [ ] Garantir que PipelineJob registre errorMessage quando qualquer etapa falhar

**Arquivos:** `exception/RetrievalException.java`, `exception/GlobalExceptionHandler.java`

---

### Etapa 9.14 — Integração com MessageServiceImpl

**Objetivo:** Conectar o pipeline RAG ao fluxo de mensagens existente.

**Tarefas:**

- [x] Atualizar `MessageServiceImpl.send()` para:
  - Detectar se a sessão tem documentos indexados
  - Se sim, usar RagPipeline em vez de MockMessageProcessingStrategy
  - Se não, manter comportamento atual (mock)
- [x] Salvar SourceReference no banco junto com a mensagem ASSISTANT
- [x] Incluir metadados das fontes no campo `metadata` da Message

**Arquivos:** `service/impl/MessageServiceImpl.java`

---

### Etapa 9.15 — Repositories do Pipeline

**Objetivo:** Criar repositórios JPA para as novas entidades.

**Tarefas:**

- [x] Criar `repository/DocumentChunkRepository.java`:
  - `List<DocumentChunk> findByDocumentId(Long documentId)`
  - `void deleteByDocumentId(Long documentId)`
- [x] Criar `repository/SourceReferenceRepository.java`:
  - `List<SourceReference> findByMessageId(Long messageId)`
- [x] Criar `repository/PipelineJobRepository.java`:
  - `Optional<PipelineJob> findByDocumentId(Long documentId)`
  - `List<PipelineJob> findByStatus(PipelineStatus status)`

**Arquivos:** `repository/DocumentChunkRepository.java`, `repository/SourceReferenceRepository.java`, `repository/PipelineJobRepository.java`

---

### Etapa 9.16 — Atualização dos Spec-drivens

**Objetivo:** Manter a documentação sincronizada com a implementação.

**Tarefas:**

- [ ] Atualizar `spec-dominio.md` — adicionar entidades DocumentChunk, SourceReference, PipelineJob + enums PipelineStatus
- [ ] Atualizar `spec-api.md` — adicionar endpoints RAG, DTOs, webhooks, códigos de erro 502
- [ ] Atualizar `spec-casos-de-uso.md` — adicionar fluxos: Ingestão de Documento, Query RAG, Webhook n8n
- [ ] Atualizar `spec-arquitetura.md` — adicionar pacotes: parser/, chunking/, embedding/, retrieval/, pipeline/, integration/, webhook/
- [ ] Atualizar `spec-nao-funcionais.md` — adicionar requisitos de performance (cosine similarity, cache embedding, timeout Ollama)
- [ ] Atualizar `spec-diretrizes.md` — adicionar regras de isolamento do pipeline RAG

**Arquivos:** `docs/spec-dominio.md`, `docs/spec-api.md`, `docs/spec-casos-de-uso.md`, `docs/spec-arquitetura.md`, `docs/spec-nao-funcionais.md`, `docs/spec-diretrizes.md`

---

### 🧭 Roadmap Visual

```
Etapa 9.1  ─── Docker Compose (Ollama)
     │
Etapa 9.2  ─── Migrations V5-V7
     │
Etapa 9.3  ─── Entidades JPA (DocumentChunk, SourceReference, PipelineJob)
     │
     ├── Etapa 9.4  ─── VectorStore (interface + Mock)
     ├── Etapa 9.5  ─── Parser Strategy (PDF + TXT)
     ├── Etapa 9.6  ─── Chunking Strategy (FixedSize)
     ├── Etapa 9.7  ─── Embedding Strategy (Ollama)
     │
     ├── Etapa 9.8  ─── Retriever
     │
     ├── Etapa 9.9  ─── RagPipeline Orchestrator
     ├── Etapa 9.10 ─── RagIngestionService (async)
     │
     ├── Etapa 9.11 ─── Controllers + DTOs RAG
     ├── Etapa 9.12 ─── n8n Webhooks
     ├── Etapa 9.13 ─── Error Handling
     │
     ├── Etapa 9.14 ─── Integração MessageServiceImpl
     ├── Etapa 9.15 ─── Repositories
     │
     └── Etapa 9.16 ─── Atualização Spec-drivens
```

---

> **Nota:** Cada etapa é autocontida e segue o princípio SDD de isolamento. Nenhuma etapa mistura responsabilidades de camadas diferentes. O código gerado deve respeitar estritamente os limites definidos nos spec-drivens.
