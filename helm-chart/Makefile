# Makefile - Bridal Cover CRM Helm Chart
# Comandos essenciais para gerenciamento do chart

CHART_NAME := bridal-cover-crm
CHART_PATH := ./$(CHART_NAME)
RELEASE_NAME := bridal-crm

# Namespaces
NS_DEV := dev
NS_STAGING := staging
NS_PROD := production

# Cores
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m

.PHONY: help
help: ## Mostrar esta ajuda
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  Bridal Cover CRM - Helm Chart$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""

##@ Setup

.PHONY: setup
setup: ## Configurar dependências (executar apenas uma vez)
	@echo "$(GREEN)Configurando repositórios Helm...$(NC)"
	helm repo add bitnami https://charts.bitnami.com/bitnami
	helm repo update
	@echo "$(GREEN)Atualizando dependências do chart...$(NC)"
	cd $(CHART_PATH) && helm dependency update
	@echo "$(GREEN)✅ Setup completo!$(NC)"

##@ Validação

.PHONY: lint
lint: ## Validar sintaxe do chart
	@echo "$(GREEN)Validando chart...$(NC)"
	helm lint $(CHART_PATH)

.PHONY: dry-run-dev
dry-run-dev: ## Simular instalação em dev
	@echo "$(GREEN)Simulando instalação em DEV...$(NC)"
	helm install $(RELEASE_NAME)-dev $(CHART_PATH) \
		-f $(CHART_PATH)/values-dev.yaml \
		-n $(NS_DEV) --dry-run --debug

.PHONY: dry-run-staging
dry-run-staging: ## Simular instalação em staging
	@echo "$(GREEN)Simulando instalação em STAGING...$(NC)"
	helm install $(RELEASE_NAME)-staging $(CHART_PATH) \
		-f $(CHART_PATH)/values-staging.yaml \
		-n $(NS_STAGING) --dry-run --debug

.PHONY: dry-run-prod
dry-run-prod: ## Simular instalação em produção
	@echo "$(YELLOW)Simulando instalação em PRODUÇÃO...$(NC)"
	helm install $(RELEASE_NAME)-prod $(CHART_PATH) \
		-f $(CHART_PATH)/values-prod.yaml \
		-n $(NS_PROD) --dry-run --debug

##@ Deploy

.PHONY: deploy-dev
deploy-dev: ## Deploy completo em dev
	@echo "$(GREEN)Deploy em DEV...$(NC)"
	kubectl create namespace $(NS_DEV) --dry-run=client -o yaml | kubectl apply -f -
	cd $(CHART_PATH) && helm dependency update
	helm upgrade --install $(RELEASE_NAME)-dev $(CHART_PATH) \
		-f $(CHART_PATH)/values-dev.yaml \
		-n $(NS_DEV)
	@echo "$(GREEN)✅ Deploy DEV completo!$(NC)"

.PHONY: deploy-staging
deploy-staging: ## Deploy completo em staging
	@echo "$(GREEN)Deploy em STAGING...$(NC)"
	kubectl create namespace $(NS_STAGING) --dry-run=client -o yaml | kubectl apply -f -
	cd $(CHART_PATH) && helm dependency update
	helm upgrade --install $(RELEASE_NAME)-staging $(CHART_PATH) \
		-f $(CHART_PATH)/values-staging.yaml \
		-n $(NS_STAGING)
	@echo "$(GREEN)✅ Deploy STAGING completo!$(NC)"

.PHONY: deploy-prod
deploy-prod: ## Deploy completo em produção
	@echo "$(RED)⚠️  Deploy em PRODUÇÃO!$(NC)"
	@echo "$(RED)Pressione Ctrl+C para cancelar, ou aguarde 5s...$(NC)"
	@sleep 5
	kubectl create namespace $(NS_PROD) --dry-run=client -o yaml | kubectl apply -f -
	cd $(CHART_PATH) && helm dependency update
	helm upgrade --install $(RELEASE_NAME)-prod $(CHART_PATH) \
		-f $(CHART_PATH)/values-prod.yaml \
		-n $(NS_PROD)
	@echo "$(GREEN)✅ Deploy PRODUÇÃO completo!$(NC)"

##@ Atualização

.PHONY: upgrade-dev
upgrade-dev: ## Atualizar release em dev
	@echo "$(GREEN)Atualizando DEV...$(NC)"
	cd $(CHART_PATH) && helm dependency update
	helm upgrade $(RELEASE_NAME)-dev $(CHART_PATH) \
		-f $(CHART_PATH)/values-dev.yaml \
		-n $(NS_DEV)

.PHONY: upgrade-staging
upgrade-staging: ## Atualizar release em staging
	@echo "$(GREEN)Atualizando STAGING...$(NC)"
	cd $(CHART_PATH) && helm dependency update
	helm upgrade $(RELEASE_NAME)-staging $(CHART_PATH) \
		-f $(CHART_PATH)/values-staging.yaml \
		-n $(NS_STAGING)

.PHONY: upgrade-prod
upgrade-prod: ## Atualizar release em produção
	@echo "$(RED)⚠️  Atualizando PRODUÇÃO!$(NC)"
	@echo "$(RED)Pressione Ctrl+C para cancelar, ou aguarde 5s...$(NC)"
	@sleep 5
	cd $(CHART_PATH) && helm dependency update
	helm upgrade $(RELEASE_NAME)-prod $(CHART_PATH) \
		-f $(CHART_PATH)/values-prod.yaml \
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

.PHONY: pods-dev
pods-dev: ## Ver pods em dev
	@kubectl get pods -n $(NS_DEV) -l app.kubernetes.io/instance=$(RELEASE_NAME)-dev

.PHONY: pods-staging
pods-staging: ## Ver pods em staging
	@kubectl get pods -n $(NS_STAGING) -l app.kubernetes.io/instance=$(RELEASE_NAME)-staging

.PHONY: pods-prod
pods-prod: ## Ver pods em produção
	@kubectl get pods -n $(NS_PROD) -l app.kubernetes.io/instance=$(RELEASE_NAME)-prod

.PHONY: logs-dev
logs-dev: ## Ver logs em dev
	@kubectl logs -n $(NS_DEV) -l app.kubernetes.io/instance=$(RELEASE_NAME)-dev -f --tail=100

.PHONY: logs-staging
logs-staging: ## Ver logs em staging
	@kubectl logs -n $(NS_STAGING) -l app.kubernetes.io/instance=$(RELEASE_NAME)-staging -f --tail=100

.PHONY: logs-prod
logs-prod: ## Ver logs em produção
	@kubectl logs -n $(NS_PROD) -l app.kubernetes.io/instance=$(RELEASE_NAME)-prod -f --tail=100

##@ Rollback

.PHONY: history-dev
history-dev: ## Ver histórico em dev
	@helm history $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: history-staging
history-staging: ## Ver histórico em staging
	@helm history $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: history-prod
history-prod: ## Ver histórico em produção
	@helm history $(RELEASE_NAME)-prod -n $(NS_PROD)

.PHONY: rollback-dev
rollback-dev: ## Rollback em dev
	@echo "$(YELLOW)Fazendo rollback em DEV...$(NC)"
	helm rollback $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: rollback-staging
rollback-staging: ## Rollback em staging
	@echo "$(YELLOW)Fazendo rollback em STAGING...$(NC)"
	helm rollback $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: rollback-prod
rollback-prod: ## Rollback em produção
	@echo "$(RED)⚠️  Fazendo rollback em PRODUÇÃO!$(NC)"
	@echo "$(RED)Pressione Ctrl+C para cancelar, ou aguarde 5s...$(NC)"
	@sleep 5
	helm rollback $(RELEASE_NAME)-prod -n $(NS_PROD)

##@ Limpeza

.PHONY: uninstall-dev
uninstall-dev: ## Desinstalar de dev
	@echo "$(YELLOW)Desinstalando de DEV...$(NC)"
	helm uninstall $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: uninstall-staging
uninstall-staging: ## Desinstalar de staging
	@echo "$(YELLOW)Desinstalando de STAGING...$(NC)"
	helm uninstall $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: uninstall-prod
uninstall-prod: ## Desinstalar de produção
	@echo "$(RED)⚠️  Desinstalando de PRODUÇÃO!$(NC)"
	@echo "$(RED)Pressione Ctrl+C para cancelar, ou aguarde 10s...$(NC)"
	@sleep 10
	helm uninstall $(RELEASE_NAME)-prod -n $(NS_PROD)

.PHONY: clean
clean: ## Limpar dependências do chart
	@echo "$(GREEN)Limpando dependências...$(NC)"
	rm -rf $(CHART_PATH)/charts/*
	rm -rf $(CHART_PATH)/Chart.lock
	rm -f $(CHART_NAME)-*.tgz

##@ Utilitários

.PHONY: list
list: ## Listar todas as releases
	@helm list -A

.PHONY: port-forward-dev
port-forward-dev: ## Port forward dev para localhost:8080
	@echo "$(GREEN)Port forward DEV → http://localhost:8080$(NC)"
	kubectl port-forward -n $(NS_DEV) svc/$(RELEASE_NAME)-dev-$(CHART_NAME) 8080:8080

.PHONY: port-forward-staging
port-forward-staging: ## Port forward staging para localhost:8080
	@echo "$(GREEN)Port forward STAGING → http://localhost:8080$(NC)"
	kubectl port-forward -n $(NS_STAGING) svc/$(RELEASE_NAME)-staging-$(CHART_NAME) 8080:8080

.PHONY: port-forward-prod
port-forward-prod: ## Port forward produção para localhost:8080
	@echo "$(GREEN)Port forward PRODUÇÃO → http://localhost:8080$(NC)"
	kubectl port-forward -n $(NS_PROD) svc/$(RELEASE_NAME)-prod-$(CHART_NAME) 8080:8080

.DEFAULT_GOAL := help
