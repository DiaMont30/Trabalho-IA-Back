# Grupo 4

> **Serratec Residência de Software · Sala 34 · Trabalho avaliativo · disciplina de Inteligência Artificial**

---

## Integrantes

- DIANA MONTEIRO
- GABRIEL AGUIAR
- KAIQUE ABRANCHES
- ROBERTA ROCHA

---

## Estrutura do Repositório

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

---

## Como executar o código

### Pré-requisitos

- Java 17+ instalado
- Maven 3.8+ instalado (ou utilizar o wrapper da IDE)
- PostgreSQL rodando (local ou remoto)

### Passos

1. **Clonar o repositório**

   ```bash
   git clone https://github.com/DiaMont30/Trabalho-IA-Back.git
   cd Trabalho-IA-Back
   ```

2. **Criar o arquivo `.env`** na raiz do projeto com as credenciais do banco:

   ```env
   DB_USERNAME=chatbot
   DB_PASSWORD=***
   ```

3. **Executar**

   `mvn spring-boot:run`

   A aplicação estará disponível em `http://localhost:8080` ou `http://localhost:8080/swagger-ui/index.html`.

---

## Como executar o `.jar`

### Pré-requisito

- Java 17+ instalado

### Passos

1. **Baixar o `.jar`** — disponível em `target/conversacional-0.0.1-SNAPSHOT.jar`

2. **Criar o arquivo `.env`** no mesmo diretório do `.jar`:

   ```env
   DB_USERNAME=chatbot
   DB_PASSWORD=***
   ```

3. **Executar**:

   ```bash
   java -jar conversacional-0.0.1-SNAPSHOT.jar
   ```

   A aplicação sobe na porta `8080`.

---

## Tecnologias

- [Java 17](https://openjdk.org/projects/jdk/17/) — Linguagem de programação
- [Spring Boot 3.2](https://spring.io/projects/spring-boot) — Framework para desenvolvimento de aplicações web
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) — Camada de persistência com Hibernate
- [Spring Security](https://spring.io/projects/spring-security) — Autenticação e controle de acesso
- [PostgreSQL](https://www.postgresql.org/) — Banco de dados relacional
- [Flyway](https://flywaydb.org/) — Versionamento e migração do banco de dados
- [MapStruct](https://mapstruct.org/) — Mapeamento entre entidades e DTOs
- [SpringDoc OpenAPI](https://springdoc.org/) — Documentação da API (Swagger)
- [Spring Dotenv](https://github.com/paulschwarz/spring-dotenv) — Carregamento de variáveis de ambiente do arquivo `.env`

---

<p align="center">
  <strong>Grupo 4 — Serratec Residência · Sala 34</strong>
</p>
