# Documento de Especificação do Sistema — Plataforma Conversacional

**Visão Geral da Arquitetura**

---

## 1. Visão Geral da Arquitetura

A arquitetura adotada é uma **variante estrita do MVC (Model-View-Controller) implementada sobre Spring Boot**, com camadas adicionais de isolamento inspiradas nos princípios da **Clean Architecture** e **Hexagonal Architecture**.

### Stack Tecnológica

| Componente | Versão / Tecnologia |
|---|---|
| Linguagem | Java 17 LTS |
| Framework | Spring Boot 3.2.0 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de Dados | PostgreSQL 15+ |
| Build | Maven 3.9+ |
| Migrations | Flyway |
| Mapeamento DTO/Entity | MapStruct 1.5.5.Final |
| Validação | Jakarta Validation + Hibernate Validator |
| Documentação API | SpringDoc OpenAPI 2.3.0 |
| Segurança | Spring Security + JWT (em implementação) |
| Logs | Logback (SLF4J) |
| Testes | JUnit 5 + Mockito |
| Storage | Local filesystem (extensível) |
| Gerenciamento de Env | spring-dotenv 4.0.0 |

### Camadas e Comunicação

```
[HTTP] → Controller → DTO → Service (interface) → ServiceImpl → Repository → Entity → PostgreSQL
                              ↕                              ↕
                           Mapper                        Mapper (inverso)
```

### Princípios Arquiteturais

#### SOLID — Aplicação Concreta

| Princípio | Aplicação no Projeto |
|---|---|
| **S** — Single Responsibility | Cada classe possui exatamente um motivo para mudar. Controller só roteia. Service só orquestra regras. Repository só persiste. |
| **O** — Open/Closed | Services são abertos para extensão (novas impls) e fechados para modificação via interfaces. Estratégias de IA seguem o mesmo padrão. |
| **L** — Liskov Substitution | Qualquer implementação de MessageService pode substituir a interface sem quebrar o consumidor (Controller). |
| **I** — Interface Segregation | Interfaces de serviço são coesas e específicas. Não existe um CrudService genérico. Cada domínio tem seu contrato. |
| **D** — Dependency Inversion | Controller depende de abstrações (MessageService), não de implementações concretas (MessageServiceImpl). Repositories idem. |

### Justificativas Técnicas

| Decisão | Justificativa |
|---|---|
| MVC estrito | Alinhamento com o ecossistema Spring Boot, facilitando adoção pela equipe e aproveitando convenções do framework |
| Services como interface + impl | Permite polimorfismo, testes com mocks e preparação para múltiplas estratégias (ex: IA mock → LLM real) |
| DTOs como única via de comunicação externa | Isola o domínio de contratos HTTP, permitindo que serviços sejam reutilizados em outros contextos (fila, websocket, etc.) |
| Mapper explícito (MapStruct) | Evita vazamento de Entity para camadas superiores e vice-versa |
| Strategy Pattern para mensagens | Prepara o terreno para diferentes provedores de IA sem modificar o fluxo principal |
| Event Publisher para mensagens | Prepara integração futura com RabbitMQ/Kafka sem modificar a lógica de negócio |

### Fluxo Arquitetural Genérico

```
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
PostgreSQL
```

### Clean Architecture — Adaptação

- **Entidades** (entity/) são o núcleo: contêm apenas atributos, relacionamentos JPA e anotações de mapeamento. Sem regras de negócio.
- **Casos de Uso** (service/) orquestram o fluxo e contêm regras. Dependem de abstrações de repositório, nunca de detalhes de infraestrutura.
- **Adaptadores** (controller/, repository/, storage/) traduzem entre o mundo externo e o domínio.
- **Frameworks** (Spring, PostgreSQL) estão na camada mais externa.

### Spec-Driven Development (SDD)

Este documento e seus complementos (`spec-*.md`) constituem a **fonte única de verdade**. Todo código gerado — seja por equipe humana ou por IA — deve derivar exclusivamente das especificações aqui contidas. Nenhuma decisão de implementação deve contrariar os contratos, responsabilidades e restrições definidos a seguir.

---

*Este documento constitui a especificação arquitetural de referência para o desenvolvimento da Plataforma Conversacional. Consulte os arquivos complementares `spec-arquitetura.md`, `spec-dominio.md`, `spec-api.md`, `spec-casos-de-uso.md`, `spec-diretrizes.md` e `spec-nao-funcionais.md` para detalhes específicos de cada área.*
