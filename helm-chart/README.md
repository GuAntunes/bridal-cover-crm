# Helm Charts - Bridal Cover CRM

Este diretório contém os Helm Charts para deployar o Bridal Cover CRM no Kubernetes.

## 📦 Estrutura

```
helm-chart/
└── bridal-cover-crm/
    ├── Chart.yaml              # Metadados do chart
    ├── values.yaml             # Valores padrão
    ├── values-dev.yaml         # Valores para desenvolvimento
    ├── values-staging.yaml     # Valores para staging
    ├── values-prod.yaml        # Valores para produção
    ├── .helmignore            # Arquivos a ignorar
    ├── README.md              # Documentação do chart
    └── templates/             # Templates Kubernetes
        ├── _helpers.tpl       # Funções auxiliares
        ├── deployment.yaml    # Deployment
        ├── service.yaml       # Service
        ├── ingress.yaml       # Ingress
        ├── configmap.yaml     # ConfigMap
        ├── serviceaccount.yaml # ServiceAccount
        ├── hpa.yaml           # HorizontalPodAutoscaler
        ├── pdb.yaml           # PodDisruptionBudget
        └── NOTES.txt          # Notas pós-instalação
```

## 🚀 Quick Start

### 1. Instalar Helm

```bash
# macOS
brew install helm

# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Verificar instalação
helm version
```

### 2. Adicionar dependências

```bash
# Adicionar repositório Bitnami (para PostgreSQL)
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Atualizar dependências do chart
cd helm-chart/bridal-cover-crm
helm dependency update
```

### 3. Validar o Chart

```bash
# Lint (verificar problemas)
helm lint bridal-cover-crm/

# Ver templates gerados
helm template my-release bridal-cover-crm/

# Dry run
helm install my-release bridal-cover-crm/ --dry-run --debug
```

## 📝 Deployment por Ambiente

### Desenvolvimento

```bash
# Criar namespace
kubectl create namespace dev

# Instalar
helm install bridal-crm-dev ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-dev.yaml \
  -n dev

# Verificar
kubectl get pods -n dev
```

### Staging

```bash
# Criar namespace
kubectl create namespace staging

# Instalar com secrets externos
kubectl create secret generic postgres-staging-secret \
  --from-literal=password=secure-staging-password \
  -n staging

helm install bridal-crm-staging ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-staging.yaml \
  -n staging

# Verificar
kubectl get all -n staging
```

### Produção

```bash
# Criar namespace
kubectl create namespace production

# Criar secret do banco de dados
kubectl create secret generic postgres-prod-secret \
  --from-literal=password=$DB_PASSWORD \
  -n production

# Instalar
helm install bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  -n production

# Verificar deploy
helm status bridal-crm-prod -n production
kubectl get pods -n production -w
```

## 🔄 Atualização

```bash
# Atualizar imagem
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  --set image.tag=1.1.0 \
  -n production

# Atualizar com novos valores
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod-updated.yaml \
  -n production

# Ver diferenças (requer plugin helm-diff)
helm diff upgrade bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml
```

## ⏮️ Rollback

```bash
# Ver histórico
helm history bridal-crm-prod -n production

# Rollback para revisão anterior
helm rollback bridal-crm-prod -n production

# Rollback para revisão específica
helm rollback bridal-crm-prod 3 -n production
```

## 🔍 Debugging

```bash
# Ver valores aplicados
helm get values bridal-crm-prod -n production

# Ver todos os valores (incluindo defaults)
helm get values bridal-crm-prod --all -n production

# Ver manifestos gerados
helm get manifest bridal-crm-prod -n production

# Ver logs
kubectl logs -n production -l app.kubernetes.io/name=bridal-cover-crm -f

# Testar conectividade
kubectl port-forward -n production svc/bridal-crm-prod-bridal-cover-crm 8080:8080
curl http://localhost:8080/actuator/health
```

## 📦 Empacotamento

```bash
# Criar pacote .tgz
helm package bridal-cover-crm/

# Output: bridal-cover-crm-1.0.0.tgz

# Gerar índice (para repositório)
helm repo index .
```

## 🛠️ Customização

### Sobrescrever valores via CLI

```bash
helm install my-release ./bridal-cover-crm \
  --set replicaCount=5 \
  --set image.tag=1.2.0 \
  --set postgresql.auth.password=newpass
```

### Usar múltiplos arquivos de valores

```bash
helm install my-release ./bridal-cover-crm \
  -f values.yaml \
  -f values-prod.yaml \
  -f secrets.yaml
```

### Variáveis de ambiente customizadas

```yaml
# custom-values.yaml
app:
  extraEnv:
    - name: CUSTOM_API_KEY
      value: "my-api-key"
    - name: FEATURE_FLAG
      value: "true"
```

```bash
helm install my-release ./bridal-cover-crm -f custom-values.yaml
```

## 🔐 Gerenciamento de Secrets

### Opção 1: Valores inline (NÃO recomendado para produção)

```bash
helm install my-release ./bridal-cover-crm \
  --set postgresql.auth.password=mypassword
```

### Opção 2: Arquivo de secrets (gitignored)

```yaml
# secrets.yaml (adicionar ao .gitignore!)
postgresql:
  auth:
    password: supersecretpassword
```

```bash
helm install my-release ./bridal-cover-crm \
  -f values.yaml \
  -f secrets.yaml
```

### Opção 3: Sealed Secrets (recomendado)

```bash
# Instalar Sealed Secrets Controller
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# Criar e selar secret
kubectl create secret generic db-secret \
  --from-literal=password=mypassword \
  --dry-run=client -o yaml | \
  kubeseal -o yaml > sealed-secret.yaml

kubectl apply -f sealed-secret.yaml

# Usar no Helm
helm install my-release ./bridal-cover-crm \
  --set postgresql.auth.existingSecret=db-secret
```

### Opção 4: External Secrets Operator

```bash
# Instalar External Secrets
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets -n external-secrets-system --create-namespace

# Criar ExternalSecret
kubectl apply -f - <<EOF
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: db-secret
spec:
  secretStoreRef:
    name: aws-secretsmanager
    kind: SecretStore
  target:
    name: postgres-secret
  data:
  - secretKey: password
    remoteRef:
      key: prod/database/password
EOF
```

## 🧪 Testing

```bash
# Criar testes
cat > templates/tests/test-connection.yaml <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: "{{ include "bridal-cover-crm.fullname" . }}-test"
  annotations:
    "helm.sh/hook": test
spec:
  containers:
  - name: wget
    image: busybox
    command: ['wget']
    args: ['{{ include "bridal-cover-crm.fullname" . }}:{{ .Values.service.port }}/actuator/health']
  restartPolicy: Never
EOF

# Executar testes
helm test bridal-crm-prod -n production
```

## 📊 Monitoramento

### Verificar HPA

```bash
kubectl get hpa -n production
kubectl describe hpa bridal-crm-prod-bridal-cover-crm -n production
```

### Verificar métricas

```bash
kubectl top pods -n production
kubectl top nodes
```

### Dashboard do Kubernetes

```bash
kubectl proxy
# Acesse: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
```

## 🔧 Ferramentas Úteis

### Helm Plugins

```bash
# Helm Diff - Comparar mudanças
helm plugin install https://github.com/databus23/helm-diff

# Helm Secrets - Gerenciar secrets
helm plugin install https://github.com/jkroepke/helm-secrets

# Listar plugins
helm plugin list
```

### Helmfile (Gerenciador declarativo)

```yaml
# helmfile.yaml
releases:
  - name: bridal-crm-dev
    namespace: dev
    chart: ./bridal-cover-crm
    values:
      - ./bridal-cover-crm/values-dev.yaml
  
  - name: bridal-crm-staging
    namespace: staging
    chart: ./bridal-cover-crm
    values:
      - ./bridal-cover-crm/values-staging.yaml
  
  - name: bridal-crm-prod
    namespace: production
    chart: ./bridal-cover-crm
    values:
      - ./bridal-cover-crm/values-prod.yaml
```

```bash
# Instalar helmfile
brew install helmfile

# Aplicar
helmfile sync
```

## 📚 Documentação Adicional

- **[Helm Official Docs](https://helm.sh/docs/)**
- **[Chart Best Practices](https://helm.sh/docs/chart_best_practices/)**
- **[Helm Template Guide](https://helm.sh/docs/chart_template_guide/)**
- **[Guia Helm e Tiller](../docs/kubernetes/15-helm-tiller-guide.md)** - Documentação completa interna

## 🆘 Troubleshooting

### Chart não instala

```bash
# Verificar sintaxe
helm lint ./bridal-cover-crm

# Debug template
helm template test ./bridal-cover-crm --debug
```

### Pods não iniciam

```bash
# Ver eventos
kubectl get events -n production --sort-by='.lastTimestamp'

# Descrever pod
kubectl describe pod <pod-name> -n production

# Ver logs
kubectl logs <pod-name> -n production
```

### Erro de dependências

```bash
# Limpar cache
rm -rf bridal-cover-crm/charts/*

# Re-baixar dependências
helm dependency update bridal-cover-crm/
```

## 🤝 Contribuindo

1. Faça suas alterações no chart
2. Valide: `helm lint bridal-cover-crm/`
3. Teste: `helm install test ./bridal-cover-crm --dry-run --debug`
4. Incremente a versão em `Chart.yaml`
5. Atualize o `README.md` se necessário
6. Faça commit e PR

## 📄 Licença

[Inserir licença aqui]


