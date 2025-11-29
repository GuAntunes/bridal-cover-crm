# Makefile Simplificado - Bridal Cover CRM
# Gerencia PostgreSQL + Backend de forma integrada

# Configurações
ENV ?= dev
NAMESPACE_dev := dev
NAMESPACE_staging := staging
NAMESPACE_prod := production
NAMESPACE := $(NAMESPACE_$(ENV))

# Releases
POSTGRES_RELEASE := postgres
BACKEND_RELEASE := backend

# Cores
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
BLUE := \033[0;34m
NC := \033[0m

.DEFAULT_GOAL := help

##@ Ajuda

.PHONY: help
help: ## Mostrar comandos disponíveis
	@echo "$(GREEN)╔══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)║         Bridal Cover CRM - Comandos Helm                ║$(NC)"
	@echo "$(GREEN)╚══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(BLUE)Ambiente atual: $(ENV) (namespace: $(NAMESPACE))$(NC)"
	@echo "$(YELLOW)Mude com: make ENV=staging <comando>$(NC)"
	@echo ""
	@echo "$(GREEN)━━━ Instalação Completa ━━━$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep -E 'install$$|uninstall$$|reinstall$$' | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(BLUE)%-25s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(GREEN)━━━ PostgreSQL ━━━$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep 'postgres-' | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(BLUE)%-25s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(GREEN)━━━ Backend ━━━$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep 'backend-' | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(BLUE)%-25s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(GREEN)━━━ Status e Monitoramento ━━━$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep -E 'status|diagnose|test-' | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(BLUE)%-25s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(GREEN)━━━ Utilitários ━━━$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep -E 'restart|fix-|clean' | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(BLUE)%-25s$(NC) %s\n", $$1, $$2}'
	@echo ""

##@ Instalação Completa

.PHONY: install
install: postgres-install backend-install ## Instalar PostgreSQL + Backend
	@echo "$(GREEN)✅ Instalação completa!$(NC)"
	@echo ""
	@make status

.PHONY: uninstall
uninstall: backend-uninstall postgres-uninstall ## Desinstalar Backend + PostgreSQL
	@echo "$(YELLOW)✅ Desinstalação completa!$(NC)"

.PHONY: reinstall
reinstall: uninstall install ## Reinstalar tudo

##@ PostgreSQL

.PHONY: postgres-install
postgres-install: ## Instalar PostgreSQL
	@echo "$(GREEN)📦 Instalando PostgreSQL ($(ENV))...$(NC)"
	@kubectl create namespace $(NAMESPACE) --dry-run=client -o yaml | kubectl apply -f - > /dev/null 2>&1
	@cd postgresql && helm install $(POSTGRES_RELEASE) . \
		--namespace $(NAMESPACE) \
		--values values-$(ENV).yaml \
		--wait --timeout 3m
	@echo "$(GREEN)✅ PostgreSQL instalado!$(NC)"
	@echo "$(YELLOW)⏳ Aguardando pod ficar pronto...$(NC)"
	@kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=bridal-cover-crm-postgresql -n $(NAMESPACE) --timeout=120s
	@echo "$(GREEN)✅ PostgreSQL está pronto!$(NC)"

.PHONY: postgres-uninstall
postgres-uninstall: ## Desinstalar PostgreSQL (mantém dados)
	@echo "$(YELLOW)🗑️  Desinstalando PostgreSQL ($(ENV))...$(NC)"
	@helm uninstall $(POSTGRES_RELEASE) -n $(NAMESPACE) 2>/dev/null || echo "PostgreSQL já foi removido"

.PHONY: postgres-reinstall
postgres-reinstall: postgres-uninstall postgres-install ## Reinstalar PostgreSQL

.PHONY: postgres-reinstall-no-pvc
postgres-reinstall-no-pvc: ## Reinstalar PostgreSQL SEM persistência
	@echo "$(YELLOW)🗑️  Removendo PostgreSQL e dados...$(NC)"
	@helm uninstall $(POSTGRES_RELEASE) -n $(NAMESPACE) 2>/dev/null || true
	@kubectl delete pvc -n $(NAMESPACE) -l app.kubernetes.io/name=bridal-cover-crm-postgresql 2>/dev/null || true
	@echo "$(GREEN)📦 Instalando PostgreSQL SEM persistência...$(NC)"
	@cd postgresql && helm install $(POSTGRES_RELEASE) . \
		--namespace $(NAMESPACE) \
		--values values-$(ENV)-no-pvc.yaml \
		--wait --timeout 3m

.PHONY: postgres-status
postgres-status: ## Ver status do PostgreSQL
	@echo "$(BLUE)PostgreSQL Status ($(ENV)):$(NC)"
	@helm status $(POSTGRES_RELEASE) -n $(NAMESPACE) 2>/dev/null || echo "$(RED)PostgreSQL não está instalado$(NC)"
	@echo ""
	@kubectl get pods -n $(NAMESPACE) -l app.kubernetes.io/name=bridal-cover-crm-postgresql 2>/dev/null || true

.PHONY: postgres-logs
postgres-logs: ## Ver logs do PostgreSQL
	@kubectl logs -n $(NAMESPACE) -l app.kubernetes.io/name=bridal-cover-crm-postgresql -f --tail=100

.PHONY: postgres-connect
postgres-connect: ## Conectar no PostgreSQL via psql
	@kubectl exec -it -n $(NAMESPACE) $$(kubectl get pods -n $(NAMESPACE) -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}') -- psql -U postgres -d bridal_cover_crm_$(ENV)

.PHONY: postgres-port-forward
postgres-port-forward: ## Port-forward PostgreSQL para localhost:5432
	@echo "$(GREEN)🔌 PostgreSQL disponível em localhost:5432$(NC)"
	@echo "$(YELLOW)Conecte com: psql -h localhost -p 5432 -U postgres -d bridal_cover_crm_$(ENV)$(NC)"
	@kubectl port-forward -n $(NAMESPACE) svc/$(POSTGRES_RELEASE)-bridal-cover-crm-postgresql 5432:5432

##@ Backend

.PHONY: backend-install
backend-install: ## Instalar Backend
	@echo "$(GREEN)📦 Instalando Backend ($(ENV))...$(NC)"
	@kubectl create namespace $(NAMESPACE) --dry-run=client -o yaml | kubectl apply -f - > /dev/null 2>&1
	@cd bridal-cover-crm && helm dependency update > /dev/null 2>&1
	@cd bridal-cover-crm && helm install $(BACKEND_RELEASE) . \
		--namespace $(NAMESPACE) \
		--values values-$(ENV).yaml \
		--wait --timeout 5m
	@echo "$(GREEN)✅ Backend instalado!$(NC)"

.PHONY: backend-upgrade
backend-upgrade: ## Atualizar Backend
	@echo "$(GREEN)🔄 Atualizando Backend ($(ENV))...$(NC)"
	@cd bridal-cover-crm && helm dependency update > /dev/null 2>&1
	@cd bridal-cover-crm && helm upgrade $(BACKEND_RELEASE) . \
		--namespace $(NAMESPACE) \
		--values values-$(ENV).yaml \
		--wait --timeout 5m
	@echo "$(GREEN)✅ Backend atualizado!$(NC)"

.PHONY: backend-uninstall
backend-uninstall: ## Desinstalar Backend
	@echo "$(YELLOW)🗑️  Desinstalando Backend ($(ENV))...$(NC)"
	@helm uninstall $(BACKEND_RELEASE) -n $(NAMESPACE) 2>/dev/null || echo "Backend já foi removido"

.PHONY: backend-reinstall
backend-reinstall: backend-uninstall backend-install ## Reinstalar Backend

.PHONY: backend-status
backend-status: ## Ver status do Backend
	@echo "$(BLUE)Backend Status ($(ENV)):$(NC)"
	@helm status $(BACKEND_RELEASE) -n $(NAMESPACE) 2>/dev/null || echo "$(RED)Backend não está instalado$(NC)"
	@echo ""
	@kubectl get pods -n $(NAMESPACE) -l app.kubernetes.io/name=bridal-cover-crm 2>/dev/null || true

.PHONY: backend-logs
backend-logs: ## Ver logs do Backend
	@kubectl logs -n $(NAMESPACE) -l app.kubernetes.io/name=bridal-cover-crm -f --tail=100

.PHONY: backend-port-forward
backend-port-forward: ## Port-forward Backend para localhost:8080
	@echo "$(GREEN)🔌 Backend disponível em http://localhost:8080$(NC)"
	@echo "$(YELLOW)Health: http://localhost:8080/actuator/health$(NC)"
	@kubectl port-forward -n $(NAMESPACE) svc/$(BACKEND_RELEASE)-bridal-cover-crm 8080:8080

.PHONY: backend-restart
backend-restart: ## Reiniciar Backend
	@echo "$(YELLOW)🔄 Reiniciando Backend...$(NC)"
	@kubectl rollout restart deployment/$(BACKEND_RELEASE)-bridal-cover-crm -n $(NAMESPACE)
	@kubectl rollout status deployment/$(BACKEND_RELEASE)-bridal-cover-crm -n $(NAMESPACE)
	@echo "$(GREEN)✅ Backend reiniciado!$(NC)"

##@ Status e Monitoramento

.PHONY: status
status: ## Ver status completo (PostgreSQL + Backend)
	@echo "$(GREEN)╔══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)║             Status Completo - $(ENV)                      $(NC)"
	@echo "$(GREEN)╚══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(BLUE)━━━ Helm Releases ━━━$(NC)"
	@helm list -n $(NAMESPACE) 2>/dev/null || echo "Nenhuma release encontrada"
	@echo ""
	@echo "$(BLUE)━━━ Pods ━━━$(NC)"
	@kubectl get pods -n $(NAMESPACE) 2>/dev/null || echo "Nenhum pod encontrado"
	@echo ""
	@echo "$(BLUE)━━━ Services ━━━$(NC)"
	@kubectl get svc -n $(NAMESPACE) 2>/dev/null || echo "Nenhum service encontrado"
	@echo ""
	@echo "$(BLUE)━━━ PVCs ━━━$(NC)"
	@kubectl get pvc -n $(NAMESPACE) 2>/dev/null || echo "Nenhum PVC encontrado"

.PHONY: diagnose
diagnose: ## Diagnóstico completo do ambiente
	@echo "$(YELLOW)🔍 Executando diagnóstico...$(NC)"
	@echo ""
	@echo "$(BLUE)━━━ Namespace ━━━$(NC)"
	@kubectl get namespace $(NAMESPACE) 2>/dev/null || echo "$(RED)Namespace $(NAMESPACE) não existe$(NC)"
	@echo ""
	@echo "$(BLUE)━━━ Helm Releases ━━━$(NC)"
	@helm list -n $(NAMESPACE) 2>/dev/null || echo "Nenhuma release"
	@echo ""
	@echo "$(BLUE)━━━ Pods ━━━$(NC)"
	@kubectl get pods -n $(NAMESPACE) -o wide 2>/dev/null || echo "Nenhum pod"
	@echo ""
	@echo "$(BLUE)━━━ PVCs ━━━$(NC)"
	@kubectl get pvc -n $(NAMESPACE) 2>/dev/null || echo "Nenhum PVC"
	@echo ""
	@echo "$(BLUE)━━━ StorageClasses ━━━$(NC)"
	@kubectl get storageclass 2>/dev/null || echo "Nenhuma storageclass"
	@echo ""
	@echo "$(BLUE)━━━ Eventos Recentes ━━━$(NC)"
	@kubectl get events -n $(NAMESPACE) --sort-by='.lastTimestamp' 2>/dev/null | tail -15 || echo "Sem eventos"

.PHONY: test-connection
test-connection: ## Testar conexão Backend -> PostgreSQL
	@echo "$(YELLOW)🔍 Testando conexão Backend -> PostgreSQL...$(NC)"
	@kubectl run test-connection --rm -it --restart=Never \
		--image=postgres:15-alpine \
		--namespace=$(NAMESPACE) \
		--env="PGPASSWORD=postgres" \
		-- psql -h $(POSTGRES_RELEASE)-bridal-cover-crm-postgresql -U postgres -d bridal_cover_crm_$(ENV) -c "SELECT version();"

##@ Utilitários

.PHONY: restart
restart: backend-restart ## Reiniciar aplicação (apenas backend)

.PHONY: fix-storage
fix-storage: ## Instalar storage provisioner (corrige PVC Pending)
	@echo "$(GREEN)📦 Instalando local-path-provisioner...$(NC)"
	@kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml
	@sleep 5
	@kubectl patch storageclass local-path -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}' 2>/dev/null || true
	@echo "$(GREEN)✅ Storage provisioner instalado!$(NC)"
	@kubectl get storageclass

.PHONY: clean-all
clean-all: ## ⚠️  CUIDADO: Remove tudo incluindo DADOS
	@echo "$(RED)⚠️  ATENÇÃO: Isso irá remover TUDO incluindo dados do banco!$(NC)"
	@echo "$(RED)Pressione Ctrl+C nos próximos 5 segundos para cancelar...$(NC)"
	@sleep 5
	@helm uninstall $(BACKEND_RELEASE) -n $(NAMESPACE) 2>/dev/null || true
	@helm uninstall $(POSTGRES_RELEASE) -n $(NAMESPACE) 2>/dev/null || true
	@kubectl delete pvc -n $(NAMESPACE) --all 2>/dev/null || true
	@kubectl delete namespace $(NAMESPACE) 2>/dev/null || true
	@echo "$(GREEN)✅ Tudo removido!$(NC)"

.PHONY: pods
pods: ## Listar todos os pods
	@kubectl get pods -n $(NAMESPACE)

.PHONY: events
events: ## Ver eventos recentes
	@kubectl get events -n $(NAMESPACE) --sort-by='.lastTimestamp'

##@ Informações

.PHONY: info
info: ## Mostrar informações de conexão
	@echo "$(GREEN)╔══════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)║         Informações de Conexão - $(ENV)                  $(NC)"
	@echo "$(GREEN)╚══════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(BLUE)PostgreSQL:$(NC)"
	@echo "  Host (interno): $(POSTGRES_RELEASE)-bridal-cover-crm-postgresql.$(NAMESPACE).svc.cluster.local"
	@echo "  Host (curto):   $(POSTGRES_RELEASE)-bridal-cover-crm-postgresql"
	@echo "  Port:           5432"
	@echo "  Database:       bridal_cover_crm_$(ENV)"
	@echo "  Username:       postgres"
	@echo "  Password:       postgres"
	@echo ""
	@echo "$(BLUE)Backend:$(NC)"
	@echo "  Service:        $(BACKEND_RELEASE)-bridal-cover-crm.$(NAMESPACE).svc.cluster.local"
	@echo "  Port:           8080"
	@echo ""
	@echo "$(YELLOW)Para acessar localmente:$(NC)"
	@echo "  PostgreSQL:     make postgres-port-forward"
	@echo "  Backend:        make backend-port-forward"
