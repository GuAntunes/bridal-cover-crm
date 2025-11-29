# Helm Chart - Bridal Cover CRM

Documentação completa para deploy do Bridal Cover CRM usando Helm no Kubernetes.

## 📋 Índice

- [Pré-requisitos](#-pré-requisitos)
- [Estrutura](#-estrutura)
- [Início Rápido](#-início-rápido)
- [Deploy por Ambiente](#-deploy-por-ambiente)
- [Comandos Make](#-comandos-make)
- [Gerenciamento de Secrets](#-gerenciamento-de-secrets)
- [Atualização e Rollback](#-atualização-e-rollback)
- [Monitoramento](#-monitoramento)
- [Troubleshooting](#-troubleshooting)
- [Referência de Comandos](#-referência-de-comandos)

## 🔧 Pré-requisitos

### Ferramentas Necessárias

- **Kubernetes Cluster** (Minikube, kind, ou cluster real)
- **kubectl** configurado
- **Helm 3.10+**
- **make** (opcional, mas recomendado)

### Instalação das Ferramentas

```bash
# Helm
# macOS
brew install helm

# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# kubectl
# macOS
brew install kubectl

# Verificar instalação
kubectl version --client
helm version
```

## 📦 Estrutura

```
helm-chart/
├── bridal-cover-crm/          # Chart principal
│   ├── Chart.yaml             # Metadados e dependências
│   ├── values.yaml            # Valores padrão
│   ├── values-dev.yaml        # Configuração desenvolvimento
│   ├── values-staging.yaml    # Configuração staging
│   ├── values-prod.yaml       # Configuração produção
│   └── templates/             # Templates Kubernetes
│       ├── deployment.yaml
│       ├── service.yaml
│       ├── ingress.yaml
│       ├── configmap.yaml
│       ├── hpa.yaml
│       └── _helpers.tpl
├── Makefile                   # Comandos automatizados
└── README.md                  # Este arquivo
```

## 🚀 Início Rápido

### Opção 1: Usando Make (Recomendado)

```bash
# 1. Setup inicial (apenas primeira vez)
make setup

# 2. Ver comandos disponíveis
make help

# 3. Deploy completo em desenvolvimento
make deploy-dev

# 4. Verificar status
make status-dev
make pods-dev
```

### Opção 2: Usando Helm Diretamente

```bash
# 1. Adicionar repositórios
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# 2. Atualizar dependências
cd bridal-cover-crm
helm dependency update

# 3. Instalar
kubectl create namespace dev
helm install bridal-crm-dev . -f values-dev.yaml -n dev

# 4. Ver status
helm status bridal-crm-dev -n dev
```

### Validação do Chart

```bash
# Lint
helm lint ./bridal-cover-crm

# Ver templates gerados
helm template test ./bridal-cover-crm -f ./bridal-cover-crm/values-dev.yaml

# Dry run
helm install test ./bridal-cover-crm -f ./bridal-cover-crm/values-dev.yaml --dry-run --debug
```

## 🌍 Deploy por Ambiente

### Desenvolvimento

**Características:** 1 réplica, NodePort, recursos mínimos

```bash
# Via Make
make deploy-dev

# Via Helm
kubectl create namespace dev
helm install bridal-crm-dev ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-dev.yaml \
  -n dev
```

**Acessar a aplicação:**

```bash
# Port forward
kubectl port-forward -n dev svc/bridal-crm-dev-bridal-cover-crm 8080:8080

# Acessar
curl http://localhost:8080/actuator/health
```

### Staging

**Características:** 2 réplicas, Ingress, recursos médios

```bash
# 1. Criar secret do banco
kubectl create secret generic postgres-staging-secret \
  --from-literal=password=staging-password \
  -n staging

# 2. Deploy via Make
make deploy-staging

# 3. Deploy via Helm
helm install bridal-crm-staging ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-staging.yaml \
  -n staging
```

### Produção

**Características:** 5+ réplicas, HA, recursos altos, DB externo

```bash
# ⚠️ IMPORTANTE: Configurar secrets antes!

# 1. Criar namespace
kubectl create namespace production

# 2. Criar secret do banco
kubectl create secret generic postgres-prod-secret \
  --from-literal=password=$SECURE_PASSWORD \
  -n production

# 3. Deploy via Make
make deploy-prod

# 4. Deploy via Helm
helm install bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  -n production

# 5. Verificar
kubectl get pods -n production -w
```

## 🛠️ Comandos Make

```bash
# Ver todos os comandos disponíveis
make help

# Setup inicial (executar apenas uma vez)
make setup                   # Configurar repositórios e dependências

# Validação
make lint                    # Validar sintaxe do chart
make dry-run-dev             # Simular instalação em dev

# Deploy (upgrade or install)
make deploy-dev              # Deploy completo em dev
make deploy-staging          # Deploy completo em staging
make deploy-prod             # Deploy completo em produção

# Atualização
make upgrade-dev             # Atualizar release em dev
make upgrade-staging         # Atualizar release em staging
make upgrade-prod            # Atualizar release em produção

# Status e Logs
make status-dev              # Ver status da release
make pods-dev                # Ver pods
make logs-dev                # Ver logs (últimas 100 linhas)

# Rollback
make history-dev             # Ver histórico de versões
make rollback-dev            # Voltar para versão anterior

# Limpeza
make uninstall-dev           # Desinstalar release
make clean                   # Limpar dependências

# Utilitários
make list                    # Listar todas as releases
make port-forward-dev        # Port forward para localhost:8080
```

## 🔐 Gerenciamento de Secrets

### Opção 1: Secret Manual (Dev/Staging)

```bash
kubectl create secret generic db-secret \
  --from-literal=password=mypassword \
  -n dev
```

### Opção 2: Sealed Secrets (Produção - Recomendado)

```bash
# 1. Instalar Sealed Secrets Controller
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# 2. Criar secret
kubectl create secret generic db-secret \
  --from-literal=password=mypassword \
  --dry-run=client -o yaml > secret.yaml

# 3. Selar (pode commitar sealed-secret.yaml no git)
kubeseal -f secret.yaml -w sealed-secret.yaml

# 4. Aplicar
kubectl apply -f sealed-secret.yaml -n production
```

### Opção 3: Valores via CLI

```bash
helm install bridal-crm-dev ./bridal-cover-crm \
  --set postgresql.auth.password=mypassword \
  -n dev
```

## 🔄 Atualização e Rollback

### Atualizar Release

```bash
# Atualizar apenas a imagem
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  --set image.tag=1.2.0 \
  --reuse-values \
  -n production

# Atualizar com novos valores
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  -n production

# Via Make
make upgrade-prod
```

### Rollback

```bash
# Ver histórico
helm history bridal-crm-prod -n production

# Rollback para versão anterior
helm rollback bridal-crm-prod -n production

# Rollback para versão específica
helm rollback bridal-crm-prod 3 -n production

# Via Make
make history-prod
make rollback-prod
```

### Escalar Aplicação

```bash
# Via Helm
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  --set replicaCount=10 \
  --reuse-values \
  -n production

# Via kubectl (temporário)
kubectl scale deployment/bridal-crm-prod-bridal-cover-crm --replicas=10 -n production

# Restart pods
kubectl rollout restart deployment/bridal-crm-prod-bridal-cover-crm -n production
```

## 📊 Monitoramento

### Verificar Status

```bash
# Pods
kubectl get pods -n production
kubectl top pods -n production

# HPA (Horizontal Pod Autoscaler)
kubectl get hpa -n production
kubectl describe hpa bridal-crm-prod-bridal-cover-crm -n production

# Todos recursos
kubectl get all -n production
```

### Logs

```bash
# Seguir logs
kubectl logs -n production -l app.kubernetes.io/name=bridal-cover-crm -f

# Via Make
make logs-prod

# Logs de pod específico
kubectl logs <pod-name> -n production -f

# Logs do container anterior (se crashou)
kubectl logs <pod-name> -n production --previous
```

### Health Checks

```bash
# Port forward
kubectl port-forward -n production svc/bridal-crm-prod-bridal-cover-crm 8080:8080

# Health
curl http://localhost:8080/actuator/health

# Liveness
curl http://localhost:8080/actuator/health/liveness

# Readiness
curl http://localhost:8080/actuator/health/readiness

# Metrics (Prometheus)
curl http://localhost:8080/actuator/prometheus
```

### Conectar no PostgreSQL

```bash
# 1. Obter senha
export POSTGRES_PASSWORD=$(kubectl get secret bridal-crm-prod-postgresql \
  -n production \
  -o jsonpath="{.data.password}" | base64 -d)

# 2. Conectar
kubectl run postgresql-client --rm --tty -i --restart='Never' \
  --namespace production \
  --image docker.io/bitnami/postgresql:latest \
  --env="PGPASSWORD=$POSTGRES_PASSWORD" \
  --command -- psql \
  --host bridal-crm-prod-postgresql \
  -U bridalcover \
  -d bridalcover_db
```

## 🆘 Troubleshooting

### Pod não inicia

```bash
# Ver detalhes do pod
kubectl describe pod <pod-name> -n production

# Ver eventos
kubectl get events -n production --sort-by='.lastTimestamp'

# Ver logs
kubectl logs <pod-name> -n production

# Ver configuração aplicada
kubectl get pod <pod-name> -n production -o yaml
```

### ImagePullBackOff

```bash
# Descrever pod
kubectl describe pod <pod-name> -n production

# Verificar se imagem existe
docker pull guantunes/bridal-cover-crm:1.0.0

# Verificar secrets de pull
kubectl get secrets -n production
```

### CrashLoopBackOff

```bash
# Ver logs do container anterior
kubectl logs <pod-name> -n production --previous

# Ver eventos
kubectl get events -n production --sort-by='.lastTimestamp'

# Executar shell no pod (se possível)
kubectl exec -it <pod-name> -n production -- /bin/sh
```

### Release Preso

```bash
# Desinstalar completamente
helm uninstall bridal-crm-dev -n dev

# Limpar namespace
kubectl delete namespace dev

# Recriar
kubectl create namespace dev
make deploy-dev
```

### Erro de Dependências

```bash
# Limpar e reinstalar
make clean
make deps-update
make deploy-dev
```

### Verificar Configuração

```bash
# Ver valores aplicados
helm get values bridal-crm-prod -n production

# Ver todos valores (incluindo defaults)
helm get values bridal-crm-prod --all -n production

# Ver manifests gerados
helm get manifest bridal-crm-prod -n production

# Ver ConfigMap
kubectl get configmap bridal-crm-prod-bridal-cover-crm -n production -o yaml
```

## 📚 Referência de Comandos

### Helm Essencial

```bash
# Listar releases
helm list -A
helm list -n production

# Informações da release
helm status <release> -n <namespace>
helm get values <release> -n <namespace>
helm get manifest <release> -n <namespace>

# Histórico e rollback
helm history <release> -n <namespace>
helm rollback <release> -n <namespace>
helm rollback <release> <revision> -n <namespace>

# Desinstalar
helm uninstall <release> -n <namespace>

# Empacotamento
helm package ./bridal-cover-crm
helm repo index .
```

### Kubectl Essencial

```bash
# Recursos
kubectl get all -n <namespace>
kubectl get pods -n <namespace>
kubectl describe pod <pod-name> -n <namespace>

# Logs
kubectl logs -f <pod-name> -n <namespace>
kubectl logs -l app=myapp -n <namespace> -f

# Execução
kubectl exec -it <pod-name> -n <namespace> -- /bin/sh
kubectl port-forward -n <namespace> svc/<service> 8080:8080

# Métricas
kubectl top pods -n <namespace>
kubectl top nodes

# Eventos
kubectl get events -n <namespace> --sort-by='.lastTimestamp'
```

### Customização de Valores

```bash
# Múltiplos arquivos de valores
helm install my-release ./bridal-cover-crm \
  -f values.yaml \
  -f values-prod.yaml \
  -f secrets.yaml

# Sobrescrever valores específicos
helm install my-release ./bridal-cover-crm \
  --set replicaCount=5 \
  --set image.tag=1.2.0 \
  --set postgresql.auth.password=newpass

# Reaproveitando valores existentes
helm upgrade my-release ./bridal-cover-crm \
  --reuse-values \
  --set image.tag=1.3.0
```

## 💡 Boas Práticas

1. **Sempre faça dry-run** antes de deploy em produção
2. **Use valores separados** para cada ambiente
3. **Versione seus charts** seguindo SemVer
4. **Teste rollbacks** regularmente
5. **Use Sealed Secrets** para produção
6. **Monitore recursos** (CPU, memória, HPA)
7. **Implemente health checks** adequados
8. **Mantenha backups** do banco de dados
9. **Automatize via CI/CD**
10. **Documente mudanças** no Chart.yaml

## 🔗 Links Úteis

- **[Helm Official Docs](https://helm.sh/docs/)**
- **[Chart Best Practices](https://helm.sh/docs/chart_best_practices/)**
- **[Kubernetes Docs](https://kubernetes.io/docs/)**
- **[Helm Template Guide](https://helm.sh/docs/chart_template_guide/)**

---

**📞 Suporte:** Abra uma issue no repositório ou consulte a documentação em `docs/kubernetes/`

**🚀 Boa sorte com seu deploy!**
