# Makefile for Helm Chart Management
# Bridal Cover CRM

CHART_NAME := bridal-cover-crm
CHART_PATH := ./$(CHART_NAME)
RELEASE_NAME := bridal-crm

# Namespaces
NS_DEV := dev
NS_STAGING := staging
NS_PROD := production

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m # No Color

.PHONY: help
help: ## Show this help message
	@echo "$(GREEN)Bridal Cover CRM - Helm Chart Management$(NC)"
	@echo ""
	@echo "$(YELLOW)Available targets:$(NC)"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(GREEN)%-25s$(NC) %s\n", $$1, $$2}'

##@ Dependencies

.PHONY: deps-add
deps-add: ## Add Helm repositories
	@echo "$(GREEN)Adding Helm repositories...$(NC)"
	helm repo add bitnami https://charts.bitnami.com/bitnami
	helm repo update

.PHONY: deps-update
deps-update: ## Update chart dependencies
	@echo "$(GREEN)Updating chart dependencies...$(NC)"
	cd $(CHART_PATH) && helm dependency update

.PHONY: deps-build
deps-build: ## Build chart dependencies
	@echo "$(GREEN)Building chart dependencies...$(NC)"
	cd $(CHART_PATH) && helm dependency build

##@ Validation

.PHONY: lint
lint: ## Lint the chart
	@echo "$(GREEN)Linting chart...$(NC)"
	helm lint $(CHART_PATH)

.PHONY: template
template: ## Render templates locally
	@echo "$(GREEN)Rendering templates...$(NC)"
	helm template $(RELEASE_NAME) $(CHART_PATH)

.PHONY: template-dev
template-dev: ## Render templates for dev environment
	@echo "$(GREEN)Rendering templates for DEV...$(NC)"
	helm template $(RELEASE_NAME)-dev $(CHART_PATH) -f $(CHART_PATH)/values-dev.yaml

.PHONY: template-staging
template-staging: ## Render templates for staging environment
	@echo "$(GREEN)Rendering templates for STAGING...$(NC)"
	helm template $(RELEASE_NAME)-staging $(CHART_PATH) -f $(CHART_PATH)/values-staging.yaml

.PHONY: template-prod
template-prod: ## Render templates for production environment
	@echo "$(GREEN)Rendering templates for PRODUCTION...$(NC)"
	helm template $(RELEASE_NAME)-prod $(CHART_PATH) -f $(CHART_PATH)/values-prod.yaml

.PHONY: dry-run-dev
dry-run-dev: ## Dry run for dev environment
	@echo "$(GREEN)Dry run for DEV...$(NC)"
	helm install $(RELEASE_NAME)-dev $(CHART_PATH) \
		-f $(CHART_PATH)/values-dev.yaml \
		-n $(NS_DEV) \
		--dry-run --debug

.PHONY: dry-run-staging
dry-run-staging: ## Dry run for staging environment
	@echo "$(GREEN)Dry run for STAGING...$(NC)"
	helm install $(RELEASE_NAME)-staging $(CHART_PATH) \
		-f $(CHART_PATH)/values-staging.yaml \
		-n $(NS_STAGING) \
		--dry-run --debug

.PHONY: dry-run-prod
dry-run-prod: ## Dry run for production environment
	@echo "$(GREEN)Dry run for PRODUCTION...$(NC)"
	helm install $(RELEASE_NAME)-prod $(CHART_PATH) \
		-f $(CHART_PATH)/values-prod.yaml \
		-n $(NS_PROD) \
		--dry-run --debug

##@ Installation

.PHONY: install-dev
install-dev: deps-update ## Install chart in dev environment
	@echo "$(GREEN)Installing in DEV environment...$(NC)"
	kubectl create namespace $(NS_DEV) --dry-run=client -o yaml | kubectl apply -f -
	helm install $(RELEASE_NAME)-dev $(CHART_PATH) \
		-f $(CHART_PATH)/values-dev.yaml \
		-n $(NS_DEV)

.PHONY: install-staging
install-staging: deps-update ## Install chart in staging environment
	@echo "$(GREEN)Installing in STAGING environment...$(NC)"
	kubectl create namespace $(NS_STAGING) --dry-run=client -o yaml | kubectl apply -f -
	helm install $(RELEASE_NAME)-staging $(CHART_PATH) \
		-f $(CHART_PATH)/values-staging.yaml \
		-n $(NS_STAGING)

.PHONY: install-prod
install-prod: deps-update ## Install chart in production environment
	@echo "$(YELLOW)⚠️  Installing in PRODUCTION environment...$(NC)"
	@echo "$(RED)Press Ctrl+C to cancel, or wait 5 seconds to continue...$(NC)"
	@sleep 5
	kubectl create namespace $(NS_PROD) --dry-run=client -o yaml | kubectl apply -f -
	helm install $(RELEASE_NAME)-prod $(CHART_PATH) \
		-f $(CHART_PATH)/values-prod.yaml \
		-n $(NS_PROD)

##@ Upgrade

.PHONY: upgrade-dev
upgrade-dev: deps-update ## Upgrade release in dev environment
	@echo "$(GREEN)Upgrading in DEV environment...$(NC)"
	helm upgrade $(RELEASE_NAME)-dev $(CHART_PATH) \
		-f $(CHART_PATH)/values-dev.yaml \
		-n $(NS_DEV)

.PHONY: upgrade-staging
upgrade-staging: deps-update ## Upgrade release in staging environment
	@echo "$(GREEN)Upgrading in STAGING environment...$(NC)"
	helm upgrade $(RELEASE_NAME)-staging $(CHART_PATH) \
		-f $(CHART_PATH)/values-staging.yaml \
		-n $(NS_STAGING)

.PHONY: upgrade-prod
upgrade-prod: deps-update ## Upgrade release in production environment
	@echo "$(YELLOW)⚠️  Upgrading in PRODUCTION environment...$(NC)"
	@echo "$(RED)Press Ctrl+C to cancel, or wait 5 seconds to continue...$(NC)"
	@sleep 5
	helm upgrade $(RELEASE_NAME)-prod $(CHART_PATH) \
		-f $(CHART_PATH)/values-prod.yaml \
		-n $(NS_PROD)

##@ Uninstall

.PHONY: uninstall-dev
uninstall-dev: ## Uninstall release from dev environment
	@echo "$(GREEN)Uninstalling from DEV environment...$(NC)"
	helm uninstall $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: uninstall-staging
uninstall-staging: ## Uninstall release from staging environment
	@echo "$(GREEN)Uninstalling from STAGING environment...$(NC)"
	helm uninstall $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: uninstall-prod
uninstall-prod: ## Uninstall release from production environment
	@echo "$(RED)⚠️  Uninstalling from PRODUCTION environment...$(NC)"
	@echo "$(RED)Press Ctrl+C to cancel, or wait 10 seconds to continue...$(NC)"
	@sleep 10
	helm uninstall $(RELEASE_NAME)-prod -n $(NS_PROD)

##@ Status & Info

.PHONY: status-dev
status-dev: ## Show status of dev release
	@echo "$(GREEN)Status of DEV release:$(NC)"
	helm status $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: status-staging
status-staging: ## Show status of staging release
	@echo "$(GREEN)Status of STAGING release:$(NC)"
	helm status $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: status-prod
status-prod: ## Show status of production release
	@echo "$(GREEN)Status of PRODUCTION release:$(NC)"
	helm status $(RELEASE_NAME)-prod -n $(NS_PROD)

.PHONY: list
list: ## List all releases
	@echo "$(GREEN)All Helm releases:$(NC)"
	helm list -A

.PHONY: values-dev
values-dev: ## Show values of dev release
	@echo "$(GREEN)Values of DEV release:$(NC)"
	helm get values $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: values-staging
values-staging: ## Show values of staging release
	@echo "$(GREEN)Values of STAGING release:$(NC)"
	helm get values $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: values-prod
values-prod: ## Show values of production release
	@echo "$(GREEN)Values of PRODUCTION release:$(NC)"
	helm get values $(RELEASE_NAME)-prod -n $(NS_PROD)

##@ History & Rollback

.PHONY: history-dev
history-dev: ## Show history of dev release
	@echo "$(GREEN)History of DEV release:$(NC)"
	helm history $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: history-staging
history-staging: ## Show history of staging release
	@echo "$(GREEN)History of STAGING release:$(NC)"
	helm history $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: history-prod
history-prod: ## Show history of production release
	@echo "$(GREEN)History of PRODUCTION release:$(NC)"
	helm history $(RELEASE_NAME)-prod -n $(NS_PROD)

.PHONY: rollback-dev
rollback-dev: ## Rollback dev release to previous revision
	@echo "$(GREEN)Rolling back DEV release...$(NC)"
	helm rollback $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: rollback-staging
rollback-staging: ## Rollback staging release to previous revision
	@echo "$(GREEN)Rolling back STAGING release...$(NC)"
	helm rollback $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: rollback-prod
rollback-prod: ## Rollback production release to previous revision
	@echo "$(RED)⚠️  Rolling back PRODUCTION release...$(NC)"
	@echo "$(RED)Press Ctrl+C to cancel, or wait 5 seconds to continue...$(NC)"
	@sleep 5
	helm rollback $(RELEASE_NAME)-prod -n $(NS_PROD)

##@ Packaging

.PHONY: package
package: deps-update ## Package the chart
	@echo "$(GREEN)Packaging chart...$(NC)"
	helm package $(CHART_PATH)

.PHONY: package-sign
package-sign: deps-update ## Package and sign the chart
	@echo "$(GREEN)Packaging and signing chart...$(NC)"
	helm package --sign --key mykey $(CHART_PATH)

##@ Testing

.PHONY: test-dev
test-dev: ## Run tests for dev release
	@echo "$(GREEN)Testing DEV release...$(NC)"
	helm test $(RELEASE_NAME)-dev -n $(NS_DEV)

.PHONY: test-staging
test-staging: ## Run tests for staging release
	@echo "$(GREEN)Testing STAGING release...$(NC)"
	helm test $(RELEASE_NAME)-staging -n $(NS_STAGING)

.PHONY: test-prod
test-prod: ## Run tests for production release
	@echo "$(GREEN)Testing PRODUCTION release...$(NC)"
	helm test $(RELEASE_NAME)-prod -n $(NS_PROD)

##@ Kubernetes Operations

.PHONY: pods-dev
pods-dev: ## Show pods in dev namespace
	@echo "$(GREEN)Pods in DEV:$(NC)"
	kubectl get pods -n $(NS_DEV) -l app.kubernetes.io/instance=$(RELEASE_NAME)-dev

.PHONY: pods-staging
pods-staging: ## Show pods in staging namespace
	@echo "$(GREEN)Pods in STAGING:$(NC)"
	kubectl get pods -n $(NS_STAGING) -l app.kubernetes.io/instance=$(RELEASE_NAME)-staging

.PHONY: pods-prod
pods-prod: ## Show pods in production namespace
	@echo "$(GREEN)Pods in PRODUCTION:$(NC)"
	kubectl get pods -n $(NS_PROD) -l app.kubernetes.io/instance=$(RELEASE_NAME)-prod

.PHONY: logs-dev
logs-dev: ## Show logs from dev pods
	@echo "$(GREEN)Logs from DEV:$(NC)"
	kubectl logs -n $(NS_DEV) -l app.kubernetes.io/instance=$(RELEASE_NAME)-dev -f

.PHONY: logs-staging
logs-staging: ## Show logs from staging pods
	@echo "$(GREEN)Logs from STAGING:$(NC)"
	kubectl logs -n $(NS_STAGING) -l app.kubernetes.io/instance=$(RELEASE_NAME)-staging -f

.PHONY: logs-prod
logs-prod: ## Show logs from production pods
	@echo "$(GREEN)Logs from PRODUCTION:$(NC)"
	kubectl logs -n $(NS_PROD) -l app.kubernetes.io/instance=$(RELEASE_NAME)-prod -f

.PHONY: describe-dev
describe-dev: ## Describe dev resources
	@echo "$(GREEN)Describing DEV resources:$(NC)"
	kubectl describe all -n $(NS_DEV) -l app.kubernetes.io/instance=$(RELEASE_NAME)-dev

.PHONY: describe-staging
describe-staging: ## Describe staging resources
	@echo "$(GREEN)Describing STAGING resources:$(NC)"
	kubectl describe all -n $(NS_STAGING) -l app.kubernetes.io/instance=$(RELEASE_NAME)-staging

.PHONY: describe-prod
describe-prod: ## Describe production resources
	@echo "$(GREEN)Describing PRODUCTION resources:$(NC)"
	kubectl describe all -n $(NS_PROD) -l app.kubernetes.io/instance=$(RELEASE_NAME)-prod

##@ Cleanup

.PHONY: clean
clean: ## Clean chart dependencies
	@echo "$(GREEN)Cleaning chart dependencies...$(NC)"
	rm -rf $(CHART_PATH)/charts/*
	rm -rf $(CHART_PATH)/Chart.lock
	rm -f $(CHART_NAME)-*.tgz

.PHONY: clean-all
clean-all: clean ## Clean everything including namespaces
	@echo "$(RED)⚠️  This will delete all namespaces and data!$(NC)"
	@echo "$(RED)Press Ctrl+C to cancel, or wait 10 seconds to continue...$(NC)"
	@sleep 10
	kubectl delete namespace $(NS_DEV) $(NS_STAGING) $(NS_PROD) --ignore-not-found

##@ Development

.PHONY: watch-dev
watch-dev: ## Watch resources in dev namespace
	@echo "$(GREEN)Watching DEV resources:$(NC)"
	watch -n 2 kubectl get all -n $(NS_DEV)

.PHONY: watch-staging
watch-staging: ## Watch resources in staging namespace
	@echo "$(GREEN)Watching STAGING resources:$(NC)"
	watch -n 2 kubectl get all -n $(NS_STAGING)

.PHONY: watch-prod
watch-prod: ## Watch resources in production namespace
	@echo "$(GREEN)Watching PRODUCTION resources:$(NC)"
	watch -n 2 kubectl get all -n $(NS_PROD)

.PHONY: port-forward-dev
port-forward-dev: ## Port forward to dev service
	@echo "$(GREEN)Port forwarding DEV service to localhost:8080...$(NC)"
	kubectl port-forward -n $(NS_DEV) svc/$(RELEASE_NAME)-dev-$(CHART_NAME) 8080:8080

.PHONY: port-forward-staging
port-forward-staging: ## Port forward to staging service
	@echo "$(GREEN)Port forwarding STAGING service to localhost:8080...$(NC)"
	kubectl port-forward -n $(NS_STAGING) svc/$(RELEASE_NAME)-staging-$(CHART_NAME) 8080:8080

.PHONY: port-forward-prod
port-forward-prod: ## Port forward to production service
	@echo "$(GREEN)Port forwarding PRODUCTION service to localhost:8080...$(NC)"
	kubectl port-forward -n $(NS_PROD) svc/$(RELEASE_NAME)-prod-$(CHART_NAME) 8080:8080

##@ All-in-one Commands

.PHONY: deploy-dev
deploy-dev: lint deps-update install-dev status-dev ## Lint, install and show status for dev
	@echo "$(GREEN)✅ DEV deployment complete!$(NC)"

.PHONY: deploy-staging
deploy-staging: lint deps-update install-staging status-staging ## Lint, install and show status for staging
	@echo "$(GREEN)✅ STAGING deployment complete!$(NC)"

.PHONY: deploy-prod
deploy-prod: lint deps-update install-prod status-prod ## Lint, install and show status for production
	@echo "$(GREEN)✅ PRODUCTION deployment complete!$(NC)"

.PHONY: redeploy-dev
redeploy-dev: uninstall-dev deploy-dev ## Completely redeploy dev environment
	@echo "$(GREEN)✅ DEV redeployment complete!$(NC)"

.PHONY: redeploy-staging
redeploy-staging: uninstall-staging deploy-staging ## Completely redeploy staging environment
	@echo "$(GREEN)✅ STAGING redeployment complete!$(NC)"

.DEFAULT_GOAL := help

