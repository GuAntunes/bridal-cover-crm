# Boas Práticas do Kubernetes

Guia de melhores práticas para usar Kubernetes em produção.

## 1. Organização

### Use Namespaces

Separe ambientes e aplicações usando namespaces:

```yaml
# Criar namespaces para diferentes ambientes
kubectl create namespace development
kubectl create namespace staging
kubectl create namespace production
```

### Labels Consistentes

Use labels padronizadas em todos os recursos:

```yaml
metadata:
  labels:
    app.kubernetes.io/name: myapp
    app.kubernetes.io/instance: myapp-prod
    app.kubernetes.io/version: "1.0.0"
    app.kubernetes.io/component: backend
    app.kubernetes.io/part-of: bridal-cover-crm
    app.kubernetes.io/managed-by: helm
    environment: production
    team: backend
```

### Annotations para Metadados

Use annotations para informações adicionais:

```yaml
metadata:
  annotations:
    description: "Backend API for Bridal Cover CRM"
    owner: "backend-team@company.com"
    repository: "https://github.com/company/bridal-cover-crm"
    documentation: "https://docs.company.com/bridal-cover-crm"
```

---

## 2. Segurança

### Não Execute Containers como Root

```yaml
securityContext:
  runAsNonRoot: true
  runAsUser: 1000
  fsGroup: 1000
  capabilities:
    drop:
      - ALL
  readOnlyRootFilesystem: true
```

### Use RBAC

Controle permissões com Role-Based Access Control:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: pod-reader
  namespace: default
rules:
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: read-pods
  namespace: default
subjects:
- kind: User
  name: jane
  apiGroup: rbac.authorization.k8s.io
roleRef:
  kind: Role
  name: pod-reader
  apiGroup: rbac.authorization.k8s.io
```

### Use Secrets para Informações Sensíveis

Nunca use ConfigMaps para dados sensíveis:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
stringData:
  username: admin
  password: super-secret-password
```

### Network Policies

Controle tráfego entre pods:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: backend-network-policy
spec:
  podSelector:
    matchLabels:
      app: backend
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: frontend
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: database
    ports:
    - protocol: TCP
      port: 5432
```

### Escaneie Imagens

```bash
# Use ferramentas como Trivy
trivy image myapp:latest

# Ou Snyk
snyk container test myapp:latest
```

---

## 3. Recursos (Requests e Limits)

### Sempre Defina Requests e Limits

```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

### Diretrizes por Tipo de Aplicação

**Backend API (Spring Boot):**
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

**Frontend (React/Angular):**
```yaml
resources:
  requests:
    memory: "128Mi"
    cpu: "100m"
  limits:
    memory: "256Mi"
    cpu: "200m"
```

**Database (PostgreSQL):**
```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "1000m"
  limits:
    memory: "2Gi"
    cpu: "2000m"
```

### Resource Quotas por Namespace

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: compute-quota
  namespace: development
spec:
  hard:
    requests.cpu: "10"
    requests.memory: "20Gi"
    limits.cpu: "20"
    limits.memory: "40Gi"
    persistentvolumeclaims: "10"
```

---

## 4. Health Checks

### Liveness Probe

Verifica se container está vivo:

```yaml
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

### Readiness Probe

Verifica se container está pronto para receber tráfego:

```yaml
readinessProbe:
  httpGet:
    path: /ready
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
```

### Startup Probe

Para aplicações que demoram para iniciar:

```yaml
startupProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 0
  periodSeconds: 10
  timeoutSeconds: 3
  failureThreshold: 30  # 30 * 10s = 5 minutos para iniciar
```

### Exemplo Completo

```yaml
spec:
  containers:
  - name: app
    image: myapp:1.0.0
    ports:
    - containerPort: 8080
    livenessProbe:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
      initialDelaySeconds: 60
      periodSeconds: 10
    readinessProbe:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 5
    startupProbe:
      httpGet:
        path: /actuator/health
        port: 8080
      failureThreshold: 30
      periodSeconds: 10
```

---

## 5. Deployment Strategies

### Rolling Update (Padrão)

```yaml
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1        # Máximo de pods extras durante update
      maxUnavailable: 0  # Mínimo de pods disponíveis durante update
```

### Recreate

Todos os pods antigos são deletados antes de criar novos:

```yaml
spec:
  strategy:
    type: Recreate
```

### Blue-Green Deployment

Use labels para alternar entre versões:

```bash
# Deploy versão green
kubectl apply -f deployment-green.yaml

# Testar versão green
kubectl port-forward deployment/myapp-green 8080:8080

# Alternar tráfego
kubectl patch service myapp -p '{"spec":{"selector":{"version":"green"}}}'
```

### Canary Deployment

```yaml
# Deployment principal (90% tráfego)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-stable
spec:
  replicas: 9
  # ...

---
# Deployment canary (10% tráfego)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-canary
spec:
  replicas: 1
  # ... (imagem com nova versão)
```

---

## 6. Configuração

### Separe Configuração do Código

Use ConfigMaps e Secrets:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  application.yaml: |
    server:
      port: 8080
    spring:
      profiles:
        active: production
```

### Versionamento de Configuração

```yaml
metadata:
  name: app-config-v2
  labels:
    version: "2.0"
```

### Externalização de Configuração

```yaml
envFrom:
- configMapRef:
    name: app-config
- secretRef:
    name: app-secrets
env:
- name: DATABASE_URL
  valueFrom:
    configMapKeyRef:
      name: app-config
      key: database.url
```

---

## 7. Storage e Persistência

### Use StatefulSets para Aplicações Stateful

```yaml
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgresql
spec:
  serviceName: postgresql
  replicas: 1
  selector:
    matchLabels:
      app: postgresql
  template:
    metadata:
      labels:
        app: postgresql
    spec:
      containers:
      - name: postgresql
        image: postgres:14
        volumeMounts:
        - name: data
          mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:
  - metadata:
      name: data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 10Gi
```

### Backup Regular

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: database-backup
spec:
  schedule: "0 2 * * *"  # Diariamente às 2h
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: backup-tool:latest
            command: ["/backup.sh"]
          restartPolicy: OnFailure
```

---

## 8. Observabilidade

### Logging Estruturado

Configure aplicações para logar em JSON:

```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "level": "INFO",
  "service": "backend-api",
  "message": "Request processed",
  "duration_ms": 45,
  "user_id": "12345"
}
```

### Prometheus Annotations

```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/actuator/prometheus"
```

### Distributed Tracing

Use headers de correlação:

```yaml
env:
- name: OTEL_SERVICE_NAME
  value: "backend-api"
- name: OTEL_TRACES_EXPORTER
  value: "jaeger"
```

---

## 9. PodDisruptionBudget

Garanta disponibilidade durante manutenção:

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: myapp-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: myapp
```

Ou use `maxUnavailable`:

```yaml
spec:
  maxUnavailable: 1
  selector:
    matchLabels:
      app: myapp
```

---

## 10. Affinity e Anti-Affinity

### Pod Anti-Affinity

Distribua pods entre nodes diferentes:

```yaml
affinity:
  podAntiAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
    - labelSelector:
        matchExpressions:
        - key: app
          operator: In
          values:
          - myapp
      topologyKey: kubernetes.io/hostname
```

### Node Affinity

Escolha nodes específicos:

```yaml
affinity:
  nodeAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      nodeSelectorTerms:
      - matchExpressions:
        - key: node-type
          operator: In
          values:
          - high-memory
```

---

## 11. GitOps

### Versionamento de Manifests

```bash
# Estrutura de diretórios
k8s/
├── base/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── kustomization.yaml
├── overlays/
│   ├── development/
│   │   ├── kustomization.yaml
│   │   └── patch.yaml
│   ├── staging/
│   │   ├── kustomization.yaml
│   │   └── patch.yaml
│   └── production/
│       ├── kustomization.yaml
│       └── patch.yaml
```

### Use Helm ou Kustomize

```bash
# Helm
helm install myapp ./chart -f values-prod.yaml

# Kustomize
kubectl apply -k overlays/production
```

---

## 12. Testes

### Dry-run

```bash
# Verificar sintaxe sem aplicar
kubectl apply -f deployment.yaml --dry-run=client

# Verificar no servidor
kubectl apply -f deployment.yaml --dry-run=server
```

### Validação de YAML

```bash
# Validar com kubeval
kubeval deployment.yaml

# Ou com yamllint
yamllint deployment.yaml
```

### Testes de Smoke

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: smoke-test
spec:
  containers:
  - name: curl
    image: curlimages/curl
    command: ['sh', '-c', 'curl -f http://myapp:8080/health']
  restartPolicy: Never
```

---

## Próximos Passos

Agora que você conhece as boas práticas:

- **[Kubernetes Dashboard](10-dashboard-setup.md)** - Visualizar recursos graficamente
- **[Referências](11-references.md)** - Ferramentas e recursos adicionais

---

[← Anterior: Comandos Essenciais](08-essential-commands.md) | [Voltar ao Índice](README.md) | [Próximo: Dashboard →](10-dashboard-setup.md)

