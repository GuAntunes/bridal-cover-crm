# 🚀 Getting Started - Bridal Cover CRM

Guia rápido para começar a desenvolver no projeto.

## 📋 Pré-requisitos

- **Java 17** ou superior
- **Docker** e **Docker Compose**
- **Make** (opcional, mas recomendado)
- **Git**

## ⚡ Setup Rápido (5 minutos)

### 1. Clone o Repositório

```bash
git clone https://github.com/your-username/bridal-cover-crm.git
cd bridal-cover-crm
```

### 2. Inicie o Banco de Dados

```bash
# Com Make
make db-up

# Ou com Docker Compose
docker-compose up -d postgres pgadmin
```

### 3. Execute a Aplicação

```bash
# Com Make
make run

# Ou com Gradle
./gradlew bootRun
```

### 4. Acesse a Aplicação

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **PgAdmin:** http://localhost:8081 (admin@bridalcrm.com / admin123)
- **Database:** localhost:5432 (postgres / postgres)

## 🧪 Executar Testes

```bash
# Todos os testes
make test

# Apenas testes de arquitetura
make arch-test

# Com Gradle
./gradlew test
```

## 📦 Build

```bash
# Com Make
make build

# Ou com Gradle
./gradlew clean build
```

## 🗂️ Estrutura do Projeto

```
src/main/kotlin/br/com/gustavoantunes/bridalcovercrm/
├── domain/                    # 🎯 Domínio (regras de negócio)
│   ├── model/                 # Entidades e Value Objects
│   └── port/                  # Interfaces (contratos)
│       ├── in/                # Casos de uso
│       └── out/               # Repositórios
│
├── application/               # 🔄 Casos de uso (orquestração)
│   ├── usecase/               # Implementações
│   └── dto/                   # Commands e Queries
│
└── infrastructure/            # 🔧 Adaptadores (tecnologia)
    ├── adapter/
    │   ├── in/rest/           # Controllers REST
    │   └── out/persistence/   # Repositórios
    └── config/                # Configurações
```

## 🛠️ Comandos Úteis

### Banco de Dados

```bash
make db-up          # Iniciar PostgreSQL e PgAdmin
make db-down        # Parar banco de dados
```

### Jenkins (CI/CD)

```bash
make jenkins-up           # Iniciar Jenkins
make jenkins-password     # Ver senha inicial
make jenkins-logs         # Ver logs
```

### Aplicação

```bash
make run           # Executar aplicação
make build         # Build completo
make test          # Executar testes
make clean         # Limpar artefatos
```

### Ambiente Completo

```bash
make start-all     # Iniciar tudo (DB + Jenkins)
make stop-all      # Parar tudo
make help          # Ver todos os comandos
```

## 📝 Desenvolvimento

### Adicionando um Novo Endpoint

1. **Definir no OpenAPI** (`src/main/resources/static/openapi.yaml`)
   ```yaml
   /api/v1/leads:
     post:
       summary: Cadastrar lead
       # ... definição completa
   ```

2. **Criar UseCase** (Port)
   ```kotlin
   // domain/port/in/lead/RegisterLeadUseCase.kt
   interface RegisterLeadUseCase {
       fun execute(command: RegisterLeadCommand): Lead
   }
   ```

3. **Implementar Service**
   ```kotlin
   // application/usecase/lead/RegisterLeadService.kt
   @Service
   class RegisterLeadService : RegisterLeadUseCase {
       override fun execute(command: RegisterLeadCommand): Lead { ... }
   }
   ```

4. **Criar Controller**
   ```kotlin
   // infrastructure/adapter/in/rest/lead/LeadController.kt
   @RestController
   @RequestMapping("/api/v1/leads")
   class LeadController {
       @PostMapping
       fun register(@RequestBody request: LeadRequest): ResponseEntity<LeadResponse>
   }
   ```

### Adicionando Testes

```kotlin
// src/test/kotlin/...
@SpringBootTest
class RegisterLeadServiceTest {
    @Test
    fun `should register lead successfully`() {
        // Arrange
        // Act
        // Assert
    }
}
```

## 🔍 Debug

### Logs

```bash
# Ver logs da aplicação
tail -f logs/application.log

# Logs do Docker
docker-compose logs -f postgres
```

### Banco de Dados

```bash
# Conectar via psql
docker exec -it bridal-cover-crm-postgres psql -U postgres -d bridal_cover_crm_dev

# Ver tabelas
\dt

# Query
SELECT * FROM leads;
```

## 🐛 Troubleshooting

### Porta já em uso

```bash
# Verificar processo na porta 8080
lsof -i :8080

# Matar processo
kill -9 <PID>
```

### Banco não inicia

```bash
# Verificar logs
docker-compose logs postgres

# Remover volumes e reiniciar
docker-compose down -v
make db-up
```

### Build falha

```bash
# Limpar cache do Gradle
./gradlew clean --no-daemon

# Rebuild
./gradlew clean build
```

## 📚 Próximos Passos

1. Leia a [Arquitetura Hexagonal](../architecture/hexagonal-structure.md)
2. Entenda a [Linguagem Ubíqua](../ubiquitous-language.md)
3. Veja os [Casos de Uso](../use-cases.md)
4. Explore o [Swagger UI](http://localhost:8080/swagger-ui.html)

## 💡 Dicas

- Use o Makefile para comandos comuns
- Sempre execute testes antes de commitar
- Siga as convenções de nomenclatura do ArchUnit
- Documente mudanças no OpenAPI primeiro (API-First)

---

**Pronto para começar?** Execute `make start-all` e acesse http://localhost:8080/swagger-ui.html 🚀

