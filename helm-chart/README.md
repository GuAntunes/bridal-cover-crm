# Helm Charts - Bridal Cover CRM

Deploy do Bridal Cover CRM (PostgreSQL + Backend Spring Boot) no Kubernetes usando Helm.

## 📋 Índice

- [Início Rápido](#-início-rápido)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Comandos](#-comandos)
- [Ambientes](#-ambientes)
- [Atualização](#-atualização)

---

## 🚀 Início Rápido

```bash
cd helm-chart

# Instalar PostgreSQL + Backend
make install

# Verificar status
make status

# Acessar aplicação
make backend-port-forward
# Em outro terminal:
curl http://localhost:8080/actuator/health
```

---

## 🏗️ Arquitetura

Este projeto usa **2 Helm Charts separados**:

1. **PostgreSQL** (`postgresql/`) - Banco de dados standalone
2. **Backend** (`bridal-cover-crm/`) - API Spring Boot

### Conectividade

```
Backend (Spring Boot)
    ↓
jdbc://postgres-bridal-cover-crm-postgresql:5432/bridal_cover_crm_dev
    ↓
PostgreSQL
```

O backend conecta automaticamente no PostgreSQL através do **Service DNS** do Kubernetes.

---

## 🔧 Pré-requisitos

- **Kubernetes cluster** rodando (Minikube, Kind, ou cluster real)
- **kubectl** configurado
- **Helm 3.10+** instalado

```bash
# macOS
brew install kubectl helm

# Verificar instalações
kubectl version --client
helm version
kubectl cluster-info
```

---

## 📦 Instalação

### Instalação Completa (Recomendado)

```bash
cd helm-chart

# Instalar PostgreSQL + Backend
make install
```

### Instalação Passo a Passo

```bash
# 1. PostgreSQL
make postgres-install

# 2. Backend
make backend-install

# 3. Verificar
make status
```

### Verificação

```bash
# Ver pods
kubectl get pods -n dev

# Ver logs do backend
make backend-logs

# Testar aplicação
make backend-port-forward
# Em outro terminal:
curl http://localhost:8080/actuator/health
```

---

## 🛠️ Comandos

### Comandos Gerais

```bash
make help          # Ver todos os comandos
make install       # Instalar PostgreSQL + Backend
make status        # Ver status completo
make uninstall     # Desinstalar tudo (mantém dados)
```

### PostgreSQL

```bash
make postgres-install       # Instalar
make postgres-status        # Ver status
make postgres-logs          # Ver logs
make postgres-connect       # Conectar via psql
make postgres-port-forward  # Port-forward (localhost:5432)
```

### Backend

```bash
make backend-install        # Instalar
make backend-status         # Ver status
make backend-logs           # Ver logs
make backend-upgrade        # Atualizar
make backend-restart        # Reiniciar
make backend-port-forward   # Port-forward (localhost:8080)
```

### Utilitários

```bash
make info              # Ver informações de conexão
make diagnose          # Diagnóstico completo
make test-connection   # Testar Backend → PostgreSQL
```

---

## 🌍 Ambientes

### Desenvolvimento (padrão)

```bash
make install
```

- 1 réplica do backend
- NodePort para acesso local
- Recursos mínimos

### Staging

```bash
make ENV=staging install
```

- 2 réplicas do backend
- Recursos médios

### Produção

```bash
make ENV=prod install
```

- 3+ réplicas do backend
- High Availability
- Recursos maiores

---

## 🔄 Atualização

### Atualizar Imagem do Backend

```bash
# 1. Build e push nova imagem
docker build -t gustavoantunes/bridal-cover-crm:1.0.1 .
docker push gustavoantunes/bridal-cover-crm:1.0.1

# 2. Atualizar no Kubernetes
make backend-upgrade
```

### Atualizar Configuração

```bash
# Editar values
vim bridal-cover-crm/values-dev.yaml

# Aplicar mudanças
make backend-upgrade
```

### Rollback

```bash
# Ver histórico
helm history backend -n dev

# Voltar versão anterior
helm rollback backend -n dev
```

---

## 📚 Informações Adicionais

### Estrutura

```
helm-chart/
├── Makefile                  # Comandos simplificados
├── README.md                 # Esta documentação
│
├── postgresql/               # Chart PostgreSQL
│   ├── values-dev.yaml
│   ├── values-staging.yaml
│   └── values-prod.yaml
│
└── bridal-cover-crm/         # Chart Backend
    ├── values-dev.yaml
    ├── values-staging.yaml
    └── values-prod.yaml
```

### Credenciais (DEV)

- **Database:** `bridal_cover_crm_dev`
- **Username:** `postgres`
- **Password:** `postgres`
- **Host (interno):** `postgres-bridal-cover-crm-postgresql`
- **Port:** `5432`

### Conexão PostgreSQL

A conexão é configurada automaticamente em `values-dev.yaml`:

```yaml
externalDatabase:
  host: postgres-bridal-cover-crm-postgresql
  port: 5432
  database: bridal_cover_crm_dev
  username: postgres
  existingSecret: postgres-bridal-cover-crm-postgresql
  secretKey: POSTGRES_PASSWORD
```

### Acessar Localmente

```bash
# Backend
make backend-port-forward
# http://localhost:8080

# PostgreSQL
make postgres-port-forward
# psql -h localhost -p 5432 -U postgres -d bridal_cover_crm_dev
```

---

## 🎯 Comandos Úteis do Kubectl

```bash
# Ver pods
kubectl get pods -n dev

# Ver todos os recursos
kubectl get all -n dev

# Logs em tempo real
kubectl logs -f <pod-name> -n dev

# Descrever pod
kubectl describe pod <pod-name> -n dev

# Ver eventos
kubectl get events -n dev --sort-by='.lastTimestamp'
```

---

**Documentação completa dos charts:** Veja os arquivos `values-*.yaml` para todas as opções de configuração.
