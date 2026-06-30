# Especificação da API — Contratos REST

---

Base URL: `/api/v1`

## 1. Sumário de Endpoints

### 1.1 Sessões

| Método | Endpoint | Request | Response | Erros |
|---|---|---|---|---|
| POST | `/sessions` | — | SessionResponse | 500 |
| GET | `/sessions/{sessionId}` | — | SessionResponse | 404 |
| GET | `/sessions` | — | List<SessionResponse> | — |

### 1.2 Mensageria

| Método | Endpoint | Request | Response | Erros |
|---|---|---|---|---|
| POST | `/sessions/{sessionId}/messages` | SendMessageRequest (JSON) | MessageResponse | 404, 400, 500 |
| GET | `/sessions/{sessionId}/messages?page=0&size=20` | — | SessionHistoryResponse | 404, 400 |

### 1.3 Documentos

| Método | Endpoint | Request | Response | Erros |
|---|---|---|---|---|
| POST | `/documents/upload` | Multipart (file + sessionId opcional) | DocumentResponse | 400, 413, 500 |
| GET | `/documents/{documentId}` | — | DocumentResponse | 404 |
| GET | `/documents/{documentId}/status` | — | DocumentStatusResponse | 404 |

### 1.4 Health Check

| Método | Endpoint | Request | Response | Erros |
|---|---|---|---|---|
| GET | `/health` | — | HealthResponse | 503 |

---

## 2. DTOs de Requisição

### SendMessageRequest

```json
{
  "content": "string (obrigatório, 1-5000 caracteres)"
}
```

| Campo | Tipo | Validação |
|---|---|---|
| content | String | @NotBlank, @Size(min=1, max=5000) |

### UploadDocumentRequest

| Campo | Tipo | Validação |
|---|---|---|
| file | MultipartFile | @NotNull, tipos permitidos: PDF, TXT |
| sessionId | Long (opcional) | — |

---

## 3. DTOs de Resposta

### SessionResponse

```json
{
  "id": 1,
  "title": "Nova conversa",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### MessageResponse

```json
{
  "id": 1,
  "sessionId": 1,
  "content": "string",
  "role": "USER",
  "status": "SENT",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00",
  "metadata": null,
  "sources": null
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| id | Long | ID da mensagem |
| sessionId | Long | ID da sessão |
| content | String | Conteúdo da mensagem |
| role | String | USER, ASSISTANT ou SYSTEM |
| status | String | SENT, RECEIVED ou FAILED |
| createdAt | String | ISO-8601 |
| updatedAt | String | ISO-8601 |
| metadata | String (JSON) | Metadados opcionais (fontes em formato JSON quando RAG) |
| sources | List\<SourceDetailResponse\> | Fontes utilizadas na resposta RAG (null quando não RAG) |

### SessionHistoryResponse

```json
{
  "sessionId": 1,
  "messages": [MessageResponse],
  "page": 0,
  "totalPages": 1,
  "totalElements": 10,
  "hasNext": false
}
```

### DocumentResponse

```json
{
  "id": 1,
  "fileName": "uuid-nome-arquivo.pdf",
  "originalName": "documento.pdf",
  "type": "PDF",
  "size": 1024000,
  "storagePath": "./uploads/uuid-nome-arquivo.pdf",
  "sessionId": null,
  "uploadedAt": "2024-01-01T00:00:00"
}
```

### HealthResponse

```json
{
  "status": "UP",
  "database": "UP",
  "timestamp": "2024-01-01T00:00:00",
  "version": "0.0.1-SNAPSHOT"
}
```

### DocumentStatusResponse

```json
{
  "documentId": 1,
  "status": "READY",
  "chunksCount": 15,
  "errorMessage": null
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| documentId | Long | ID do documento |
| status | String | PENDING, PROCESSING, READY ou FAILED |
| chunksCount | Integer | Quantidade de chunks indexados (null se não processado) |
| errorMessage | String | Mensagem de erro (null se bem-sucedido) |

### ErrorResponse

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Session not found with id: 99",
  "timestamp": "2024-01-01T00:00:00",
  "path": "/api/v1/sessions/99",
  "validationErrors": null
}
```

---

## 4. Códigos de Erro

| HTTP Status | Quando ocorre | Exceção |
|---|---|---|
| 400 BAD_REQUEST | Validação de DTO ou tipo de arquivo inválido | MethodArgumentNotValidException, InvalidFileTypeException |
| 404 NOT_FOUND | Recurso não encontrado | ResourceNotFoundException |
| 409 CONFLICT | Conflito de estado (documento já indexado, sessão encerrada) | ConflictException |
| 422 UNPROCESSABLE_ENTITY | Regra de negócio violada | BusinessException |
| 500 INTERNAL_SERVER_ERROR | Erro inesperado | Exception genérica |
| 503 SERVICE_UNAVAILABLE | Health check com dependência crítica DOWN | — |

---

## 5. Contratos de Resposta para Erro

Toda exceção retorna `ErrorResponse` com:
- `status`: código HTTP
- `error`: descrição curta do tipo de erro
- `message`: mensagem legível
- `timestamp`: ISO-8601
- `path`: endpoint que gerou o erro
- `validationErrors`: lista de erros de validação (null quando não aplicável)

---

## 6. Endpoints do Pipeline RAG

### 6.1 Query RAG

| Método | Endpoint | Request | Response | Erros |
|---|---|---|---|---|
| POST | `/rag/query` | RagQueryRequest (JSON) | RagQueryResponse | 400, 404, 422 |

### 6.2 Ingestão de Documento

| Método | Endpoint | Request | Response | Erros |
|---|---|---|---|---|
| POST | `/rag/ingest/{documentId}` | — | IngestionStatusResponse | 400, 404 |
| GET | `/rag/ingest/{jobId}/status` | — | IngestionStatusResponse | 404 |

### 6.3 Fontes de Resposta

| Método | Endpoint | Request | Response | Erros |
|---|---|---|---|---|
| GET | `/rag/sources/{messageId}` | — | List\<SourceDetailResponse\> | 404 |

### 6.4 Webhooks n8n

| Método | Endpoint | Request | Response | Erros |
|---|---|---|---|---|
| POST | `/webhooks/n8n/rag-response` | N8nWebhookPayload | 200 OK | 400 |

---

## 7. DTOs da API RAG

### RagQueryRequest

```json
{
  "query": "string (obrigatório, 1-5000 caracteres)",
  "sessionId": 1
}
```

| Campo | Tipo | Validação |
|---|---|---|
| query | String | @NotBlank, @Size(min=1, max=5000) |
| sessionId | Long | @NotNull |

### RagQueryResponse

```json
{
  "answer": "Resposta gerada com base nos documentos...",
  "sources": [
    {
      "documentId": 1,
      "documentName": "contrato.pdf",
      "excerpt": "O prazo de entrega é de 30 dias úteis...",
      "relevanceScore": 0.95
    }
  ]
}
```

### SourceDetailResponse

```json
{
  "documentId": 1,
  "documentName": "contrato.pdf",
  "excerpt": "O prazo de entrega é de 30 dias úteis...",
  "relevanceScore": 0.95
}
```

### IngestionStatusResponse

```json
{
  "jobId": 1,
  "documentId": 1,
  "status": "READY",
  "chunksCount": 15,
  "errorMessage": null
}
```

### Códigos de Erro Adicionais

| HTTP Status | Quando ocorre | Exceção |
|---|---|---|
| 422 UNPROCESSABLE_ENTITY | Falha no pipeline RAG (parsing, chunking) | RagProcessingException, ParsingException |
| 502 BAD_GATEWAY | Serviço de embedding indisponível (Ollama) | EmbeddingException |
| 500 INTERNAL_SERVER_ERROR | Falha na recuperação de chunks | RetrievalException |
