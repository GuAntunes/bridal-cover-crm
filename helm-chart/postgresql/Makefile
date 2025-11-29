# PostgreSQL Helm Chart Makefile
# ⚠️ RECOMENDAÇÃO: Use o Makefile na raiz (../Makefile) para comandos simplificados

CHART_NAME := postgresql
RELEASE_NAME_DEV := postgres
RELEASE_NAME_STAGING := postgres
RELEASE_NAME_PROD := postgres
# Namespaces alinhados com o backend
NAMESPACE_DEV := dev
NAMESPACE_STAGING := staging
NAMESPACE_PROD := production

.PHONY: help
help: ## Exibe esta mensagem de ajuda
	@echo "\033[1;33m⚠️  DICA: Use 'make help' no diretório ../helm-chart/ para comandos simplificados\033[0m"
	@echo ""
	@echo "Comandos disponíveis para o PostgreSQL (uso avançado):"
	@echo ""
	@echo "\033[1;33m🆘 TROUBLESHOOTING:\033[0m"
	@echo "  \033[36mdiagnose\033[0m                   - Diagnostica problemas com PVC"
	@echo "  \033[36mfix-pvc-issues\033[0m             - Corrige problemas de PVC automaticamente"
	@echo "  \033[36minstall-local-path-provisioner\033[0m - Instala storage provisioner"
	@echo "  \033[36minstall-dev-no-pvc\033[0m         - Instala SEM persistência (teste rápido)"
	@echo ""
	@echo "\033[1;32m📦 INSTALAÇÃO:\033[0m"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep -E 'install-dev|install-staging|install-prod' | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "\033[1;34m🔄 GERENCIAMENTO:\033[0m"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep -E 'upgrade|uninstall|reinstall|status|get-pods' | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "\033[1;35m🔍 MONITORAMENTO:\033[0m"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep -E 'logs|shell|psql|port-forward' | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "\033[1;36m💾 BACKUP:\033[0m"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep -E 'backup|list-backups' | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "\033[1;31m🗑️  LIMPEZA:\033[0m"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | grep -E 'delete-pvc|clean-all' | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-30s\033[0m %s\n", $$1, $$2}'

# ==================== DESENVOLVIMENTO ====================

.PHONY: install-dev
install-dev: ## Instala o PostgreSQL no ambiente de desenvolvimento
	@echo "📦 Instalando PostgreSQL (DEV)..."
	helm install $(RELEASE_NAME_DEV) . \
		--namespace $(NAMESPACE_DEV) \
		--create-namespace \
		--values values-dev.yaml

.PHONY: install-dev-no-pvc
install-dev-no-pvc: ## Instala PostgreSQL SEM persistência (resolve problemas de PVC)
	@echo "📦 Instalando PostgreSQL (DEV) SEM PERSISTÊNCIA..."
	@echo "⚠️  ATENÇÃO: Dados serão perdidos ao reiniciar o pod!"
	helm install $(RELEASE_NAME_DEV) . \
		--namespace $(NAMESPACE_DEV) \
		--create-namespace \
		--values values-dev-no-pvc.yaml

.PHONY: install-dev-local-path
install-dev-local-path: ## Instala PostgreSQL usando local-path storageClass
	@echo "📦 Instalando PostgreSQL (DEV) com local-path..."
	helm install $(RELEASE_NAME_DEV) . \
		--namespace $(NAMESPACE_DEV) \
		--create-namespace \
		--values values-dev.yaml \
		--set persistence.storageClass=local-path

.PHONY: upgrade-dev
upgrade-dev: ## Atualiza o PostgreSQL no ambiente de desenvolvimento
	@echo "🔄 Atualizando PostgreSQL (DEV)..."
	helm upgrade $(RELEASE_NAME_DEV) . \
		--namespace $(NAMESPACE_DEV) \
		--values values-dev.yaml

.PHONY: uninstall-dev
uninstall-dev: ## Desinstala o PostgreSQL do ambiente de desenvolvimento
	@echo "🗑️  Desinstalando PostgreSQL (DEV)..."
	helm uninstall $(RELEASE_NAME_DEV) --namespace $(NAMESPACE_DEV)

.PHONY: reinstall-dev
reinstall-dev: uninstall-dev install-dev ## Reinstala o PostgreSQL no ambiente de desenvolvimento

# ==================== STAGING ====================

.PHONY: install-staging
install-staging: ## Instala o PostgreSQL no ambiente de staging
	@echo "📦 Instalando PostgreSQL (STAGING)..."
	helm install $(RELEASE_NAME_STAGING) . \
		--namespace $(NAMESPACE_STAGING) \
		--create-namespace \
		--values values-staging.yaml

.PHONY: upgrade-staging
upgrade-staging: ## Atualiza o PostgreSQL no ambiente de staging
	@echo "🔄 Atualizando PostgreSQL (STAGING)..."
	helm upgrade $(RELEASE_NAME_STAGING) . \
		--namespace $(NAMESPACE_STAGING) \
		--values values-staging.yaml

.PHONY: uninstall-staging
uninstall-staging: ## Desinstala o PostgreSQL do ambiente de staging
	@echo "🗑️  Desinstalando PostgreSQL (STAGING)..."
	helm uninstall $(RELEASE_NAME_STAGING) --namespace $(NAMESPACE_STAGING)

# ==================== PRODUÇÃO ====================

.PHONY: install-prod
install-prod: ## Instala o PostgreSQL no ambiente de produção
	@echo "⚠️  ATENÇÃO: Você está instalando em PRODUÇÃO!"
	@echo "📦 Instalando PostgreSQL (PROD)..."
	helm install $(RELEASE_NAME_PROD) . \
		--namespace $(NAMESPACE_PROD) \
		--create-namespace \
		--values values-prod.yaml

.PHONY: upgrade-prod
upgrade-prod: ## Atualiza o PostgreSQL no ambiente de produção
	@echo "⚠️  ATENÇÃO: Você está atualizando PRODUÇÃO!"
	@echo "🔄 Atualizando PostgreSQL (PROD)..."
	helm upgrade $(RELEASE_NAME_PROD) . \
		--namespace $(NAMESPACE_PROD) \
		--values values-prod.yaml

.PHONY: uninstall-prod
uninstall-prod: ## Desinstala o PostgreSQL do ambiente de produção
	@echo "⚠️  ATENÇÃO: Você está desinstalando de PRODUÇÃO!"
	@read -p "Tem certeza? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		helm uninstall $(RELEASE_NAME_PROD) --namespace $(NAMESPACE_PROD); \
	fi

# ==================== UTILITIES ====================

.PHONY: diagnose
diagnose: ## Diagnostica problemas com PVC e storage
	@echo "🔍 Diagnóstico do PostgreSQL..."
	@echo ""
	@echo "=== StorageClasses disponíveis ==="
	@kubectl get storageclass || echo "Nenhuma storageclass encontrada"
	@echo ""
	@echo "=== PersistentVolumes disponíveis ==="
	@kubectl get pv || echo "Nenhum PV encontrado"
	@echo ""
	@echo "=== PersistentVolumeClaims no namespace ==="
	@kubectl get pvc -n $(NAMESPACE_DEV) || echo "Nenhum PVC encontrado"
	@echo ""
	@echo "=== Pods no namespace ==="
	@kubectl get pods -n $(NAMESPACE_DEV) || echo "Nenhum pod encontrado"
	@echo ""
	@echo "=== Eventos recentes ==="
	@kubectl get events -n $(NAMESPACE_DEV) --sort-by='.lastTimestamp' | tail -20 || echo "Sem eventos"

.PHONY: install-local-path-provisioner
install-local-path-provisioner: ## Instala local-path-provisioner (resolve problemas de PVC)
	@echo "📦 Instalando local-path-provisioner..."
	kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml
	@echo "⏳ Aguardando provisioner ficar pronto..."
	@sleep 5
	kubectl patch storageclass local-path -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}' || true
	@echo "✅ Local-path-provisioner instalado!"
	@echo ""
	@kubectl get storageclass

.PHONY: fix-pvc-issues
fix-pvc-issues: ## Corrige problemas de PVC (instala provisioner e reinstala)
	@echo "🔧 Corrigindo problemas de PVC..."
	@make install-local-path-provisioner
	@echo ""
	@echo "🗑️  Removendo instalação atual..."
	@make uninstall-dev || true
	@kubectl delete pvc -n $(NAMESPACE_DEV) --all || true
	@echo ""
	@echo "📦 Reinstalando PostgreSQL..."
	@make install-dev-local-path
	@echo "✅ Pronto!"

.PHONY: lint
lint: ## Valida os templates do Helm chart
	@echo "🔍 Validando templates..."
	helm lint .

.PHONY: template-dev
template-dev: ## Mostra os manifestos renderizados para DEV
	@helm template $(RELEASE_NAME_DEV) . \
		--namespace $(NAMESPACE_DEV) \
		--values values-dev.yaml

.PHONY: template-staging
template-staging: ## Mostra os manifestos renderizados para STAGING
	@helm template $(RELEASE_NAME_STAGING) . \
		--namespace $(NAMESPACE_STAGING) \
		--values values-staging.yaml

.PHONY: template-prod
template-prod: ## Mostra os manifestos renderizados para PROD
	@helm template $(RELEASE_NAME_PROD) . \
		--namespace $(NAMESPACE_PROD) \
		--values values-prod.yaml

.PHONY: dry-run-dev
dry-run-dev: ## Simula a instalação em DEV sem aplicar
	@echo "🎯 Dry run (DEV)..."
	helm install $(RELEASE_NAME_DEV) . \
		--namespace $(NAMESPACE_DEV) \
		--values values-dev.yaml \
		--dry-run --debug

.PHONY: status-dev
status-dev: ## Mostra o status do release em DEV
	@helm status $(RELEASE_NAME_DEV) --namespace $(NAMESPACE_DEV)

.PHONY: status-staging
status-staging: ## Mostra o status do release em STAGING
	@helm status $(RELEASE_NAME_STAGING) --namespace $(NAMESPACE_STAGING)

.PHONY: status-prod
status-prod: ## Mostra o status do release em PROD
	@helm status $(RELEASE_NAME_PROD) --namespace $(NAMESPACE_PROD)

# ==================== KUBERNETES ====================

.PHONY: get-pods-dev
get-pods-dev: ## Lista os pods do PostgreSQL em DEV
	@kubectl get pods -n $(NAMESPACE_DEV) -l app.kubernetes.io/name=bridal-cover-crm-postgresql

.PHONY: get-pods-staging
get-pods-staging: ## Lista os pods do PostgreSQL em STAGING
	@kubectl get pods -n $(NAMESPACE_STAGING) -l app.kubernetes.io/name=bridal-cover-crm-postgresql

.PHONY: get-pods-prod
get-pods-prod: ## Lista os pods do PostgreSQL em PROD
	@kubectl get pods -n $(NAMESPACE_PROD) -l app.kubernetes.io/name=bridal-cover-crm-postgresql

.PHONY: logs-dev
logs-dev: ## Mostra os logs do PostgreSQL em DEV
	@kubectl logs -n $(NAMESPACE_DEV) -l app.kubernetes.io/name=bridal-cover-crm-postgresql -f

.PHONY: logs-staging
logs-staging: ## Mostra os logs do PostgreSQL em STAGING
	@kubectl logs -n $(NAMESPACE_STAGING) -l app.kubernetes.io/name=bridal-cover-crm-postgresql -f

.PHONY: logs-prod
logs-prod: ## Mostra os logs do PostgreSQL em PROD
	@kubectl logs -n $(NAMESPACE_PROD) -l app.kubernetes.io/name=bridal-cover-crm-postgresql -f

.PHONY: shell-dev
shell-dev: ## Abre um shell no pod do PostgreSQL em DEV
	@kubectl exec -it -n $(NAMESPACE_DEV) $$(kubectl get pods -n $(NAMESPACE_DEV) -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}') -- /bin/sh

.PHONY: psql-dev
psql-dev: ## Conecta ao PostgreSQL em DEV usando psql
	@kubectl exec -it -n $(NAMESPACE_DEV) $$(kubectl get pods -n $(NAMESPACE_DEV) -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}') -- psql -U postgres -d bridal_cover_crm_dev

.PHONY: port-forward-dev
port-forward-dev: ## Cria port-forward para o PostgreSQL em DEV (porta 5432)
	@echo "🔌 Port-forward criado: localhost:5432 -> PostgreSQL"
	@kubectl port-forward -n $(NAMESPACE_DEV) svc/$(RELEASE_NAME_DEV)-bridal-cover-crm-postgresql 5432:5432

# ==================== BACKUP & RESTORE ====================

.PHONY: backup-dev
backup-dev: ## Cria backup do banco de dados DEV
	@echo "💾 Criando backup..."
	@mkdir -p backups
	@kubectl exec -n $(NAMESPACE_DEV) $$(kubectl get pods -n $(NAMESPACE_DEV) -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}') -- \
		pg_dump -U postgres bridal_cover_crm_dev > backups/backup-dev-$$(date +%Y%m%d-%H%M%S).sql
	@echo "✅ Backup criado em backups/"

.PHONY: list-backups
list-backups: ## Lista todos os backups disponíveis
	@ls -lh backups/ 2>/dev/null || echo "Nenhum backup encontrado"

# ==================== CLEANUP ====================

.PHONY: delete-pvc-dev
delete-pvc-dev: ## CUIDADO: Deleta o PVC (dados) do PostgreSQL em DEV
	@echo "⚠️  ATENÇÃO: Isso irá deletar TODOS OS DADOS do PostgreSQL!"
	@read -p "Tem certeza? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		kubectl delete pvc -n $(NAMESPACE_DEV) -l app.kubernetes.io/name=bridal-cover-crm-postgresql; \
	fi

.PHONY: clean-all-dev
clean-all-dev: uninstall-dev delete-pvc-dev ## Remove tudo do PostgreSQL em DEV (release + PVC)

# ==================== PACKAGE ====================

.PHONY: package
package: ## Cria um pacote do Helm chart
	@echo "📦 Criando pacote..."
	@helm package .
	@echo "✅ Pacote criado"

