# Especificação de Requisitos Não Funcionais

---

## 1. Escalabilidade

- Camada de serviço stateless para múltiplas instâncias horizontais.
- Paginação obrigatória em consultas de histórico.
- Redis, RabbitMQ/Kafka preparados atrás de interfaces (Strategy Pattern + Event Pattern).
- FileStorageService abstrai storage local/cloud.
- Banco PostgreSQL remoto (Render.com) com pool de conexões.

---

## 2. Extensibilidade

- Novos provedores de IA via implementação de MessageProcessingStrategy.
- Novos storages implementando FileStorageService.
- Eventos de domínio (MessageSentEvent) permitem novos consumers sem modificar a lógica de negócio.
- Mappers MapStruct permitem novos campos sem quebrar contratos existentes.
- Profiles Spring (dev/prod) para configurações específicas de ambiente.

---

## 3. Manutenibilidade

- Cobertura de testes mínima de 80% (estado atual: 0% — pendente).
- Nomenclatura consistente em todo o projeto (ver spec-diretrizes.md).
- Código em inglês; comentários só para contexto de domínio.
- Injeção de dependências via construtor.
- Interfaces de serviço permitem troca de implementações.
- Validação em duas camadas: DTO (Jakarta) + Service (regras).

---

## 4. Observabilidade

- Logs estruturados via SLF4J/Logback (preparação para JSON logging).
- Métricas preparadas via Spring Actuator + Micrometer.
- Health check por dependência individual (aplicação + banco).
- Tracing preparado para OpenTelemetry.
- Versão da aplicação exposta via variável de ambiente APP_VERSION.

---

## 5. Segurança

- Validação em duas camadas: DTO (Jakarta Validation) + Service (regras de negócio).
- Upload: validação de extensão de arquivo (futuro: magic bytes).
- JWT preparado desde o início (SecurityFilter + JwtTokenProvider — pendente de implementação final).
- Senhas armazenadas com BCrypt.
- CORS configurado para permitir requisições de qualquer origem (ambiente dev).
- CSRF desabilitado (API stateless com JWT).
- Segurança por endpoint via SecurityConfig (em implementação).

---

## 6. Testabilidade

| Camada | Abordagem | Framework |
|---|---|---|
| Controllers | @WebMvcTest com MockMvc | JUnit 5 + Mockito |
| Services | @ExtendWith(MockitoExtension.class) com mocks | Mockito |
| Repositories | @DataJpaTest com banco embutido | JUnit 5 |
| Integração | @SpringBootTest | JUnit 5 + Spring Test |

**Estado atual:** Todos os testes são skeletons vazios — 0% de cobertura.

---

## 7. Performance

- Índices em todas as FKs e colunas de ordenação (created_at).
- Paginação obrigatória para coleções.
- Lazy loading em relacionamentos (@ManyToOne, @OneToMany).
- Limite de 10MB para upload de arquivos.
- Timeout de 3s para health check de banco.
- Limite de 5000 caracteres por mensagem.

---

## 8. Portabilidade

- PostgreSQL como banco principal (compatível com Render.com e local).
- Docker-ready (preparação para containerização).
- Profiles de configuração: dev (localhost), prod (Render.com).
- spring-dotenv para variáveis de ambiente em desenvolvimento.
- Flyway garante consistência do schema entre ambientes.

---

## 9. Requisitos do Pipeline RAG

### 9.1 Performance da Busca Vetorial

- Similaridade por cosseno implementada em Java (O(n) para busca linear) — MVP.
- Limite de chunks por query: topK default 5, configurável via `app.rag.retrieval.top-k`.
- Score mínimo de relevância: 0.7 (configurável via `app.rag.retrieval.min-score`).
- Preparação para pgvector (PostgreSQL): armazenar embedding como DOUBLE PRECISION[] para migração futura.

### 9.2 Embedding (Ollama)

- Timeout de conexão: 5s (`app.rag.ollama.timeout`).
- Modelo padrão: `nomic-embed-text` (768 dimensões, 137M parâmetros).
- Cache de embeddings por texto (evitar reprocessamento do mesmo chunk).
- Fallback para `MockEmbeddingStrategy` se Ollama estiver indisponível (modo dev).
- Health check do Ollama via endpoint `/api/tags`.

### 9.3 Chunking

- Tamanho máximo do chunk: 512 caracteres (configurável).
- Overlap entre chunks: 64 caracteres (configurável).
- Estratégia inicial: `FixedSizeChunker` (divisão por tamanho fixo).
- Estratégia futura: `RecursiveChunker` (divisão por parágrafos → frases).

### 9.4 Ingestão Assíncrona

- Processamento de ingestão via `@Async` (Spring TaskExecutor).
- PipelineJob rastreia estado de cada etapa (QUEUED → PARSING → CHUNKING → EMBEDDING → READY/FAILED).
- Evento `DocumentIngestedEvent` publicado ao finalizar.
- Consulta de status via `GET /api/v1/rag/ingest/{jobId}/status`.

### 9.5 Rastreamento de Fontes

- Toda resposta RAG armazena `SourceReference` no banco (messageId + chunkId + score + excerpt).
- Consulta de fontes via `GET /api/v1/rag/sources/{messageId}`.
- Fontes expostas no response de query para renderização no frontend.

### 9.6 n8n Integration

- Webhook URL configurável via `app.n8n.webhook-url`.
- Desligável via `app.n8n.enabled=false` (default).
- Payload tipado via `N8nWebhookPayload` DTO.
- Webhook de retorno: `POST /api/v1/webhooks/n8n/rag-response`.

### 9.7 Resiliência

- Todas as chamadas ao Ollama com timeout e retry (1 tentativa adicional).
- Falha de embedding não quebra o fluxo de mensagens — fallback para resposta mock.
- PipelineJob registra erro detalhado em caso de falha.
- Retry de ingestão: novo job é criado ao reprocessar documento.
