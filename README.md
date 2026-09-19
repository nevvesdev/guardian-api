# Guardian API

API de gestão de recursos com OAuth2, JWT, RBAC e auditoria. Construída com Spring Boot 4.0.8, Gradle, PostgreSQL e Redis.

## 📋 Visão Geral

Solução completa de autenticação e autorização granular, projetada para cenários de produção em grandes tecnologias. Implementa:

- **OAuth2 + JWT**: Autenticação segura com tokens JWT
- **RBAC**: Role-Based Access Control com permissões granulares
- **Auditoria**: Rastreamento completo de acessos e mudanças
- **Rate Limiting**: Proteção contra abuso com Redis
- **Soft Deletes**: Exclusão lógica de dados
- **Versionamento**: Histórico de mudanças em recursos

## 🛠️ Tecnologias

- **Java 25**
- **Spring Boot 4.0.8**
- **Spring Security + OAuth2**
- **PostgreSQL 16**
- **Redis 7**
- **Gradle**
- **Lombok**
- **JUnit 5 + Mockito**

## 🚀 Instalação & Setup

### Pré-requisitos

- Java 25
- Gradle (ou use `./gradlew` se tiver wrapper)
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

Verifica se os containers estão rodando:

```bash
docker-compose ps
```

### Compilar o projeto

```bash
gradle build
```

Ou, se tiver Gradle wrapper:

```bash
./gradlew build
```

## ▶️ Como Rodar

```bash
gradle bootRun
```

Ou:

```bash
./gradlew bootRun
```

A API estará disponível em: `http://localhost:8080/api`

## 📝 Contribuindo

Este projeto segue um fluxo de branches:

- `main`: Produção (protegida)
- `develop`: Integração
- `feature/*`: Features isoladas (ex: `feature/oauth2-setup`)

### Processo

1. Crie uma branch a partir de `develop`:
```bash
   git checkout develop
   git checkout -b feature/sua-feature
```

2. Faça commits atômicos e descritivos em português

3. Abra um Pull Request para `develop`

4. Após merge, a branch é deletada

---

Desenvolvido por [nevvesdev](https://github.com/nevvesdev)