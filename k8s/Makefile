# Makefile - Infraestrutura Kubernetes
# Gerenciamento de recursos base do cluster

INFRA_DIR := infrastructure

# Cores
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m

.PHONY: help
help: ## Mostrar esta ajuda
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Infraestrutura Kubernetes - Bridal Cover CRM$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-25s$(NC) %s\n", $$1, $$2}'
	@echo ""

##@ Setup

.PHONY: setup-dirs
setup-dirs: ## Criar diretórios para PersistentVolumes (apenas Minikube/local)
	@echo "$(GREEN)Criando diretórios para volumes...$(NC)"
	@echo "$(YELLOW)⚠️  Requer sudo. Certifique-se de estar usando cluster local!$(NC)"
	sudo mkdir -p /mnt/data/postgres-dev
	sudo mkdir -p /mnt/data/postgres-staging
	sudo mkdir -p /mnt/data/postgres-prod
	sudo chmod -R 777 /mnt/data/postgres-*
	@echo "$(GREEN)✅ Diretórios criados!$(NC)"

.PHONY: setup-infra
setup-infra: ## Aplicar toda infraestrutura (namespaces, PVs, quotas)
	@echo "$(GREEN)Aplicando recursos de infraestrutura...$(NC)"
	kubectl apply -f $(INFRA_DIR)/namespaces.yaml
	kubectl apply -f $(INFRA_DIR)/postgres-volumes.yaml
	kubectl apply -f $(INFRA_DIR)/resource-quotas.yaml
	@echo "$(GREEN)✅ Infraestrutura aplicada!$(NC)"

.PHONY: setup-all
setup-all: setup-dirs setup-infra status-infra ## Setup completo (dirs + infra)
	@echo ""
	@echo "$(GREEN)✅ Setup completo!$(NC)"
	@echo ""
	@echo "$(YELLOW)Próximos passos:$(NC)"
	@echo "  1. Deploy da aplicação via Helm:"
	@echo "     cd ../helm-chart && make deploy-dev"
	@echo ""

##@ Status

.PHONY: status-infra
status-infra: ## Ver status de toda infraestrutura
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Namespaces$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@kubectl get namespaces -l project=bridal-cover-crm
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  PersistentVolumes$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@kubectl get pv
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Resource Quotas$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@kubectl get resourcequota -A

.PHONY: status-pv
status-pv: ## Ver status dos PersistentVolumes
	@kubectl get pv

.PHONY: status-namespaces
status-namespaces: ## Ver status dos namespaces
	@kubectl get namespaces -l project=bridal-cover-crm

.PHONY: status-quotas
status-quotas: ## Ver resource quotas de todos ambientes
	@kubectl get resourcequota -A

.PHONY: describe-quota-dev
describe-quota-dev: ## Ver detalhes da quota de dev
	@kubectl describe resourcequota dev-quota -n dev

.PHONY: describe-quota-staging
describe-quota-staging: ## Ver detalhes da quota de staging
	@kubectl describe resourcequota staging-quota -n staging

.PHONY: describe-quota-prod
describe-quota-prod: ## Ver detalhes da quota de produção
	@kubectl describe resourcequota production-quota -n production

##@ Aplicar Recursos Individuais

.PHONY: apply-namespaces
apply-namespaces: ## Aplicar apenas namespaces
	@echo "$(GREEN)Aplicando namespaces...$(NC)"
	kubectl apply -f $(INFRA_DIR)/namespaces.yaml

.PHONY: apply-volumes
apply-volumes: ## Aplicar apenas PersistentVolumes
	@echo "$(GREEN)Aplicando PersistentVolumes...$(NC)"
	kubectl apply -f $(INFRA_DIR)/postgres-volumes.yaml

.PHONY: apply-quotas
apply-quotas: ## Aplicar apenas ResourceQuotas
	@echo "$(GREEN)Aplicando ResourceQuotas...$(NC)"
	kubectl apply -f $(INFRA_DIR)/resource-quotas.yaml

##@ Limpeza

.PHONY: delete-quotas
delete-quotas: ## Deletar ResourceQuotas
	@echo "$(YELLOW)Deletando ResourceQuotas...$(NC)"
	kubectl delete -f $(INFRA_DIR)/resource-quotas.yaml --ignore-not-found=true

.PHONY: delete-volumes
delete-volumes: ## Deletar PersistentVolumes
	@echo "$(RED)⚠️  Deletando PersistentVolumes!$(NC)"
	@echo "$(RED)Certifique-se de ter backup dos dados!$(NC)"
	@echo "$(RED)Pressione Ctrl+C para cancelar, ou aguarde 5s...$(NC)"
	@sleep 5
	kubectl delete -f $(INFRA_DIR)/postgres-volumes.yaml --ignore-not-found=true

.PHONY: delete-namespaces
delete-namespaces: ## Deletar namespaces (deleta TUDO dentro!)
	@echo "$(RED)⚠️  Deletando namespaces!$(NC)"
	@echo "$(RED)Isso deletará TODOS os recursos dentro dos namespaces!$(NC)"
	@echo "$(RED)Pressione Ctrl+C para cancelar, ou aguarde 10s...$(NC)"
	@sleep 10
	kubectl delete namespace dev --ignore-not-found=true
	kubectl delete namespace staging --ignore-not-found=true
	kubectl delete namespace production --ignore-not-found=true

.PHONY: clean-infra
clean-infra: delete-quotas ## Limpar infraestrutura (quotas apenas)
	@echo "$(GREEN)✅ Quotas removidas!$(NC)"
	@echo "$(YELLOW)Namespaces e PVs não foram deletados (use comandos específicos)$(NC)"

.PHONY: clean-all
clean-all: ## Limpar TUDO (namespaces, PVs, quotas)
	@echo "$(RED)⚠️  CUIDADO: Isso deletará TODA a infraestrutura!$(NC)"
	@echo "$(RED)Pressione Ctrl+C para cancelar, ou aguarde 10s...$(NC)"
	@sleep 10
	@make delete-namespaces
	@make delete-volumes
	@make delete-quotas
	@echo "$(GREEN)✅ Tudo limpo!$(NC)"

##@ Utilitários

.PHONY: validate
validate: ## Validar YAMLs sem aplicar
	@echo "$(GREEN)Validando YAMLs...$(NC)"
	kubectl apply -f $(INFRA_DIR)/ --dry-run=client

.PHONY: top-nodes
top-nodes: ## Ver uso de recursos dos nodes
	@kubectl top nodes

.PHONY: top-dev
top-dev: ## Ver uso de recursos em dev
	@kubectl top pods -n dev

.PHONY: top-staging
top-staging: ## Ver uso de recursos em staging
	@kubectl top pods -n staging

.PHONY: top-prod
top-prod: ## Ver uso de recursos em produção
	@kubectl top pods -n production

.DEFAULT_GOAL := help

