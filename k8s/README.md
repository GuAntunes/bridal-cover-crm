# Infraestrutura Kubernetes - Bridal Cover CRM

Este diretório contém **apenas recursos de infraestrutura base** do cluster Kubernetes.

## 📌 Importante: Helm vs kubectl

**Este projeto usa uma arquitetura híbrida:**

- 🏗️ **Infraestrutura (este diretório)**: Gerenciada via `kubectl apply`
- 🚀 **Aplicações** (`../helm-chart/`): Gerenciadas via `Helm`

> ⚠️ **Regra de Ouro**: Um recurso deve ser gerenciado por **APENAS UMA ferramenta** (kubectl OU Helm)

Para entender melhor quando usar cada ferramenta, leia: [`docs/deployment/helm-vs-kubectl.md`](../docs/deployment/helm-vs-kubectl.md)

## 📁 Estrutura

```
k8s/
└── infrastructure/
    ├── namespaces.yaml          # Namespaces (dev, staging, production)
    ├── postgres-volumes.yaml    # PersistentVolumes para PostgreSQL
    ├── resource-quotas.yaml     # Limites de recursos por namespace
    └── README.md                # Documentação
```

## 🚀 Setup Inicial (Executar Uma Vez)

### Passo 1: Criar diretórios físicos (HostPath)

Se você está usando **Minikube** ou **cluster local** com HostPath:

```bash
# Criar diretórios para os volumes
sudo mkdir -p /mnt/data/postgres-dev
sudo mkdir -p /mnt/data/postgres-staging
sudo mkdir -p /mnt/data/postgres-prod

# Dar permissões
sudo chmod -R 777 /mnt/data/
```

> 💡 **Cloud Providers**: Se estiver usando AWS, GCP ou Azure, pule este passo. Use StorageClasses dinâmicas em vez de HostPath.

### Passo 2: Aplicar recursos de infraestrutura

```bash
# Aplicar todos os recursos de infraestrutura
kubectl apply -f k8s/infrastructure/

# OU aplicar individualmente
kubectl apply -f k8s/infrastructure/namespaces.yaml
kubectl apply -f k8s/infrastructure/postgres-volumes.yaml
kubectl apply -f k8s/infrastructure/resource-quotas.yaml
```

### Passo 3: Verificar

```bash
# Ver namespaces
kubectl get namespaces

# Ver PersistentVolumes
kubectl get pv

# Ver quotas
kubectl get resourcequota -A

# Ver tudo
kubectl get all -A
```

## 📝 Recursos de Infraestrutura

### 1. Namespaces (`namespaces.yaml`)

Cria namespaces isolados para cada ambiente:
- `dev` - Desenvolvimento
- `staging` - Homologação
- `production` - Produção

**Por que criar manualmente?**
- Permite aplicar labels e anotações consistentes
- Garante que namespaces existem antes do deploy das apps
- Facilita aplicação de políticas (NetworkPolicies, ResourceQuotas)

### 2. PersistentVolumes (`postgres-volumes.yaml`)

Cria volumes persistentes para PostgreSQL em cada ambiente:
- `postgres-pv-dev` - 5Gi
- `postgres-pv-staging` - 10Gi
- `postgres-pv-prod` - 50Gi

**Por que criar manualmente?**
- PVs são recursos **globais** (não pertencem a um namespace)
- Representam armazenamento físico que existe independentemente das apps
- Não devem ser deletados quando a aplicação é desinstalada
- Política `Retain` preserva dados mesmo após deletar PVC

### 3. ResourceQuotas (`resource-quotas.yaml`)

Limita recursos por namespace para evitar consumo excessivo:

| Ambiente | CPU Requests | Memória Requests | Pods Max |
|----------|--------------|------------------|----------|
| **Dev** | 4 cores | 8Gi | 20 |
| **Staging** | 6 cores | 12Gi | 30 |
| **Production** | 16 cores | 32Gi | 50 |

**Ajuste conforme seu cluster!**

```bash
# Ver uso atual de recursos
kubectl describe resourcequota -n production
```

## 🔄 Atualização

Se precisar modificar os recursos de infraestrutura:

```bash
# Editar arquivo
vim k8s/infrastructure/postgres-volumes.yaml

# Reaplicar
kubectl apply -f k8s/infrastructure/postgres-volumes.yaml

# Verificar
kubectl get pv
```

## 🗑️ Limpeza

### Deletar Namespaces (e tudo dentro deles)

```bash
# ⚠️ CUIDADO: Isso deleta TUDO no namespace!
kubectl delete namespace dev
kubectl delete namespace staging
kubectl delete namespace production
```

### Deletar PersistentVolumes

```bash
# ⚠️ CUIDADO: Verifique backups antes!
kubectl delete pv postgres-pv-dev
kubectl delete pv postgres-pv-staging
kubectl delete pv postgres-pv-prod

# OU deletar todos de uma vez
kubectl delete -f k8s/infrastructure/postgres-volumes.yaml
```

## 🛠️ Makefile

Para facilitar o gerenciamento, use o Makefile:

```bash
# Aplicar toda infraestrutura
make setup-infra

# Ver status
make status-infra

# Limpar tudo (cuidado!)
make clean-infra
```

## 📊 Comandos Úteis

### Verificar recursos

```bash
# Listar todos PVs
kubectl get pv

# Ver detalhes de um PV
kubectl describe pv postgres-pv-prod

# Listar namespaces
kubectl get ns

# Ver quotas de todos namespaces
kubectl get resourcequota -A

# Ver uso de recursos
kubectl top nodes
kubectl top pods -n production
```

### Troubleshooting

```bash
# PV não está "Available"?
kubectl get pv postgres-pv-dev -o yaml

# Verificar se diretório existe
ls -la /mnt/data/postgres-dev

# Namespace não deleta?
kubectl get namespace production -o json | jq '.spec.finalizers = []' | kubectl replace --raw "/api/v1/namespaces/production/finalize" -f -
```

## 🔐 Boas Práticas

1. **Backups**: Faça backup regular dos PVs (especialmente produção)
2. **Monitoramento**: Configure alertas para quotas próximas do limite
3. **Labels**: Use labels consistentes para facilitar busca e organização
4. **Documentação**: Documente mudanças em infraestrutura
5. **GitOps**: Versione estes arquivos no Git como fonte da verdade

## 🚀 Próximos Passos

Após configurar a infraestrutura:

1. **Deploy da aplicação** via Helm:
   ```bash
   cd helm-chart/
   make deploy-dev
   ```

2. **Ver documentação do Helm**:
   - [`helm-chart/README.md`](../helm-chart/README.md)

3. **Configurar CI/CD**:
   - Jenkins, GitLab CI, ou GitHub Actions

4. **Monitoramento** (futuro):
   - Prometheus + Grafana
   - AlertManager

## 📚 Leitura Adicional

- [Helm vs kubectl - Quando usar cada um?](../docs/deployment/helm-vs-kubectl.md)
- [Kubernetes PersistentVolumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/)
- [Resource Quotas](https://kubernetes.io/docs/concepts/policy/resource-quotas/)
- [Namespaces](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/)

---

**📞 Dúvidas?** Consulte a documentação em `docs/kubernetes/` ou abra uma issue.

