# Guia Completo: Microserviço de Autenticação e Autorização

## 📋 Visão Geral do Projeto

Você irá criar um microserviço responsável por gerenciar autenticação (login) e autorização (permissões) usando:
- **Kotlin** como linguagem
- **Spring Boot** como framework
- **JWT (JSON Web Token)** para autenticação
- **PostgreSQL** como banco de dados
- **Arquitetura Hexagonal** (igual ao projeto principal)

---

## 🔧 FASE 1: Preparação do Ambiente

### 1.1 Instalar o JDK 17

```bash
# macOS
brew install openjdk@17

# Verificar instalação
java -version
# Deve mostrar: openjdk version "17.x.x"
```

### 1.2 Instalar o IntelliJ IDEA

- Baixar Community Edition (gratuito): https://www.jetbrains.com/idea/download/
- Instalar o plugin Kotlin (geralmente já vem incluído)

### 1.3 Instalar o Docker Desktop

- Download: https://www.docker.com/products/docker-desktop/
- Necessário para rodar o PostgreSQL

### 1.4 Instalar o Gradle (opcional, pois usaremos o wrapper)

```bash
brew install gradle
```

### 1.5 Instalar o Postman ou Insomnia

- Para testar as APIs REST
- Postman: https://www.postman.com/downloads/

---

## 🏗️ FASE 2: Criar a Estrutura do Projeto

### 2.1 Criar o Projeto via Spring Initializr

Acesse: https://start.spring.io/

**Configurações:**
- **Project:** Gradle - Kotlin
- **Language:** Kotlin
- **Spring Boot:** 3.5.3
- **Project Metadata:**
  - Group: `br.com.gustavoantunes`
  - Artifact: `auth-service`
  - Name: `auth-service`
  - Package name: `br.com.gustavoantunes.authservice`
  - Packaging: Jar
  - Java: 17

**Dependencies (adicionar):**
- Spring Web
- Spring Security
- Spring Data JDBC
- PostgreSQL Driver
- Flyway Migration
- Validation
- Spring Boot DevTools

Clique em **GENERATE** e extraia o ZIP.

### 2.2 Estrutura de Pacotes

Crie a seguinte estrutura dentro de `src/main/kotlin/br/com/gustavoantunes/authservice/`:

```
authservice/
├── domain/
│   ├── model/
│   │   ├── common/
│   │   ├── user/
│   │   └── role/
│   ├── port/
│   │   ├── in/
│   │   │   ├── auth/
│   │   │   ├── user/
│   │   │   └── role/
│   │   └── out/
│   │       ├── repository/
│   │       └── security/
│   └── event/
├── application/
│   ├── dto/
│   │   ├── auth/
│   │   ├── user/
│   │   └── role/
│   └── usecase/
│       ├── auth/
│       ├── user/
│       └── role/
├── infrastructure/
│   ├── adapter/
│   │   ├── in/
│   │   │   └── rest/
│   │   │       ├── auth/
│   │   │       ├── user/
│   │   │       └── role/
│   │   └── out/
│   │       ├── persistence/
│   │       └── security/
│   └── config/
└── AuthServiceApplication.kt
```

---

## 💾 FASE 3: Configurar o Banco de Dados

### 3.1 Criar o docker-compose.yml

Crie na raiz do projeto:

```yaml
services:
  postgres:
    image: postgres:15-alpine
    container_name: auth-service-postgres
    environment:
      POSTGRES_DB: auth_service_dev
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5433:5432"  # Porta 5433 para não conflitar com outro PostgreSQL
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - auth-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:

networks:
  auth-network:
    driver: bridge
```

### 3.2 Iniciar o PostgreSQL

```bash
docker-compose up -d
```

### 3.3 Configurar application.yaml

Em `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: auth-service
  
  datasource:
    url: jdbc:postgresql://localhost:5433/auth_service_dev
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
    
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
    
  jackson:
    time-zone: America/Sao_Paulo
    date-format: yyyy-MM-dd'T'HH:mm:ss

# Configurações JWT
jwt:
  secret: ${JWT_SECRET:sua-chave-secreta-super-segura-change-in-production}
  expiration: 86400000  # 24 horas em milissegundos

# Logging
logging:
  level:
    br.com.gustavoantunes.authservice: INFO
    org.springframework.security: DEBUG
```

### 3.4 Criar Migrations do Flyway

Crie `src/main/resources/db/migration/V1__create_users_table.sql`:

```sql
-- Tabela de usuários
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Índices
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
```

Crie `src/main/resources/db/migration/V2__create_roles_and_permissions.sql`:

```sql
-- Tabela de roles (perfis)
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de permissões
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    resource VARCHAR(50) NOT NULL,  -- ex: 'LEAD', 'USER'
    action VARCHAR(50) NOT NULL,    -- ex: 'READ', 'WRITE', 'DELETE'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tabela de relacionamento usuário-role (N:N)
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tabela de relacionamento role-permission (N:N)
CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Inserir roles padrão
INSERT INTO roles (id, name, description) VALUES
    (gen_random_uuid(), 'ADMIN', 'Administrador do sistema'),
    (gen_random_uuid(), 'USER', 'Usuário comum'),
    (gen_random_uuid(), 'MANAGER', 'Gerente de equipe');

-- Inserir permissões padrão
INSERT INTO permissions (id, name, description, resource, action) VALUES
    (gen_random_uuid(), 'LEAD_READ', 'Visualizar leads', 'LEAD', 'READ'),
    (gen_random_uuid(), 'LEAD_WRITE', 'Criar e editar leads', 'LEAD', 'WRITE'),
    (gen_random_uuid(), 'LEAD_DELETE', 'Excluir leads', 'LEAD', 'DELETE'),
    (gen_random_uuid(), 'USER_READ', 'Visualizar usuários', 'USER', 'READ'),
    (gen_random_uuid(), 'USER_WRITE', 'Criar e editar usuários', 'USER', 'WRITE'),
    (gen_random_uuid(), 'USER_DELETE', 'Excluir usuários', 'USER', 'DELETE');
```

---

## 📝 FASE 4: Implementar o Domínio

### 4.1 Value Objects Comuns

Crie `domain/model/common/AggregateId.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.model.common

import java.io.Serializable
import java.util.UUID

abstract class AggregateId(
    open val value: UUID = UUID.randomUUID()
) : Serializable {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateId) return false
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()
    
    override fun toString(): String = value.toString()
}
```

### 4.2 Modelo de Domínio - User

Crie `domain/model/user/UserId.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.model.user

import br.com.gustavoantunes.authservice.domain.model.common.AggregateId
import java.util.UUID

data class UserId(
    override val value: UUID = UUID.randomUUID()
) : AggregateId(value) {
    companion object {
        fun from(value: String): UserId = UserId(UUID.fromString(value))
    }
}
```

Crie `domain/model/user/Email.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.model.user

data class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email não pode estar vazio" }
        require(isValidEmail(value)) { "Email inválido: $value" }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    override fun toString(): String = value
}
```

Crie `domain/model/user/User.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.model.user

import java.time.LocalDateTime

data class User(
    val id: UserId,
    val username: String,
    val email: Email,
    val passwordHash: String,
    val fullName: String,
    val isActive: Boolean = true,
    val roles: Set<String> = emptySet(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastLogin: LocalDateTime? = null
) {
    init {
        require(username.isNotBlank()) { "Username não pode estar vazio" }
        require(username.length >= 3) { "Username deve ter no mínimo 3 caracteres" }
        require(fullName.isNotBlank()) { "Nome completo não pode estar vazio" }
    }

    fun withUpdatedLogin(): User = copy(lastLogin = LocalDateTime.now())
    
    fun hasRole(role: String): Boolean = roles.contains(role)
    
    fun isAdmin(): Boolean = hasRole("ADMIN")
}
```

### 4.3 Modelo de Domínio - Role

Crie `domain/model/role/RoleId.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.model.role

import br.com.gustavoantunes.authservice.domain.model.common.AggregateId
import java.util.UUID

data class RoleId(
    override val value: UUID = UUID.randomUUID()
) : AggregateId(value) {
    companion object {
        fun from(value: String): RoleId = RoleId(UUID.fromString(value))
    }
}
```

Crie `domain/model/role/Role.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.model.role

import java.time.LocalDateTime

data class Role(
    val id: RoleId,
    val name: String,
    val description: String?,
    val permissions: Set<Permission> = emptySet(),
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(name.isNotBlank()) { "Nome da role não pode estar vazio" }
    }

    fun hasPermission(permissionName: String): Boolean =
        permissions.any { it.name == permissionName }
}

data class Permission(
    val name: String,
    val description: String?,
    val resource: String,
    val action: String
) {
    init {
        require(name.isNotBlank()) { "Nome da permissão não pode estar vazio" }
        require(resource.isNotBlank()) { "Resource não pode estar vazio" }
        require(action.isNotBlank()) { "Action não pode estar vazio" }
    }
}
```

---

## 🔌 FASE 5: Criar as Portas (Interfaces)

### 5.1 Portas de Entrada - Use Cases

Crie `domain/port/in/auth/AuthenticateUseCase.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.port.`in`.auth

interface AuthenticateUseCase {
    fun execute(username: String, password: String): AuthenticationResult
}

data class AuthenticationResult(
    val token: String,
    val userId: String,
    val username: String,
    val email: String,
    val roles: Set<String>,
    val expiresIn: Long
)
```

Crie `domain/port/in/user/RegisterUserUseCase.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.port.`in`.user

import br.com.gustavoantunes.authservice.domain.model.user.User

interface RegisterUserUseCase {
    fun execute(command: RegisterUserCommand): User
}

data class RegisterUserCommand(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val roles: Set<String> = setOf("USER")
)
```

Crie `domain/port/in/user/GetUserUseCase.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.port.`in`.user

import br.com.gustavoantunes.authservice.domain.model.user.User
import br.com.gustavoantunes.authservice.domain.model.user.UserId

interface GetUserUseCase {
    fun execute(userId: UserId): User?
    fun executeByUsername(username: String): User?
}
```

### 5.2 Portas de Saída - Repositórios

Crie `domain/port/out/repository/UserRepository.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.port.out.repository

import br.com.gustavoantunes.authservice.domain.model.user.User
import br.com.gustavoantunes.authservice.domain.model.user.UserId

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun delete(id: UserId)
}
```

Crie `domain/port/out/security/PasswordEncoder.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.port.out.security

interface PasswordEncoder {
    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
```

Crie `domain/port/out/security/TokenGenerator.kt`:

```kotlin
package br.com.gustavoantunes.authservice.domain.port.out.security

import br.com.gustavoantunes.authservice.domain.model.user.User

interface TokenGenerator {
    fun generateToken(user: User): String
    fun validateToken(token: String): Boolean
    fun getUsernameFromToken(token: String): String?
}
```

---

## 🔐 FASE 6: Implementar JWT e Segurança

### 6.1 Configurar Dependências

Adicione ao `build.gradle.kts`:

```kotlin
dependencies {
    // ... outras dependências
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // BCrypt para senha
    implementation("org.springframework.security:spring-security-crypto")
}
```

### 6.2 Implementar TokenGenerator

Crie `infrastructure/adapter/out/security/JwtTokenGenerator.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.out.security

import br.com.gustavoantunes.authservice.domain.model.user.User
import br.com.gustavoantunes.authservice.domain.port.out.security.TokenGenerator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenGenerator(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) : TokenGenerator {

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    override fun generateToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(user.username)
            .claim("userId", user.id.toString())
            .claim("email", user.email.toString())
            .claim("roles", user.roles.joinToString(","))
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    override fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getUsernameFromToken(token: String): String? {
        return try {
            val claims: Claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            claims.subject
        } catch (e: Exception) {
            null
        }
    }
}
```

### 6.3 Implementar PasswordEncoder

Crie `infrastructure/adapter/out/security/BCryptPasswordEncoder.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.out.security

import br.com.gustavoantunes.authservice.domain.port.out.security.PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder as SpringBCrypt
import org.springframework.stereotype.Component

@Component
class BCryptPasswordEncoder : PasswordEncoder {
    
    private val encoder = SpringBCrypt()

    override fun encode(rawPassword: String): String {
        return encoder.encode(rawPassword)
    }

    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return encoder.matches(rawPassword, encodedPassword)
    }
}
```

---

## 💾 FASE 7: Implementar Repositórios

### 7.1 Entidade de Persistência

Crie `infrastructure/adapter/out/persistence/entity/UserEntity.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.out.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("users")
data class UserEntity(
    @Id val id: UUID,
    val username: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastLogin: LocalDateTime?
)
```

### 7.2 Spring Data Repository

Crie `infrastructure/adapter/out/persistence/repository/UserDataJdbcRepository.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.out.persistence.repository

import br.com.gustavoantunes.authservice.infrastructure.adapter.out.persistence.entity.UserEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserDataJdbcRepository : CrudRepository<UserEntity, UUID> {
    fun findByUsername(username: String): UserEntity?
    fun findByEmail(email: String): UserEntity?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
    
    @Query("""
        SELECT u.* FROM users u
        INNER JOIN user_roles ur ON u.id = ur.user_id
        INNER JOIN roles r ON ur.role_id = r.id
        WHERE u.id = :userId
    """)
    fun findUserRoles(userId: UUID): List<String>
}
```

### 7.3 Mapper

Crie `infrastructure/adapter/out/persistence/mapper/UserMapper.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.out.persistence.mapper

import br.com.gustavoantunes.authservice.domain.model.user.Email
import br.com.gustavoantunes.authservice.domain.model.user.User
import br.com.gustavoantunes.authservice.domain.model.user.UserId
import br.com.gustavoantunes.authservice.infrastructure.adapter.out.persistence.entity.UserEntity

object UserMapper {
    
    fun toDomain(entity: UserEntity, roles: Set<String> = emptySet()): User {
        return User(
            id = UserId(entity.id),
            username = entity.username,
            email = Email(entity.email),
            passwordHash = entity.passwordHash,
            fullName = entity.fullName,
            isActive = entity.isActive,
            roles = roles,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            lastLogin = entity.lastLogin
        )
    }

    fun toEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id.value,
            username = user.username,
            email = user.email.value,
            passwordHash = user.passwordHash,
            fullName = user.fullName,
            isActive = user.isActive,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
            lastLogin = user.lastLogin
        )
    }
}
```

### 7.4 Adapter do Repositório

Crie `infrastructure/adapter/out/persistence/repository/UserRepositoryAdapter.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.out.persistence.repository

import br.com.gustavoantunes.authservice.domain.model.user.User
import br.com.gustavoantunes.authservice.domain.model.user.UserId
import br.com.gustavoantunes.authservice.domain.port.out.repository.UserRepository
import br.com.gustavoantunes.authservice.infrastructure.adapter.out.persistence.mapper.UserMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class UserRepositoryAdapter(
    private val repository: UserDataJdbcRepository,
    private val jdbcTemplate: JdbcTemplate
) : UserRepository {

    override fun save(user: User): User {
        val entity = UserMapper.toEntity(user)
        val saved = repository.save(entity)
        
        // Salvar roles do usuário
        saveUserRoles(user.id.value, user.roles)
        
        return UserMapper.toDomain(saved, user.roles)
    }

    override fun findById(id: UserId): User? {
        val entity = repository.findById(id.value).orElse(null) ?: return null
        val roles = getUserRoles(id.value)
        return UserMapper.toDomain(entity, roles)
    }

    override fun findByUsername(username: String): User? {
        val entity = repository.findByUsername(username) ?: return null
        val roles = getUserRoles(entity.id)
        return UserMapper.toDomain(entity, roles)
    }

    override fun findByEmail(email: String): User? {
        val entity = repository.findByEmail(email) ?: return null
        val roles = getUserRoles(entity.id)
        return UserMapper.toDomain(entity, roles)
    }

    override fun existsByUsername(username: String): Boolean =
        repository.existsByUsername(username)

    override fun existsByEmail(email: String): Boolean =
        repository.existsByEmail(email)

    override fun delete(id: UserId) {
        repository.deleteById(id.value)
    }

    private fun getUserRoles(userId: java.util.UUID): Set<String> {
        return jdbcTemplate.query(
            """
            SELECT r.name FROM roles r
            INNER JOIN user_roles ur ON r.id = ur.role_id
            WHERE ur.user_id = ?
            """,
            { rs, _ -> rs.getString("name") },
            userId
        ).toSet()
    }

    private fun saveUserRoles(userId: java.util.UUID, roles: Set<String>) {
        // Limpar roles existentes
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId)
        
        // Inserir novas roles
        roles.forEach { roleName ->
            jdbcTemplate.update(
                """
                INSERT INTO user_roles (user_id, role_id)
                SELECT ?, id FROM roles WHERE name = ?
                """,
                userId, roleName
            )
        }
    }
}
```

---

## 🎯 FASE 8: Implementar Use Cases (Serviços)

### 8.1 Serviço de Autenticação

Crie `application/usecase/auth/AuthenticateService.kt`:

```kotlin
package br.com.gustavoantunes.authservice.application.usecase.auth

import br.com.gustavoantunes.authservice.domain.port.`in`.auth.AuthenticateUseCase
import br.com.gustavoantunes.authservice.domain.port.`in`.auth.AuthenticationResult
import br.com.gustavoantunes.authservice.domain.port.out.repository.UserRepository
import br.com.gustavoantunes.authservice.domain.port.out.security.PasswordEncoder
import br.com.gustavoantunes.authservice.domain.port.out.security.TokenGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthenticateService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenGenerator: TokenGenerator,
    @Value("\${jwt.expiration}") private val expiration: Long
) : AuthenticateUseCase {

    override fun execute(username: String, password: String): AuthenticationResult {
        val user = userRepository.findByUsername(username)
            ?: throw InvalidCredentialsException("Usuário ou senha inválidos")

        if (!user.isActive) {
            throw InactiveUserException("Usuário inativo")
        }

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw InvalidCredentialsException("Usuário ou senha inválidos")
        }

        // Atualizar último login
        val updatedUser = user.withUpdatedLogin()
        userRepository.save(updatedUser)

        val token = tokenGenerator.generateToken(updatedUser)

        return AuthenticationResult(
            token = token,
            userId = updatedUser.id.toString(),
            username = updatedUser.username,
            email = updatedUser.email.toString(),
            roles = updatedUser.roles,
            expiresIn = expiration
        )
    }
}

class InvalidCredentialsException(message: String) : RuntimeException(message)
class InactiveUserException(message: String) : RuntimeException(message)
```

### 8.2 Serviço de Registro de Usuário

Crie `application/usecase/user/RegisterUserService.kt`:

```kotlin
package br.com.gustavoantunes.authservice.application.usecase.user

import br.com.gustavoantunes.authservice.domain.model.user.Email
import br.com.gustavoantunes.authservice.domain.model.user.User
import br.com.gustavoantunes.authservice.domain.model.user.UserId
import br.com.gustavoantunes.authservice.domain.port.`in`.user.RegisterUserCommand
import br.com.gustavoantunes.authservice.domain.port.`in`.user.RegisterUserUseCase
import br.com.gustavoantunes.authservice.domain.port.out.repository.UserRepository
import br.com.gustavoantunes.authservice.domain.port.out.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class RegisterUserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : RegisterUserUseCase {

    override fun execute(command: RegisterUserCommand): User {
        // Validações
        if (userRepository.existsByUsername(command.username)) {
            throw UsernameAlreadyExistsException("Username já existe: ${command.username}")
        }

        if (userRepository.existsByEmail(command.email)) {
            throw EmailAlreadyExistsException("Email já existe: ${command.email}")
        }

        // Criar usuário
        val user = User(
            id = UserId(),
            username = command.username,
            email = Email(command.email),
            passwordHash = passwordEncoder.encode(command.password),
            fullName = command.fullName,
            isActive = true,
            roles = command.roles
        )

        return userRepository.save(user)
    }
}

class UsernameAlreadyExistsException(message: String) : RuntimeException(message)
class EmailAlreadyExistsException(message: String) : RuntimeException(message)
```

### 8.3 Serviço de Busca de Usuário

Crie `application/usecase/user/GetUserService.kt`:

```kotlin
package br.com.gustavoantunes.authservice.application.usecase.user

import br.com.gustavoantunes.authservice.domain.model.user.User
import br.com.gustavoantunes.authservice.domain.model.user.UserId
import br.com.gustavoantunes.authservice.domain.port.`in`.user.GetUserUseCase
import br.com.gustavoantunes.authservice.domain.port.out.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class GetUserService(
    private val userRepository: UserRepository
) : GetUserUseCase {

    override fun execute(userId: UserId): User? {
        return userRepository.findById(userId)
    }

    override fun executeByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }
}
```

---

## 🌐 FASE 9: Criar Controllers REST

### 9.1 DTOs de Request/Response

Crie `infrastructure/adapter/in/rest/dto/LoginRequest.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.`in`.rest.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Username é obrigatório")
    val username: String,
    
    @field:NotBlank(message = "Password é obrigatório")
    val password: String
)
```

Crie `infrastructure/adapter/in/rest/dto/RegisterUserRequest.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.`in`.rest.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterUserRequest(
    @field:NotBlank(message = "Username é obrigatório")
    @field:Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    val username: String,
    
    @field:NotBlank(message = "Email é obrigatório")
    @field:Email(message = "Email inválido")
    val email: String,
    
    @field:NotBlank(message = "Password é obrigatório")
    @field:Size(min = 6, message = "Password deve ter no mínimo 6 caracteres")
    val password: String,
    
    @field:NotBlank(message = "Nome completo é obrigatório")
    val fullName: String,
    
    val roles: Set<String> = setOf("USER")
)
```

Crie `infrastructure/adapter/in/rest/dto/AuthResponse.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.`in`.rest.dto

data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val userId: String,
    val username: String,
    val email: String,
    val roles: Set<String>,
    val expiresIn: Long
)
```

### 9.2 Controller de Autenticação

Crie `infrastructure/adapter/in/rest/auth/AuthController.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.`in`.rest.auth

import br.com.gustavoantunes.authservice.domain.port.`in`.auth.AuthenticateUseCase
import br.com.gustavoantunes.authservice.domain.port.`in`.user.RegisterUserCommand
import br.com.gustavoantunes.authservice.domain.port.`in`.user.RegisterUserUseCase
import br.com.gustavoantunes.authservice.infrastructure.adapter.`in`.rest.dto.AuthResponse
import br.com.gustavoantunes.authservice.infrastructure.adapter.`in`.rest.dto.LoginRequest
import br.com.gustavoantunes.authservice.infrastructure.adapter.`in`.rest.dto.RegisterUserRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticateUseCase: AuthenticateUseCase,
    private val registerUserUseCase: RegisterUserUseCase
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val result = authenticateUseCase.execute(request.username, request.password)
        
        val response = AuthResponse(
            token = result.token,
            userId = result.userId,
            username = result.username,
            email = result.email,
            roles = result.roles,
            expiresIn = result.expiresIn
        )
        
        return ResponseEntity.ok(response)
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<Map<String, String>> {
        val command = RegisterUserCommand(
            username = request.username,
            email = request.email,
            password = request.password,
            fullName = request.fullName,
            roles = request.roles
        )
        
        val user = registerUserUseCase.execute(command)
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapOf(
                "message" to "Usuário criado com sucesso",
                "userId" to user.id.toString(),
                "username" to user.username
            ))
    }
}
```

### 9.3 Exception Handler

Crie `infrastructure/adapter/in/rest/exception/GlobalExceptionHandler.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.adapter.`in`.rest.exception

import br.com.gustavoantunes.authservice.application.usecase.auth.InactiveUserException
import br.com.gustavoantunes.authservice.application.usecase.auth.InvalidCredentialsException
import br.com.gustavoantunes.authservice.application.usecase.user.EmailAlreadyExistsException
import br.com.gustavoantunes.authservice.application.usecase.user.UsernameAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "Unauthorized",
                message = ex.message ?: "Credenciais inválidas",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(InactiveUserException::class)
    fun handleInactiveUser(ex: InactiveUserException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = ex.message ?: "Usuário inativo",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(UsernameAlreadyExistsException::class, EmailAlreadyExistsException::class)
    fun handleConflict(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = ex.message ?: "Recurso já existe",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Error",
                message = errors,
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "Erro interno do servidor",
                timestamp = LocalDateTime.now()
            ))
    }
}

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime
)
```

---

## ⚙️ FASE 10: Configuração de Segurança

Crie `infrastructure/config/SecurityConfig.kt`:

```kotlin
package br.com.gustavoantunes.authservice.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            }

        return http.build()
    }
}
```

---

## 🧪 FASE 11: Testes

### 11.1 Teste do Use Case

Crie `src/test/kotlin/application/usecase/auth/AuthenticateServiceTest.kt`:

```kotlin
package br.com.gustavoantunes.authservice.application.usecase.auth

import br.com.gustavoantunes.authservice.domain.model.user.Email
import br.com.gustavoantunes.authservice.domain.model.user.User
import br.com.gustavoantunes.authservice.domain.model.user.UserId
import br.com.gustavoantunes.authservice.domain.port.out.repository.UserRepository
import br.com.gustavoantunes.authservice.domain.port.out.security.PasswordEncoder
import br.com.gustavoantunes.authservice.domain.port.out.security.TokenGenerator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthenticateServiceTest {

    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val tokenGenerator: TokenGenerator = mock()
    
    private val service = AuthenticateService(
        userRepository, 
        passwordEncoder, 
        tokenGenerator,
        86400000L
    )

    @Test
    fun `deve autenticar usuário com credenciais válidas`() {
        // Given
        val username = "johndoe"
        val password = "password123"
        val user = createTestUser()
        
        whenever(userRepository.findByUsername(username)).thenReturn(user)
        whenever(passwordEncoder.matches(password, user.passwordHash)).thenReturn(true)
        whenever(tokenGenerator.generateToken(any())).thenReturn("fake-jwt-token")

        // When
        val result = service.execute(username, password)

        // Then
        assertNotNull(result)
        assertEquals("fake-jwt-token", result.token)
        assertEquals(username, result.username)
        verify(userRepository).save(any())
    }

    @Test
    fun `deve lançar exceção quando usuário não existe`() {
        // Given
        whenever(userRepository.findByUsername(any())).thenReturn(null)

        // When & Then
        assertThrows<InvalidCredentialsException> {
            service.execute("nonexistent", "password")
        }
    }

    @Test
    fun `deve lançar exceção quando senha está incorreta`() {
        // Given
        val user = createTestUser()
        whenever(userRepository.findByUsername(any())).thenReturn(user)
        whenever(passwordEncoder.matches(any(), any())).thenReturn(false)

        // When & Then
        assertThrows<InvalidCredentialsException> {
            service.execute("johndoe", "wrongpassword")
        }
    }

    private fun createTestUser() = User(
        id = UserId(),
        username = "johndoe",
        email = Email("john@example.com"),
        passwordHash = "hashed-password",
        fullName = "John Doe",
        isActive = true,
        roles = setOf("USER")
    )
}
```

---

## 🚀 FASE 12: Executar e Testar

### 12.1 Iniciar a Aplicação

```bash
# Terminal 1: Iniciar PostgreSQL
docker-compose up -d

# Terminal 2: Compilar e executar
./gradlew bootRun
```

### 12.2 Testar Endpoints com cURL

**Registrar usuário:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "fullName": "John Doe",
    "roles": ["USER"]
  }'
```

**Fazer login:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

**Resposta esperada:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["USER"],
  "expiresIn": 86400000
}
```

---

## 📚 FASE 13: Próximos Passos

### 13.1 Funcionalidades Adicionais para Implementar

1. **Gerenciamento de Roles e Permissões**
   - CRUD de roles
   - CRUD de permissões
   - Associar/remover permissões de roles

2. **Refresh Token**
   - Implementar mecanismo de refresh token
   - Armazenar refresh tokens no banco

3. **Recuperação de Senha**
   - Endpoint de "esqueci minha senha"
   - Envio de e-mail com token

4. **Endpoints Protegidos**
   - Criar filtro JWT para validar token
   - Adicionar anotações de autorização

5. **Auditoria**
   - Log de tentativas de login
   - Histórico de alterações de permissões

### 13.2 Melhorias de Segurança

1. Rate limiting para login
2. Bloqueio de conta após N tentativas
3. Política de senha forte
4. Rotação de chave JWT
5. HTTPS obrigatório em produção

---

## 📖 Conceitos Importantes para Estudar

### Para entender Kotlin:
1. **Data Classes** - classes imutáveis para dados
2. **Null Safety** - `?`, `!!`, `?.let`
3. **Extension Functions**
4. **Companion Objects** - equivalente a `static`
5. **When expression** - switch melhorado

### Para entender Spring Boot:
1. **Dependency Injection** - `@Autowired`, `@Component`
2. **Beans e @Configuration**
3. **Spring Data JDBC** vs JPA
4. **Transações** - `@Transactional`

### Para entender Arquitetura:
1. **Hexagonal Architecture** - Ports & Adapters
2. **Domain-Driven Design (DDD)**
3. **SOLID Principles**
4. **Clean Code**

### Para entender JWT:
1. **Estrutura de um JWT** - Header, Payload, Signature
2. **Stateless authentication**
3. **Claims e seu uso**

---

## 🎓 Material de Apoio

### Documentação Oficial:
- Kotlin: https://kotlinlang.org/docs/home.html
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Security: https://spring.io/projects/spring-security
- JWT: https://jwt.io/introduction

### Tutoriais Recomendados:
- Kotlin for Java Developers (Coursera)
- Spring Boot with Kotlin (Baeldung)
- Clean Architecture (Uncle Bob)

---

## ⚠️ Dicas Importantes

1. **Sempre validar inputs** - use Bean Validation
2. **Nunca exponha senha em logs** - mascare dados sensíveis
3. **Use variáveis de ambiente** para secrets
4. **Teste cada camada isoladamente**
5. **Siga as convenções de nomenclatura**
6. **Documente decisões arquiteturais**
7. **Faça commits pequenos e frequentes**
8. **Escreva testes antes de refatorar**

---

## 🐛 Problemas Comuns e Soluções

### Erro: "Cannot find symbol @Table"
**Solução:** Adicione `spring-boot-starter-data-jdbc` nas dependências

### Erro: "No qualifying bean of type 'PasswordEncoder'"
**Solução:** Certifique-se de que a classe tem `@Component`

### Erro: Migration failed
**Solução:** Verifique se o PostgreSQL está rodando e acessível

### Erro: JWT parsing error
**Solução:** Verifique se a secret key tem pelo menos 256 bits (32 caracteres)

---

## 📝 Checklist de Implementação

### Setup Inicial
- [ ] JDK 17 instalado
- [ ] IntelliJ IDEA instalado
- [ ] Docker Desktop instalado
- [ ] Projeto criado via Spring Initializr
- [ ] Estrutura de pacotes criada

### Banco de Dados
- [ ] docker-compose.yml configurado
- [ ] PostgreSQL rodando
- [ ] application.yaml configurado
- [ ] Migrations criadas (V1 e V2)
- [ ] Migrations executadas com sucesso

### Domínio
- [ ] AggregateId criado
- [ ] UserId criado
- [ ] Email value object criado
- [ ] User domain model criado
- [ ] Role e Permission criados

### Portas
- [ ] AuthenticateUseCase criado
- [ ] RegisterUserUseCase criado
- [ ] GetUserUseCase criado
- [ ] UserRepository interface criada
- [ ] PasswordEncoder interface criada
- [ ] TokenGenerator interface criada

### Segurança
- [ ] Dependências JWT adicionadas
- [ ] JwtTokenGenerator implementado
- [ ] BCryptPasswordEncoder implementado

### Persistência
- [ ] UserEntity criado
- [ ] UserDataJdbcRepository criado
- [ ] UserMapper criado
- [ ] UserRepositoryAdapter implementado

### Aplicação
- [ ] AuthenticateService implementado
- [ ] RegisterUserService implementado
- [ ] GetUserService implementado

### Controllers
- [ ] DTOs de request criados
- [ ] DTOs de response criados
- [ ] AuthController implementado
- [ ] GlobalExceptionHandler implementado

### Configuração
- [ ] SecurityConfig criado
- [ ] CORS configurado (se necessário)

### Testes
- [ ] AuthenticateServiceTest criado
- [ ] Testes unitários passando
- [ ] Testes de integração (opcional)

### Validação
- [ ] Aplicação iniciando sem erros
- [ ] Endpoint /register funcionando
- [ ] Endpoint /login funcionando
- [ ] JWT sendo gerado corretamente
- [ ] Validações funcionando

---

Este guia fornece uma base sólida para criar o microserviço de autenticação. O programador deve seguir os passos sequencialmente e estudar os conceitos conforme avança. Boa sorte! 🚀

