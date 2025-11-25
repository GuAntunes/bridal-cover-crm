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
echo "💾 Passo 2: Criando PersistentVolumes"
kubectl apply -f k8s/postgres-volumes.yaml
echo -e "${GREEN}✅ PersistentVolumes criados${NC}"

echo ""
echo "📊 Verificando PersistentVolumes:"
kubectl get pv

echo ""
echo "📦 Passo 3: Criando namespace bridal-cover-crm-dev"
kubectl create namespace bridal-cover-crm-dev --dry-run=client -o yaml | kubectl apply -f -
echo -e "${GREEN}✅ Namespace criado${NC}"

echo ""
echo "🔧 Passo 4: Aplicando configurações do ambiente DEV"
kubectl apply -k k8s/overlays/dev
echo -e "${GREEN}✅ Configurações aplicadas${NC}"

echo ""
echo "⏳ Aguardando pods iniciarem (30 segundos)..."
sleep 30

echo ""
echo "=========================================="
echo "📊 STATUS DOS RECURSOS"
echo "=========================================="
kubectl get all,pvc,pv -n bridal-cover-crm-dev

echo ""
echo "=========================================="
echo "📝 LOGS DO POSTGRESQL"
echo "=========================================="
kubectl logs -n bridal-cover-crm-dev statefulset/postgres --tail=20 || echo -e "${YELLOW}⚠️  Pod do PostgreSQL ainda não está pronto${NC}"

echo ""
echo "=========================================="
echo "✅ SETUP CONCLUÍDO!"
echo "=========================================="
echo ""
echo "📌 Comandos úteis:"
echo ""
echo "  Ver pods:"
echo "    kubectl get pods -n bridal-cover-crm-dev"
echo ""
echo "  Ver logs do PostgreSQL:"
echo "    kubectl logs -n bridal-cover-crm-dev statefulset/postgres -f"
echo ""
echo "  Ver logs da aplicação:"
echo "    kubectl logs -n bridal-cover-crm-dev deployment/bridal-cover-crm -f"
echo ""
echo "  Conectar ao PostgreSQL:"
echo "    kubectl exec -it postgres-0 -n bridal-cover-crm-dev -- psql -U postgres -d bridal_cover_crm_dev"
echo ""
echo "  Port-forward para o PostgreSQL (acessar localmente):"
echo "    kubectl port-forward -n bridal-cover-crm-dev service/postgres-service 5432:5432"
echo ""
echo "  Port-forward para a aplicação:"
echo "    kubectl port-forward -n bridal-cover-crm-dev service/bridal-cover-crm-service 8080:8080"
echo ""

