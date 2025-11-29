# PostgreSQL Helm Chart para Bridal Cover CRM

Este Helm chart gerencia o deployment do PostgreSQL para o sistema Bridal Cover CRM.

## Características

- PostgreSQL 15 Alpine
- Persistência de dados configurável
- Scripts de inicialização de banco de dados
- Configurações específicas por ambiente (dev, staging, prod)
- Health checks (liveness e readiness probes)
- Recursos configuráveis (CPU e memória)
- Suporte a NodePort e ClusterIP

## Instalação

### Ambiente de Desenvolvimento

```bash
helm install postgresql-dev ./postgresql \
  --namespace dev \
  --create-namespace \
  --values postgresql/values-dev.yaml
```

### Ambiente de Staging

```bash
helm install postgresql-staging ./postgresql \
  --namespace bridal-crm-staging \
  --create-namespace \
  --values postgresql/values-staging.yaml
```

### Ambiente de Produção

```bash
# IMPORTANTE: Atualize a senha no values-prod.yaml antes de fazer o deploy!
helm install postgresql-prod ./postgresql \
  --namespace bridal-crm-prod \
  --create-namespace \
  --values postgresql/values-prod.yaml
```

## Atualização

```bash
# Desenvolvimento
helm upgrade postgresql-dev ./postgresql \
  --namespace dev \
  --values postgresql/values-dev.yaml

# Staging
helm upgrade postgresql-staging ./postgresql \
  --namespace bridal-crm-staging \
  --values postgresql/values-staging.yaml

# Produção
helm upgrade postgresql-prod ./postgresql \
  --namespace bridal-crm-prod \
  --values postgresql/values-prod.yaml
```

## Desinstalação

```bash
# ATENÇÃO: Isso irá remover o pod, mas o PVC (dados) será mantido
helm uninstall postgresql-dev --namespace dev

# Para remover também os dados:
kubectl delete pvc -n dev postgresql-dev-bridal-cover-crm-postgresql-data
```

## Configuração

### Valores Principais

| Parâmetro | Descrição | Padrão |
|-----------|-----------|--------|
| `replicaCount` | Número de réplicas | `1` |
| `image.repository` | Repositório da imagem | `postgres` |
| `image.tag` | Tag da imagem | `15-alpine` |
| `postgresql.database` | Nome do banco de dados | `bridal_cover_crm_dev` |
| `postgresql.username` | Usuário do PostgreSQL | `postgres` |
| `postgresql.password` | Senha do PostgreSQL | `postgres` |
| `service.type` | Tipo do serviço | `NodePort` |
| `service.nodePort` | Porta do NodePort | `30432` |
| `persistence.enabled` | Habilitar persistência | `true` |
| `persistence.size` | Tamanho do volume | `5Gi` |

### Customização

Você pode customizar os valores criando um arquivo `custom-values.yaml`:

```yaml
postgresql:
  password: minha-senha-segura

resources:
  limits:
    memory: 1Gi
  requests:
    memory: 512Mi

persistence:
  size: 10Gi
```

E então instalar com:

```bash
helm install postgresql ./postgresql -f custom-values.yaml
```

## Conexão ao Banco de Dados

### De dentro do cluster Kubernetes

```bash
# Service name: postgresql-dev-bridal-cover-crm-postgresql
# Se estiver no mesmo namespace:
psql -h postgresql-dev-bridal-cover-crm-postgresql -U postgres -d bridal_cover_crm_dev

# Se estiver em outro namespace:
psql -h postgresql-dev-bridal-cover-crm-postgresql.bridal-crm.svc.cluster.local -U postgres -d bridal_cover_crm_dev
```

### De fora do cluster (usando NodePort)

```bash
# Obter o IP do node
kubectl get nodes -o wide

# Conectar usando o NodePort (30432)
psql -h <NODE_IP> -p 30432 -U postgres -d bridal_cover_crm_dev
```

### Usando Port Forward

```bash
# Criar port forward
kubectl port-forward -n dev svc/postgresql-dev-bridal-cover-crm-postgresql 5432:5432

# Em outro terminal, conectar
psql -h localhost -U postgres -d bridal_cover_crm_dev
```

## Bancos de Dados Criados

O script de inicialização cria os seguintes bancos de dados:

- `bridal_cover_crm` - Banco principal
- `bridal_cover_crm_dev` - Ambiente de desenvolvimento
- `bridal_cover_crm_test` - Ambiente de testes
- `bridal_cover_crm_prod` - Ambiente de produção

E também cria o usuário `bridal_user` com permissões em todos os bancos.

## Backup e Restore

### Backup

```bash
# Obter a senha
PGPASSWORD=$(kubectl get secret -n dev postgresql-dev-bridal-cover-crm-postgresql -o jsonpath='{.data.POSTGRES_PASSWORD}' | base64 -d)

# Criar backup
kubectl exec -n dev postgresql-dev-bridal-cover-crm-postgresql-0 -- \
  pg_dump -U postgres bridal_cover_crm_dev > backup.sql
```

### Restore

```bash
# Restaurar backup
cat backup.sql | kubectl exec -i -n dev postgresql-dev-bridal-cover-crm-postgresql-0 -- \
  psql -U postgres -d bridal_cover_crm_dev
```

## Troubleshooting

### Ver logs do PostgreSQL

```bash
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm-postgresql -f
```

### Verificar status do pod

```bash
kubectl get pods -n dev -l app.kubernetes.io/name=bridal-cover-crm-postgresql
```

### Verificar PVC

```bash
kubectl get pvc -n dev
```

### Conectar ao shell do container

```bash
kubectl exec -it -n dev <pod-name> -- /bin/sh
```

### Verificar se o PostgreSQL está aceitando conexões

```bash
kubectl exec -n dev <pod-name> -- pg_isready -U postgres
```

## Segurança em Produção

Para produção, é altamente recomendado:

1. **Usar Secrets externos**: Integrar com sistemas como HashiCorp Vault ou AWS Secrets Manager
2. **Alterar senhas padrão**: Nunca usar as senhas padrão em produção
3. **Configurar backup automático**: Implementar estratégia de backup regular
4. **Habilitar SSL/TLS**: Configurar conexões criptografadas
5. **Limitar acesso de rede**: Usar NetworkPolicies do Kubernetes
6. **Monitoramento**: Configurar monitoramento e alertas

## Recursos Adicionais

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [Kubernetes PersistentVolumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/)

