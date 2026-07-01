# Projeto IA – Back-end

## Grupo 4

> **Serratec Residência de Software · Sala 34 · Trabalho avaliativo · disciplina de Inteligência Artificial**

---

## Integrantes

- DIANA MONTEIRO
- GABRIEL AGUIAR
- KAIQUE ABRANCHES
- ROBERTA ROCHA

---

## Back-end

O repositório do back-end está disponível em:

```
https://github.com/DiaMont30/Trabalho-IA-Back.git
```

### Estrutura do Back-end

```
Trabalho-IA-Back/
├── .env
├── pom.xml
├── docs/
├── uploads/
├── src/
│   ├── main/
│   │   ├── java/com/plataforma/conversacional/
│   │   │   ├── config/
│   │   │   ├── constants/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   │   ├── internal/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── entity/
│   │   │   ├── enums/
│   │   │   ├── event/
│   │   │   ├── exception/
│   │   │   ├── health/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   │   └── impl/
│   │   │   ├── specification/
│   │   │   ├── storage/
│   │   │   ├── strategy/
│   │   │   ├── util/
│   │   │   └── validation/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
└── target/
    └── conversacional-0.0.1-SNAPSHOT.jar
```

### Tecnologias do Back-end

| Tecnologia | Finalidade |
|---|---|
| Java 17 | Linguagem de programação |
| Spring Boot 3.2 | Framework web |
| Spring Data JPA | Camada de persistência com Hibernate |
| Spring Security | Autenticação e controle de acesso |
| PostgreSQL | Banco de dados relacional |
| MapStruct | Mapeamento entre entidades e DTOs |
| SpringDoc OpenAPI | Documentação da API (Swagger) |
| Spring Dotenv | Carregamento de variáveis do `.env` |

### Pré-requisitos do Back-end

- Java 17+
- Maven 3.8+
- PostgreSQL rodando (local ou remoto)

### Como Executar via Maven

```bash
git clone https://github.com/DiaMont30/Trabalho-IA-Back.git
cd Trabalho-IA-Back
```

Crie o arquivo `.env` na raiz com as credenciais do banco:

```env
DB_USERNAME=chatbot
DB_PASSWORD=***
```

Execute:

```bash
mvn spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

A documentação Swagger estará em `http://localhost:8080/swagger-ui/index.html`.

### Como Executar via .jar

Pré-requisito: Java 17+

Baixe o `.jar` disponível em `target/conversacional-0.0.1-SNAPSHOT.jar`, crie o arquivo `.env` no mesmo diretório e execute:

```bash
java -jar conversacional-0.0.1-SNAPSHOT.jar
```

---

## Endpoints Consumidos pelo Front-end

| Método | Endpoint | Finalidade |
|---|---|---|
| GET | `/api/health` | Verificar disponibilidade da API |
| POST | `/api/auth/login` | Autenticar usuário |
| POST | `/api/auth/register` | Registrar novo usuário |
| POST | `/api/chat` | Enviar mensagem |
| GET | `/api/chat/{sessionId}` | Recuperar histórico da sessão |
| POST | `/api/upload` | Upload de arquivos |
| GET | `/api/ingestion/{id}` | Verificar status de ingestão |

---

## Tipos de Arquivos Suportados no Upload

- `.txt`
- `.pdf`

---

## Documentação

A documentação técnica do front-end encontra-se na pasta `docs/`:

- **SYSTEM_DOCS.md** — especificação arquitetural da aplicação
- **IMPLEMENTATION_PLAN.md** — plano de implementação seguido durante o desenvolvimento
- **IMPLEMENTATION_PENDENCIAS_PLAN.md** — pendências e ajustes da implementação
- **SPEC-PENDENCIAS-FRONTEND.md** — especificação das pendências do front-end

---

## Desenvolvimento Assistido por IA

Este projeto utilizou uma ferramenta de IA durante o processo de desenvolvimento.
