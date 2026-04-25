# 🤖 Customer Knowledge Assistant

A production-ready **Spring Boot 3** REST API that combines **JWT authentication**, **role-based access control**, and **Retrieval-Augmented Generation (RAG)** using Spring AI, PostgreSQL with PGVector, and OpenRouter LLM — enabling an AI-powered customer knowledge chatbot.

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6 + JWT (JJWT 0.11.5) |
| AI / LLM | Spring AI 1.0.0 + OpenRouter (Gemini 2.5 Flash) |
| Vector Store | PGVector (PostgreSQL extension) |
| Embeddings | Local LM Studio (Nomic Embed Text v2) |
| Database | PostgreSQL 16 |
| Migrations | Flyway 10.21.0 |
| ORM | Spring Data JPA / Hibernate |
| API Docs | SpringDoc OpenAPI 2.6.0 (Swagger UI) |
| Build | Maven |
| Utilities | Lombok |

---

## 📐 Architecture Overview

```
Client (Browser / curl)
        │
        ▼
 Spring Security Filter
  └── JwtAuthenticationFilter
        │
        ▼
  REST Controllers
  ├── AuthController              /api/auth/**
  ├── UserController              /api/users/**
  ├── UserManagementController    /api/management/users/**
  ├── ProjectController           /api/management/projects/**
  ├── DatasourceController        /api/management/datasources/**
  ├── AdminController             /api/admin/**
  └── RagController               /ai/rag
        │
        ▼
  Service Layer
  ├── AuthService
  ├── UserManagementService
  ├── ProjectService
  ├── DatasourceService
  └── RagService
        ├── VectorStore (PGVector) ◄─── Nomic Embed (Local LM Studio)
        └── ChatClient (OpenRouter / Gemini 2.5 Flash)
        │
        ▼
  PostgreSQL 16
  ├── users, roles, user_role
  ├── projects, datasources, project_datasource
  ├── user_project
  └── vector_store (pgvector)
```

---

## 📋 Prerequisites

- **Java 21**
- **PostgreSQL 16** with the `pgvector` extension enabled
- **Maven 3.8+**
- **LM Studio** running locally for embeddings (or any OpenAI-compatible embedding endpoint)
- **OpenRouter API key** for LLM chat completions

---

## 🗄️ Database Setup

### 1. Create PostgreSQL database and enable pgvector

```sql
CREATE DATABASE postgres;

-- Connect to the database, then run:
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. Update `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Flyway migrations (applied automatically on startup)

| Migration | Description |
|---|---|
| V1 | Create `roles` table |
| V2 | Create `users` table |
| V3 | Insert default roles (`ROLE_USER`, `ROLE_ADMIN`) |
| V4 | Extend users and user_role |
| V5 | Create `projects` and `user_project` tables |
| V6 | Create `datasources` and `project_datasource` tables |
| V7 | Add `app_source` column to `users` |

---

## ⚙️ Configuration

Full `application.properties` reference:

```properties
# Server
server.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=user
spring.datasource.password=password

# JWT
jwt.secret=MySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongForHS512Algorithm
jwt.expiration=3600000

# Spring AI - Embeddings (Local LM Studio)
spring.ai.openai.base-url=http://127.0.0.1:1234
spring.ai.openai.api-key=ignored
spring.ai.openai.embedding.options.model=text-embedding-nomic-embed-text-v2-moe
spring.ai.openai.embedding.options.dimensions=768

# Spring AI - Chat (OpenRouter)
spring.ai.openai.chat.base-url=https://openrouter.ai/api/v1
spring.ai.openai.chat.completions-path=/chat/completions
spring.ai.openai.chat.api-key=your-openrouter-api-key
spring.ai.openai.chat.options.model=google/gemini-2.5-flash
spring.ai.openai.chat.options.max-tokens=2048

# PGVector
spring.ai.vectorstore.pgvector.index-type=HNSW
spring.ai.vectorstore.pgvector.distance-type=COSINE_DISTANCE
spring.ai.vectorstore.pgvector.dimensions=768
spring.ai.vectorstore.pgvector.initialize-schema=true
spring.ai.vectorstore.pgvector.table-name=vector_store
```

---

## 🚀 Running the Application

```bash
# Clone the repository
git clone <repo-url>
cd customer-knowledge-assistant

# Build and run
mvn clean spring-boot:run
```

The application starts on **http://localhost:8080**

---

## 🔐 Security & Authentication

### AppSource Enum

Every user must be assigned an `appSource` at creation time:

| Value | Description |
|---|---|
| `CHATBOT` | User can log in via the chatbot frontend |
| `ADMINPANEL` | User can log in via the admin panel |

### Login Access Rules

| Role | CHATBOT | ADMINPANEL |
|---|---|---|
| `ROLE_USER` | ✅ Allowed | ❌ Denied |
| `ROLE_ADMIN` | ✅ Allowed | ✅ Allowed |

### JWT Flow

```
POST /api/auth/login
  └─► Returns JWT token
         │
         ▼
Authorization: Bearer <token>   ← Include in all subsequent requests
```

---

## 📡 API Endpoints

### 🔓 Authentication — `/api/auth`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | Public |
| POST | `/api/auth/login` | Login and receive JWT token | Public |

**Register Request:**
```json
{
  "username": "john",
  "password": "password123",
  "role": "ROLE_USER",
  "appSource": "CHATBOT"
}
```

**Login Request:**
```json
{
  "username": "john",
  "password": "password123"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "john",
  "role": "ROLE_USER"
}
```

---

### 👤 User Profile — `/api/users`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/users/me` | Get current logged-in user profile | 🔒 Any |

---

### 👥 User Management — `/api/management/users`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/management/users` | Create a user | 🔒 Authenticated |
| GET | `/api/management/users` | List all users (paginated) | 🔒 Authenticated |
| GET | `/api/management/users/{id}` | Get user by ID | 🔒 Authenticated |
| PUT | `/api/management/users/{id}` | Update user | 🔒 Authenticated |
| DELETE | `/api/management/users/{id}` | Delete user | 🔒 Authenticated |

**Create User Request:**
```json
{
  "username": "jane",
  "password": "password123",
  "fullname": "Jane Doe",
  "email": "jane@example.com",
  "jobTitle": "Engineer",
  "status": "Active",
  "roleId": 1,
  "projectId": 1,
  "appSource": "CHATBOT"
}
```

> `projectId` is **optional** — if omitted, the user is created without a project assignment.

**Pagination Query Params (all list endpoints):**

| Param | Default | Description |
|---|---|---|
| `page` | `0` | Zero-based page index |
| `size` | `10` | Items per page |
| `sortBy` | `id` | Field to sort by |
| `sortDir` | `asc` | `asc` or `desc` |

---

### 📁 Project Management — `/api/management/projects`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/management/projects` | Create a project | 🔒 Authenticated |
| GET | `/api/management/projects` | List all projects (paginated) | 🔒 Authenticated |
| GET | `/api/management/projects/my` | Get logged-in user's projects | 🔒 Authenticated |
| GET | `/api/management/projects/{id}` | Get project by ID | 🔒 Authenticated |
| PUT | `/api/management/projects/{id}` | Update project | 🔒 Authenticated |
| DELETE | `/api/management/projects/{id}` | Delete project | 🔒 Authenticated |

---

### 🗃️ Datasource Management — `/api/management/datasources`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/management/datasources` | Create a datasource | 🔒 Authenticated |
| GET | `/api/management/datasources` | List all datasources (paginated) | 🔒 Authenticated |
| GET | `/api/management/datasources/{id}` | Get datasource by ID | 🔒 Authenticated |
| PUT | `/api/management/datasources/{id}` | Update datasource | 🔒 Authenticated |
| DELETE | `/api/management/datasources/{id}` | Delete datasource | 🔒 Authenticated |

---

### 🛡️ Admin — `/api/admin`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/admin/users` | List all users | 🔒 `ROLE_ADMIN` only |

---

### 🤖 RAG (AI) — `/ai`

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/ai/rag` | Ask a question using RAG | 🔒 Authenticated |

**Request:**
```json
{
  "message": "Tell me about the product return policy"
}
```

**Response:**
```
Based on the retrieved documents, the return policy states that...
```

#### How RAG Works

```
User Query
    │
    ▼
Generate Embedding  (LM Studio / Nomic Embed Text v2)
    │
    ▼
Similarity Search in PGVector  (top-K = 4 documents)
    │
    ▼
Build Prompt  (rag-prompt.st template + retrieved context)
    │
    ▼
Send to LLM  (OpenRouter / Gemini 2.5 Flash)
    │
    ▼
Return AI-generated response
```

---

## 🗃️ Data Model

```
roles                               users
 └─ id, name                         └─ id, username, password, fullname,
                                         job_title, email, status,
                                         app_source, role_id

user_role                           user_project
 └─ user_id, role_id                 └─ user_id, project_id

projects                            datasources
 └─ id, name, path,                  └─ id, name, status
    database_table_name, status

project_datasource                  vector_store (pgvector)
 └─ project_id, datasource_id,       └─ id, content, metadata, embedding
    api_key, folder_url
```

---

## 🌐 CORS Configuration

The backend is configured to accept requests from **all origins** with credentials support, making it compatible with any frontend regardless of host or port (e.g. `http://127.0.0.1:5173`, `http://172.16.9.23:5173`):

```java
config.setAllowedOriginPatterns(List.of("*"));
config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
config.setAllowedHeaders(List.of("*"));
config.setAllowCredentials(true);
```

> `setAllowedOriginPatterns` is used instead of `setAllowedOrigins` because `allowCredentials=true` is incompatible with a wildcard in `setAllowedOrigins`.

---

## 📖 Swagger UI

Interactive API documentation available at:

```
http://localhost:8080/swagger-ui.html
```

To authenticate in Swagger:
1. Call `POST /api/auth/login` and copy the `token` value
2. Click **Authorize** 🔒 at the top right of the page
3. Enter: `Bearer <your-token>`
4. All secured endpoints are now accessible

---

## 📁 Project Structure

```
src/main/java/com/example/auth/
├── config/
│   ├── OpenApiConfig.java              # Swagger/OpenAPI configuration
│   └── SecurityConfig.java             # Spring Security + CORS config
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── UserManagementController.java
│   ├── ProjectController.java
│   ├── DatasourceController.java
│   ├── AdminController.java
│   └── RagController.java
├── dto/                                # Request/Response DTOs
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── Project.java
│   ├── Datasource.java
│   ├── AppSource.java                  # Enum: CHATBOT | ADMINPANEL
│   ├── UserRole.java
│   ├── UserProject.java
│   └── ProjectDatasource.java
├── repository/                         # Spring Data JPA repositories
├── security/
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── JwtUtil.java
│   └── UserDetailsServiceImpl.java
└── service/
    ├── AuthService.java
    ├── UserService.java
    ├── UserManagementService.java
    ├── ProjectService.java
    ├── DatasourceService.java
    └── RagService.java

src/main/resources/
├── application.properties
├── db/migration/                       # Flyway SQL migrations V1–V7
└── prompts/
    └── rag-prompt.st                   # System prompt template for RAG
```

---

## 🐛 Common Issues & Fixes

| Error | Cause | Fix |
|---|---|---|
| `Unsupported Database: PostgreSQL 16` | Flyway missing PostgreSQL module | Add `flyway-database-postgresql` with same version as `flyway-core` and set `flyway.version` property |
| `cannot retry due to server authentication, in streaming mode` | LLM returning streamed response | Remove `stream=false` option; use correct `chat.base-url` and `completions-path` |
| `Tokens in a single document exceeds the maximum` | Document too large for embedding model | Split documents into ≤1500 char chunks before ingesting into vector store |
| `CORS error` in browser | Missing/incorrect CORS headers | `CorsConfigurationSource` bean configured in `SecurityConfig` allows all origins |
| `WebSecurityConfigurerAdapter not found` | Spring Boot 2 → 3 migration | Replaced with `SecurityFilterChain` bean |
| `javax.* not found` | Spring Boot 2 → 3 migration | All `javax.*` replaced with `jakarta.*` imports |
| `400 Bad Request` on `/ai/rag` | Wrong OpenRouter URL or `max-tokens` too low | Set `chat.base-url=https://openrouter.ai/api/v1` and `max-tokens=2048` |

---

## 📄 License

This project is for internal use.
