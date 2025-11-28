# Helm Quick Reference - Bridal Cover CRM

## 🚀 Comandos Mais Usados

### Instalação

```bash
# Dev
make deploy-dev

# Staging
make deploy-staging

# Produção
make deploy-prod
```

### Atualização

```bash
# Dev
make upgrade-dev

# Staging
make upgrade-staging

# Produção
make upgrade-prod
```

### Status

```bash
# Ver status
make status-dev
make status-staging
make status-prod

# Ver pods
make pods-dev
make pods-staging
make pods-prod

# Ver logs
make logs-dev
make logs-staging
make logs-prod
```

### Rollback

```bash
# Ver histórico
make history-prod

# Rollback
make rollback-prod
```

## 📝 Comandos Helm Diretos

### Instalação

```bash
# Desenvolvimento
helm install bridal-crm-dev ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-dev.yaml \
  -n dev --create-namespace

# Staging
helm install bridal-crm-staging ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-staging.yaml \
  -n staging --create-namespace

# Produção
helm install bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  -n production --create-namespace
```

### Upgrade

```bash
# Atualizar imagem
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  --set image.tag=1.1.0 \
  -n production

# Atualizar com novos valores
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  -n production

# Upgrade com recriar pods
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  --force \
  -n production
```

### Informações

```bash
# Status da release
helm status bridal-crm-prod -n production

# Valores aplicados
helm get values bridal-crm-prod -n production

# Todos os valores (incluindo defaults)
helm get values bridal-crm-prod --all -n production

# Manifests gerados
helm get manifest bridal-crm-prod -n production

# Histórico
helm history bridal-crm-prod -n production
```

### Rollback

```bash
# Rollback para revisão anterior
helm rollback bridal-crm-prod -n production

# Rollback para revisão específica
helm rollback bridal-crm-prod 3 -n production

# Rollback com dry-run
helm rollback bridal-crm-prod --dry-run -n production
```

### Desinstalação

```bash
# Desinstalar
helm uninstall bridal-crm-dev -n dev

# Desinstalar mantendo histórico
helm uninstall bridal-crm-dev --keep-history -n dev
```

## 🔍 Debug e Troubleshooting

### Validação

```bash
# Lint
helm lint ./bridal-cover-crm

# Dry run
helm install test ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  --dry-run --debug

# Ver templates gerados
helm template test ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml
```

### Kubernetes

```bash
# Ver todos recursos
kubectl get all -n production

# Ver pods com labels
kubectl get pods -n production -l app.kubernetes.io/name=bridal-cover-crm

# Descrever pod
kubectl describe pod <pod-name> -n production

# Ver logs
kubectl logs -f <pod-name> -n production

# Ver eventos
kubectl get events -n production --sort-by='.lastTimestamp'

# Port forward
kubectl port-forward -n production svc/bridal-crm-prod-bridal-cover-crm 8080:8080
```

### Dependências

```bash
# Adicionar repos
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Atualizar dependências
cd bridal-cover-crm
helm dependency update

# Listar dependências
helm dependency list
```

## 🔐 Gerenciamento de Secrets

### Criar Secret Manualmente

```bash
# PostgreSQL password
kubectl create secret generic postgres-prod-secret \
  --from-literal=password=supersecret123 \
  -n production

# Usar no Helm
helm install bridal-crm-prod ./bridal-cover-crm \
  --set postgresql.enabled=false \
  --set externalDatabase.host=postgres.example.com \
  --set externalDatabase.username=bridalcover \
  --set externalDatabase.database=bridalcover_db \
  --set externalDatabase.existingSecret=postgres-prod-secret \
  -n production
```

### Sealed Secrets

```bash
# Criar secret normal
kubectl create secret generic db-secret \
  --from-literal=password=mypassword \
  --dry-run=client -o yaml > secret.yaml

# Selar
kubeseal -f secret.yaml -w sealed-secret.yaml

# Aplicar (pode commitar sealed-secret.yaml)
kubectl apply -f sealed-secret.yaml
```

## 📊 Monitoramento

### HPA (Horizontal Pod Autoscaler)

```bash
# Ver status do HPA
kubectl get hpa -n production

# Descrever HPA
kubectl describe hpa bridal-crm-prod-bridal-cover-crm -n production

# Watch HPA
kubectl get hpa -n production -w
```

### Métricas

```bash
# Top pods
kubectl top pods -n production

# Top nodes
kubectl top nodes

# Logs em tempo real
kubectl logs -n production -l app.kubernetes.io/name=bridal-cover-crm -f --tail=100
```

### Health Checks

```bash
# Port forward
kubectl port-forward -n production svc/bridal-crm-prod-bridal-cover-crm 8080:8080

# Health check
curl http://localhost:8080/actuator/health

# Liveness
curl http://localhost:8080/actuator/health/liveness

# Readiness
curl http://localhost:8080/actuator/health/readiness

# Metrics
curl http://localhost:8080/actuator/prometheus
```

## 🎯 Cenários Comuns

### Atualizar apenas a imagem

```bash
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  --set image.tag=1.2.0 \
  --reuse-values \
  -n production
```

### Escalar manualmente

```bash
# Via Helm
helm upgrade bridal-crm-prod ./bridal-cover-crm \
  --set replicaCount=10 \
  --reuse-values \
  -n production

# Via kubectl (temporário)
kubectl scale deployment/bridal-crm-prod-bridal-cover-crm --replicas=10 -n production
```

### Reiniciar pods

```bash
# Restart deployment
kubectl rollout restart deployment/bridal-crm-prod-bridal-cover-crm -n production

# Ver status do rollout
kubectl rollout status deployment/bridal-crm-prod-bridal-cover-crm -n production
```

### Conectar no PostgreSQL

```bash
# Obter senha
export POSTGRES_PASSWORD=$(kubectl get secret bridal-crm-prod-postgresql \
  -n production \
  -o jsonpath="{.data.password}" | base64 -d)

# Conectar
kubectl run postgresql-client --rm --tty -i --restart='Never' \
  --namespace production \
  --image docker.io/bitnami/postgresql:latest \
  --env="PGPASSWORD=$POSTGRES_PASSWORD" \
  --command -- psql \
  --host bridal-crm-prod-postgresql \
  -U bridalcover \
  -d bridalcover_db \
  -p 5432
```

### Backup do Banco

```bash
# Port forward PostgreSQL
kubectl port-forward -n production svc/bridal-crm-prod-postgresql 5432:5432

# Fazer dump (em outro terminal)
export PGPASSWORD=$(kubectl get secret bridal-crm-prod-postgresql \
  -n production \
  -o jsonpath="{.data.password}" | base64 -d)

pg_dump -h localhost -U bridalcover -d bridalcover_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

### Verificar configuração aplicada

```bash
# Ver ConfigMap
kubectl get configmap bridal-crm-prod-bridal-cover-crm -n production -o yaml

# Ver Secret (base64 encoded)
kubectl get secret bridal-crm-prod-postgresql -n production -o yaml

# Ver variáveis de ambiente do pod
kubectl exec -n production <pod-name> -- env | grep SPRING
```

## 🆘 Troubleshooting Rápido

### Pod não inicia

```bash
# Ver status
kubectl get pods -n production

# Descrever pod
kubectl describe pod <pod-name> -n production

# Ver logs
kubectl logs <pod-name> -n production

# Ver logs do container anterior (se crashou)
kubectl logs <pod-name> -n production --previous
```

### ImagePullBackOff

```bash
# Verificar secret de pull
kubectl get secrets -n production

# Descrever pod (ver mensagem de erro)
kubectl describe pod <pod-name> -n production

# Verificar se imagem existe
docker pull guantunes/bridal-cover-crm:1.0.0
```

### CrashLoopBackOff

```bash
# Ver logs
kubectl logs <pod-name> -n production --previous

# Ver eventos
kubectl get events -n production --sort-by='.lastTimestamp'

# Executar shell no pod (se possível)
kubectl exec -it <pod-name> -n production -- /bin/sh
```

### Release preso em "pending-install"

```bash
# Ver secret da release
kubectl get secrets -n production | grep sh.helm.release

# Deletar release manualmente
kubectl delete secret -n production sh.helm.release.v1.bridal-crm-prod.v1

# Reinstalar
helm install bridal-crm-prod ./bridal-cover-crm \
  -f ./bridal-cover-crm/values-prod.yaml \
  -n production
```

## 📦 Empacotamento e Distribuição

### Criar pacote

```bash
# Package
helm package ./bridal-cover-crm

# Output: bridal-cover-crm-1.0.0.tgz

# Instalar do pacote
helm install my-release bridal-cover-crm-1.0.0.tgz
```

### Repositório Local

```bash
# Criar índice
helm repo index .

# Servir via HTTP
python3 -m http.server 8080

# Adicionar repo (em outra máquina)
helm repo add local http://localhost:8080
helm search repo local
```

## 🔄 CI/CD Integration

### GitLab CI

```yaml
deploy:production:
  stage: deploy
  script:
    - helm upgrade --install bridal-crm-prod ./helm-chart/bridal-cover-crm
        -f ./helm-chart/bridal-cover-crm/values-prod.yaml
        --set image.tag=$CI_COMMIT_TAG
        -n production
  only:
    - tags
```

### GitHub Actions

```yaml
- name: Deploy to Production
  run: |
    helm upgrade --install bridal-crm-prod ./helm-chart/bridal-cover-crm \
      -f ./helm-chart/bridal-cover-crm/values-prod.yaml \
      --set image.tag=${{ github.ref_name }} \
      -n production
```

## 📚 Recursos

- **Documentação Helm**: https://helm.sh/docs/
- **Chart Best Practices**: https://helm.sh/docs/chart_best_practices/
- **Guia Completo**: [docs/kubernetes/15-helm-tiller-guide.md](../docs/kubernetes/15-helm-tiller-guide.md)
- **Chart README**: [bridal-cover-crm/README.md](bridal-cover-crm/README.md)

---

💡 **Dica**: Use `make help` para ver todos os comandos disponíveis no Makefile!


