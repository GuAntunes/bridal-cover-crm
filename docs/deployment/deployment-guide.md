# 🚀 Deployment Guide - Bridal Cover CRM

Guia simplificado para deploy da aplicação.

## 📋 Ambientes

### 🔧 Desenvolvimento (Local)
- Docker Compose
- Banco PostgreSQL local
- Hot reload com Spring DevTools

### 🔨 Staging (Futuro)
- Docker containers
- PostgreSQL gerenciado
- CI/CD com Jenkins

### 🏭 Produção (Futuro)
- Kubernetes
- ArgoCD para GitOps
- Alta disponibilidade

---

## 💻 Deploy Local (Docker Compose)

### Pré-requisitos
- Docker Desktop instalado
- Portas livres: 5432, 8080, 8081, 9090

### Passos

```bash
# 1. Build da aplicação
./gradlew clean build

# 2. Build da imagem Docker
docker build -t bridal-cover-crm:latest .

# 3. Iniciar todos os serviços
docker-compose up -d

# 4. Verificar status
docker-compose ps

# 5. Ver logs
docker-compose logs -f app
```

### Acessar

- **Aplicação:** http://localhost:8080
- **Swagger:** http://localhost:8080/swagger-ui.html
- **PgAdmin:** http://localhost:8081
- **Jenkins:** http://localhost:9090

### Parar

```bash
# Parar sem remover dados
docker-compose stop

# Parar e remover containers
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados!)
docker-compose down -v
```

---

## 🔄 CI/CD com Jenkins

### Setup Inicial

```bash
# 1. Iniciar Jenkins
make jenkins-up

# 2. Obter senha inicial
make jenkins-password

# 3. Acessar e configurar
# http://localhost:9090
```

### Configurar Pipeline

1. **New Item** → Pipeline
2. **Pipeline script from SCM**
3. **Repository URL:** seu repositório Git
4. **Script Path:** Jenkinsfile

### Pipeline Atual

O `Jenkinsfile` na raiz executa:

1. ✅ Checkout do código
2. ✅ Build com Gradle
3. ✅ Testes automatizados
4. ✅ Build da imagem Docker
5. ⏳ Deploy (a implementar)

### Webhook GitHub (Opcional)

```bash
# URL do webhook
http://seu-jenkins:9090/github-webhook/
```

---

## 📦 Build de Produção

### JAR Executável

```bash
# Build
./gradlew clean bootJar

# Executar
java -jar build/libs/bridal-cover-crm-0.0.1-SNAPSHOT.jar
```

### Imagem Docker

```bash
# Build
docker build -t bridal-cover-crm:v1.0.0 .

# Tag para registry
docker tag bridal-cover-crm:v1.0.0 your-registry/bridal-cover-crm:v1.0.0

# Push
docker push your-registry/bridal-cover-crm:v1.0.0
```

---

## 🔧 Configuração

### Variáveis de Ambiente

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/bridal_cover_crm
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Logging
LOG_LEVEL=INFO
```

### application.yaml

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/bridal_cover_crm_dev}
    username: ${DATABASE_USER:postgres}
    password: ${DATABASE_PASSWORD:postgres}
```

---

## ✅ Health Checks

### Endpoint de Health

```bash
# Verificar saúde da aplicação
curl http://localhost:8080/health

# Resposta esperada
{
  "status": "UP",
  "timestamp": "2025-11-08T10:30:00"
}
```

### Database Health

```bash
# Conectar ao PostgreSQL
docker exec -it bridal-cover-crm-postgres psql -U postgres -d bridal_cover_crm_dev

# Verificar tabelas
\dt

# Query de teste
SELECT COUNT(*) FROM leads;
```

---

## 🐛 Troubleshooting

### Aplicação não inicia

```bash
# Ver logs
docker-compose logs app

# Verificar banco de dados
docker-compose logs postgres

# Verificar conectividade
docker-compose exec app ping postgres
```

### Flyway Migration Falha

```bash
# Ver histórico de migrations
docker exec -it bridal-cover-crm-postgres psql -U postgres -d bridal_cover_crm_dev -c "SELECT * FROM flyway_schema_history;"

# Forçar baseline (CUIDADO!)
docker-compose down -v
docker-compose up -d
```

### Porta já em uso

```bash
# Verificar processos
lsof -i :8080
lsof -i :5432

# Mudar porta no docker-compose.yml
ports:
  - "8081:8080"  # Porta host:Porta container
```

---

## 📊 Monitoramento

### Logs

```bash
# Aplicação
docker-compose logs -f app

# Banco de dados
docker-compose logs -f postgres

# Todos os serviços
docker-compose logs -f
```

### Métricas (Futuro)

Quando implementado:
- Prometheus para coleta de métricas
- Grafana para visualização
- Alertmanager para alertas

---

## 🔮 Roadmap de Deployment

### ✅ Fase 1: Local Development (ATUAL)
- Docker Compose
- Hot reload
- Banco local

### 📋 Fase 2: Staging (PRÓXIMO)
- Deploy em servidor remoto
- CI/CD automatizado
- Migrations automáticas

### 📋 Fase 3: Production (FUTURO)
- Cluster Kubernetes gerenciado via [platform-gitops](https://github.com/GuAntunes/platform-gitops)
- Alta disponibilidade
- Backup automatizado
- Monitoramento completo

---

## 📚 Referências

- [Docker Documentation](https://docs.docker.com/)
- [Jenkins no Kubernetes](jenkins-kubernetes.md)
- [Jenkins (referência / Compose)](../technologies/jenkins.md)
- [Getting Started Guide](../development/getting-started.md)
- [platform-gitops](https://github.com/GuAntunes/platform-gitops) — Helm, overlays e Argo CD

---

**Nota:** Este é um guia para a fase atual do projeto. Conforme o projeto amadurece, estratégias de deployment mais sofisticadas serão implementadas.

