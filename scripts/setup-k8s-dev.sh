#!/bin/bash

# =============================================================================
# Script para configurar ambiente DEV no Kubernetes
# =============================================================================

set -e  # Parar se houver erro

echo "=========================================="
echo "🚀 Configurando ambiente DEV no Kubernetes"
echo "=========================================="

# Cores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar se kubectl está disponível
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}❌ kubectl não encontrado. Instale o kubectl primeiro.${NC}"
    exit 1
fi

echo ""
echo "📁 Passo 1: Criando diretórios para volumes"
sudo mkdir -p /mnt/data/postgres-dev
sudo mkdir -p /mnt/data/postgres-staging
sudo mkdir -p /mnt/data/postgres-prod
sudo chmod -R 777 /mnt/data/postgres-*
echo -e "${GREEN}✅ Diretórios criados${NC}"

echo ""
echo "💾 Passo 2: Criando infraestrutura base"
kubectl apply -f k8s/infrastructure/namespaces.yaml
kubectl apply -f k8s/infrastructure/postgres-volumes.yaml
kubectl apply -f k8s/infrastructure/resource-quotas.yaml
echo -e "${GREEN}✅ Infraestrutura criada${NC}"

echo ""
echo "📊 Verificando infraestrutura:"
kubectl get namespaces -l project=bridal-cover-crm
kubectl get pv
kubectl get resourcequota -A

echo ""
echo "🚀 Passo 3: Deploy da aplicação via Helm"
echo -e "${YELLOW}Navegue para: cd helm-chart/${NC}"
echo -e "${YELLOW}Execute: make deploy-dev${NC}"
echo ""
echo -e "${GREEN}✅ Setup de infraestrutura concluído!${NC}"
echo -e "${YELLOW}Próximo passo: Deploy da aplicação com Helm${NC}"
exit 0


