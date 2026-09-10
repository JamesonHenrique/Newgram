# AGENTS.md — Newgram

Rede social full stack: Spring Boot 3.4.4 (Java 17) + Angular 19 + PostgreSQL + AWS S3.

## Estrutura

- `src/main/java/com/jhcs/newgram/presentation` → Resources REST (borda HTTP). Nada de regra de negócio aqui.
- `src/main/java/com/jhcs/newgram/application` → Services + DTOs (casos de uso).
- `src/main/java/com/jhcs/newgram/core/domain` → Entities, Enums, Repositories (coração do domínio).
- `src/main/java/com/jhcs/newgram/infrastructure` → Security JWT, S3, config, exception handling.
- `src/main/resources/db/migration` → Flyway versionado (`V1__...` → `V14__...`). Nunca edite migration aplicada; crie `V15__...`.
- `newgram-ui/` → Angular 19, client TS gerado via `ng-openapi-gen` (`ng-openapi-gen.json`).

## Comandos

```bash
./mvnw spring-boot:run
docker build -t newgram .
docker run -p 8080:8080 newgram
cd newgram-ui
npx ng-openapi-gen
ng serve
```

Swagger: `http://localhost:8080/swagger-ui.html`. Front: `http://localhost:4200`.

## Convenções

- Camadas só dependem para dentro: presentation → application → core.domain. Infrastructure implementa interfaces do core.
- DTOs por operação: `XCreateDTO`, `XUpdateDTO`, `XResponseDTO`, `XSummaryDTO`. Nunca exponha Entity no controller.
- Exceções tipadas em `infrastructure/exception` (`BusinessException`, `ResourceNotFoundException`, `UnauthorizedException`). Nunca `catch (e) {}` vazio.
- Migrações e seeds em `db/migration`; dados de exemplo via Flyway na primeira execução.
- Segurança: JWT access + refresh (`JwtService`, `JwtAuthFilter`), S3 via presigned URL com expiração configurável.
- TypeScript `strict: true`. Nomear pelo domínio (`honorarioContratual`-style, ex. `postArquivado`), nunca `dataObj2`.
- Commits conventional commits, imperativo, <72 chars (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`). Um commit por mudança lógica.

## OpenCode + GitHub

- Workflow `.github/workflows/opencode.yml` responde `/oc` ou `/opencode` em Issue/PR. Requer secret `ANTHROPIC_API_KEY`.
- MCP `github` em `opencode.json` usa `ghcr.io/github/github-mcp-server` via Docker com `{env:GITHUB_PERSONAL_ACCESS_TOKEN}` e toolsets `repos,issues,pull_requests`. Reinicie o opencode após mudar config.
