# Getting Started - Helm Chart Bridal Cover CRM

Este guia vai te ajudar a começar a usar o Helm Chart do Bridal Cover CRM.

## 📋 Pré-requisitos

### 1. Ferramentas Necessárias

- **Kubernetes Cluster** (Minikube, kind, ou cluster real)
- **kubectl** configurado e conectado ao cluster
- **Helm 3.10+** instalado
- **make** (opcional, mas recomendado)

### Verificar se tudo está instalado:

```bash
# Kubernetes
kubectl version --client
kubectl cluster-info

# Helm
helm version

# Make (opcional)
make --version
```

### 2. Instalar Ferramentas (se necessário)

#### Helm

```bash
# macOS
brew install helm

# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Windows (Chocolatey)
choco install kubernetes-helm
```

#### kubectl

```bash
# macOS
brew install kubectl

# Linux
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

# Windows (Chocolatey)
choco install kubernetes-cli
```

## 🚀 Instalação Rápida (5 minutos)

### Opção 1: Usando Make (Recomendado)

```bash
# 1. Navegar para o diretório
cd helm-chart/

# 2. Ver comandos disponíveis
make help

# 3. Deploy em desenvolvimento
make deploy-dev

# 4. Verificar status
make status-dev
make pods-dev
```

### Opção 2: Usando Helm Diretamente

```bash
# 1. Adicionar repositórios necessários
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# 2. Atualizar dependências
cd helm-chart/bridal-cover-crm
helm dependency update

# 3. Instalar em dev
kubectl create namespace dev
helm install bridal-crm-dev . \
  -f values-dev.yaml \
  -n dev

# 4. Ver status
helm status bridal-crm-dev -n dev
```

## 🎯 Primeiros Passos

### 1. Validar o Chart

Antes de instalar, valide o chart:

```bash
# Lint (verificar problemas)
helm lint ./bridal-cover-crm

# Ver templates que serão gerados
helm template test ./bridal-cover-crm -f ./bridal-cover-crm/values-dev.yaml

# Dry run (simular instalação)
helm install test ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-dev.yaml \
  --dry-run --debug
```

### 2. Instalar em Desenvolvimento

```bash
# Criar namespace
kubectl create namespace dev

# Instalar
cd helm-chart
make install-dev

# OU manualmente:
helm install bridal-crm-dev ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-dev.yaml \
  -n dev
```

### 3. Verificar a Instalação

```bash
# Ver status
helm status bridal-crm-dev -n dev

# Ver pods
kubectl get pods -n dev

# Ver todos recursos
kubectl get all -n dev

# Ver logs
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm -f
```

### 4. Acessar a Aplicação

#### Desenvolvimento (NodePort)

```bash
# Obter a URL
export NODE_PORT=$(kubectl get --namespace dev -o jsonpath="{.spec.ports[0].nodePort}" services bridal-crm-dev-bridal-cover-crm)
export NODE_IP=$(kubectl get nodes --namespace dev -o jsonpath="{.items[0].status.addresses[0].address}")
echo "URL: http://$NODE_IP:$NODE_PORT"
```

#### Ou via Port Forward

```bash
# Port forward
kubectl port-forward -n dev svc/bridal-crm-dev-bridal-cover-crm 8080:8080

# Acessar
curl http://localhost:8080/actuator/health
open http://localhost:8080
```

## 📝 Customização

### Arquivo de Valores Customizado

Crie seu próprio arquivo de valores:

```yaml
# my-values.yaml
replicaCount: 2

image:
  tag: "latest"

app:
  environment: local

postgresql:
  auth:
    password: mypassword123
```

Instale com seus valores:

```bash
helm install bridal-crm-dev ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-dev.yaml \
  -f my-values.yaml \
  -n dev
```

### Sobrescrever Valores via CLI

```bash
helm install bridal-crm-dev ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-dev.yaml \
  --set image.tag=v1.0.0 \
  --set replicaCount=3 \
  -n dev
```

## 🔄 Workflow de Desenvolvimento

### 1. Fazer Mudanças no Chart

```bash
# Editar templates ou values
vim bridal-cover-crm/values-dev.yaml
vim bridal-cover-crm/templates/deployment.yaml
```

### 2. Validar Mudanças

```bash
# Lint
make lint

# Template
make template-dev

# Dry run
make dry-run-dev
```

### 3. Atualizar Release

```bash
# Upgrade
make upgrade-dev

# OU manualmente:
helm upgrade bridal-crm-dev ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-dev.yaml \
  -n dev
```

### 4. Verificar

```bash
# Status
make status-dev

# Pods
make pods-dev

# Logs
make logs-dev
```

### 5. Rollback (se necessário)

```bash
# Ver histórico
make history-dev

# Rollback
make rollback-dev
```

## 🌍 Deploy em Outros Ambientes

### Staging

```bash
# Criar namespace
kubectl create namespace staging

# Instalar
make install-staging

# OU manualmente com secrets:
kubectl create secret generic postgres-staging-secret \
  --from-literal=password=staging-password \
  -n staging

helm install bridal-crm-staging ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-staging.yaml \
  -n staging
```

### Produção

```bash
# ⚠️ IMPORTANTE: Configure secrets antes!

# 1. Criar namespace
kubectl create namespace production

# 2. Criar secret do banco
kubectl create secret generic postgres-prod-secret \
  --from-literal=password=$SECURE_PASSWORD \
  -n production

# 3. Deploy
make install-prod

# OU manualmente:
helm install bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  -n production

# 4. Verificar
make status-prod
make pods-prod
```

## 🔐 Gerenciamento de Secrets

### Opção 1: Secrets Manuais (Dev/Staging)

```bash
kubectl create secret generic db-secret \
  --from-literal=password=mypassword \
  -n dev
```

### Opção 2: Sealed Secrets (Produção)

```bash
# Instalar controller
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.24.0/controller.yaml

# Criar secret
kubectl create secret generic db-secret \
  --from-literal=password=mypassword \
  --dry-run=client -o yaml > secret.yaml

# Selar (pode commitar sealed-secret.yaml)
kubeseal -f secret.yaml -w sealed-secret.yaml

# Aplicar
kubectl apply -f sealed-secret.yaml -n production
```

## 📊 Monitoramento

### Ver Métricas

```bash
# Pods
kubectl top pods -n dev

# Nodes
kubectl top nodes

# HPA (se habilitado)
kubectl get hpa -n dev
```

### Health Checks

```bash
# Port forward
kubectl port-forward -n dev svc/bridal-crm-dev-bridal-cover-crm 8080:8080

# Health
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/prometheus
```

### Logs

```bash
# Seguir logs
kubectl logs -n dev -l app.kubernetes.io/name=bridal-cover-crm -f

# Logs de pod específico
kubectl logs -n dev <pod-name> -f

# Logs do container anterior (se crashou)
kubectl logs -n dev <pod-name> --previous
```

## 🧪 Testing

### Executar Testes

```bash
# Criar teste
cat > bridal-cover-crm/templates/tests/test-health.yaml <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: "{{ include "bridal-cover-crm.fullname" . }}-test-health"
  annotations:
    "helm.sh/hook": test
spec:
  containers:
  - name: wget
    image: busybox
    command: ['wget']
    args: ['--spider', '{{ include "bridal-cover-crm.fullname" . }}:{{ .Values.service.port }}/actuator/health']
  restartPolicy: Never
EOF

# Executar teste
helm test bridal-crm-dev -n dev
```

## 🆘 Troubleshooting

### Pods não iniciam

```bash
# Ver detalhes do pod
kubectl describe pod <pod-name> -n dev

# Ver eventos
kubectl get events -n dev --sort-by='.lastTimestamp'

# Ver logs
kubectl logs <pod-name> -n dev
```

### Erro de dependências

```bash
# Limpar e reinstalar
cd helm-chart
make clean
make deps-update
make install-dev
```

### Release preso

```bash
# Desinstalar completamente
helm uninstall bridal-crm-dev -n dev

# Limpar namespace
kubectl delete namespace dev

# Recriar
kubectl create namespace dev
make install-dev
```

## 📚 Próximos Passos

1. **Ler a documentação completa**:
   - [Guia Helm e Tiller](../docs/kubernetes/15-helm-tiller-guide.md)
   - [README do Chart](bridal-cover-crm/README.md)
   - [Quick Reference](QUICK-REFERENCE.md)

2. **Customizar para seu ambiente**:
   - Criar arquivo `values-local.yaml`
   - Configurar ingress com seu domínio
   - Ajustar resources conforme necessário

3. **Implementar CI/CD**:
   - Integrar com GitHub Actions ou GitLab CI
   - Automatizar deploys
   - Implementar testes automatizados

4. **Configurar monitoramento**:
   - Prometheus + Grafana
   - AlertManager
   - Logs centralizados (ELK, Loki)

5. **Melhorar segurança**:
   - Implementar Network Policies
   - Configurar RBAC granular
   - Usar Sealed Secrets ou External Secrets

## 🎓 Aprendizado

### Comandos Essenciais para Aprender

```bash
# Helm
helm list -A                    # Listar todas releases
helm get values <release>       # Ver valores aplicados
helm history <release>          # Ver histórico
helm rollback <release>         # Rollback

# Kubernetes
kubectl get all -n <namespace>  # Ver todos recursos
kubectl describe <resource>     # Detalhes do recurso
kubectl logs -f <pod>           # Seguir logs
kubectl exec -it <pod> -- sh    # Shell no pod
```

### Recursos de Aprendizado

- [Helm Official Docs](https://helm.sh/docs/)
- [Kubernetes Docs](https://kubernetes.io/docs/)
- [Chart Best Practices](https://helm.sh/docs/chart_best_practices/)
- [Guia interno completo](../docs/kubernetes/15-helm-tiller-guide.md)

## 💡 Dicas

1. **Sempre faça dry-run** antes de deploy em produção
2. **Use valores separados** para cada ambiente
3. **Versione seus charts** seguindo SemVer
4. **Documente mudanças** no Chart.yaml
5. **Teste rollbacks** regularmente
6. **Mantenha backups** do banco de dados
7. **Use secrets** para informações sensíveis
8. **Monitore recursos** (CPU, memória)
9. **Implemente health checks** adequados
10. **Automatize** via CI/CD

## 🤝 Contribuindo

Encontrou um problema ou quer melhorar o chart?

1. Fork o repositório
2. Crie uma branch: `git checkout -b feature/melhoria`
3. Faça suas mudanças
4. Valide: `make lint`
5. Teste: `make dry-run-dev`
6. Commit: `git commit -m "Descrição da melhoria"`
7. Push: `git push origin feature/melhoria`
8. Abra um Pull Request

## 📞 Suporte

- **Issues**: https://github.com/GuAntunes/bridal-cover-crm/issues
- **Documentação**: [docs/kubernetes/](../docs/kubernetes/)
- **Chat**: [Slack/Discord se houver]

---

**Boa sorte com seu deploy! 🚀**


