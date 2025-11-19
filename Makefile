.PHONY: help
help: ## Mostra esta ajuda
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo "  Bridal Cover CRM - Makefile Commands"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo ""
	@echo "📦 Build & Install:"
	@echo "  make install           - Build da aplicação (Gradle)"
	@echo "  make build             - Build com Gradle"
	@echo "  make clean             - Limpar artefatos"
	@echo ""
	@echo "🗄️  Database:"
	@echo "  make up                - Subir Docker Compose"
	@echo "  make down              - Parar Docker Compose"
	@echo ""
	@echo "▶️  Run & Test:"
	@echo "  make run               - Executar aplicação"
	@echo "  make test              - Executar todos os testes"
	@echo "  make arch-test         - Testes de arquitetura"
	@echo ""
	@echo "🐳 Docker - Build Geral:"
	@echo "  make docker-build      - Build local (arquitetura atual)"
	@echo "  make docker-push       - Build + Push"
	@echo "  make docker-release    - Build multi-plataforma (AMD64+ARM64) + Push"
	@echo "  make docker-clean      - Limpar imagens antigas"
	@echo "  make docker-images     - Ver imagens locais"
	@echo "  make docker-test       - Testar imagem localmente"
	@echo ""
	@echo "🔵 Docker - DEV Environment:"
	@echo "  make docker-build-dev     - Build local (dev-latest)"
	@echo "  make docker-push-dev      - Build + Push DEV"
	@echo "  make docker-release-dev   - Build multi-plataforma DEV + Push"
	@echo ""
	@echo "🟡 Docker - STAGING Environment:"
	@echo "  make docker-build-staging   - Build local (staging-latest)"
	@echo "  make docker-push-staging    - Build + Push STAGING"
	@echo "  make docker-release-staging - Build multi-plataforma STAGING + Push"
	@echo ""
	@echo "🟢 Docker - PROD Environment:"
	@echo "  make docker-build-prod    - Build local (latest, version)"
	@echo "  make docker-push-prod     - Build + Push PROD"
	@echo "  make docker-release-prod  - Build multi-plataforma PROD + Push"
	@echo ""
	@echo "🌈 Docker - Todos os Ambientes:"
	@echo "  make docker-build-all     - Build local de todos"
	@echo "  make docker-push-all      - Build + Push de todos"
	@echo "  make docker-release-all   - Build multi-plataforma de todos + Push"
	@echo ""
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Build & Install
install:
	./gradlew clean build

# Database
up:
	docker-compose up -d

down:
	docker-compose down

# Run application
run:
	./gradlew bootRun

# Tests
test:
	./gradlew test

arch-test:
	./gradlew test --tests "*ArchitectureTest"

# Build and verify
build:
	./gradlew build

# Clean build artifacts
clean:
	./gradlew clean


DOCKER_USER := gustavoantunes
APP_NAME := bridal-cover-crm
VERSION := $(shell grep '^version' build.gradle.kts | cut -d'"' -f2)
BUILD_DATE := $(shell date +%Y%m%d-%H%M%S)
GIT_HASH := $(shell git rev-parse --short HEAD 2>/dev/null || echo "unknown")
IMAGE_BASE := $(DOCKER_USER)/$(APP_NAME)

# ==================== Docker Build Commands ====================

# Build da imagem Docker (local - arquitetura atual)
docker-build:
	@echo "Building Docker image for current platform..."
	docker build -t $(IMAGE_BASE):latest \
		-t $(IMAGE_BASE):$(VERSION) \
		-t $(IMAGE_BASE):$(BUILD_DATE) \
		-t $(IMAGE_BASE):$(GIT_HASH) \
		.
	@echo "✅ Image built successfully!"

# Push para Docker Hub (apenas se já construiu localmente)
docker-push: docker-build
	@echo "Pushing to Docker Hub..."
	docker push $(IMAGE_BASE):latest
	docker push $(IMAGE_BASE):$(VERSION)
	docker push $(IMAGE_BASE):$(BUILD_DATE)
	docker push $(IMAGE_BASE):$(GIT_HASH)
	@echo "✅ Images pushed successfully!"

# Build + Push multi-plataforma (AMD64 + ARM64) - Resolve erro "no match for platform"
docker-release:
	@echo "Building multi-platform Docker image (AMD64 + ARM64)..."
	@docker buildx create --use --name multiarch-builder 2>/dev/null || docker buildx use multiarch-builder
	docker buildx build \
		--platform linux/amd64,linux/arm64 \
		-t $(IMAGE_BASE):latest \
		-t $(IMAGE_BASE):$(VERSION) \
		-t $(IMAGE_BASE):$(BUILD_DATE) \
		-t $(IMAGE_BASE):$(GIT_HASH) \
		--push \
		.
	@echo "✅ Release $(VERSION) completed!"
	@echo "Platforms: linux/amd64, linux/arm64"

# Limpar imagens locais antigas
docker-clean:
	@echo "Cleaning old Docker images..."
	docker images | grep $(APP_NAME) | grep -v latest | awk '{print $$3}' | xargs docker rmi -f || true
	@echo "✅ Cleanup completed!"

# Ver imagens locais
docker-images:
	@echo "Local images:"
	@docker images | grep $(APP_NAME) || echo "No images found"

# Testar imagem localmente
docker-test:
	@echo "Testing Docker image locally..."
	docker run --rm -p 8082:8082 \
		-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/bridal_cover_crm_dev \
		-e SPRING_DATASOURCE_USERNAME=postgres \
		-e SPRING_DATASOURCE_PASSWORD=postgres \
		$(IMAGE_BASE):latest

# ==================== Build por Ambiente ====================

# DEV - Build local
docker-build-dev:
	@echo "🔵 Building Docker image for DEV environment..."
	docker build -t $(IMAGE_BASE):dev-latest \
		-t $(IMAGE_BASE):dev-$(BUILD_DATE) \
		-t $(IMAGE_BASE):dev-$(GIT_HASH) \
		.
	@echo "✅ DEV image built successfully!"
	@echo "   Tag: dev-latest"

# DEV - Build e Push
docker-push-dev: docker-build-dev
	@echo "🔵 Pushing DEV image to Docker Hub..."
	docker push $(IMAGE_BASE):dev-latest
	docker push $(IMAGE_BASE):dev-$(BUILD_DATE)
	docker push $(IMAGE_BASE):dev-$(GIT_HASH)
	@echo "✅ DEV images pushed successfully!"

# DEV - Build multi-plataforma e Push
docker-release-dev:
	@echo "🔵 Building multi-platform Docker image for DEV (AMD64 + ARM64)..."
	@docker buildx create --use --name multiarch-builder 2>/dev/null || docker buildx use multiarch-builder
	docker buildx build \
		--platform linux/amd64,linux/arm64 \
		-t $(IMAGE_BASE):dev-latest \
		-t $(IMAGE_BASE):dev-$(BUILD_DATE) \
		-t $(IMAGE_BASE):dev-$(GIT_HASH) \
		--push \
		.
	@echo "✅ DEV release completed!"
	@echo "   Tag: dev-latest"
	@echo "   Platforms: linux/amd64, linux/arm64"

# STAGING - Build local
docker-build-staging:
	@echo "🟡 Building Docker image for STAGING environment..."
	docker build -t $(IMAGE_BASE):staging-latest \
		-t $(IMAGE_BASE):staging-$(BUILD_DATE) \
		-t $(IMAGE_BASE):staging-$(GIT_HASH) \
		.
	@echo "✅ STAGING image built successfully!"
	@echo "   Tag: staging-latest"

# STAGING - Build e Push
docker-push-staging: docker-build-staging
	@echo "🟡 Pushing STAGING image to Docker Hub..."
	docker push $(IMAGE_BASE):staging-latest
	docker push $(IMAGE_BASE):staging-$(BUILD_DATE)
	docker push $(IMAGE_BASE):staging-$(GIT_HASH)
	@echo "✅ STAGING images pushed successfully!"

# STAGING - Build multi-plataforma e Push
docker-release-staging:
	@echo "🟡 Building multi-platform Docker image for STAGING (AMD64 + ARM64)..."
	@docker buildx create --use --name multiarch-builder 2>/dev/null || docker buildx use multiarch-builder
	docker buildx build \
		--platform linux/amd64,linux/arm64 \
		-t $(IMAGE_BASE):staging-latest \
		-t $(IMAGE_BASE):staging-$(BUILD_DATE) \
		-t $(IMAGE_BASE):staging-$(GIT_HASH) \
		--push \
		.
	@echo "✅ STAGING release completed!"
	@echo "   Tag: staging-latest"
	@echo "   Platforms: linux/amd64, linux/arm64"

# PROD - Build local
docker-build-prod:
	@echo "🟢 Building Docker image for PROD environment..."
	docker build -t $(IMAGE_BASE):latest \
		-t $(IMAGE_BASE):$(VERSION) \
		-t $(IMAGE_BASE):prod-$(BUILD_DATE) \
		-t $(IMAGE_BASE):prod-$(GIT_HASH) \
		.
	@echo "✅ PROD image built successfully!"
	@echo "   Tags: latest, $(VERSION)"

# PROD - Build e Push
docker-push-prod: docker-build-prod
	@echo "🟢 Pushing PROD image to Docker Hub..."
	docker push $(IMAGE_BASE):latest
	docker push $(IMAGE_BASE):$(VERSION)
	docker push $(IMAGE_BASE):prod-$(BUILD_DATE)
	docker push $(IMAGE_BASE):prod-$(GIT_HASH)
	@echo "✅ PROD images pushed successfully!"

# PROD - Build multi-plataforma e Push
docker-release-prod:
	@echo "🟢 Building multi-platform Docker image for PROD (AMD64 + ARM64)..."
	@docker buildx create --use --name multiarch-builder 2>/dev/null || docker buildx use multiarch-builder
	docker buildx build \
		--platform linux/amd64,linux/arm64 \
		-t $(IMAGE_BASE):latest \
		-t $(IMAGE_BASE):$(VERSION) \
		-t $(IMAGE_BASE):prod-$(BUILD_DATE) \
		-t $(IMAGE_BASE):prod-$(GIT_HASH) \
		--push \
		.
	@echo "✅ PROD release completed!"
	@echo "   Tags: latest, $(VERSION)"
	@echo "   Platforms: linux/amd64, linux/arm64"

# Build todos os ambientes (local)
docker-build-all: docker-build-dev docker-build-staging docker-build-prod
	@echo "✅ All environment images built!"

# Push todos os ambientes
docker-push-all: docker-push-dev docker-push-staging docker-push-prod
	@echo "✅ All environment images pushed!"

# Release todos os ambientes (multi-plataforma)
docker-release-all: docker-release-dev docker-release-staging docker-release-prod
	@echo "✅ All environment releases completed!"