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
	kubectl apply --server-side --force-conflicts -n $(ARGOCD_NAMESPACE) -f $(ARGOCD_INSTALL_URL)
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

REPO_URL := git@github.com:GuAntunes/bridal-cover-crm.git
DEPLOY_KEY := $(HOME)/.ssh/argocd_bridal_cover_crm

.PHONY: generate-deploy-key
generate-deploy-key: ## Gerar par de chaves SSH para Deploy Key do GitHub
	@test -f $(DEPLOY_KEY) || ssh-keygen -t ed25519 -f $(DEPLOY_KEY) -N "" -C "argocd-deploy-key-bridal-cover-crm"
	@chmod 600 $(DEPLOY_KEY)
	@echo "$(GREEN)Chave gerada em $(DEPLOY_KEY)$(NC)"
	@make show-deploy-key

.PHONY: show-deploy-key
show-deploy-key: ## Exibir chave publica para adicionar no GitHub
	@echo "$(YELLOW)Adicione em: https://github.com/GuAntunes/bridal-cover-crm/settings/keys$(NC)"
	@cat $(DEPLOY_KEY).pub

.PHONY: setup-repo-secret
setup-repo-secret: generate-deploy-key ## Registrar repo SSH no ArgoCD (Secret no cluster)
	kubectl create secret generic bridal-cover-crm-repo -n $(ARGOCD_NAMESPACE) \
		--from-literal=url=$(REPO_URL) \
		--from-file=sshPrivateKey=$(DEPLOY_KEY) \
		--dry-run=client -o yaml | kubectl apply -f -
	kubectl label secret bridal-cover-crm-repo -n $(ARGOCD_NAMESPACE) \
		argocd.argoproj.io/secret-type=repository --overwrite
	kubectl rollout restart deployment argocd-repo-server -n $(ARGOCD_NAMESPACE)
	@echo "$(GREEN)Secret do repositorio aplicado$(NC)"
	@echo "$(YELLOW)Adicione a Deploy Key no GitHub: make show-deploy-key$(NC)"

.PHONY: verify-repo
verify-repo: ## Verificar conexao com o repositorio Git
	@kubectl port-forward svc/argocd-server -n $(ARGOCD_NAMESPACE) 8080:443 >/dev/null 2>&1 & \
		PF_PID=$$!; sleep 3; \
		ARGOCD_PASS=$$(kubectl -n $(ARGOCD_NAMESPACE) get secret argocd-initial-admin-secret -o jsonpath='{.data.password}' | base64 -d); \
		argocd login localhost:8080 --username admin --password "$$ARGOCD_PASS" --insecure >/dev/null 2>&1 && \
		argocd repo list || true; \
		kill $$PF_PID 2>/dev/null || true
