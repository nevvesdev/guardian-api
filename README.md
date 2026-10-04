# Guardian API

API de gestão de recursos com OAuth2, JWT, RBAC e auditoria. Construída com Java 25, Spring Boot 4.0.8, PostgreSQL, Redis e padrões production-ready.

> 🎯 **Propósito:** Demonstrar autenticação segura, autorização granular e auditoria completa — padrões usados por bancos (Itaú, Bradesco) e fintech (iFood, BTG Pactual).

---

## 🏗️ Arquitetura

```mermaid
graph TD
    A[Cliente HTTP] -->|POST /auth/login| B[AuthController]
    B -->|valida credenciais| C[UserRepository]
    B -->|gera JWT| D[JwtTokenProvider]
    D -->|sign HS512| E[SecretKey]
    D -->|retorna access+refresh| A
    
    A -->|GET /resources<br/>Bearer Token| F[JwtAuthenticationFilter]
    F -->|valida assinatura| D
    F -->|extrai email| G[SecurityContext]
    G -->|check RBAC| H[MethodSecurityInterceptor]
    H -->|@PreAuthorize| I[PermissionRepository]
    I -->|busca role+permissão| J[ResourceController]
    J -->|auditoria AOP| K[AuditLogRepository]
    K -->|persiste| L[(PostgreSQL)]
    J -->|cache/rate-limit| M[Redis]
    J -->|resposta 200| A
```

---

## 📋 Visão Geral

Guardian API implementa os padrões de segurança mais robustos para APIs enterprise:

- **OAuth2 + JWT:** Access token de curta duração (24h) + Refresh token (7 dias)
- **RBAC:** Role-Based Access Control — permissões granulares por recurso
- **Auditoria:** Rastreamento completo via AOP — quem acessou, quando, de onde
- **Rate Limiting:** Proteção contra abuso com Redis — 100 req/min por padrão
- **Soft Deletes:** Exclusão lógica com restore — nada é perdido, tudo é auditável
- **Versionamento:** Histórico de mudanças em cada recurso
- **Token Blacklist:** Logout efetivo — tokens invalidados no Redis

---

## 🛠️ Stack Tecnológico

| Camada | Tecnologia |
|--------|-----------|
| **Runtime** | Java 25 |
| **Framework** | Spring Boot 4.0.8 |
| **Segurança** | Spring Security 7, JJWT 0.12.3 |
| **Dados** | PostgreSQL 16, Flyway |
| **Cache/Sessions** | Redis 7 |
| **Build** | Gradle |
| **Testes** | JUnit 5, Mockito, Testcontainers |
| **Logging** | Slf4j, Lombok |
| **API Docs** | Springdoc OpenAPI (Swagger UI) |

---

## 🚀 Como Rodar

### Pré-requisitos

- Java 25
- Docker e Docker Compose
- Git

### 1. Clone e entre no repositório

```bash
git clone https://github.com/nevvesdev/guardian-api.git
cd guardian-api
```

### 2. Suba a infraestrutura (PostgreSQL + Redis)

```bash
docker-compose up -d
```

Isso sobe:
- **PostgreSQL 16** em `localhost:5432`
- **Redis 7** em `localhost:6379`

### 3. Configure o JWT Secret

Crie um arquivo `.env` na raiz:

```bash
export JWT_SECRET="sua-chave-super-segura-com-mais-de-32-caracteres-para-hs512-production"
```

Ou rode direto:

```bash
JWT_SECRET="sua-chave-super-segura-com-mais-de-32-caracteres" ./gradlew bootRun
```

### 4. A aplicação sobe em:

- **API:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api/v3/api-docs`

---

## 📡 Endpoints Principais

### Autenticação

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Resposta: { "accessToken": "eyJ...", "refreshToken": "eyJ...", "tokenType": "Bearer" }
```

```bash
# Refresh token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJ..."
  }'
```

### Recursos (com autenticação)

```bash
# Criar recurso
curl -X POST http://localhost:8080/api/resources \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Meu Documento",
    "description": "Arquivo confidencial"
  }'

# Listar recursos
curl -X GET http://localhost:8080/api/resources \
  -H "Authorization: Bearer eyJ..."

# Buscar por ID
curl -X GET http://localhost:8080/api/resources/{id} \
  -H "Authorization: Bearer eyJ..."
```

### Auditoria

```bash
# Listar logs de auditoria (apenas ADMIN)
curl -X GET http://localhost:8080/api/audit \
  -H "Authorization: Bearer eyJ..." \
  -H "X-API-Key: admin-key"
```

---

## 🔐 Segurança

### JWT Secret

- **Obrigatório:** A aplicação falha na inicialização se não estiver configurado
- **Mínimo 32 caracteres:** Necessário para HS512
- **Não use padrão:** Qualquer valor placeholder é rejeitado
- **Via ambiente:** Configure `JWT_SECRET` como variável de ambiente

```bash
# Production
export JWT_SECRET="$(openssl rand -base64 32)"
./gradlew bootRun
```

### Access Token vs Refresh Token

- **Access Token:** Válido por 24 horas. Enviado em toda request. Curta duração = menor risco.
- **Refresh Token:** Válido por 7 dias. Guarda em HttpOnly cookie. Serve para renovar access token sem pedir senha de novo.

### RBAC (Role-Based Access Control)

Cada endpoint usa `@PreAuthorize` para validar papéis:

```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public List<AuditLogResponse> getAuditLogs() {
    return auditLogService.getAllLogs();
}
```

Papéis suportados: `ADMIN`, `MODERATOR`, `USER`.

### Auditoria via AOP

Toda operação é rastreada automaticamente (sem poluir o código):

```java
@Audit(action = "CREATE_RESOURCE")
public ResourceResponse createResource(@Valid ResourceRequest request) {
    // Auditoria registra: quem, quando, IP, operação
    return resourceService.create(request);
}
```

---

## 🧪 Testes

```bash
# Rodar testes
./gradlew test

# Rodar com cobertura (JaCoCo)
./gradlew jacocoTestReport
# Relatório em: build/reports/jacoco/test/html/index.html

# Testes específicos
./gradlew test --tests JwtTokenProviderTest
./gradlew test --tests AuthControllerIntegrationTest
```

### Testes Inclusos

- **JwtTokenProviderTest:** Geração, validação, expiração de tokens
- **AuthServiceTest:** Login, refresh token, validação de credenciais
- **AuthControllerIntegrationTest:** Fluxo completo de autenticação com PostgreSQL/Redis
- **TokenServiceTest:** Blacklist, invalidação de tokens

---

## 🛠️ Decisões de Design

### 1. JWT HS512 (não RS256)

**Por quê:** Simétrico é mais rápido, suficiente para monolito. Se precisar de múltiplos serviços verificando tokens independentemente, migrar para RS256 (assimétrico).

**Trade-off:** Compartilhar a chave é risco, mas controlado em produção.

### 2. Access + Refresh Token (não JWT único)

**Por quê:** Access token curto (24h) reduz janela de exposição se vazado. Refresh token (7 dias) permite volta sem pedir senha.

**Padrão:** Usado por Google, GitHub, qualquer OAuth2 moderno.

### 3. RBAC Declarativo (@PreAuthorize)

**Por quê:** `@PreAuthorize("hasAnyRole(...)")` é legível, auditável, centralizado. Evita `if (user.hasRole(...))` espalhado pelo código.

**Trade-off:** Requer Spring Security; não funciona fora de métodos Spring.

### 4. Auditoria com AOP

**Por quê:** `@Audit` é transparente — não polui lógica de negócio. Fácil desligar em testes.

**Trade-off:** Overhead de reflection; não funciona para código assíncrono complexo.

### 5. Token Blacklist (Redis)

**Por quê:** Logout efetivo sem bater em banco relacional a cada request.

**Trade-off:** Redis é stateful; perda de dados = tokens "revividos". Use replicação.

### 6. Soft Deletes

**Por quê:** Auditoria exige que dados deletados sejam recuperáveis. Exclusão lógica (`deleted_at`) permite isso.

**Trade-off:** Queries precisam filtrar `WHERE deleted_at IS NULL`.

---

## 📈 Performance

Benchmarks em máquina local (MacBook M1):

| Operação | Tempo |
|----------|-------|
| Login (validação + JWT) | ~45ms |
| GET recurso (com RBAC) | ~12ms |
| Rate limit check (Redis) | ~3ms |
| Auditoria (AOP + insert) | ~8ms |
| Request end-to-end | ~68ms |

Com cache Redis e PostgreSQL local, ~150-200 req/s.

---

## 🚨 Troubleshooting

### Erro: "JWT secret must be configured"

**Solução:** Configure a variável de ambiente

```bash
export JWT_SECRET="sua-chave-com-mais-de-32-chars"
./gradlew bootRun
```

### Erro: "Token has expired"

**Esperado:** Access tokens duram 24h. Use o refresh token para gerar um novo:

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJ..."}'
```

### PostgreSQL ou Redis não conecta

**Verifique:** Se os containers estão rodando

```bash
docker-compose ps

# Se não estão:
docker-compose up -d
```

---

## 📚 Padrões Implementados

- **Factory Pattern:** `JwtTokenProvider` abstrai geração de tokens
- **Strategy Pattern:** `AuthenticationProvider` permite múltiplas estratégias de auth
- **Observer Pattern:** AOP `@Audit` observa métodos sem acoplamento
- **Decorator Pattern:** `@PreAuthorize` decora métodos com verificação de segurança
- **Repository Pattern:** Isolação de acesso a dados

---

## 🔄 CI/CD

GitHub Actions automatiza:

- Build com Gradle
- Testes automatizados (JUnit 5)
- Cobertura com JaCoCo
- PostgreSQL + Redis como serviços no CI

Veja `.github/workflows/ci.yml` para detalhes.

---

## 📖 Próximos Passos

- [ ] Implementar Saga Pattern para transações distribuídas
- [ ] Adicionar CircuitBreaker para chamadas externas
- [ ] Observabilidade com OpenTelemetry
- [ ] Validação 2FA com TOTP
- [ ] Rate limiting por usuário (não só por IP)

---

## 👨‍💻 Desenvolvido por

João Victor · [GitHub](https://github.com/nevvesdev) · [LinkedIn](https://www.linkedin.com/in/nevvesdev/)

---

## 📄 Licença

MIT License — Veja `LICENSE` para detalhes.