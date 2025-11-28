# Guia Completo: Helm e Tiller

## Índice

1. [Introdução](#introdução)
2. [O que é o Helm?](#o-que-é-o-helm)
3. [O que é o Tiller?](#o-que-é-o-tiller)
4. [Arquitetura do Helm](#arquitetura-do-helm)
5. [Helm 2 vs Helm 3](#helm-2-vs-helm-3)
6. [Conceitos Fundamentais](#conceitos-fundamentais)
7. [Instalação](#instalação)
8. [Configuração Inicial](#configuração-inicial)
9. [Utilização do Helm](#utilização-do-helm)
10. [Criando Charts Personalizados](#criando-charts-personalizados)
11. [Boas Práticas](#boas-práticas)
12. [Troubleshooting](#troubleshooting)
13. [Referências](#referências)

---

## Introdução

O Helm é o gerenciador de pacotes para Kubernetes, frequentemente chamado de "apt/yum do Kubernetes". Ele simplifica o deployment e gerenciamento de aplicações no Kubernetes através de pacotes reutilizáveis chamados **Charts**.

---

## O que é o Helm?

### Definição

**Helm** é uma ferramenta que ajuda a:
- **Definir**: Criar templates de recursos Kubernetes
- **Instalar**: Deployar aplicações no cluster
- **Atualizar**: Fazer upgrade de aplicações de forma controlada
- **Gerenciar**: Administrar o ciclo de vida completo das aplicações

### Principais Benefícios

1. **Reutilização**: Charts podem ser compartilhados e reutilizados
2. **Versionamento**: Controle de versões de aplicações
3. **Rollback**: Reversão fácil para versões anteriores
4. **Configuração**: Separação entre templates e valores
5. **Dependências**: Gerenciamento automático de dependências

### Como Funciona

```
Developer → Helm Chart → Helm CLI → Kubernetes API → Cluster
```

O Helm usa **Go templates** para criar manifestos Kubernetes dinamicamente, substituindo valores de acordo com arquivos de configuração.

---

## O que é o Tiller?

### Definição (Helm 2)

**Tiller** era o componente server-side do Helm 2 que rodava dentro do cluster Kubernetes. Ele era responsável por:

- Receber requisições do Helm Client
- Interagir diretamente com a API do Kubernetes
- Gerenciar releases e histórico
- Aplicar alterações no cluster

### Arquitetura do Tiller (Helm 2)

```
┌─────────────────┐
│   Helm Client   │  (CLI na máquina do desenvolvedor)
└────────┬────────┘
         │
         │ gRPC
         ↓
┌─────────────────┐
│     Tiller      │  (Pod rodando no cluster)
└────────┬────────┘
         │
         │ REST API
         ↓
┌─────────────────┐
│  Kubernetes API │
└─────────────────┘
```

### Problemas do Tiller

1. **Segurança**: 
   - Tiller tinha permissões muito amplas (cluster-admin)
   - Difícil implementar RBAC granular
   - Vulnerabilidades de segurança

2. **Complexidade**:
   - Componente adicional para gerenciar
   - Necessidade de inicialização (`helm init`)
   - Problemas de networking e service accounts

3. **Multi-tenancy**:
   - Difícil isolar usuários
   - Problemas com múltiplos Tillers no mesmo cluster

---

## Arquitetura do Helm

### Helm 2 (com Tiller)

```
┌──────────────────────────────────────────┐
│          Desenvolvedor/Ops               │
│                                          │
│  ┌────────────────┐                     │
│  │  Helm Client   │                     │
│  └────────┬───────┘                     │
└───────────┼──────────────────────────────┘
            │
            │ gRPC (porta 44134)
            ↓
┌──────────────────────────────────────────┐
│         Kubernetes Cluster               │
│                                          │
│  ┌────────────────┐                     │
│  │  Tiller Pod    │                     │
│  │  (kube-system) │                     │
│  └────────┬───────┘                     │
│           │                              │
│           │ Kubernetes API               │
│           ↓                              │
│  ┌────────────────┐                     │
│  │  API Server    │                     │
│  └────────────────┘                     │
│                                          │
│  Cria/Atualiza Recursos:                │
│  - Deployments                           │
│  - Services                              │
│  - ConfigMaps                            │
│  - etc.                                  │
└──────────────────────────────────────────┘
```

### Helm 3 (sem Tiller)

```
┌──────────────────────────────────────────┐
│          Desenvolvedor/Ops               │
│                                          │
│  ┌────────────────┐                     │
│  │  Helm Client   │                     │
│  └────────┬───────┘                     │
└───────────┼──────────────────────────────┘
            │
            │ Kubernetes API (direto)
            │ RBAC do usuário
            ↓
┌──────────────────────────────────────────┐
│         Kubernetes Cluster               │
│                                          │
│  ┌────────────────┐                     │
│  │  API Server    │                     │
│  └────────┬───────┘                     │
│           │                              │
│           │                              │
│           ↓                              │
│  Recursos Kubernetes                     │
│  + Secrets (release info)                │
│                                          │
└──────────────────────────────────────────┘
```

---

## Helm 2 vs Helm 3

### Principais Diferenças

| Aspecto | Helm 2 | Helm 3 |
|---------|--------|--------|
| **Tiller** | ✅ Necessário | ❌ Removido |
| **Segurança** | Tiller com cluster-admin | Usa RBAC do usuário |
| **Inicialização** | `helm init` necessário | Pronto para uso |
| **Armazenamento de Release** | ConfigMaps no kube-system | Secrets no namespace da release |
| **Chart API Version** | v1 | v2 |
| **Namespaces** | Opcional | Obrigatório |
| **3-Way Merge** | ❌ Não | ✅ Sim |
| **JSON Schema** | ❌ Não | ✅ Validação de valores |
| **Chart Dependencies** | requirements.yaml | Chart.yaml |
| **Bibliotecas** | ❌ Não | ✅ Library Charts |

### Por que o Tiller foi Removido?

**Motivos Principais:**

1. **Segurança**: Eliminou o ponto único de falha de segurança
2. **Simplicidade**: Menos componentes para gerenciar
3. **RBAC Nativo**: Usa as permissões do usuário kubectl
4. **Maturidade do Kubernetes**: A API do Kubernetes evoluiu
5. **Melhor Multi-tenancy**: Isolamento natural por namespace

---

## Conceitos Fundamentais

### Chart

Um **Chart** é um pacote Helm. Contém:

```
mychart/
├── Chart.yaml          # Metadados do chart
├── values.yaml         # Valores padrão de configuração
├── charts/             # Dependências (sub-charts)
├── templates/          # Templates dos recursos K8s
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── _helpers.tpl   # Funções auxiliares
│   └── NOTES.txt      # Notas pós-instalação
└── .helmignore        # Arquivos a ignorar
```

### Release

Uma **Release** é uma instância de um Chart rodando no cluster.

```bash
# Mesmo chart, múltiplas releases
helm install mysql-dev bitnami/mysql
helm install mysql-prod bitnami/mysql
```

### Repository

Um **Repository** é onde os Charts são armazenados e compartilhados.

```bash
# Repositórios oficiais
https://charts.helm.sh/stable
https://charts.bitnami.com/bitnami
```

### Values

**Values** são configurações que customizam o Chart:

```yaml
# values.yaml
replicaCount: 3
image:
  repository: nginx
  tag: "1.21"
service:
  type: LoadBalancer
  port: 80
```

---

## Instalação

### Helm 3 (Recomendado)

#### macOS

```bash
# Usando Homebrew
brew install helm

# Ou com script oficial
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

#### Linux (Ubuntu/Debian)

```bash
# Método 1: Snap
sudo snap install helm --classic

# Método 2: Script oficial
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Método 3: Download direto
wget https://get.helm.sh/helm-v3.13.0-linux-amd64.tar.gz
tar -zxvf helm-v3.13.0-linux-amd64.tar.gz
sudo mv linux-amd64/helm /usr/local/bin/helm
```

#### Windows

```powershell
# Usando Chocolatey
choco install kubernetes-helm

# Ou usando Scoop
scoop install helm
```

### Verificar Instalação

```bash
# Verificar versão
helm version

# Output esperado:
# version.BuildInfo{Version:"v3.13.0", GitCommit:"...", GitTreeState:"clean", GoVersion:"go1.21.0"}
```

### Helm 2 (Legado - Não Recomendado)

Se você ainda precisa usar Helm 2:

```bash
# Instalar Helm 2
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-2 | bash

# Inicializar Tiller
helm init

# Configurar service account (segurança)
kubectl create serviceaccount tiller --namespace kube-system
kubectl create clusterrolebinding tiller-cluster-rule \
  --clusterrole=cluster-admin \
  --serviceaccount=kube-system:tiller

# Atualizar Tiller com service account
helm init --service-account tiller --upgrade
```

#### ⚠️ Aviso de Segurança (Helm 2)

```bash
# NUNCA use Tiller em produção sem RBAC adequado
# O exemplo acima dá cluster-admin - use apenas para desenvolvimento/testes
```

---

## Configuração Inicial

### Adicionar Repositórios

```bash
# Adicionar repositório Bitnami (popular)
helm repo add bitnami https://charts.bitnami.com/bitnami

# Adicionar repositório Stable (oficial)
helm repo add stable https://charts.helm.sh/stable

# Adicionar repositório personalizado
helm repo add myrepo https://my-charts-repository.com

# Listar repositórios
helm repo list

# Atualizar índice dos repositórios
helm repo update
```

### Buscar Charts

```bash
# Buscar por nome
helm search repo nginx

# Buscar em todos os repositórios
helm search hub wordpress

# Ver versões disponíveis
helm search repo bitnami/mysql --versions
```

### Configurar Ambiente

```bash
# Definir namespace padrão
export HELM_NAMESPACE=my-namespace

# Configurar kubeconfig
export KUBECONFIG=~/.kube/config

# Debug mode
export HELM_DEBUG=true
```

---

## Utilização do Helm

### Instalar uma Release

```bash
# Instalação básica
helm install my-nginx bitnami/nginx

# Instalar em namespace específico
helm install my-nginx bitnami/nginx --namespace production --create-namespace

# Instalar com valores customizados
helm install my-nginx bitnami/nginx --set replicaCount=3

# Instalar com arquivo de valores
helm install my-nginx bitnami/nginx -f custom-values.yaml

# Dry run (simular instalação)
helm install my-nginx bitnami/nginx --dry-run --debug

# Gerar nome automático
helm install bitnami/nginx --generate-name
```

### Listar Releases

```bash
# Listar releases no namespace atual
helm list

# Listar em todos namespaces
helm list --all-namespaces

# Listar incluindo releases desinstaladas
helm list --all

# Formato JSON
helm list -o json
```

### Atualizar Release

```bash
# Atualizar com novos valores
helm upgrade my-nginx bitnami/nginx --set replicaCount=5

# Atualizar com arquivo de valores
helm upgrade my-nginx bitnami/nginx -f production-values.yaml

# Atualizar ou instalar (se não existir)
helm upgrade --install my-nginx bitnami/nginx

# Forçar atualização
helm upgrade my-nginx bitnami/nginx --force

# Atualizar com timeout
helm upgrade my-nginx bitnami/nginx --timeout 10m
```

### Ver Status

```bash
# Status da release
helm status my-nginx

# Valores usados na release
helm get values my-nginx

# Ver todos os valores (incluindo padrões)
helm get values my-nginx --all

# Ver manifests gerados
helm get manifest my-nginx

# Ver histórico de revisões
helm history my-nginx
```

### Rollback

```bash
# Voltar para revisão anterior
helm rollback my-nginx

# Voltar para revisão específica
helm rollback my-nginx 2

# Rollback com dry-run
helm rollback my-nginx 1 --dry-run

# Ver diferenças antes do rollback
helm diff rollback my-nginx 2
```

### Desinstalar Release

```bash
# Desinstalar release
helm uninstall my-nginx

# Desinstalar mantendo histórico (Helm 2 style)
helm uninstall my-nginx --keep-history

# Desinstalar com timeout
helm uninstall my-nginx --timeout 5m
```

### Inspecionar Charts

```bash
# Mostrar informações do chart
helm show chart bitnami/nginx

# Mostrar valores padrão
helm show values bitnami/nginx

# Mostrar tudo
helm show all bitnami/nginx

# Mostrar README
helm show readme bitnami/nginx

# Baixar chart sem instalar
helm pull bitnami/nginx

# Baixar e descompactar
helm pull bitnami/nginx --untar
```

---

## Criando Charts Personalizados

### Criar Estrutura Básica

```bash
# Criar novo chart
helm create bridal-cover-app

# Estrutura criada:
# bridal-cover-app/
# ├── Chart.yaml
# ├── values.yaml
# ├── charts/
# └── templates/
#     ├── deployment.yaml
#     ├── service.yaml
#     ├── ingress.yaml
#     ├── _helpers.tpl
#     └── NOTES.txt
```

### Chart.yaml

```yaml
apiVersion: v2
name: bridal-cover-app
description: CRM para noivas - Backend API
type: application
version: 1.0.0
appVersion: "0.0.1-SNAPSHOT"
keywords:
  - crm
  - kotlin
  - spring-boot
home: https://github.com/GuAntunes/bridal-cover-crm
sources:
  - https://github.com/GuAntunes/bridal-cover-crm
maintainers:
  - name: Gustavo Antunes
    email: gustavo@example.com
dependencies:
  - name: postgresql
    version: 12.x.x
    repository: https://charts.bitnami.com/bitnami
    condition: postgresql.enabled
```

### values.yaml

```yaml
# values.yaml
replicaCount: 3

image:
  repository: guantunes/bridal-cover-crm
  pullPolicy: IfNotPresent
  tag: "latest"

service:
  type: ClusterIP
  port: 8080

ingress:
  enabled: true
  className: nginx
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: api.bridalcover.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: bridalcover-tls
      hosts:
        - api.bridalcover.com

resources:
  limits:
    cpu: 1000m
    memory: 1Gi
  requests:
    cpu: 500m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80

postgresql:
  enabled: true
  auth:
    username: bridalcover
    password: changeme
    database: bridalcover_db
  primary:
    persistence:
      enabled: true
      size: 10Gi
```

### Templates com Go Template

**templates/deployment.yaml**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "bridal-cover-app.fullname" . }}
  labels:
    {{- include "bridal-cover-app.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "bridal-cover-app.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "bridal-cover-app.selectorLabels" . | nindent 8 }}
    spec:
      containers:
      - name: {{ .Chart.Name }}
        image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
        imagePullPolicy: {{ .Values.image.pullPolicy }}
        ports:
        - name: http
          containerPort: {{ .Values.service.port }}
          protocol: TCP
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: http
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: http
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          {{- toYaml .Values.resources | nindent 10 }}
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: {{ .Values.environment }}
        {{- if .Values.postgresql.enabled }}
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://{{ include "bridal-cover-app.fullname" . }}-postgresql:5432/{{ .Values.postgresql.auth.database }}"
        - name: SPRING_DATASOURCE_USERNAME
          value: {{ .Values.postgresql.auth.username }}
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: {{ include "bridal-cover-app.fullname" . }}-postgresql
              key: password
        {{- end }}
```

**templates/_helpers.tpl**

```yaml
{{/*
Expand the name of the chart.
*/}}
{{- define "bridal-cover-app.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "bridal-cover-app.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "bridal-cover-app.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "bridal-cover-app.labels" -}}
helm.sh/chart: {{ include "bridal-cover-app.chart" . }}
{{ include "bridal-cover-app.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "bridal-cover-app.selectorLabels" -}}
app.kubernetes.io/name: {{ include "bridal-cover-app.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
```

### Validar Chart

```bash
# Validar sintaxe do chart
helm lint bridal-cover-app/

# Verificar templates gerados
helm template my-release bridal-cover-app/

# Dry run completo
helm install my-release bridal-cover-app/ --dry-run --debug

# Validar com valores específicos
helm template my-release bridal-cover-app/ -f values-prod.yaml
```

### Empacotar Chart

```bash
# Criar pacote .tgz
helm package bridal-cover-app/

# Output: bridal-cover-app-1.0.0.tgz

# Criar com dependências
helm package bridal-cover-app/ --dependency-update

# Assinar pacote
helm package bridal-cover-app/ --sign --key mykey
```

### Instalar Chart Local

```bash
# Instalar do diretório
helm install my-release ./bridal-cover-app/

# Instalar do pacote
helm install my-release bridal-cover-app-1.0.0.tgz

# Instalar com valores customizados
helm install my-release ./bridal-cover-app/ \
  --set postgresql.auth.password=secret123 \
  --set replicaCount=5
```

---

## Boas Práticas

### 1. Estrutura de Valores

```yaml
# ✅ BOM: Valores bem organizados e documentados
# values.yaml

# Configuração da aplicação
app:
  # Nome da aplicação
  name: bridal-cover-crm
  # Ambiente (dev, staging, prod)
  environment: dev

# Configuração de imagem
image:
  # Repositório da imagem Docker
  repository: guantunes/bridal-cover-crm
  # Tag da imagem
  tag: "1.0.0"
  # Política de pull
  pullPolicy: IfNotPresent

# ❌ RUIM: Valores desorganizados
imageRepo: guantunes/bridal-cover-crm
imgTag: latest
pullpolicy: Always
```

### 2. Usar Helpers para Reutilização

```yaml
# templates/_helpers.tpl

{{/* Gerar nome completo */}}
{{- define "app.fullname" -}}
{{- printf "%s-%s" .Release.Name .Chart.Name }}
{{- end }}

# Usar nos templates
metadata:
  name: {{ include "app.fullname" . }}
```

### 3. Validação com JSON Schema

```yaml
# values.schema.json
{
  "$schema": "https://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["replicaCount", "image"],
  "properties": {
    "replicaCount": {
      "type": "integer",
      "minimum": 1,
      "maximum": 100
    },
    "image": {
      "type": "object",
      "required": ["repository", "tag"],
      "properties": {
        "repository": {
          "type": "string"
        },
        "tag": {
          "type": "string"
        }
      }
    }
  }
}
```

### 4. Versionamento Semântico

```yaml
# Chart.yaml
apiVersion: v2
name: my-app
# MAJOR.MINOR.PATCH
# MAJOR: mudanças incompatíveis
# MINOR: novas funcionalidades compatíveis
# PATCH: correções de bugs
version: 1.2.3
appVersion: "2.0.1"
```

### 5. Notas de Instalação

```yaml
# templates/NOTES.txt
Parabéns! {{ .Chart.Name }} foi instalado com sucesso.

Release: {{ .Release.Name }}
Namespace: {{ .Release.Namespace }}

Para acessar a aplicação:

{{- if .Values.ingress.enabled }}
  URL: https://{{ (index .Values.ingress.hosts 0).host }}
{{- else }}
  Execute:
  kubectl port-forward svc/{{ include "app.fullname" . }} 8080:{{ .Values.service.port }}
  
  Acesse: http://localhost:8080
{{- end }}

Para ver os logs:
  kubectl logs -f deployment/{{ include "app.fullname" . }}
```

### 6. Separar Ambientes

```bash
# values-dev.yaml
replicaCount: 1
resources:
  limits:
    memory: 512Mi

# values-staging.yaml
replicaCount: 2
resources:
  limits:
    memory: 1Gi

# values-prod.yaml
replicaCount: 5
resources:
  limits:
    memory: 2Gi
autoscaling:
  enabled: true

# Usar
helm install app ./chart -f values-prod.yaml
```

### 7. Gerenciar Secrets com Cuidado

```bash
# ❌ NUNCA commitar secrets no Git
# values.yaml
database:
  password: mypassword123

# ✅ Usar Sealed Secrets, SOPS ou External Secrets
# ✅ Ou passar via --set durante deploy
helm install app ./chart --set database.password=$DB_PASSWORD

# ✅ Ou usar arquivo externo (não versionado)
helm install app ./chart -f secrets.yaml
```

### 8. Dependências

```yaml
# Chart.yaml
dependencies:
  - name: postgresql
    version: "12.x.x"
    repository: https://charts.bitnami.com/bitnami
    condition: postgresql.enabled
  
  - name: redis
    version: "17.x.x"
    repository: https://charts.bitnami.com/bitnami
    condition: redis.enabled

# Atualizar dependências
# helm dependency update ./chart
```

### 9. Hooks do Helm

```yaml
# templates/db-migration-job.yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: {{ include "app.fullname" . }}-db-migration
  annotations:
    # Executar antes da instalação
    "helm.sh/hook": pre-install,pre-upgrade
    # Ordem de execução
    "helm.sh/hook-weight": "0"
    # Deletar após sucesso
    "helm.sh/hook-delete-policy": hook-succeeded
spec:
  template:
    spec:
      containers:
      - name: db-migration
        image: {{ .Values.image.repository }}:{{ .Values.image.tag }}
        command: ["./migrate.sh"]
      restartPolicy: Never
```

### 10. Testing

```bash
# Criar teste
cat << 'EOF' > templates/tests/test-connection.yaml
apiVersion: v1
kind: Pod
metadata:
  name: "{{ include "app.fullname" . }}-test-connection"
  annotations:
    "helm.sh/hook": test
spec:
  containers:
  - name: wget
    image: busybox
    command: ['wget']
    args: ['{{ include "app.fullname" . }}:{{ .Values.service.port }}']
  restartPolicy: Never
EOF

# Executar testes
helm test my-release
```

---

## Troubleshooting

### Problemas Comuns

#### 1. Release Stuck in Pending

```bash
# Verificar status
helm status my-release

# Ver pods
kubectl get pods -l app.kubernetes.io/instance=my-release

# Ver eventos
kubectl get events --sort-by='.lastTimestamp'

# Forçar delete
helm uninstall my-release --no-hooks
```

#### 2. Erro de Template

```bash
# Debug template
helm template my-release ./chart --debug

# Validar com valores específicos
helm template my-release ./chart -f values.yaml --debug | less

# Lint para encontrar erros
helm lint ./chart
```

#### 3. Valores Não Aplicados

```bash
# Verificar valores atuais
helm get values my-release

# Ver todos os valores (incluindo defaults)
helm get values my-release --all

# Comparar com esperado
diff <(helm get values my-release --all) values.yaml
```

#### 4. Rollback Falhou

```bash
# Ver histórico
helm history my-release

# Rollback para revisão específica
helm rollback my-release 3

# Se ainda falhar, reinstalar
helm uninstall my-release
helm install my-release ./chart
```

#### 5. Chart Dependencies

```bash
# Erro: dependency not found
# Solução: Atualizar dependências
helm dependency update ./chart

# Listar dependências
helm dependency list ./chart

# Limpar cache
rm -rf ./chart/charts/*
helm dependency update ./chart
```

### Debug Avançado

```bash
# Modo debug completo
helm install my-release ./chart --debug --dry-run > debug.yaml

# Ver o que será aplicado
helm diff upgrade my-release ./chart

# Verificar permissões RBAC
kubectl auth can-i create deployments
kubectl auth can-i create services

# Ver logs do Tiller (Helm 2)
kubectl logs -n kube-system -l name=tiller
```

### Comandos Úteis

```bash
# Verificar conexão com cluster
helm version
kubectl cluster-info

# Limpar releases antigas
helm list --all-namespaces | grep -v deployed | awk '{print $1, $2}' | xargs -L1 helm uninstall

# Exportar manifests de release existente
helm get manifest my-release > current-state.yaml

# Comparar charts
diff <(helm template release1 ./chart1) <(helm template release2 ./chart2)
```

---

## Migração Helm 2 para Helm 3

### Preparação

```bash
# Backup de releases Helm 2
helm2 list --all --output yaml > helm2-releases-backup.yaml

# Instalar plugin de migração
helm3 plugin install https://github.com/helm/helm-2to3
```

### Migração

```bash
# Migrar configuração
helm3 2to3 move config

# Migrar repositórios
helm3 repo list

# Migrar release específica
helm3 2to3 convert my-release

# Migrar todas as releases
helm3 2to3 convert --all

# Limpar Helm 2 (após verificar que tudo funciona)
helm3 2to3 cleanup
```

### Limpeza do Tiller

```bash
# Remover Tiller deployment
kubectl delete deployment tiller-deploy -n kube-system

# Remover service account
kubectl delete serviceaccount tiller -n kube-system

# Remover cluster role binding
kubectl delete clusterrolebinding tiller-cluster-rule

# Verificar que foi removido
kubectl get all -n kube-system | grep tiller
```

---

## Referências

### Documentação Oficial

- **Helm Docs**: https://helm.sh/docs/
- **Chart Best Practices**: https://helm.sh/docs/chart_best_practices/
- **Chart Template Guide**: https://helm.sh/docs/chart_template_guide/
- **Helm GitHub**: https://github.com/helm/helm

### Repositórios de Charts

- **Artifact Hub**: https://artifacthub.io/
- **Bitnami Charts**: https://github.com/bitnami/charts
- **Helm Stable (deprecated)**: https://github.com/helm/charts

### Ferramentas Complementares

- **Helmfile**: Gerenciador declarativo de releases
  - https://github.com/roboll/helmfile
  
- **Helm Diff**: Plugin para ver diferenças
  - https://github.com/databus23/helm-diff
  
- **Helm Secrets**: Gerenciar secrets criptografados
  - https://github.com/jkroepke/helm-secrets
  
- **Chart Testing**: Ferramenta de testes do Helm
  - https://github.com/helm/chart-testing

### Tutoriais e Guias

- **Helm Learning Path**: https://helm.sh/docs/intro/using_helm/
- **CNCF Helm Introduction**: https://www.cncf.io/projects/helm/
- **Kubernetes Package Management**: https://kubernetes.io/docs/tasks/manage-kubernetes-objects/

### Comunidade

- **Helm Slack**: https://slack.k8s.io/ (#helm-users)
- **Stack Overflow**: Tag `helm`
- **Reddit**: r/kubernetes

---

## Conclusão

O **Helm** revolucionou o gerenciamento de aplicações no Kubernetes, e a remoção do **Tiller** no Helm 3 tornou a ferramenta mais segura e simples. 

### Principais Aprendizados:

1. ✅ **Use Helm 3**: Mais seguro, sem Tiller
2. ✅ **Charts são reutilizáveis**: DRY principle
3. ✅ **Values separados por ambiente**: dev, staging, prod
4. ✅ **Versionamento semântico**: Controle de mudanças
5. ✅ **Templates bem estruturados**: Helpers e boas práticas
6. ✅ **Testes automatizados**: Helm test
7. ✅ **Documentação**: README e NOTES.txt

### Próximos Passos:

1. Criar charts personalizados para suas aplicações
2. Implementar CI/CD com Helm
3. Explorar Helmfile para gerenciamento declarativo
4. Integrar com ArgoCD ou FluxCD (GitOps)
5. Implementar chart museum privado

**Helm torna o Kubernetes mais acessível e gerenciável! 🚀**

