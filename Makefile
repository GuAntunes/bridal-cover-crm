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

# Build da imagem Docker
docker-build:
	@echo "Building Docker image..."
	docker build -t $(IMAGE_BASE):latest \
		-t $(IMAGE_BASE):$(VERSION) \
		-t $(IMAGE_BASE):$(BUILD_DATE) \
		-t $(IMAGE_BASE):$(GIT_HASH) \
		.
	@echo "✅ Image built successfully!"

# Push para Docker Hub
docker-push: docker-build
	@echo "Pushing to Docker Hub..."
	docker push $(IMAGE_BASE):latest
	docker push $(IMAGE_BASE):$(VERSION)
	docker push $(IMAGE_BASE):$(BUILD_DATE)
	docker push $(IMAGE_BASE):$(GIT_HASH)
	@echo "✅ Images pushed successfully!"
	@echo "Available at: https://hub.docker.com/r/$(DOCKER_USER)/$(APP_NAME)"

# Build + Push (comando único)
docker-release: docker-push
	@echo "🚀 Release $(VERSION) completed!"
	@echo "Latest commit: $(GIT_HASH)"
	@echo "Build date: $(BUILD_DATE)"

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