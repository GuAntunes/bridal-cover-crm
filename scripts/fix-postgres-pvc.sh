#!/bin/bash
# Script para corrigir problema de PVC do PostgreSQL
# Uso: ./scripts/fix-postgres-pvc.sh

set -e

NAMESPACE="${1:-dev}"
RELEASE_NAME="bridal-cover-crm"
CHART_DIR="./helm-chart/bridal-cover-crm"

echo "🔍 Verificando problema de PVC do PostgreSQL..."
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Diagnóstico ===${NC}"
echo ""

# Verificar StorageClasses disponíveis
echo -e "${YELLOW}StorageClasses disponíveis:${NC}"
kubectl get storageclass 2>/dev/null || echo "Nenhuma StorageClass encontrada"
echo ""

# Verificar PVs
echo -e "${YELLOW}PersistentVolumes disponíveis:${NC}"
kubectl get pv 2>/dev/null || echo "Nenhum PV encontrado"
echo ""

# Verificar PVCs no namespace
echo -e "${YELLOW}PersistentVolumeClaims no namespace ${NAMESPACE}:${NC}"
kubectl get pvc -n "$NAMESPACE" 2>/dev/null || echo "Nenhum PVC encontrado"
echo ""

# Verificar se existe StorageClass padrão
DEFAULT_SC=$(kubectl get storageclass -o jsonpath='{.items[?(@.metadata.annotations.storageclass\.kubernetes\.io/is-default-class=="true")].metadata.name}' 2>/dev/null)

if [ -n "$DEFAULT_SC" ]; then
    echo -e "${GREEN}✓ StorageClass padrão encontrada: $DEFAULT_SC${NC}"
    SOLUTION="default-sc"
else
    echo -e "${YELLOW}⚠ Nenhuma StorageClass padrão encontrada${NC}"
    SOLUTION="manual-pv"
fi
echo ""

# Perguntar qual solução aplicar
echo -e "${BLUE}=== Soluções Disponíveis ===${NC}"
echo "1) Criar PersistentVolume manual (requer storageClass: 'manual')"
echo "2) Usar StorageClass padrão do cluster (recomendado)"
echo "3) Desabilitar persistência (dados serão perdidos ao reiniciar)"
echo "4) Apenas diagnosticar (não aplicar correção)"
echo ""
read -p "Escolha a solução (1-4): " choice

case $choice in
    1)
        echo -e "${BLUE}Aplicando Solução 1: PersistentVolume Manual${NC}"
        
        # Criar PV se o arquivo existir
        if [ -f "k8s/infrastructure/postgresql-pv-dev.yaml" ]; then
            echo "Criando PersistentVolume..."
            kubectl apply -f k8s/infrastructure/postgresql-pv-dev.yaml
            echo -e "${GREEN}✓ PV criado${NC}"
        else
            echo -e "${RED}✗ Arquivo k8s/infrastructure/postgresql-pv-dev.yaml não encontrado${NC}"
            exit 1
        fi
        
        # Fazer upgrade do Helm
        echo "Fazendo upgrade do Helm chart..."
        helm upgrade --install "$RELEASE_NAME" "$CHART_DIR" \
            -f "$CHART_DIR/values-dev.yaml" \
            -n "$NAMESPACE" --create-namespace \
            --wait --timeout 5m
        
        echo -e "${GREEN}✓ Deploy concluído${NC}"
        ;;
        
    2)
        echo -e "${BLUE}Aplicando Solução 2: StorageClass Padrão${NC}"
        
        # Fazer upgrade do Helm com storageClass vazio
        echo "Fazendo upgrade do Helm chart..."
        helm upgrade --install "$RELEASE_NAME" "$CHART_DIR" \
            -f "$CHART_DIR/values-dev.yaml" \
            --set postgresql.primary.persistence.storageClass="" \
            -n "$NAMESPACE" --create-namespace \
            --wait --timeout 5m
        
        echo -e "${GREEN}✓ Deploy concluído${NC}"
        ;;
        
    3)
        echo -e "${YELLOW}Aplicando Solução 3: Desabilitar Persistência${NC}"
        echo -e "${RED}AVISO: Os dados do PostgreSQL serão perdidos quando o pod reiniciar!${NC}"
        read -p "Tem certeza? (sim/não): " confirm
        
        if [ "$confirm" != "sim" ]; then
            echo "Operação cancelada."
            exit 0
        fi
        
        # Fazer upgrade do Helm sem persistência
        echo "Fazendo upgrade do Helm chart..."
        helm upgrade --install "$RELEASE_NAME" "$CHART_DIR" \
            -f "$CHART_DIR/values-dev.yaml" \
            --set postgresql.primary.persistence.enabled=false \
            -n "$NAMESPACE" --create-namespace \
            --wait --timeout 5m
        
        echo -e "${GREEN}✓ Deploy concluído${NC}"
        ;;
        
    4)
        echo -e "${BLUE}Apenas diagnóstico - nenhuma ação tomada${NC}"
        exit 0
        ;;
        
    *)
        echo -e "${RED}Opção inválida${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${BLUE}=== Verificação Pós-Deploy ===${NC}"
echo ""

# Aguardar alguns segundos
sleep 3

# Verificar PVCs
echo -e "${YELLOW}PVCs:${NC}"
kubectl get pvc -n "$NAMESPACE"
echo ""

# Verificar Pods
echo -e "${YELLOW}Pods PostgreSQL:${NC}"
kubectl get pods -n "$NAMESPACE" -l app.kubernetes.io/name=postgresql
echo ""

# Verificar eventos recentes
echo -e "${YELLOW}Eventos recentes:${NC}"
kubectl get events -n "$NAMESPACE" --sort-by='.lastTimestamp' | tail -10
echo ""

echo -e "${GREEN}✓ Script concluído!${NC}"
echo ""
echo "Para verificar logs do PostgreSQL:"
echo "  kubectl logs -n $NAMESPACE -l app.kubernetes.io/name=postgresql --tail=50"
echo ""
echo "Para testar conexão:"
echo "  kubectl exec -it -n $NAMESPACE <postgres-pod-name> -- psql -U bridalcover_dev -d bridalcover_dev"

