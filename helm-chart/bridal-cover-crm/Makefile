# Backend (Bridal Cover CRM) Helm Chart Makefile
# ⚠️ RECOMENDAÇÃO: Use o Makefile na raiz (../Makefile) para comandos simplificados

CHART_NAME := bridal-cover-crm
RELEASE_NAME := backend

# Namespaces
NS_DEV := dev
NS_STAGING := staging
NS_PROD := production

# Cores
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m

.DEFAULT_GOAL := help

.PHONY: help
help: ## Mostrar esta ajuda
	@echo "\033[1;33m⚠️  DICA: Use 'make help' no diretório ../helm-chart/ para comandos simplificados\033[0m"
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Backend - Bridal Cover CRM (uso avançado)$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""

##@ Setup

.PHONY: deps-update
deps-update: ## Atualizar dependências do chart
	@echo "$(GREEN)Atualizando dependências...$(NC)"
	helm dependency update
	@echo "$(GREEN)✅ Dependências atualizadas!$(NC)"

##@ Validação

.PHONY: lint
lint: ## Validar sintaxe do chart
	@echo "$(GREEN)Validando chart...$(NC)"
	helm lint .

.PHONY: template-dev
template-dev: ## Ver templates renderizados (dev)
	@helm template $(RELEASE_NAME)-dev . -f values-dev.yaml

.PHONY: dry-run-dev
dry-run-dev: ## Simular instalação em dev
	@echo "$(GREEN)Simulando instalação em DEV...$(NC)"
	helm install $(RELEASE_NAME)-dev . \
		-f values-dev.yaml \
		-n $(NS_DEV) --dry-run --debug

##@ Deploy

.PHONY: install-dev
install-dev: ## Instalar em dev
	@echo "$(GREEN)Instalando Backend (DEV)...$(NC)"
	kubectl create namespace $(NS_DEV) --dry-run=client -o yaml | kubectl apply -f -
	helm dependency update
	helm install $(RELEASE_NAME)-dev . \
		-f values-dev.yaml \
		-n $(NS_DEV)
	@echo "$(GREEN)✅ Backend instalado!$(NC)"

.PHONY: install-staging
install-staging: ## Instalar em staging
	@echo "$(GREEN)Instalando Backend (STAGING)...$(NC)"
	kubectl create namespace $(NS_STAGING) --dry-run=client -o yaml | kubectl apply -f -
	helm dependency update
	helm install $(RELEASE_NAME)-staging . \
		-f values-staging.yaml \
		-n $(NS_STAGING)

.PHONY: install-prod
install-prod: ## Instalar em produção
	@echo "$(RED)⚠️  Instalando em PRODUÇÃO!$(NC)"
	@sleep 3
	kubectl create namespace $(NS_PROD) --dry-run=client -o yaml | kubectl apply -f -
	helm dependency update
	helm install $(RELEASE_NAME)-prod . \
		-f values-prod.yaml \
		-n $(NS_PROD)

##@ Atualização

.PHONY: upgrade-dev
upgrade-dev: ## Atualizar release em dev
	@echo "$(GREEN)Atualizando Backend (DEV)...$(NC)"
	helm dependency update
	helm upgrade $(RELEASE_NAME)-dev . \
		-f values-dev.yaml \
		-n $(NS_DEV)

.PHONY: upgrade-staging
upgrade-staging: ## Atualizar release em staging
	@echo "$(GREEN)Atualizando Backend (STAGING)...$(NC)"
	helm dependency update
	helm upgrade $(RELEASE_NAME)-staging . \
		-f values-staging.yaml \
		-n $(NS_STAGING)

.PHONY: upgrade-prod
upgrade-prod: ## Atualizar release em produção
	@echo "$(RED)⚠️  Atualizando PRODUÇÃO!$(NC)"
	@sleep 3
	helm dependency update
	helm upgrade $(RELEASE_NAME)-prod . \
		-f values-prod.yaml \
		-n $(NS_PROD)

##@ Status

.PHONY: status-dev
status-dev: ## Ver status em dev
	@helm status $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: status-staging
status-staging: ## Ver status em staging
	@helm status $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: status-prod
status-prod: ## Ver status em produção
	@helm status $(RELEASE_NAME)-prod -n $(NS_PROD)

##@ Limpeza

.PHONY: uninstall-dev
uninstall-dev: ## Desinstalar de dev
	@echo "$(YELLOW)Desinstalando Backend (DEV)...$(NC)"
	helm uninstall $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: uninstall-staging
uninstall-staging: ## Desinstalar de staging
	@echo "$(YELLOW)Desinstalando Backend (STAGING)...$(NC)"
	helm uninstall $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: uninstall-prod
uninstall-prod: ## Desinstalar de produção
	@echo "$(RED)⚠️  Desinstalando de PRODUÇÃO!$(NC)"
	@sleep 5
	helm uninstall $(RELEASE_NAME)-prod -n $(NS_PROD)

.PHONY: clean
clean: ## Limpar dependências
	@echo "$(GREEN)Limpando dependências...$(NC)"
	rm -rf charts/*
	rm -rf Chart.lock

