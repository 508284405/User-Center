# Repository Guidelines

## Project Structure & Modules
- Root is a multi‑module Maven project (Java 17, Spring Boot 3).
- Modules:
  - `user-center-client`: Public DTOs/clients shared across modules.
  - `user-center-adapter`: Web/API adapters (controllers, REST, config).
  - `user-center-app`: Application services/use cases and orchestrations.
  - `user-center-domain`: Core domain model and domain services.
  - `user-center-infrastructure`: Persistence, MyBatis(Plus) mappers, integrations.
  - `start`: Spring Boot launcher and runtime config.
- Resources live under each module’s `src/main/resources` (e.g., MyBatis XML under `user-center-infrastructure/src/main/resources`). Tests are under `src/test/java`.

## Build, Test, Run
- Build all: `mvn clean install -DskipTests`
- Run tests: `mvn test`
- Run only one module’s tests: `mvn -pl user-center-domain -am test`
- Run locally (dev):
  - Spring Boot: `mvn -pl start spring-boot:run`
  - From jar: `java -jar start/target/start-1.0.0-SNAPSHOT.jar`

## Coding Style & Naming
- Java 17; 4‑space indentation; UTF‑8.
- Packages: lowercase; Classes: PascalCase; methods/fields: camelCase; constants: UPPER_SNAKE_CASE.
- Suffixes: `*Controller` (adapter), `*Service` (app/domain), `*Mapper` (infra), DTO/VO/DO where applicable.
- Use Lombok for boilerplate and MapStruct for mappings; avoid manual getters/setters where Lombok applies.

## Testing Guidelines
- Framework: JUnit via Maven Surefire.
- Place tests in `src/test/java`; name as `ClassNameTest`.
- Prefer unit tests for domain/app; use slice or integration tests for adapter/infra where needed.
- Run with `mvn test` or per‑module as above.

## Commit & Pull Requests
- Commit style follows Conventional Commits with scopes (seen in history): `feat(scope): ...`, `fix(scope): ...`, `refactor(scope): ...`. English or Chinese descriptions are both used; write in imperative present.
- PRs should include:
  - Clear description and rationale; link related issues (e.g., `Closes #123`).
  - What changed, how to test, and risk/rollback notes.
  - Evidence for behavior changes (logs, cURL/Postman examples, or screenshots for UI).

## Security & Configuration
- Config is in `application.yml`/`bootstrap.yaml` under `start` and infra; externalize secrets (DB, Nacos, JWT) via env or config server. Example: `SPRING_PROFILES_ACTIVE=dev`.
- JWT keys exist under `start/src/main/resources/keystore`; do not commit replacements or plaintext secrets.
