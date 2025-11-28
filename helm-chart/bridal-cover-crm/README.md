# Bridal Cover CRM - Helm Chart

Este é o Chart Helm oficial para o Bridal Cover CRM, um sistema de gerenciamento de leads para noivas.

## Pré-requisitos

- Kubernetes 1.24+
- Helm 3.10+
- PV provisioner (para PostgreSQL persistence)

## Instalação

### Adicionar o repositório Helm (se publicado)

```bash
helm repo add bridal-cover https://charts.bridalcover.com
helm repo update
```

### Instalar o Chart

#### Instalação básica

```bash
helm install my-crm bridal-cover-crm/
```

#### Instalação em namespace específico

```bash
kubectl create namespace production
helm install my-crm bridal-cover-crm/ -n production
```

#### Instalação com valores customizados

```bash
helm install my-crm bridal-cover-crm/ -f values-prod.yaml
```

#### Instalação via arquivo local

```bash
helm install my-crm ./bridal-cover-crm/
```

## Configuração

### Principais Parâmetros

| Parâmetro | Descrição | Valor Padrão |
|-----------|-----------|--------------|
| `replicaCount` | Número de réplicas | `3` |
| `image.repository` | Repositório da imagem Docker | `guantunes/bridal-cover-crm` |
| `image.tag` | Tag da imagem | `""` (usa appVersion) |
| `service.type` | Tipo do Service | `ClusterIP` |
| `service.port` | Porta do Service | `8080` |
| `ingress.enabled` | Habilitar Ingress | `true` |
| `ingress.hosts` | Hosts do Ingress | `[api.bridalcover.com]` |
| `postgresql.enabled` | Usar PostgreSQL via dependency | `true` |
| `postgresql.auth.password` | Senha do PostgreSQL | `changeme-in-production` |
| `resources.limits.cpu` | Limite de CPU | `1000m` |
| `resources.limits.memory` | Limite de memória | `1Gi` |
| `autoscaling.enabled` | Habilitar HPA | `true` |
| `autoscaling.minReplicas` | Mínimo de réplicas | `2` |
| `autoscaling.maxReplicas` | Máximo de réplicas | `10` |

### Exemplos de Configuração

#### Desenvolvimento

```yaml
# values-dev.yaml
replicaCount: 1

image:
  tag: "dev"

app:
  environment: dev

autoscaling:
  enabled: false

resources:
  limits:
    cpu: 500m
    memory: 512Mi

postgresql:
  auth:
    password: devpassword
  primary:
    persistence:
      size: 5Gi
```

```bash
helm install my-crm ./bridal-cover-crm -f values-dev.yaml
```

#### Staging

```yaml
# values-staging.yaml
replicaCount: 2

image:
  tag: "staging"

app:
  environment: staging

ingress:
  hosts:
    - host: staging-api.bridalcover.com

postgresql:
  auth:
    password: stagingpassword
  primary:
    persistence:
      size: 10Gi
```

```bash
helm install my-crm ./bridal-cover-crm -f values-staging.yaml
```

#### Produção

```yaml
# values-prod.yaml
replicaCount: 5

image:
  tag: "1.0.0"
  pullPolicy: IfNotPresent

app:
  environment: prod

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 20
  targetCPUUtilizationPercentage: 70

resources:
  limits:
    cpu: 2000m
    memory: 2Gi
  requests:
    cpu: 1000m
    memory: 1Gi

postgresql:
  auth:
    existingSecret: postgres-secret
  primary:
    persistence:
      size: 50Gi
      storageClass: fast-ssd
    resources:
      limits:
        cpu: 1000m
        memory: 1Gi

podDisruptionBudget:
  enabled: true
  minAvailable: 2
```

```bash
helm install my-crm ./bridal-cover-crm -f values-prod.yaml -n production
```

## Atualizando a Release

```bash
# Atualizar com novos valores
helm upgrade my-crm ./bridal-cover-crm -f values-prod.yaml

# Atualizar forçando recriação dos pods
helm upgrade my-crm ./bridal-cover-crm --force

# Upgrade ou Install
helm upgrade --install my-crm ./bridal-cover-crm
```

## Desinstalação

```bash
# Desinstalar a release
helm uninstall my-crm

# Desinstalar mantendo histórico
helm uninstall my-crm --keep-history
```

## Rollback

```bash
# Ver histórico de releases
helm history my-crm

# Fazer rollback para revisão anterior
helm rollback my-crm

# Rollback para revisão específica
helm rollback my-crm 3
```

## Troubleshooting

### Ver logs

```bash
kubectl logs -l app.kubernetes.io/name=bridal-cover-crm -f
```

### Debug do template

```bash
helm template my-crm ./bridal-cover-crm --debug
```

### Verificar valores aplicados

```bash
helm get values my-crm
helm get values my-crm --all
```

### Testar a release

```bash
helm test my-crm
```

## Banco de Dados Externo

Para usar um banco de dados PostgreSQL externo ao invés da dependency:

```yaml
# values-external-db.yaml
postgresql:
  enabled: false

externalDatabase:
  host: postgres.example.com
  port: 5432
  username: bridalcover
  database: bridalcover_db
  existingSecret: external-db-secret
```

Crie o secret:

```bash
kubectl create secret generic external-db-secret \
  --from-literal=password=yourpassword
```

## Secrets Management

### Usando valores inline (NÃO recomendado para produção)

```bash
helm install my-crm ./bridal-cover-crm \
  --set postgresql.auth.password=mypassword
```

### Usando arquivo de secrets (gitignored)

```yaml
# secrets.yaml (não commitar!)
postgresql:
  auth:
    password: supersecretpassword
```

```bash
helm install my-crm ./bridal-cover-crm -f values.yaml -f secrets.yaml
```

### Usando Sealed Secrets (recomendado)

```bash
# Instalar Sealed Secrets Controller
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# Criar secret normal
kubectl create secret generic postgres-secret \
  --from-literal=password=mypassword \
  --dry-run=client -o yaml > secret.yaml

# Selar o secret
kubeseal -f secret.yaml -w sealed-secret.yaml

# Aplicar sealed secret (pode ser commitado)
kubectl apply -f sealed-secret.yaml

# Usar no Helm
helm install my-crm ./bridal-cover-crm \
  --set postgresql.auth.existingSecret=postgres-secret
```

## Monitoramento

### Prometheus

```yaml
serviceMonitor:
  enabled: true
  interval: 30s
```

### Grafana Dashboard

Importe o dashboard ID: (a definir)

## Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature
3. Faça commit das mudanças
4. Push para a branch
5. Abra um Pull Request

## Licença

[Inserir licença]

## Suporte

- Issues: https://github.com/GuAntunes/bridal-cover-crm/issues
- Docs: https://github.com/GuAntunes/bridal-cover-crm/tree/main/docs


