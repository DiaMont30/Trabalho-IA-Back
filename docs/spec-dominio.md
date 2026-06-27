# Especificação do Domínio — Modelo Conceitual

---

## 1. Entidades e Relacionamentos

```
+------------------------------------------------------------+
|                          SESSION                             |
+------------------------------------------------------------+
| id                  : Long (PK, auto-increment)              |
| title               : String (opcional, max 255)             |
| status              : SessionStatus                         |
| createdAt           : LocalDateTime                          |
| updatedAt           : LocalDateTime                          |
| closedAt            : LocalDateTime (nullable)               |
+------------------------------------------------------------+
| Relacionamentos: 1 --- N → Message, 1 --- N → Document     |
+------------------------------------------------------------+

+------------------------------------------------------------+
|                          MESSAGE                             |
+------------------------------------------------------------+
| id                  : Long (PK, auto-increment)              |
| session             : Session (FK, ManyToOne, LAZY)          |
| content             : String (@Lob TEXT)                     |
| role                : MessageRole                           |
| status              : MessageStatus                         |
| metadata            : String (JSON, opcional)                |
| createdAt           : LocalDateTime                          |
| updatedAt           : LocalDateTime                          |
+------------------------------------------------------------+
| Relacionamentos: N --- 1 → Session                          |
+------------------------------------------------------------+

+------------------------------------------------------------+
|                          DOCUMENT                            |
+------------------------------------------------------------+
| id                  : Long (PK, auto-increment)              |
| session             : Session (FK, ManyToOne, LAZY, nullable)|
| originalName        : String (max 255)                      |
| storageFileName     : String (max 255, único)               |
| storagePath         : String (max 500)                      |
| type                : DocumentType (PDF, TXT)               |
| size                : Long (bytes)                          |
| contentType         : String (MIME type, max 100)           |
| uploadedAt          : LocalDateTime                          |
+------------------------------------------------------------+
| Relacionamentos: N --- 1 → Session (opcional)               |
+------------------------------------------------------------+
```

---

## 2. Cardinalidades

| Origem | Destino | Tipo | Obrigatório | Cascade |
|---|---|---|---|---|
| Session | Message | 1 → N | Sim | ALL (orphanRemoval) |
| Message | Session | N → 1 | Sim | — |
| Session | Document | 1 → N | Não | — |
| Document | Session | N → 1 | Não | — |

---

## 3. Índices

| Nome | Tabela | Coluna(s) | Tipo |
|---|---|---|---|
| idx_message_session_id | messages | session_id | B-tree |
| idx_message_created_at | messages | created_at | B-tree |
| idx_document_session_id | documents | session_id | B-tree |
| uk_document_storage_file_name | documents | storage_file_name | Único |

---

## 4. Enums

### SessionStatus

| Valor | Descrição |
|---|---|
| ACTIVE | Sessão em andamento |
| CLOSED | Sessão encerrada |
| ARCHIVED | Sessão arquivada |

### MessageStatus

| Valor | Descrição |
|---|---|
| SENT | Mensagem enviada (pendente) |
| RECEIVED | Mensagem processada |
| FAILED | Falha no processamento |

### MessageRole

| Valor | Descrição |
|---|---|
| USER | Mensagem do usuário (front-end) |
| ASSISTANT | Resposta do sistema/IA |
| SYSTEM | Evento do sistema |

### DocumentType

| Valor | Descrição |
|---|---|
| PDF | Documento PDF |
| TXT | Documento texto |

---

## 5. Design de Domínio

- **IDs sequenciais (Long)**: simplicidade e performance com PostgreSQL serial.
- **Session como raiz agregada**: toda mensagem pertence a uma sessão.
- **Cascade ALL + orphanRemoval em Session → Message**: mensagens são dependentes da sessão.
- **Document dissociável**: upload antes de vincular à sessão (FK nullable).
- **ON DELETE CASCADE em Message**: ao deletar sessão, mensagens são removidas.
- **ON DELETE SET NULL em Document**: ao deletar sessão, documentos permanecem mas perdem vínculo.
- **role enum**: USER (front-end), ASSISTANT (sistema/IA), SYSTEM (eventos).
- **metadata JSON**: preparação para tokens de LLM, confidence scores.
- **@Lob para content**: suporta mensagens longas (TEXT no PostgreSQL).
- **Timestamps automáticos**: @PrePersist e @PreUpdate em todas as entidades.

---

## 6. Entidades do Pipeline RAG

### DocumentChunk

Tabela: `document_chunks`

| Campo | Tipo | Descrição |
|---|---|---|
| id | Long (PK) | Auto-incremento |
| document | Document (ManyToOne, LAZY) | Documento de origem |
| content | TEXT | Conteúdo textual do chunk |
| chunkIndex | INT | Posição sequencial no documento |
| embedding | TEXT | Array JSON de floats (preparação pgvector) |
| metadata | JSON | Metadados opcionais |
| createdAt | TIMESTAMP | Auto-persist |

**Relacionamentos:** N → 1 Document (ON DELETE CASCADE)

### SourceReference

Tabela: `source_references`

| Campo | Tipo | Descrição |
|---|---|---|
| id | Long (PK) | Auto-incremento |
| message | Message (ManyToOne, LAZY) | Mensagem ASSISTANT que usou a fonte |
| chunk | DocumentChunk (ManyToOne, LAZY) | Chunk referenciado |
| relevanceScore | DOUBLE | Score de similaridade (0.0 a 1.0) |
| excerpt | VARCHAR(500) | Excerto do chunk usado na resposta |
| createdAt | TIMESTAMP | Auto-persist |

**Relacionamentos:** N → 1 Message (ON DELETE CASCADE), N → 1 DocumentChunk (ON DELETE CASCADE)

### PipelineJob

Tabela: `pipeline_jobs`

| Campo | Tipo | Descrição |
|---|---|---|
| id | Long (PK) | Auto-incremento |
| document | Document (ManyToOne, LAZY) | Documento sendo processado |
| status | PipelineStatus | Estado atual do pipeline |
| chunksCount | INT | Total de chunks gerados |
| errorMessage | TEXT | Mensagem de erro se FAILED |
| createdAt | TIMESTAMP | Auto-persist |
| completedAt | TIMESTAMP | Data de conclusão |

**Relacionamentos:** N → 1 Document (ON DELETE CASCADE)

### Índices

| Nome | Tabela | Coluna(s) | Tipo |
|---|---|---|---|
| idx_chunks_document_id | document_chunks | document_id | B-tree |
| idx_sources_message_id | source_references | message_id | B-tree |
| idx_jobs_document_id | pipeline_jobs | document_id | B-tree |
| idx_jobs_status | pipeline_jobs | status | B-tree |

---

## 7. Enums do Pipeline RAG

### PipelineStatus

| Valor | Descrição |
|---|---|
| QUEUED | Aguardando processamento |
| PARSING | Extraindo texto do documento |
| CHUNKING | Dividindo texto em fragmentos |
| EMBEDDING | Gerando embeddings |
| READY | Pipeline concluído |
| FAILED | Falha no processamento |

---

## 8. DTOs Internos do Pipeline

| DTO | Pacote | Campos |
|---|---|---|
| Chunk | chunking/ | content (String), index (int) |
| ScoredChunk | retrieval/ | chunk (DocumentChunk), score (double) |
| RagResult | pipeline/ | answer (String), sources (List\<SourceDetail\>) |
| SourceDetail | pipeline/ | chunkId, documentId, documentName, excerpt, relevanceScore |
