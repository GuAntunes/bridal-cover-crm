# Kustomize - Guia Completo

## 📚 Índice

- [O que é Kustomize?](#o-que-é-kustomize)
- [Por que usar Kustomize?](#por-que-usar-kustomize)
- [Conceitos Básicos](#conceitos-básicos)
- [Estrutura de Diretórios](#estrutura-de-diretórios)
- [Como Funciona](#como-funciona)
- [Comandos Essenciais](#comandos-essenciais)
- [Exemplos Práticos](#exemplos-práticos)
- [Kustomize vs Helm](#kustomize-vs-helm)
- [Boas Práticas](#boas-práticas)
- [Troubleshooting](#troubleshooting)

---

## O que é Kustomize?

**Kustomize** é uma ferramenta **nativa do Kubernetes** (integrada ao `kubectl` desde a versão 1.14) que permite **customizar manifestos YAML** sem modificar os arquivos originais.

### Filosofia

> "Manage Kubernetes manifests without templates"

Ao invés de usar templates complexos (como Helm), o Kustomize usa **patches declarativos** sobre arquivos YAML base.

### Principais Características

- ✅ **Nativo** - Já vem no `kubectl`, sem instalação extra
- ✅ **Declarativo** - Usa YAML puro, sem lógica de templates
- ✅ **Composável** - Combina múltiplos arquivos de forma inteligente
- ✅ **Overlay Pattern** - Base + customizações específicas por ambiente
- ✅ **GitOps-friendly** - Perfeito para CI/CD e versionamento

---

## Por que usar Kustomize?

### Problema que Resolve

Imagine que você tem 3 ambientes: **Dev**, **Staging** e **Prod**.

**Sem Kustomize:**

```
k8s/
├── deployment-dev.yaml      ❌ Duplicação
├── deployment-staging.yaml  ❌ Duplicação
├── deployment-prod.yaml     ❌ Duplicação
├── service-dev.yaml         ❌ Hard to maintain
├── service-staging.yaml     ❌ Hard to maintain
└── service-prod.yaml        ❌ Hard to maintain
```

**Problemas:**
- Duplicação de código
- Difícil manter sincronizado
- Mudanças precisam ser replicadas manualmente
- Risco de inconsistências

**Com Kustomize:**

```
k8s/
├── base/                    ✅ Arquivos comuns (DRY)
│   ├── deployment.yaml
│   ├── service.yaml
│   └── kustomization.yaml
└── overlays/
    ├── dev/                 ✅ Apenas diferenças
    │   └── kustomization.yaml
    ├── staging/             ✅ Apenas diferenças
    │   └── kustomization.yaml
    └── prod/                ✅ Apenas diferenças
        └── kustomization.yaml
```

**Vantagens:**
- ✅ Um arquivo base, múltiplas variações
- ✅ Mudanças na base afetam todos os ambientes
- ✅ Customizações isoladas por ambiente
- ✅ Fácil de revisar (git diff)

---

## Conceitos Básicos

### 1. Base

**Base** é o conjunto de recursos Kubernetes **comuns a todos os ambientes**.

```yaml
# base/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - deployment.yaml
  - service.yaml
  - configmap.yaml

commonLabels:
  app: bridal-cover-crm
  managed-by: kustomize
```

### 2. Overlay

**Overlay** é uma **customização** aplicada sobre a base para um ambiente específico.

```yaml
# overlays/prod/kustomization.yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

# Herda da base
bases:
  - ../../base

# Customizações específicas de prod
nameSuffix: -prod
namespace: production

replicas:
  - name: bridal-cover-crm
    count: 5

images:
  - name: gustavoantunes/bridal-cover-crm
    newTag: v1.2.3
```

### 3. Patches

**Patches** modificam recursos existentes de forma cirúrgica.

```yaml
# overlays/prod/patch-resources.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bridal-cover-crm
spec:
  template:
    spec:
      containers:
      - name: bridal-cover-crm
        resources:
          limits:
            memory: "2Gi"
            cpu: "1000m"
```

---

## Estrutura de Diretórios

### Estrutura Recomendada

```
k8s/
├── base/                           # Recursos base
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── postgres-statefulset.yaml
│   ├── pgadmin.yaml
│   ├── ingress.yaml
│   └── kustomization.yaml
│
└── overlays/                       # Customizações por ambiente
    │
    ├── dev/
    │   ├── kustomization.yaml      # Referencia base + patches
    │   ├── configmap-dev.yaml      # ConfigMap específico de dev
    │   └── patch-replicas.yaml     # 1 réplica
    │
    ├── staging/
    │   ├── kustomization.yaml
    │   ├── configmap-staging.yaml
    │   ├── patch-replicas.yaml     # 2 réplicas
    │   └── patch-resources.yaml    # Mais recursos
    │
    └── prod/
        ├── kustomization.yaml
        ├── configmap-prod.yaml     # URLs de prod
        ├── secret-prod.yaml        # Senhas fortes
        ├── patch-replicas.yaml     # 5 réplicas
        ├── patch-resources.yaml    # Máximo de recursos
        └── ingress-tls.yaml        # SSL/TLS
```

### Nossa Estrutura Atual

```
k8s/
└── base/
    ├── deployment.yaml
    ├── service.yaml
    ├── configmap.yaml
    ├── secret.yaml
    ├── postgres-statefulset.yaml
    ├── pgadmin.yaml
    ├── ingress.yaml
    └── kustomization.yaml
```

**Status:** Temos a base pronta. Overlays podem ser adicionados quando necessário.

---

## Como Funciona

### Fluxo de Processamento

```
1. Kustomize lê o kustomization.yaml
   ↓
2. Carrega todos os resources listados
   ↓
3. Aplica transformações (patches, prefixos, etc)
   ↓
4. Mescla tudo em um único manifesto YAML
   ↓
5. kubectl apply no resultado final
```

### Exemplo Visual

**Entrada (Base):**

```yaml
# base/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app
spec:
  replicas: 1
  template:
    spec:
      containers:
      - name: app
        image: myapp:latest
```

**Transformação (Overlay):**

```yaml
# overlays/prod/kustomization.yaml
bases:
  - ../../base

nameSuffix: -prod
namespace: production
replicas:
  - name: app
    count: 5
images:
  - name: myapp
    newTag: v1.2.3
```

**Saída (Gerado):**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-prod          # ← Sufixo adicionado
  namespace: production   # ← Namespace adicionado
spec:
  replicas: 5             # ← Réplicas alteradas
  template:
    spec:
      containers:
      - name: app-prod
        image: myapp:v1.2.3  # ← Tag alterada
```

---

## Comandos Essenciais

### 1. Build (Visualizar o resultado)

```bash
# Ver o YAML final que será aplicado
kubectl kustomize k8s/base/

# Ou versão abreviada
kubectl kustomize k8s/overlays/prod/
```

### 2. Apply (Aplicar no cluster)

```bash
# Aplicar base
kubectl apply -k k8s/base/

# Aplicar overlay de prod
kubectl apply -k k8s/overlays/prod/

# Dry-run (ver sem aplicar)
kubectl apply -k k8s/base/ --dry-run=client -o yaml
```

### 3. Diff (Ver diferenças)

```bash
# Ver o que mudaria antes de aplicar
kubectl diff -k k8s/overlays/prod/
```

### 4. Delete

```bash
# Remover recursos
kubectl delete -k k8s/base/
```

### 5. Edit (Editar antes de aplicar)

```bash
# Gera YAML e abre no editor
kubectl kustomize k8s/base/ | kubectl apply -f -
```

---

## Exemplos Práticos

### Exemplo 1: Diferentes Réplicas por Ambiente

**base/kustomization.yaml**

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - deployment.yaml
  - service.yaml
```

**overlays/dev/kustomization.yaml**

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
  - ../../base

replicas:
  - name: bridal-cover-crm
    count: 1

commonLabels:
  environment: development
```

**overlays/prod/kustomization.yaml**

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
  - ../../base

replicas:
  - name: bridal-cover-crm
    count: 5

commonLabels:
  environment: production
```

**Deploy:**

```bash
# Dev (1 réplica)
kubectl apply -k k8s/overlays/dev/

# Prod (5 réplicas)
kubectl apply -k k8s/overlays/prod/
```

### Exemplo 2: ConfigMap Diferente por Ambiente

**base/configmap.yaml**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  log-level: "INFO"
```

**overlays/dev/configmap-patch.yaml**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  log-level: "DEBUG"
  database-url: "jdbc:postgresql://localhost:5432/dev_db"
```

**overlays/dev/kustomization.yaml**

```yaml
bases:
  - ../../base

patchesStrategicMerge:
  - configmap-patch.yaml
```

### Exemplo 3: Secret Diferente por Ambiente

**⚠️ IMPORTANTE:** Nunca commite secrets reais no Git!

**overlays/prod/kustomization.yaml**

```yaml
bases:
  - ../../base

# Gera secret a partir de arquivo (não commitado)
secretGenerator:
- name: db-credentials
  literals:
  - username=prod_user
  - password=STRONG_PROD_PASSWORD

# Ou a partir de arquivo
# files:
#   - password.txt
```

### Exemplo 4: Adicionar Ingress apenas em Prod

**overlays/prod/ingress.yaml**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: bridal-cover-crm
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  tls:
  - hosts:
    - bridalcrm.com
    secretName: bridalcrm-tls
  rules:
  - host: bridalcrm.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: bridal-cover-crm
            port:
              number: 8089
```

**overlays/prod/kustomization.yaml**

```yaml
bases:
  - ../../base

resources:
  - ingress.yaml  # Adiciona Ingress apenas em prod
```

### Exemplo 5: Recursos Diferentes por Ambiente

**overlays/prod/patch-resources.yaml**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bridal-cover-crm
spec:
  template:
    spec:
      containers:
      - name: bridal-cover-crm
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
```

**overlays/prod/kustomization.yaml**

```yaml
bases:
  - ../../base

patchesStrategicMerge:
  - patch-resources.yaml
```

---

## Kustomize vs Helm

| Aspecto | Kustomize | Helm |
|---------|-----------|------|
| **Instalação** | Nativo no kubectl | Precisa instalar separado |
| **Linguagem** | YAML puro | Templates Go (complexo) |
| **Curva de aprendizado** | Baixa | Média/Alta |
| **Versionamento** | Git direto | Charts versionados |
| **Reutilização** | Base + Overlays | Charts públicos |
| **Debugging** | Fácil (YAML) | Difícil (templates) |
| **Community** | Crescendo | Muito grande |
| **Package Manager** | ❌ Não | ✅ Sim |
| **Ideal para** | Seus próprios apps | Apps de terceiros |

### Quando usar cada um?

**Use Kustomize quando:**
- ✅ Seus próprios manifestos
- ✅ Múltiplos ambientes (dev/staging/prod)
- ✅ Quer simplicidade
- ✅ GitOps workflow

**Use Helm quando:**
- ✅ Instalar apps de terceiros (PostgreSQL, Redis, Nginx)
- ✅ Precisa compartilhar com outras equipes
- ✅ Versionamento complexo
- ✅ Package manager é necessário

**Use os dois juntos:**
- Helm para apps de terceiros
- Kustomize para suas aplicações customizadas

---

## Boas Práticas

### 1. Organização de Arquivos

✅ **DO:**
```
k8s/
├── base/              # Recursos compartilhados
└── overlays/          # Customizações específicas
    ├── dev/
    ├── staging/
    └── prod/
```

❌ **DON'T:**
```
k8s/
├── dev-deployment.yaml
├── staging-deployment.yaml
└── prod-deployment.yaml
```

### 2. Use CommonLabels

```yaml
commonLabels:
  app: bridal-cover-crm
  team: platform
  managed-by: kustomize
```

Facilita filtrar recursos: `kubectl get all -l app=bridal-cover-crm`

### 3. Use NamePrefix/NameSuffix

```yaml
nameSuffix: -prod
```

Evita conflitos entre ambientes no mesmo cluster.

### 4. Secrets no Git

❌ **NUNCA faça:**
```yaml
# secret.yaml
data:
  password: cGFzc3dvcmQxMjM=  # base64 não é segurança!
```

✅ **Faça:**
```yaml
# kustomization.yaml
secretGenerator:
- name: db-credentials
  literals:
  - password=${DB_PASSWORD}  # Variável de ambiente
```

Ou use:
- **Sealed Secrets**
- **External Secrets Operator**
- **HashiCorp Vault**

### 5. Use Patches Estratégicos

```yaml
# Patch apenas o necessário
patchesStrategicMerge:
  - patch-replicas.yaml
  - patch-image.yaml
```

Não duplique o deployment inteiro.

### 6. Validação

```bash
# Sempre valide antes de aplicar
kubectl kustomize k8s/overlays/prod/ | kubeval -

# Ou
kubectl apply -k k8s/overlays/prod/ --dry-run=client
```

### 7. Documentação

Sempre documente no `kustomization.yaml`:

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

# Customizações para ambiente de produção
# - 5 réplicas
# - Banco externo
# - TLS habilitado

bases:
  - ../../base
```

---

## Troubleshooting

### Problema 1: "no matches for kind"

**Erro:**
```
error: unable to recognize "STDIN": no matches for kind "XXX" in version "YYY"
```

**Solução:**
```bash
# Verificar versão da API
kubectl api-resources | grep XXX

# Atualizar apiVersion no YAML
```

### Problema 2: Patches não aplicam

**Causa:** Nome ou namespace incorreto no patch

**Solução:**
```yaml
# O patch DEVE ter exatamente o mesmo nome
# base/deployment.yaml
metadata:
  name: bridal-cover-crm

# patch.yaml
metadata:
  name: bridal-cover-crm  # ← Deve ser idêntico
```

### Problema 3: Conflito de recursos

**Erro:**
```
error: resource "xxx" already exists
```

**Solução:**
```yaml
# Use nameSuffix ou namePrefix
nameSuffix: -prod
```

### Problema 4: Secret não encontrado

**Causa:** Secret generator muda o nome

**Solução:**
```yaml
# Kustomize adiciona hash ao secret: db-credentials-abc123
# Use configMapGenerator com disableNameSuffixHash
secretGenerator:
- name: db-credentials
  disableNameSuffixHash: true
```

### Problema 5: Visualizar o que será aplicado

```bash
# Ver YAML completo gerado
kubectl kustomize k8s/overlays/prod/ | less

# Salvar em arquivo
kubectl kustomize k8s/overlays/prod/ > generated.yaml

# Aplicar com verbose
kubectl apply -k k8s/overlays/prod/ -v=8
```

---

## Referências

### Documentação Oficial

- [Kustomize.io](https://kustomize.io/)
- [Kubernetes Kustomize](https://kubernetes.io/docs/tasks/manage-kubernetes-objects/kustomization/)
- [Kustomize GitHub](https://github.com/kubernetes-sigs/kustomize)

### Tutoriais

- [Kustomize Example Patterns](https://github.com/kubernetes-sigs/kustomize/tree/master/examples)
- [GitOps with Kustomize](https://argoproj.github.io/argo-cd/user-guide/kustomize/)

### Ferramentas

- [kubeval](https://github.com/instrumenta/kubeval) - Validação de YAML
- [kustomizer](https://kustomizer.dev/) - Web UI para Kustomize
- [ArgoCD](https://argoproj.github.io/argo-cd/) - GitOps com Kustomize

---

## Próximos Passos

### Para o Projeto BridalCover CRM

1. **Criar overlays para diferentes ambientes:**
   ```bash
   mkdir -p k8s/overlays/{dev,staging,prod}
   ```

2. **Configurar dev com 1 réplica:**
   ```bash
   # k8s/overlays/dev/kustomization.yaml
   ```

3. **Configurar prod com 5 réplicas + TLS:**
   ```bash
   # k8s/overlays/prod/kustomization.yaml
   ```

4. **Integrar com CI/CD:**
   ```bash
   # Jenkins/GitHub Actions
   kubectl apply -k k8s/overlays/${ENVIRONMENT}/
   ```

5. **Adicionar validação no pipeline:**
   ```bash
   kubectl kustomize k8s/overlays/prod/ | kubeval -
   ```

---

**Criado por:** BridalCover CRM Platform Team  
**Data:** 2025-11-15  
**Versão:** 1.0

