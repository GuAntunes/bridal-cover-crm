# Makefile - ArgoCD Bootstrap e Operações
# Bridal Cover CRM

ARGOCD_NAMESPACE := argocd
ARGOCD_INSTALL_URL := https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

GREEN := \033[0;32m
YELLOW := \033[1;33m
BLUE := \033[0;34m
NC := \033[0m

.DEFAULT_GOAL := help

.PHONY: help
help: ## Mostrar comandos disponíveis
	@echo "$(GREEN)ArgoCD - Bridal Cover CRM$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(BLUE)%-22s$(NC) %s\n", $$1, $$2}'

.PHONY: install
install: ## Instalar ArgoCD no cluster (manifest non-HA)
	@echo "$(GREEN)Instalando ArgoCD...$(NC)"
	kubectl create namespace $(ARGOCD_NAMESPACE) --dry-run=client -o yaml | kubectl apply -f -
	kubectl apply -n $(ARGOCD_NAMESPACE) -f $(ARGOCD_INSTALL_URL)
	@echo "$(YELLOW)Aguardando pods...$(NC)"
	kubectl wait --for=condition=Ready pods --all -n $(ARGOCD_NAMESPACE) --timeout=300s
	@echo "$(GREEN)ArgoCD instalado!$(NC)"
	@make status

.PHONY: status
status: ## Ver status dos pods do ArgoCD
	@kubectl get pods -n $(ARGOCD_NAMESPACE)

.PHONY: port-forward
port-forward: ## Abrir UI em https://localhost:8080
	@echo "$(GREEN)UI: https://localhost:8080$(NC)"
	@echo "$(YELLOW)Usuario: admin | Senha: make password$(NC)"
	kubectl port-forward svc/argocd-server -n $(ARGOCD_NAMESPACE) 8080:443

.PHONY: password
password: ## Exibir senha inicial do admin
	@kubectl -n $(ARGOCD_NAMESPACE) get secret argocd-initial-admin-secret \
		-o jsonpath="{.data.password}" 2>/dev/null | base64 -d; echo

.PHONY: bootstrap-project
bootstrap-project: ## Aplicar AppProject bridal-cover-crm
	kubectl apply -f projects/bridal-cover-crm.yaml
	@echo "$(GREEN)AppProject aplicado$(NC)"

.PHONY: bootstrap-root
bootstrap-root: ## Aplicar root-app (App of Apps) - executar uma vez
	kubectl apply -f bootstrap/root-app.yaml
	@echo "$(GREEN)root-app aplicado$(NC)"
	@echo "$(YELLOW)Sync manual: use a UI ou 'argocd app sync root-app'$(NC)"

.PHONY: validate
validate: ## Validar YAMLs sem aplicar no cluster
	kubectl apply --dry-run=client -f projects/
	kubectl apply --dry-run=client -f applications/
	kubectl apply --dry-run=client -f bootstrap/
	@echo "$(GREEN)YAMLs validos$(NC)"
