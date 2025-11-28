# 🚀 Guia Completo de Deploy - BridalCover CRM

Este é o guia definitivo para fazer deploy de toda a stack do BridalCover CRM usando Helm.

---

## 📐 Arquitetura de Deploy

```
┌─────────────────────────────────────────────────────────────────┐
│                    KUBERNETES CLUSTER                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  INFRAESTRUTURA BASE (kubectl)                         │     │
│  │  ┌──────────────────────────────────────────────────┐  │     │
│  │  │  - Namespaces (dev, staging, prod)               │  │     │
│  │  │  - PersistentVolumes (postgres)                  │  │     │
│  │  │  - StorageClasses                                │  │     │
│  │  └──────────────────────────────────────────────────┘  │     │
│  └────────────────────────────────────────────────────────┘     │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐     │
│  │  APLICAÇÕES (Helm)                                     │     │
│  │  ┌──────────────────────────────────────────────────┐  │     │
│  │  │  Backend (Spring Boot + PostgreSQL)             │  │     │
│  │  │  - bridal-crm-dev                                │  │     │
│  │  │  - bridal-crm-staging                            │  │     │
│  │  │  - bridal-crm-prod                               │  │     │
│  │  └──────────────────────────────────────────────────┘  │     │
│  │  ┌──────────────────────────────────────────────────┐  │     │
│  │  │  Frontend (React)                                │  │     │
│  │  │  - bridal-frontend-dev                           │  │     │
│  │  │  - bridal-frontend-staging                       │  │     │
│  │  │  - bridal-frontend-prod                          │  │     │
│  │  └──────────────────────────────────────────────────┘  │     │
│  └────────────────────────────────────────────────────────┘     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Estratégia: Helm vs kubectl

### ✅ Use kubectl para:
- **Infraestrutura base** (PVs, namespaces, storage classes)
- Recursos criados **uma vez** e raramente modificados
- Recursos **globais** (não pertencem a um namespace)

### ✅ Use Helm para:
- **Aplicações** (backend, frontend)
- Recursos que mudam **frequentemente**
- **Múltiplos ambientes** (dev, staging, prod)
- Precisa de **rollback** e versionamento

---

## 📋 Pré-requisitos

```bash
# Verificar instalações
kubectl version --client
helm version

# Verificar acesso ao cluster
kubectl cluster-info
kubectl get nodes
```

---

## 🚀 Deploy Completo (do Zero)

### 1️⃣ Setup de Infraestrutura Base (Uma vez)

#### a) Criar namespaces

```bash
kubectl create namespace dev
kubectl create namespace staging
kubectl create namespace prod
```

#### b) Criar diretórios físicos para PersistentVolumes

```bash
# No node onde o Kubernetes está rodando
sudo mkdir -p /mnt/data/postgres-dev
sudo mkdir -p /mnt/data/postgres-staging
sudo mkdir -p /mnt/data/postgres-prod

# Dar permissões (ajuste conforme necessário)
sudo chmod -R 777 /mnt/data/
```

#### c) Aplicar PersistentVolumes

```bash
cd ~/bridal-cover-crm
kubectl apply -f k8s/infrastructure/postgres-volumes.yaml
```

#### d) Verificar

```bash
kubectl get pv
```

**Saída esperada:**
```
NAME                   CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      STORAGECLASS
postgres-pv-dev        5Gi        RWO            Retain           Available   manual
postgres-pv-staging    10Gi       RWO            Retain           Available   manual
postgres-pv-prod       10Gi       RWO            Retain           Available   manual
```

---

### 2️⃣ Deploy do Backend (com PostgreSQL)

#### DEV

```bash
cd ~/bridal-cover-crm/helm-chart

helm upgrade --install bridal-crm-dev bridal-cover-crm \
  --namespace dev \
  --create-namespace \
  --values bridal-cover-crm/values-dev.yaml
```

#### STAGING

```bash
helm upgrade --install bridal-crm-staging bridal-cover-crm \
  --namespace staging \
  --create-namespace \
  --values bridal-cover-crm/values-staging.yaml
```

#### PROD

```bash
helm upgrade --install bridal-crm-prod bridal-cover-crm \
  --namespace prod \
  --create-namespace \
  --values bridal-cover-crm/values-prod.yaml
```

#### Verificar

```bash
# Ver releases
helm list -A

# Ver pods
kubectl get pods -n dev
kubectl get pods -n staging
kubectl get pods -n prod

# Ver PVCs (devem estar Bound)
kubectl get pvc -n dev
```

---

### 3️⃣ Deploy do Frontend

#### DEV

```bash
cd ~/bridal-cover-crm-front-end/helm-chart

helm upgrade --install bridal-frontend-dev frontend \
  --namespace dev \
  --create-namespace \
  --values frontend/values-dev.yaml
```

#### STAGING

```bash
helm upgrade --install bridal-frontend-staging frontend \
  --namespace staging \
  --create-namespace \
  --values frontend/values-staging.yaml
```

#### PROD

```bash
helm upgrade --install bridal-frontend-prod frontend \
  --namespace prod \
  --create-namespace \
  --values frontend/values-prod.yaml
```

#### Verificar

```bash
# Ver releases
helm list -A

# Ver pods
kubectl get pods -n dev
kubectl get pods -n staging
kubectl get pods -n prod
```

---

## 🔄 Workflow de Desenvolvimento

### Ciclo de Deploy Típico

```bash
# 1. Fazer mudanças no código
# 2. Build e push da imagem
cd ~/bridal-cover-crm
make docker-release-dev

# 3. Deploy/Update via Helm
cd helm-chart/
helm upgrade bridal-crm-dev bridal-cover-crm \
  --namespace dev \
  --values bridal-cover-crm/values-dev.yaml

# 4. Verificar
kubectl get pods -n dev -w
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm --tail=100
```

### Para o Frontend

```bash
# 1. Build e push da imagem
cd ~/bridal-cover-crm-front-end
# (Adicione make target similar ao backend)

# 2. Deploy/Update via Helm
cd helm-chart/
helm upgrade bridal-frontend-dev frontend \
  --namespace dev \
  --values frontend/values-dev.yaml

# 3. Verificar
kubectl get pods -n dev -w
```

---

## 🔙 Rollback

### Ver histórico de releases

```bash
# Backend
helm history bridal-crm-dev -n dev

# Frontend
helm history bridal-frontend-dev -n dev
```

### Fazer rollback

```bash
# Para versão anterior
helm rollback bridal-crm-dev -n dev

# Para versão específica
helm rollback bridal-crm-dev 2 -n dev
```

---

## 🔍 Troubleshooting

### Ver status geral

```bash
# Todos os releases
helm list -A

# Todos os pods
kubectl get pods -A

# PersistentVolumes e PVCs
kubectl get pv
kubectl get pvc -A
```

### Backend não inicia

```bash
# Ver logs do backend
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm --tail=100

# Ver logs do PostgreSQL
kubectl logs -n dev bridal-crm-dev-postgresql-0 -c postgresql

# Descrever pod com problema
kubectl describe pod <pod-name> -n dev
```

### PVC Pending

```bash
# Ver detalhes do PVC
kubectl describe pvc <pvc-name> -n dev

# Ver PVs disponíveis
kubectl get pv

# Verificar se storageClass está correto
# Deve ser "manual" tanto no PV quanto no PVC
```

### Frontend não conecta ao backend

```bash
# Verificar ConfigMap do frontend
kubectl get configmap -n dev
kubectl describe configmap <frontend-config-name> -n dev

# Verificar se o service do backend existe
kubectl get svc -n dev

# Testar conectividade (de dentro de um pod)
kubectl exec -it <frontend-pod> -n dev -- sh
# dentro do pod:
wget -O- http://bridal-crm-dev:8080/actuator/health
```

---

## 🧹 Limpeza (Desinstalar)

### Desinstalar aplicações (preserva PVs e dados)

```bash
# Backend
helm uninstall bridal-crm-dev -n dev
helm uninstall bridal-crm-staging -n staging
helm uninstall bridal-crm-prod -n prod

# Frontend
helm uninstall bridal-frontend-dev -n dev
helm uninstall bridal-frontend-staging -n staging
helm uninstall bridal-frontend-prod -n prod
```

### Deletar PVCs (⚠️ PERDE DADOS!)

```bash
kubectl delete pvc --all -n dev
kubectl delete pvc --all -n staging
kubectl delete pvc --all -n prod
```

### Deletar PVs

```bash
kubectl delete pv postgres-pv-dev
kubectl delete pv postgres-pv-staging
kubectl delete pv postgres-pv-prod
```

### Deletar namespaces

```bash
kubectl delete namespace dev
kubectl delete namespace staging
kubectl delete namespace prod
```

---

## 📊 Monitoramento

### Pods

```bash
# Ver todos os pods
kubectl get pods -A

# Watch (atualização automática)
kubectl get pods -n dev -w

# Ver pods com mais detalhes
kubectl get pods -n dev -o wide
```

### Logs

```bash
# Logs do backend
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm --tail=100 -f

# Logs do frontend
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm-frontend --tail=100 -f

# Logs do PostgreSQL
kubectl logs -n dev bridal-crm-dev-postgresql-0 -c postgresql --tail=100
```

### Recursos

```bash
# Ver uso de recursos
kubectl top pods -n dev
kubectl top nodes

# Ver métricas do HPA (se habilitado)
kubectl get hpa -n staging
```

### Eventos

```bash
# Ver eventos recentes
kubectl get events -n dev --sort-by='.lastTimestamp'
```

---

## 🔐 Secrets e Senhas

### PostgreSQL

Senhas são geradas automaticamente pelo Helm. Para recuperar:

```bash
# Password do usuário
kubectl get secret bridal-crm-dev-postgresql -n dev -o jsonpath="{.data.password}" | base64 --decode

# Password do postgres (superuser)
kubectl get secret bridal-crm-dev-postgresql -n dev -o jsonpath="{.data.postgres-password}" | base64 --decode
```

### Conectar ao PostgreSQL

```bash
# Port forward
kubectl port-forward -n dev svc/bridal-crm-dev-postgresql 5432:5432

# Em outro terminal, conectar
psql -h localhost -U bridalcover_dev -d bridalcover_dev
# Senha: dev123 (conforme values-dev.yaml)
```

---

## 📝 Checklist de Deploy

### Inicial (Setup)
- [ ] Criar namespaces
- [ ] Criar diretórios para PVs
- [ ] Aplicar PersistentVolumes
- [ ] Verificar PVs estão Available

### Backend
- [ ] Build e push da imagem Docker
- [ ] Deploy via Helm
- [ ] Verificar pods estão Running
- [ ] Verificar PVC está Bound
- [ ] Testar endpoint de health

### Frontend
- [ ] Build e push da imagem Docker
- [ ] Deploy via Helm
- [ ] Verificar pods estão Running
- [ ] Testar acesso via browser
- [ ] Verificar conectividade com backend

---

## 🎓 Comandos Úteis Resumidos

```bash
# INFRAESTRUTURA
kubectl apply -f k8s/infrastructure/
kubectl get pv

# DEPLOY BACKEND
cd ~/bridal-cover-crm/helm-chart
helm upgrade --install bridal-crm-dev bridal-cover-crm --namespace dev --values bridal-cover-crm/values-dev.yaml

# DEPLOY FRONTEND
cd ~/bridal-cover-crm-front-end/helm-chart
helm upgrade --install bridal-frontend-dev frontend --namespace dev --values frontend/values-dev.yaml

# STATUS
helm list -A
kubectl get pods -A
kubectl get pvc -A

# LOGS
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm --tail=100
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm-frontend --tail=100

# ROLLBACK
helm rollback bridal-crm-dev -n dev

# LIMPEZA
helm uninstall bridal-crm-dev -n dev
helm uninstall bridal-frontend-dev -n dev
```

---

## 📚 Documentação Relacionada

- [Helm vs kubectl - Quando Usar](../bridal-cover-crm/docs/deployment/helm-vs-kubectl.md)
- [Backend Helm Chart README](../bridal-cover-crm/helm-chart/bridal-cover-crm/README.md)
- [Frontend Helm Chart README](helm-chart/frontend/README.md)
- [Kubernetes Concepts](../bridal-cover-crm/docs/kubernetes/03-concepts.md)

---

## 🆘 Suporte

Se encontrar problemas:

1. ✅ Verificar logs: `kubectl logs ...`
2. ✅ Verificar eventos: `kubectl get events ...`
3. ✅ Descrever recursos: `kubectl describe ...`
4. ✅ Consultar documentação do Helm: https://helm.sh/docs/
5. ✅ Consultar documentação do Kubernetes: https://kubernetes.io/docs/

---

**Última atualização:** Novembro 2025

