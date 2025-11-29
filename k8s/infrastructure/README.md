# Infraestrutura do Cluster Kubernetes

Este diretório contém recursos de **infraestrutura base** do cluster que **não são gerenciados por aplicações**.

## 📋 Conteúdo

### `postgres-volumes.yaml`
PersistentVolumes para PostgreSQL em todos os ambientes (dev, staging, prod).

**Por que aqui e não no Helm?**
- PVs são recursos **globais** (não pertencem a um namespace)
- Devem ser criados **antes** das aplicações
- Representam armazenamento físico que existe independente das apps
- Não devem ser deletados quando a app é desinstalada

## 🚀 Como Aplicar

### Opção 1: Usando Makefile (Recomendado)

```bash
# Ver comandos disponíveis
cd k8s/
make help

# Setup completo (cria diretórios + aplica recursos)
make setup-all
```

### Opção 2: Manual com kubectl

```bash
# 1. Criar diretórios físicos primeiro (apenas para clusters locais)
sudo mkdir -p /mnt/data/postgres-dev
sudo mkdir -p /mnt/data/postgres-staging
sudo mkdir -p /mnt/data/postgres-prod
sudo chmod -R 777 /mnt/data/

# 2. Aplicar recursos
kubectl apply -f k8s/infrastructure/

# 3. Verificar
kubectl get pv
kubectl get namespaces
kubectl get resourcequota -A
```

## 📝 Notas

- Estes recursos são aplicados via `kubectl apply` (não Helm)
- Devem ser criados **uma vez** e persistem entre deploys das aplicações
- Em ambientes de cloud (AWS, GCP, Azure), você usaria StorageClasses dinâmicas


