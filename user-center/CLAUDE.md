# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3 Java 17 user center application built using the DDD (Domain-Driven Design) COLA architecture pattern. The project follows a multi-module Maven structure with clear domain boundaries and implements user management, authentication, role-based access control, and third-party integrations (WeChat, Alipay, Google OAuth2).

## Architecture

### Module Structure
- **start**: Main application module containing the Spring Boot Application class and configuration
- **user-center-client**: API definitions, DTOs, commands, queries, and service interfaces
- **user-center-adapter**: Controllers, configuration, and external adapters (web layer)
- **user-center-app**: Application services and business logic implementations
- **user-center-domain**: Core domain entities, domain services, and gateways
- **user-center-infrastructure**: Database implementations, external service integrations, security config

### Key Technologies
- **Framework**: Spring Boot 3.4.4 with Java 17
- **Architecture**: DDD COLA Framework (v5.0.0)
- **Database**: MySQL with MyBatis-Plus 3.5.12
- **Security**: Spring Security with JWT (JJWT 0.12.6)
- **Caching**: Redis with Redisson 3.37.0
- **Third-party**: WeChat API, Alipay SDK, Google OAuth2
- **AI Integration**: Spring AI 1.1.0 with MCP support
- **Documentation**: Smart-doc for API documentation

## Development Commands

### Build and Run
```bash
# Clean and compile the project
mvn clean compile

# Run tests
mvn test

# Package the application
mvn clean package

# Run the application (from start module)
cd start && mvn spring-boot:run

# Or run the packaged JAR
java -jar start/target/user-center-*.jar
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl user-center-app

# Run specific test class
mvn test -Dtest=CustomerServiceTest
```

### Database
Database schema is located in `user-center-infrastructure/src/main/resources/db/schema.sql`. The application uses MySQL and includes tables for users, roles, menus, addresses, and operation logs.

## Code Conventions

### DDD and COLA Architecture Rules
1. Follow DDD principles with clear domain boundaries
2. Use COLA architecture patterns consistently
3. API parameters should be `cmd` or `qry` objects
4. Pagination requests use `PageQuery`
5. Response types: `SingleResponse`, `PageResponse`, `MultiResponse`

### Package Structure
- Commands and queries in `dto/command` and `dto/query` packages
- Domain entities in `domain` package with corresponding gateways
- Application services implement interfaces from client module
- Infrastructure implements gateway interfaces from domain

### Security
- JWT tokens with RSA key pair (configured in application.yml)
- Role-based access control with menu permissions
- Third-party OAuth2 integrations (Google, WeChat, Alipay)
- Token blacklisting and refresh mechanisms

### Key Features
- User registration and authentication
- Multi-provider login (username/password, WeChat, Alipay, Google)
- Role and permission management
- Operation logging with AOP
- File upload support
- API documentation generation

## Configuration

Main configuration files:
- `start/src/main/resources/application.yml`: Core Spring Boot configuration
- `start/src/main/resources/bootstrap.yaml`: Bootstrap configuration
- Database connection and JWT keys configured in application.yml
- Logging configuration in `logback-spring.xml`

## Important Notes

- The project uses MapStruct for object mapping with Lombok integration
- Operation logging is implemented via AOP annotations (`@OperationLog`)
- JWT keys are embedded in configuration (consider external key management for production)
- CORS is configured to allow all origins (review for production deployment)
- The application includes MCP (Model Context Protocol) support for AI integrations