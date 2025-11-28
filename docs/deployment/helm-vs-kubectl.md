# Helm vs kubectl - Quando Usar Cada Um?

## 🤔 A Confusão Comum

Muitos desenvolvedores começam com `kubectl apply` e depois migram para Helm, acabando com **recursos duplicados** em dois lugares:

```
❌ PROBLEMA:
k8s/deployment.yaml  ┐
                     ├─> Ambos gerenciam o mesmo recurso = CONFLITO!
helm-chart/          ┘
```

Este guia explica **quando usar cada ferramenta** e **como organizar seu projeto**.

---

## 📊 Comparação Rápida

| Aspecto | kubectl apply | Helm |
|---------|---------------|------|
| **Complexidade** | Simples | Moderada |
| **Gestão de Releases** | ❌ Não | ✅ Sim |
| **Rollback** | ❌ Manual | ✅ Automático |
| **Templates** | ❌ Não | ✅ Sim (Go templates) |
| **Versioning** | ❌ Não | ✅ Sim |
| **Ambientes (dev/staging/prod)** | ⚠️ Kustomize | ✅ Values files |
| **Dependências** | ❌ Manual | ✅ Charts dependencies |
| **Ideal para** | Infra base | Aplicações |

---

## ✅ Quando Usar `kubectl apply`

### Use para **recursos de infraestrutura base** que:
1. São criados **uma vez** e raramente mudam
2. Existem **independente** das aplicações
3. São **globais** (não pertencem a um namespace específico)
4. **Não precisam** de rollback ou versionamento

### Exemplos:
```yaml
# ✅ BOM - Infraestrutura base
- PersistentVolumes (storage físico)
- StorageClasses
- Namespaces
- ClusterRoles / ClusterRoleBindings
- CustomResourceDefinitions (CRDs)
- IngressClass
```

### Estrutura Recomendada:
```
k8s/
└── infrastructure/
    ├── namespaces.yaml
    ├── postgres-volumes.yaml
    ├── storage-class.yaml
    └── README.md
```

### Como Aplicar:
```bash
# Aplicar uma vez
kubectl apply -f k8s/infrastructure/

# Verificar
kubectl get pv
kubectl get namespaces
```

---

## ✅ Quando Usar `Helm`

### Use para **aplicações** que:
1. Precisam de **múltiplos ambientes** (dev, staging, prod)
2. Requerem **rollback** em caso de problema
3. Têm **dependências** (ex: app + banco de dados)
4. Precisam de **templates** reutilizáveis
5. São **deployadas frequentemente**

### Exemplos:
```yaml
# ✅ BOM - Aplicações
- Backend API (Spring Boot, Node.js, etc.)
- Frontend (React, Vue, Angular)
- Bancos de dados (PostgreSQL, MySQL como dependência)
- Message brokers (Kafka, RabbitMQ)
- Cache (Redis)
```

### Estrutura Recomendada:
```
helm-chart/
├── backend/
│   ├── Chart.yaml
│   ├── values.yaml           # Valores padrão
│   ├── values-dev.yaml       # Overrides para dev
│   ├── values-staging.yaml   # Overrides para staging
│   ├── values-prod.yaml      # Overrides para prod
│   └── templates/
│       ├── deployment.yaml
│       ├── service.yaml
│       ├── ingress.yaml
│       └── configmap.yaml
│
└── frontend/
    ├── Chart.yaml
    └── templates/...
```

### Como Deployar:
```bash
# Deploy inicial
helm install bridal-crm-dev helm-chart/backend \
  --namespace dev \
  --create-namespace \
  --values helm-chart/backend/values-dev.yaml

# Atualizar
helm upgrade bridal-crm-dev helm-chart/backend \
  --namespace dev \
  --values helm-chart/backend/values-dev.yaml

# Rollback
helm rollback bridal-crm-dev -n dev

# Ver histórico
helm history bridal-crm-dev -n dev

# Desinstalar
helm uninstall bridal-crm-dev -n dev
```

---

## 🎯 Regra de Ouro

```
┌─────────────────────────────────────────────────────────┐
│  REGRA: Um recurso deve ser gerenciado por              │
│         APENAS UMA ferramenta (kubectl OU Helm)         │
└─────────────────────────────────────────────────────────┘
```

### ❌ NUNCA faça isso:
```bash
# Criar com Helm
helm install myapp ./chart

# Depois modificar com kubectl
kubectl apply -f deployment.yaml  # ❌ CONFLITO!
```

**Problema:** Helm não sabe sobre mudanças feitas via kubectl, causando:
- 🔴 Estado inconsistente
- 🔴 Rollback quebrado
- 🔴 Confusão sobre qual é a "fonte da verdade"

---

## 🏗️ Arquitetura Recomendada: Híbrida

### Separação clara de responsabilidades:

```
PROJECT/
│
├── k8s/                          
│   └── infrastructure/           ← kubectl (infraestrutura)
│       ├── namespaces.yaml
│       ├── postgres-volumes.yaml
│       └── storage-class.yaml
│
└── helm-chart/                   ← Helm (aplicações)
    ├── backend/
    │   ├── Chart.yaml
    │   ├── values-dev.yaml
    │   ├── values-staging.yaml
    │   ├── values-prod.yaml
    │   └── templates/
    │       ├── deployment.yaml
    │       ├── service.yaml
    │       └── configmap.yaml
    │
    └── frontend/
        ├── Chart.yaml
        └── templates/...
```

---

## 🚀 Workflow de Deploy Completo

### 1️⃣ Setup Inicial (Uma vez)

```bash
# Infraestrutura base com kubectl
kubectl apply -f k8s/infrastructure/

# Criar diretórios para PVs (se necessário)
sudo mkdir -p /mnt/data/postgres-{dev,staging,prod}
sudo chmod -R 777 /mnt/data/
```

### 2️⃣ Deploy da Aplicação (Sempre)

```bash
# Backend com Helm
helm upgrade --install bridal-crm-dev helm-chart/backend \
  --namespace dev \
  --create-namespace \
  --values helm-chart/backend/values-dev.yaml

# Frontend com Helm (quando pronto)
helm upgrade --install bridal-crm-frontend-dev helm-chart/frontend \
  --namespace dev \
  --values helm-chart/frontend/values-dev.yaml
```

### 3️⃣ Verificar

```bash
# Helm releases
helm list -A

# Pods
kubectl get pods -n dev

# Logs
kubectl logs -n dev -l app.kubernetes.io/name=backend
```

---

## 🔄 Kustomize como Alternativa?

**Kustomize** é um meio-termo entre kubectl e Helm:

### Vantagens:
- ✅ Templates mais simples que Helm
- ✅ Suporte nativo no kubectl (`kubectl apply -k`)
- ✅ Overlays para ambientes diferentes

### Desvantagens:
- ❌ Sem gestão de releases
- ❌ Sem rollback automático
- ❌ Sem dependency management

### Quando Usar Kustomize?
- Projetos menores
- Quando não precisa de rollback sofisticado
- Se você acha Helm muito complexo

```bash
# Com Kustomize
kubectl apply -k k8s/overlays/dev
```

---

## 📋 Checklist: Migrar de kubectl para Helm

Se você já tem recursos criados via `kubectl apply`, siga este processo:

### 1. Preparação
```bash
# Listar recursos existentes
kubectl get all -n dev

# Exportar YAML dos recursos (para referência)
kubectl get deployment myapp -n dev -o yaml > backup.yaml
```

### 2. Criar Helm Chart
```bash
# Criar estrutura
helm create myapp

# Mover seus YAMLs para templates/
# Adicionar values para configuração
```

### 3. Deletar Recursos Antigos
```bash
# IMPORTANTE: Fazer backup do PVC se tiver dados!
kubectl get pvc -n dev

# Deletar deployment/service (mas NÃO o PVC!)
kubectl delete deployment myapp -n dev
kubectl delete service myapp -n dev
```

### 4. Deploy com Helm
```bash
helm install myapp ./myapp-chart \
  --namespace dev \
  --values values-dev.yaml
```

### 5. Verificar
```bash
helm list -n dev
kubectl get pods -n dev
```

---

## 🎓 Resumo

| Recurso | Ferramenta | Motivo |
|---------|------------|--------|
| **PersistentVolumes** | kubectl | Infraestrutura base, global |
| **Namespaces** | kubectl | Infraestrutura base, raramente muda |
| **StorageClass** | kubectl | Infraestrutura base, global |
| **Backend API** | Helm | Aplicação, deploy frequente, precisa rollback |
| **Frontend** | Helm | Aplicação, múltiplos ambientes |
| **PostgreSQL (app)** | Helm | Dependency do backend, gerenciado junto |
| **Ingress Controller** | Helm | App complexa, múltiplas config |
| **ConfigMap (app)** | Helm | Parte da aplicação |
| **ConfigMap (infra)** | kubectl | Se for config global do cluster |

---

## 💡 Dica Final

**Comece simples:**
1. 🎯 Infra base → `kubectl apply`
2. 🎯 Aplicações → `Helm`
3. 🎯 **Nunca** misture as duas para o mesmo recurso

**Quando crescer:**
- Use **ArgoCD** ou **Flux** para GitOps
- Helm continua sendo a ferramenta de packaging
- Você ganha deploy automático + histórico no Git

---

## 📚 Leitura Adicional

- [Helm Documentation](https://helm.sh/docs/)
- [Kustomize Documentation](https://kustomize.io/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [ArgoCD - GitOps](https://argo-cd.readthedocs.io/)

