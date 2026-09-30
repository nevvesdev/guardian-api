# Guardian API

API de gestão de recursos com OAuth2, JWT, RBAC e auditoria. Construída com Spring Boot 4.0.8, Gradle, PostgreSQL e Redis.

## 📋 Visão Geral

Guardian API é uma solução completa de autenticação e autorização granular, projetada para cenários de produção em grandes empresas de tecnologia. Implementa padrões utilizados em bancos como Itaú e Bradesco, e empresas como iFood e BTG Pactual.

### Funcionalidades

- **OAuth2 + JWT**: Autenticação segura com access token e refresh token
- **RBAC**: Role-Based Access Control com permissões granulares por recurso
- **Auditoria**: Rastreamento completo de acessos e mudanças via AOP
- **Rate Limiting**: Proteção contra abuso com Redis, configurável por endpoint
- **Soft Deletes**: Exclusão lógica com restore de dados
- **Versionamento**: Histórico completo de mudanças nos recursos
- **Token Blacklist**: Invalidação de tokens no logout via Redis

## 🛠️ Tecnologias

- **Java 25**
- **Spring Boot 4.0.8**
- **Spring Security 7**
- **PostgreSQL 16**
- **Redis 7**
- **Gradle**
- **Flyway**
- **Lombok**
- **JJWT 0.12.3**
- **JUnit 5 + Mockito**
- **SpringDoc OpenAPI 2.8.9**

## 🚀 Instalação & Setup

### Pré-requisitos

- Java 25
- Docker & Docker Compose

### Clonar o repositório

```bash
git clone https://github.com/nevvesdev/guardian-api.git
cd guardian-api
```

### Subir banco de dados e Redis

```bash
docker-compose up -d
```

### Compilar o projeto

```bash
./gradlew build
```

## ▶️ Como Rodar

```bash
./gradlew bootRun
```

A API estará disponível em: `http://localhost:8080/api`

Documentação Swagger: `http://localhost:8080/api/swagger-ui.html`

## 📌 Endpoints Principais

### Autenticação
| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|--|
| POST | /auth/register | Registrar usuário | ✅ |
| POST | /auth/login | Login | ✅ |
| POST | /auth/refresh | Renovar token | ✅ |
| POST | /auth/logout | Logout | ✅ |

### Recursos
| Método | Endpoint | Descrição | Role |
|--------|----------|-----------|------|
| POST | /resources | Criar recurso | USER |
| GET | /resources | Listar recursos | USER |
| GET | /resources/{id} | Buscar recurso | USER |
| PUT | /resources/{id} | Atualizar recurso | USER |
| DELETE | /resources/{id} | Deletar recurso | USER |
| POST | /resources/{id}/restore | Restaurar recurso | ADMIN |
| GET | /resources/{id}/history | Histórico do recurso | USER |

### Usuários
| Método | Endpoint | Descrição | Role |
|--------|----------|-----------|------|
| GET | /users | Listar usuários | ADMIN |
| GET | /users/{id} | Buscar usuário | ADMIN |
| POST | /users/{id}/roles | Atribuir role | ADMIN |
| DELETE | /users/{id}/roles/{role} | Remover role | ADMIN |

### Auditoria
| Método | Endpoint | Descrição | Role |
|--------|----------|-----------|------|
| GET | /audit | Todos os logs | ADMIN |
| GET | /audit/user/{email} | Logs por usuário | ADMIN |
| GET | /audit/action/{action} | Logs por ação | ADMIN |
| GET | /audit/date-range | Logs por período | ADMIN |

## 🔐 Como Usar

### 1. Registrar um usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"João Neves","email":"joao@example.com","password":"senha1234"}'
```

### 2. Fazer login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"joao@example.com","password":"senha1234"}'
```

### 3. Usar o token nas requisições

```bash
curl -X GET http://localhost:8080/api/resources \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN"
```

## 🧪 Testes

```bash
./gradlew test
```

## 📝 Contribuindo

- `main`: Produção (protegida)
- `develop`: Integração
- `feature/*`: Features isoladas

---

Desenvolvido por [nevvesdev](https://linkedin.com/in/nevvesdev)