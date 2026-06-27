# Especificação de Diretrizes — Práticas e Convenções

---

## 1. Práticas Obrigatórias

1. **Todo Service deve ter uma interface.** Nenhuma injeção em concreta.
2. **Todo Controller deve receber DTO e retornar DTO.** Proibido expor Entity.
3. **Todo DTO de request deve usar Jakarta Validation** com @Valid no Controller.
4. **Toda exceção de negócio** como BusinessException tratada pelo GlobalExceptionHandler.
5. **Toda escrita deve ser @Transactional.**
6. **Toda consulta de coleção deve ser paginada** (Pageable).
7. **Toda injeção via construtor.** Proibido @Autowired em campos.
8. **Todo mapper explícito (MapStruct).** Proibido mapeamento manual em Service.
9. **Migrações Flyway imutáveis.** Novas alterações = novas migrações.
10. **Configurações externalizadas** em application-<profile>.yml.
11. **Variáveis de ambiente** com fallback via spring-dotenv.
12. **Upload de arquivos** deve validar extensão e, futuramente, magic bytes.

---

## 2. Práticas Proibidas

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
13. Lógica de negócio em construtores ou @PostConstruct.
14. Acesso direto a banco em Controllers.

---

## 3. Convenções de Nomenclatura

| Elemento | Convenção | Exemplo |
|---|---|---|
| Controller | <Entidade>Controller | MessageController |
| Service (interface) | <Entidade>Service | MessageService |
| Service (impl) | <Entidade>ServiceImpl | MessageServiceImpl |
| Repository | <Entidade>Repository | MessageRepository |
| Entity | <Entidade> | Message |
| Request DTO | <Acao>Request | SendMessageRequest |
| Response DTO | <Entidade>Response | MessageResponse |
| Internal DTO | <Entidade>Data | FileUploadData |
| Mapper | <Entidade>Mapper | MessageMapper |
| Exception | <Descricao>Exception | ResourceNotFoundException |
| Enum | <Descricao> | MessageRole |
| Config | <Descricao>Config | SecurityConfig |
| Constante | UPPER_SNAKE_CASE | API_VERSION |
| Migration | V<numero>__<descricao>.sql | V1__create_session_table.sql |

---

## 4. Limites de Código

| Métrica | Limite |
|---|---|
| Linhas por classe | 200 |
| Linhas por método | 30 |
| Parâmetros por método | 5 |
| Complexidade ciclomática por método | 10 |
| Profundidade de aninhamento | 3 níveis |
| Tamanho de arquivo SQL de migration | 100 linhas |
| Tamanho máximo de upload | 10 MB |
| Timeout de health check DB | 3 segundos |

---

## 5. Convenções Gerais

- **Código em inglês**: classes, métodos, variáveis, constantes.
- **Comentários**: apenas para contexto de domínio quando absolutamente necessário.
- **DTOs como records**: preferir Java records para DTOs imutáveis.
- **Tratamento de Optional**: usar `orElseThrow()` com exceção específica, nunca `.get()`.
- **Logs**: usar SLF4J com Logger constante por classe.
- **Retorno de Controller**: sempre ResponseEntity com código HTTP explícito.

---

## 6. Regras de Isolamento do Pipeline RAG

### 6.1 Isolamento por Stage (SDD Rígido)

| Stage | Package | Conhece | NÃO Conhece |
|---|---|---|---|
| Parsing | parser/ | byte[], contentType | Chunker, Embedder, VectorStore, n8n |
| Chunking | chunking/ | String (texto puro) | Parser, Embedder, Repositories |
| Embedding | embedding/ | String → float[] | Parser, Chunker, Controllers |
| Retrieval | retrieval/ | EmbeddingStrategy, VectorStore | Parser, Chunker, HTTP |
| Pipeline | pipeline/ | Retriever, SourceRepository, Strategy | Controllers, Webhooks, n8n |
| Ingestão | service/impl/ | Parser, Chunker, Embedder, VectorStore | Controllers, HTTP |
| Integração | integration/ | DTOs, RestTemplate | Entity, Repository, Domínio |

### 6.2 Práticas Obrigatórias do RAG

1. **Parser não chama Embedder.** Cada stage é uma interface separada.
2. **Pipeline não conhece HTTP.** RagPipeline recebe DTOs de domínio, não Request/Response HTTP.
3. **MockStrategy para fallback.** Se Ollama estiver fora, o sistema não quebra.
4. **Ingestão é assíncrona.** Usar @Async com PipelineJob para rastrear estado.
5. **n8n é externo.** A comunicação é exclusivamente via webhooks com DTOs tipados.
6. **Todo erro de pipeline** deve ser registrado no PipelineJob.errorMessage.

### 6.3 Práticas Proibidas do RAG

1. Parser com lógica de chunking ou embedding.
2. Embedder com acesso a banco de dados ou repositórios.
3. RagPipeline com @RequestMapping ou ResponseEntity.
4. N8nWebhookClient com dependência de Entity ou Repository.
5. VectorStore com regras de negócio do pipeline.
