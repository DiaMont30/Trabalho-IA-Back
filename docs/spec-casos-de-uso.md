# Especificação de Casos de Uso — Fluxos Completos

---

## 1. Enviar Mensagem

**Endpoint:** `POST /api/v1/sessions/{sessionId}/messages`

```
[Front-end]
    │ POST /api/v1/sessions/{sessionId}/messages
    │ Content-Type: application/json
    │ Body: {"content": "Olá, assistente!"}
    ▼
[MessageController]
    │ 1. Valida @Valid no SendMessageRequest
    │ 2. Extrai sessionId do PathVariable
    │ 3. Converte sessionId de String para Long
    │ 4. Chama messageService.sendMessage(sessionId, request)
    ▼
[MessageServiceImpl]
    │ 1. Busca Session por ID (lança ResourceNotFoundException se não existir)
    │ 2. Cria entidade Message com role=USER, status=SENT
    │ 3. Salva Message no banco via MessageRepository
    │ 4. Aplica estratégia de processamento (MessageProcessingStrategy)
    │    - Atual: MockMessageProcessingStrategy
    │    - Futuro: LlamaStrategy, OpenAiStrategy
    │ 5. Cria entidade Message com role=ASSISTANT, status=RECEIVED
    │ 6. Salva resposta no banco
    │ 7. Publica MessageSentEvent (preparação para RabbitMQ/Kafka)
    │ 8. Converte para MessageResponse via MessageMapper
    ▼
[MessageRepository] → [PostgreSQL]
    ▼
[MessageController]
    │ Retorna ResponseEntity.created(messageResponse)
    ▼
[Front-end] ← 201 Created
```

---

## 2. Recuperar Histórico

**Endpoint:** `GET /api/v1/sessions/{sessionId}/messages?page=0&size=20`

```
[Front-end]
    │ GET /api/v1/sessions/{sessionId}/messages?page=0&size=20
    ▼
[MessageController]
    │ 1. Extrai sessionId, page, size
    │ 2. Chama messageService.getHistory(sessionId, page, size)
    ▼
[MessageServiceImpl]
    │ 1. Verifica existência da Session (lança 404 se não existir)
    │ 2. Busca mensagens paginadas via MessageRepository
    │    - findBysessionIdOrderByCreatedAt(sessionId, pageable)
    │ 3. Converte para SessionHistoryResponse via MessageMapper
    ▼
[MessageRepository] → [PostgreSQL]
    ▼
[MessageController]
    │ Retorna ResponseEntity.ok(historyResponse)
    ▼
[Front-end] ← 200 OK
```

---

## 3. Criar Sessão

**Endpoint:** `POST /api/v1/sessions`

```
[Front-end]
    │ POST /api/v1/sessions
    ▼
[SessionController]
    │ 1. Chama sessionService.create()
    ▼
[SessionServiceImpl]
    │ 1. Cria entidade Session com:
    │    - title = "Nova conversa"
    │    - status = ACTIVE
    │ 2. Salva via SessionRepository
    │ 3. Converte para SessionResponse via SessionMapper
    ▼
[SessionRepository] → [PostgreSQL]
    ▼
[SessionController]
    │ Retorna ResponseEntity.created(sessionResponse)
    ▼
[Front-end] ← 201 Created
```

---

## 4. Buscar Sessão por ID

**Endpoint:** `GET /api/v1/sessions/{sessionId}`

```
[Front-end]
    │ GET /api/v1/sessions/{sessionId}
    ▼
[SessionController]
    │ 1. Extrai sessionId
    │ 2. Chama sessionService.findById(sessionId)
    ▼
[SessionServiceImpl]
    │ 1. Busca Session por ID (lança 404 se não existir)
    │ 2. Converte para SessionResponse via SessionMapper
    ▼
[SessionRepository] → [PostgreSQL]
    ▼
[SessionController]
    │ Retorna ResponseEntity.ok(sessionResponse)
    ▼
[Front-end] ← 200 OK
```

---

## 5. Listar Todas as Sessões

**Endpoint:** `GET /api/v1/sessions`

```
[Front-end]
    │ GET /api/v1/sessions
    ▼
[SessionController]
    │ 1. Chama sessionService.findAll()
    ▼
[SessionServiceImpl]
    │ 1. Busca todas as Sessions via SessionRepository
    │    (sem paginação — pendente de implementação)
    │ 2. Converte para List<SessionResponse> via SessionMapper
    ▼
[SessionRepository] → [PostgreSQL]
    ▼
[SessionController]
    │ Retorna ResponseEntity.ok(sessionsResponse)
    ▼
[Front-end] ← 200 OK
```

---

## 6. Upload de Documento

**Endpoint:** `POST /api/v1/documents/upload`

```
[Front-end]
    │ POST /api/v1/documents/upload
    │ Content-Type: multipart/form-data
    │ file: [arquivo PDF/TXT]
    │ sessionId: (opcional)
    ▼
[DocumentController]
    │ 1. Recebe MultipartFile + sessionId (opcional)
    │ 2. Monta FileUploadData (DTO interno)
    │ 3. Chama documentService.store(fileUploadData)
    ▼
[DocumentServiceImpl]
    │ 1. Valida tipo do arquivo via FileUtils (extensão)
    │ 2. Gera nome único via FileUtils.generateUniqueFileName()
    │ 3. Chama fileStorageService.store(file, uniqueName)
    │ 4. Se sessionId presente, busca Session (lança 404 se inválida)
    │ 5. Cria entidade Document com metadados
    │ 6. Salva via DocumentRepository
    │ 7. Converte para DocumentResponse via DocumentMapper
    ▼
[FileStorageService] → [LocalFileStorageService] → [filesystem]
[DocumentRepository] → [PostgreSQL]
    ▼
[DocumentController]
    │ Retorna ResponseEntity.created().body(documentResponse)
    ▼
[Front-end] ← 201 Created
```

---

## 7. Buscar Documento por ID

**Endpoint:** `GET /api/v1/documents/{documentId}`

```
[Front-end]
    │ GET /api/v1/documents/{documentId}
    ▼
[DocumentController]
    │ 1. Extrai documentId
    │ 2. Chama documentService.findById(documentId)
    ▼
[DocumentServiceImpl]
    │ 1. Busca Document por ID (lança 404 se não existir)
    │ 2. Converte para DocumentResponse via DocumentMapper
    ▼
[DocumentRepository] → [PostgreSQL]
    ▼
[DocumentController]
    │ Retorna ResponseEntity.ok(documentResponse)
    ▼
[Front-end] ← 200 OK
```

---

## 8. Health Check

**Endpoint:** `GET /api/v1/health`

```
[Cliente/Probe]
    │ GET /api/v1/health
    ▼
[HealthController]
    │ 1. Chama healthService.check()
    ▼
[HealthServiceImpl]
    │ 1. Agrega status dos HealthIndicators:
    │    - ApplicationHealthIndicator (sempre UP)
    │    - DatabaseHealthIndicator (testa conexão, timeout 3s)
    │ 2. Define status geral:
    │    - UP se todos UP
    │    - DEGRADED se alguma dependência DOWN
    │    - DOWN se todas DOWN
    │ 3. Retorna HealthResponse com version do APP_VERSION
    ▼
[HealthIndicators (Spring Actuator)]
    ▼
[HealthController]
    │ Retorna ResponseEntity.ok(healthResponse)
    ▼
[Cliente] ← 200 OK
```

---

## 9. Query RAG

**Endpoint:** `POST /api/v1/rag/query`

```
[Front-end / Cliente]
    │ POST /api/v1/rag/query
    │ Content-Type: application/json
    │ Body: {"query": "Qual o prazo de entrega?", "sessionId": 1}
    ▼
[RagController]
    │ 1. Valida @Valid no RagQueryRequest
    │ 2. Chama ragPipeline.execute(query, sessionId)
    ▼
[DefaultRagPipeline]
    │ 1. Chama retriever.retrieve(query, topK=5)
    │    ├── SimilarityRetriever
    │    │   ├── embeddingStrategy.embed(query) → float[]
    │    │   └── vectorStore.searchSimilar(embedding, 5, 0.7) → List<ScoredChunk>
    │ 2. Concatena chunks em contexto string
    │ 3. Chama processingStrategy.process(context + query)
    │ 4. Cria SourceReference para cada chunk usado
    │ 5. Retorna RagResult (answer + sources)
    ▼
[VectorStore] → [MockVectorStore / PostgreSQL]
[EmbeddingStrategy] → [Ollama (Docker)]
    ▼
[RagController]
    │ Monta RagQueryResponse com answer + SourceDetail[]
    │ Retorna ResponseEntity.ok(response)
    ▼
[Front-end] ← 200 OK
    │ Response contém fontes para renderização
```

---

## 10. Ingestão de Documento

**Endpoint:** `POST /api/v1/rag/ingest/{documentId}`

```
[Cliente / Webhook]
    │ POST /api/v1/rag/ingest/1
    ▼
[RagController]
    │ 1. Chama ragIngestionService.ingestDocument(documentId)
    ▼
[RagIngestionServiceImpl (async)]
    │ 1. Busca Document por ID (404 se não existir)
    │ 2. Lê conteúdo do arquivo via FileStorageService
    │ 3. Cria PipelineJob com status = QUEUED
    │ 4. Atualiza status → PARSING
    │ 5. Extrai texto via DocumentParser (PDF ou TXT)
    │ 6. Atualiza status → CHUNKING
    │ 7. Divide texto via ChunkingStrategy (FixedSize)
    │ 8. Atualiza status → EMBEDDING
    │ 9. Gera embeddings via EmbeddingStrategy (Ollama)
    │ 10. Armazena chunks + embeddings via VectorStore
    │ 11. Atualiza status → READY
    │ 12. Publica DocumentIngestedEvent
    │ 13. Retorna IngestionStatusResponse
    ▼
[RagController]
    │ Retorna ResponseEntity.created(ingestionStatus)
    ▼
[Cliente] ← 201 Created
```

### Fluxo de Erro na Ingestão

```
[RagIngestionServiceImpl]
    │ Se parsing falha → status = FAILED, errorMessage = detalhes
    │ Se chunking falha → status = FAILED, errorMessage = detalhes
    │ Se embedding falha → status = FAILED, errorMessage = detalhes
    ▼
[PipelineJob] registra erro no banco
    ▼
[Cliente] pode consultar GET /rag/ingest/{jobId}/status
```

---

## 11. Webhook n8n (Resposta Assíncrona)

**Endpoint:** `POST /api/v1/webhooks/n8n/rag-response`

```
[n8n (externo)]
    │ POST /api/v1/webhooks/n8n/rag-response
    │ Body: { "eventType": "RAG_RESPONSE", "payload": {...} }
    ▼
[WebhookController]
    │ 1. Recebe N8nWebhookPayload
    │ 2. Processa resposta assíncrona
    │ 3. Publica evento interno de resposta recebida
    ▼
[WebhookController]
    │ Retorna ResponseEntity.ok()
    ▼
[n8n] ← 200 OK
```
