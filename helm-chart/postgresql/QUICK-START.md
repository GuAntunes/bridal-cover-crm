# Quick Start - PostgreSQL Helm Chart

Guia rápido para fazer o deploy do PostgreSQL usando Helm.

## Pré-requisitos

- Kubernetes cluster configurado e rodando
- `kubectl` configurado e conectado ao cluster
- `helm` instalado (v3+)

## 1. Instalação Rápida (Desenvolvimento)

```bash
# Opção 1: Usando Makefile (recomendado)
cd helm-chart/postgresql
make install-dev

# Opção 2: Comando direto do Helm
helm install postgresql-dev . \
  --namespace bridal-crm \
  --create-namespace \
  --values values-dev.yaml
```

## 2. Verificar Instalação

```bash
# Ver status do Helm release
helm status postgresql-dev -n bridal-crm

# Ver pods
kubectl get pods -n bridal-crm

# Ver logs
kubectl logs -n bridal-crm -l app.kubernetes.io/name=bridal-cover-crm-postgresql -f
```

## 3. Conectar ao PostgreSQL

### Opção A: Port Forward (Mais fácil)

```bash
# Terminal 1: Criar port-forward
kubectl port-forward -n bridal-crm svc/postgresql-dev-bridal-cover-crm-postgresql 5432:5432

# Terminal 2: Conectar
psql -h localhost -p 5432 -U postgres -d bridal_cover_crm_dev
# Senha padrão: postgres
```

### Opção B: NodePort (Acesso externo)

```bash
# Pegar o IP do node
kubectl get nodes -o wide

# Conectar usando NodePort 30432
psql -h <NODE_IP> -p 30432 -U postgres -d bridal_cover_crm_dev
```

### Opção C: De dentro do cluster

```bash
# Conectar ao pod
kubectl exec -it -n bridal-crm \
  $(kubectl get pods -n bridal-crm -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}') \
  -- psql -U postgres -d bridal_cover_crm_dev
```

## 4. Comandos Úteis com Makefile

```bash
# Ver todos os comandos disponíveis
make help

# Desenvolvimento
make install-dev          # Instalar
make upgrade-dev          # Atualizar
make status-dev           # Ver status
make logs-dev            # Ver logs
make psql-dev            # Conectar ao psql
make port-forward-dev    # Port-forward
make backup-dev          # Backup
make uninstall-dev       # Desinstalar

# Staging
make install-staging
make upgrade-staging
make status-staging

# Produção (use com cuidado!)
make install-prod
make upgrade-prod
make status-prod
```

## 5. Informações de Conexão

### Bancos de Dados Criados

Após a instalação, os seguintes bancos estarão disponíveis:

- `bridal_cover_crm` - Banco principal
- `bridal_cover_crm_dev` - Desenvolvimento
- `bridal_cover_crm_test` - Testes
- `bridal_cover_crm_prod` - Produção

### Usuários

- **Admin**: `postgres` / `postgres` (padrão dev)
- **App User**: `bridal_user` / `bridal_pass`

### Endpoints

**Desenvolvimento (NodePort):**
- Host: `<NODE_IP>` ou `localhost` (com port-forward)
- Porta: `30432` (NodePort) ou `5432` (port-forward)

**De dentro do cluster:**
- Host: `postgresql-dev-bridal-cover-crm-postgresql.bridal-crm.svc.cluster.local`
- Porta: `5432`

## 6. Configurar Aplicação Backend

Para conectar sua aplicação Spring Boot ao PostgreSQL:

```yaml
# application-dev.yaml
spring:
  datasource:
    url: jdbc:postgresql://postgresql-dev-bridal-cover-crm-postgresql:5432/bridal_cover_crm_dev
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

Ou usando variáveis de ambiente no Kubernetes:

```yaml
env:
  - name: SPRING_DATASOURCE_URL
    value: jdbc:postgresql://postgresql-dev-bridal-cover-crm-postgresql:5432/bridal_cover_crm_dev
  - name: SPRING_DATASOURCE_USERNAME
    valueFrom:
      secretKeyRef:
        name: postgresql-dev-bridal-cover-crm-postgresql
        key: POSTGRES_USER
  - name: SPRING_DATASOURCE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: postgresql-dev-bridal-cover-crm-postgresql
        key: POSTGRES_PASSWORD
```

## 7. Backup Manual

```bash
# Criar backup
make backup-dev

# Ou manualmente
kubectl exec -n bridal-crm \
  $(kubectl get pods -n bridal-crm -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}') \
  -- pg_dump -U postgres bridal_cover_crm_dev > backup.sql

# Restaurar backup
cat backup.sql | kubectl exec -i -n bridal-crm \
  $(kubectl get pods -n bridal-crm -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}') \
  -- psql -U postgres -d bridal_cover_crm_dev
```

## 8. Troubleshooting

### Pod não inicia

```bash
# Ver eventos
kubectl describe pod -n bridal-crm <pod-name>

# Ver logs
kubectl logs -n bridal-crm <pod-name>

# Verificar PVC
kubectl get pvc -n bridal-crm
```

### Problemas de conexão

```bash
# Testar conectividade de dentro do pod
kubectl exec -it -n bridal-crm <pod-name> -- pg_isready -U postgres

# Ver serviço
kubectl get svc -n bridal-crm
kubectl describe svc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql
```

### Resetar tudo (CUIDADO!)

```bash
# Remove release e dados
make clean-all-dev

# Ou manualmente
helm uninstall postgresql-dev -n bridal-crm
kubectl delete pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data
```

## 9. Próximos Passos

1. **Staging**: Use `make install-staging` para ambiente de staging
2. **Produção**: 
   - Atualize a senha em `values-prod.yaml`
   - Configure storage class apropriado
   - Configure backups automáticos
   - Use `make install-prod`

3. **Integração**:
   - Configure seu backend para conectar ao PostgreSQL
   - Configure CI/CD para deployment automático
   - Configure monitoramento e alertas

## 10. Desinstalar

```bash
# Apenas remove o deployment (mantém dados)
make uninstall-dev

# Remove tudo incluindo dados (CUIDADO!)
make clean-all-dev
```

## Links Úteis

- [README completo](./README.md)
- [Documentação do PostgreSQL](https://www.postgresql.org/docs/)
- [Documentação do Helm](https://helm.sh/docs/)

## Suporte

Para problemas ou dúvidas, verifique:
1. Logs do pod: `make logs-dev`
2. Status do Helm: `make status-dev`
3. Eventos do Kubernetes: `kubectl get events -n bridal-crm`

