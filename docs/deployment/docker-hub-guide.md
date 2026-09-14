# 🐳 Guia Completo: Docker Hub com BridalCover CRM

Este guia mostra como configurar e usar o Docker Hub para hospedar as imagens Docker da aplicação BridalCover CRM. Deploy em Kubernetes fica no repositório **platform-gitops**.

---

## 📋 Índice

1. [O que é Docker Hub?](#o-que-é-docker-hub)
2. [Configuração Inicial](#configuração-inicial)
3. [Build e Push de Imagens](#build-e-push-de-imagens)
4. [Integração com Desenvolvimento](#integração-com-desenvolvimento)
5. [Deploy no cluster (GitOps)](#deploy-no-cluster-gitops)
6. [Automatização com CI/CD](#automatização-com-cicd)
7. [Comandos Úteis](#comandos-úteis)
8. [Troubleshooting](#troubleshooting)
9. [Alternativas ao Docker Hub](#alternativas-ao-docker-hub)

---

## O que é Docker Hub?

**Docker Hub** é um registry de imagens Docker hospedado na nuvem, similar ao GitHub para código.

### Funcionalidades Principais:
- 🏷️ **Armazenamento de imagens**: hospeda suas imagens Docker
- 🔄 **Versionamento**: múltiplas tags/versões da mesma imagem
- 🌐 **Distribuição**: clusters Kubernetes podem baixar suas imagens de qualquer lugar
- 🆓 **Plano gratuito**: repositórios públicos ilimitados + 1 privado

### Workflow Completo:
```
[Seu Mac - Desenvolvimento]
    ↓ 1. Build da imagem
docker build -t bridal-cover-crm:v1 .
    ↓ 2. Push para Docker Hub
docker push seu-usuario/bridal-cover-crm:v1
    ↓ 3. Atualizar tag no platform-gitops + sync Argo CD
[Cluster Kubernetes]
    ↓ 4. Pull da imagem e rollout
```

---

## Configuração Inicial

### 1️⃣ Criar Conta no Docker Hub

1. Acesse: https://hub.docker.com/signup
2. Crie uma conta gratuita
3. Confirme seu email
4. Anote seu username (ex: `gustavoantunes`)

### 2️⃣ Fazer Login via Terminal

No seu Mac, execute:

```bash
# Login no Docker Hub
docker login

# Será solicitado:
# Username: seu-usuario
# Password: sua-senha

# Sucesso:
# Login Succeeded
```

**Dica:** Suas credenciais ficam salvas em `~/.docker/config.json`

### 3️⃣ Criar Repositório no Docker Hub (Opcional)

Você pode criar via web interface ou deixar criar automaticamente no primeiro push.

**Via Web:**
1. Acesse https://hub.docker.com/repositories
2. Clique em "Create Repository"
3. Nome: `bridal-cover-crm`
4. Visibilidade: 
   - **Public** (grátis, recomendado para começar)
   - **Private** (1 grátis, depois pago)
5. Clique em "Create"

**Via CLI** (cria automaticamente no primeiro push):
```bash
# Não precisa criar manualmente, só fazer push:
docker push seu-usuario/bridal-cover-crm:latest
```

---

## Build e Push de Imagens

### Estratégia de Versionamento

Recomendamos usar **múltiplas tags** para cada imagem:

```bash
# Variáveis
DOCKER_USER="seu-usuario"           # Seu username no Docker Hub
APP_NAME="bridal-cover-crm"
VERSION="1.0.0"                     # Semantic versioning
BUILD_DATE=$(date +%Y%m%d-%H%M%S)  # Timestamp
GIT_HASH=$(git rev-parse --short HEAD)  # Commit hash

# Exemplo de tags:
# - seu-usuario/bridal-cover-crm:latest
# - seu-usuario/bridal-cover-crm:1.0.0
# - seu-usuario/bridal-cover-crm:20241114-143022
# - seu-usuario/bridal-cover-crm:abc123
```

### Processo Manual (Passo a Passo)

#### 1. Build da Imagem

```bash
# Na raiz do projeto
cd /Users/gustavoantunes/Documents/GuAntunes/bridal-cover-crm

# Build usando o Dockerfile
docker build -t bridal-cover-crm:latest .

# Acompanhe o build (demora ~2-5min na primeira vez)
# Vai compilar o Kotlin, gerar o JAR, etc.
```

#### 2. Tag da Imagem

```bash
# Tag para o Docker Hub
docker tag bridal-cover-crm:latest seu-usuario/bridal-cover-crm:latest
docker tag bridal-cover-crm:latest seu-usuario/bridal-cover-crm:1.0.0

# Verificar tags criadas
docker images | grep bridal-cover-crm
```

#### 3. Push para o Docker Hub

```bash
# Push das imagens
docker push seu-usuario/bridal-cover-crm:latest
docker push seu-usuario/bridal-cover-crm:1.0.0

# Acompanhe o progresso:
# The push refers to repository [docker.io/seu-usuario/bridal-cover-crm]
# latest: digest: sha256:abc123... size: 2214
```

#### 4. Verificar no Docker Hub

Acesse: `https://hub.docker.com/r/seu-usuario/bridal-cover-crm/tags`

Você verá suas tags listadas! 🎉

---

## Integração com Desenvolvimento

### Adicionar Comandos ao Makefile

Edite o arquivo `Makefile` e adicione:

```makefile
# ==================== Docker Hub ====================
DOCKER_USER := seu-usuario
APP_NAME := bridal-cover-crm
VERSION := $(shell grep '^version' build.gradle.kts | cut -d'"' -f2)
BUILD_DATE := $(shell date +%Y%m%d-%H%M%S)
GIT_HASH := $(shell git rev-parse --short HEAD)
IMAGE_BASE := $(DOCKER_USER)/$(APP_NAME)

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

# Help
docker-help:
	@echo "Docker Hub Commands:"
	@echo "  make docker-build      - Build Docker image with multiple tags"
	@echo "  make docker-push       - Build and push to Docker Hub"
	@echo "  make docker-release    - Complete release (build + push + info)"
	@echo "  make docker-clean      - Remove old local images"
	@echo "  make docker-images     - List local Docker images"
	@echo "  make docker-test       - Test image locally"
```

**⚠️ Importante:** Altere `DOCKER_USER := seu-usuario` para seu username real do Docker Hub!

### Workflow de Desenvolvimento

Agora você pode usar comandos simplificados:

```bash
# 1. Desenvolver e testar localmente
make run
make test

# 2. Build e push para Docker Hub
make docker-release

# 3. Atualizar imagem no platform-gitops e sync Argo CD
```

---

## Deploy no cluster (GitOps)

Manifests Helm e Applications Argo CD **não** ficam neste repositório. Use **[platform-gitops](https://github.com/GuAntunes/platform-gitops)**:

1. Build e push da imagem (`make docker-release-dev` ou equivalente).
2. Atualize `image.tag` (ou delta em `*-values.yaml`) no platform-gitops.
3. Sync manual na UI do Argo CD (ou `argocd app sync`).

O cluster faz pull da imagem publicada no Docker Hub conforme o chart referencia `repository` e `tag`.

---

## Automatização com CI/CD

### Integrar com Jenkins

Edite o `Jenkinsfile` para adicionar stages de Docker:

```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_USER = 'seu-usuario'
        APP_NAME = 'bridal-cover-crm'
        DOCKER_IMAGE = "${DOCKER_USER}/${APP_NAME}"
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'  // Configurar no Jenkins
    }
    
    stages {
        stage('Build') {
            steps {
                echo 'Building application...'
                sh './gradlew clean build -x test'
            }
        }
        
        stage('Tests') {
            steps {
                echo 'Running tests...'
                sh './gradlew test'
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    def version = sh(
                        script: "grep '^version' build.gradle.kts | cut -d'\"' -f2",
                        returnStdout: true
                    ).trim()
                    
                    def buildDate = sh(
                        script: "date +%Y%m%d-%H%M%S",
                        returnStdout: true
                    ).trim()
                    
                    echo "Building Docker image: ${DOCKER_IMAGE}:${version}"
                    
                    sh """
                        docker build -t ${DOCKER_IMAGE}:latest \
                            -t ${DOCKER_IMAGE}:${version} \
                            -t ${DOCKER_IMAGE}:${buildDate} \
                            -t ${DOCKER_IMAGE}:build-${BUILD_NUMBER} \
                            .
                    """
                }
            }
        }
        
        stage('Push to Docker Hub') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    // Login no Docker Hub usando credenciais do Jenkins
                    withCredentials([
                        usernamePassword(
                            credentialsId: DOCKER_CREDENTIALS_ID,
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD'
                        )
                    ]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                    }
                    
                    // Push de todas as tags
                    sh """
                        docker push ${DOCKER_IMAGE}:latest
                        docker push ${DOCKER_IMAGE}:build-${BUILD_NUMBER}
                    """
                    
                    echo "✅ Images pushed to Docker Hub!"
                    echo "View at: https://hub.docker.com/r/${DOCKER_USER}/${APP_NAME}"
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                
                script {
                    sh """
                        kubectl set image deployment/bridal-cover-crm \
                            bridal-cover-crm=${DOCKER_IMAGE}:build-${BUILD_NUMBER} \
                            -n bridal-crm
                        
                        kubectl rollout status deployment/bridal-cover-crm -n bridal-crm
                    """
                }
                
                echo '🚀 Deploy completed!'
            }
        }
    }
    
    post {
        always {
            // Limpar imagens antigas para economizar espaço
            sh 'docker image prune -f'
        }
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed!'
        }
    }
}
```

### Configurar Credenciais no Jenkins

1. Acesse Jenkins: http://localhost:9090
2. Vá em: **Manage Jenkins** → **Credentials** → **System** → **Global credentials**
3. Clique em **Add Credentials**
4. Configure:
   - **Kind:** Username with password
   - **Username:** seu-usuario (Docker Hub)
   - **Password:** sua-senha (Docker Hub)
   - **ID:** `dockerhub-credentials`
   - **Description:** Docker Hub Credentials
5. Clique em **Create**

---

## Comandos Úteis

### Gerenciamento de Imagens

```bash
# Listar imagens locais
docker images | grep bridal-cover-crm

# Ver detalhes de uma imagem
docker inspect seu-usuario/bridal-cover-crm:latest

# Ver histórico/camadas da imagem
docker history seu-usuario/bridal-cover-crm:latest

# Remover imagem local
docker rmi seu-usuario/bridal-cover-crm:latest

# Remover todas as imagens do projeto
docker images | grep bridal-cover-crm | awk '{print $3}' | xargs docker rmi -f

# Ver tamanho das imagens
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}" | grep bridal-cover-crm
```

### Docker Hub via CLI

```bash
# Ver tags disponíveis no Docker Hub (requer curl/jq)
curl -s "https://registry.hub.docker.com/v2/repositories/seu-usuario/bridal-cover-crm/tags/" | jq -r '.results[].name'

# Pull de uma versão específica
docker pull seu-usuario/bridal-cover-crm:1.0.0

# Logout do Docker Hub
docker logout
```

### Teste Local da Imagem

```bash
# Testar imagem localmente (conecta no Postgres local)
docker run --rm -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/bridal_cover_crm_dev \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  seu-usuario/bridal-cover-crm:latest

# Testar em background
docker run -d --name test-app -p 8082:8082 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/bridal_cover_crm_dev \
  seu-usuario/bridal-cover-crm:latest

# Ver logs
docker logs -f test-app

# Parar e remover
docker stop test-app && docker rm test-app
```

### Debugging

```bash
# Entrar no container rodando
docker exec -it test-app /bin/sh

# Ver variáveis de ambiente
docker exec test-app env

# Ver processos
docker exec test-app ps aux

# Testar healthcheck
docker exec test-app curl -f http://localhost:8082/actuator/health
```

---

## Troubleshooting

### Problema: "denied: requested access to the resource is denied"

**Causa:** Não está logado ou sem permissão.

**Solução:**
```bash
# Re-login
docker login

# Verificar username
docker info | grep Username

# Tentar push novamente
docker push seu-usuario/bridal-cover-crm:latest
```

### Problema: "unauthorized: authentication required"

**Causa:** Token expirado ou credenciais inválidas.

**Solução:**
```bash
# Logout e login novamente
docker logout
docker login
```

### Problema: "image not found" no Kubernetes

**Causa:** Imagem privada sem credenciais configuradas.

**Solução:**
```bash
# Criar secret no Kubernetes
kubectl create secret docker-registry dockerhub-secret \
  --docker-server=https://index.docker.io/v1/ \
  --docker-username=seu-usuario \
  --docker-password=sua-senha \
  --docker-email=seu-email \
  -n bridal-crm

# Adicionar no deployment.yaml:
spec:
  imagePullSecrets:
  - name: dockerhub-secret
  containers:
  - name: app
    image: seu-usuario/bridal-cover-crm:latest
```

### Problema: Build muito lento

**Causa:** Docker está rebuilding camadas que não mudaram.

**Solução:**
```bash
# Use build cache e multi-stage builds (já está no Dockerfile!)
# Limpe apenas se necessário
docker builder prune

# Build com cache
docker build --cache-from seu-usuario/bridal-cover-crm:latest -t bridal-cover-crm:latest .
```

### Problema: "no space left on device"

**Causa:** Muitas imagens antigas ocupando espaço.

**Solução:**
```bash
# Ver uso de espaço
docker system df

# Limpar tudo (cuidado!)
docker system prune -a

# Limpar apenas imagens sem tag
docker image prune

# Limpar apenas containers parados
docker container prune
```

### Problema: Kubernetes não atualiza a imagem

**Causa:** `imagePullPolicy` não está forçando pull.

**Solução:**
```bash
# Método 1: Usar tags específicas (recomendado)
docker push seu-usuario/bridal-cover-crm:1.0.1
kubectl set image deployment/bridal-cover-crm \
  bridal-cover-crm=seu-usuario/bridal-cover-crm:1.0.1

# Método 2: Forçar restart (usa imagePullPolicy: Always)
kubectl rollout restart deployment/bridal-cover-crm -n bridal-crm

# Método 3: Adicionar no deployment.yaml:
spec:
  template:
    spec:
      containers:
      - name: app
        image: seu-usuario/bridal-cover-crm:latest
        imagePullPolicy: Always  # Sempre faz pull
```

---

## Alternativas ao Docker Hub

### GitHub Container Registry (ghcr.io)

**Vantagens:**
- ✅ Repos privados ilimitados (grátis!)
- ✅ Integrado com GitHub
- ✅ Bom rate limit

**Como usar:**

```bash
# 1. Criar Personal Access Token no GitHub
# Settings → Developer settings → Personal access tokens → Tokens (classic)
# Permissions: write:packages, read:packages, delete:packages

# 2. Login
echo $GITHUB_TOKEN | docker login ghcr.io -u seu-usuario --password-stdin

# 3. Tag e push
docker tag bridal-cover-crm:latest ghcr.io/seu-usuario/bridal-cover-crm:latest
docker push ghcr.io/seu-usuario/bridal-cover-crm:latest

# 4. No Kubernetes
spec:
  containers:
  - name: app
    image: ghcr.io/seu-usuario/bridal-cover-crm:latest
```

### Registry Próprio (Self-hosted)

**Vantagens:**
- ✅ Controle total
- ✅ Sem custos externos
- ✅ Sem rate limits

**Como usar:**

```bash
# Na máquina do Kubernetes:
docker run -d -p 5000:5000 \
  --restart=always \
  --name registry \
  -v registry-data:/var/lib/registry \
  registry:2

# Do seu Mac:
docker tag bridal-cover-crm:latest remote-machine:5000/bridal-cover-crm:latest
docker push remote-machine:5000/bridal-cover-crm:latest

# No Kubernetes (mesmo servidor):
spec:
  containers:
  - name: app
    image: localhost:5000/bridal-cover-crm:latest
```

---

## Checklist de Deploy

Use este checklist toda vez que fizer deploy:

- [ ] Código commitado no Git
- [ ] Testes passando localmente (`make test`)
- [ ] Build local bem-sucedido (`make build`)
- [ ] Dockerfile atualizado (se necessário)
- [ ] Versão atualizada em `build.gradle.kts`
- [ ] Build da imagem Docker (`make docker-build`)
- [ ] Push para Docker Hub (`make docker-push`)
- [ ] Verificar imagem no Docker Hub (web interface)
- [ ] Tag de imagem atualizada no platform-gitops
- [ ] Sync Argo CD da application do backend
- [ ] Testar aplicação (`curl` ou browser)
- [ ] Monitorar por alguns minutos

---

## Recursos Adicionais

### Links Úteis:
- 📚 [Docker Hub Documentation](https://docs.docker.com/docker-hub/)
- 🐳 [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- ☸️ [Kubernetes Images Guide](https://kubernetes.io/docs/concepts/containers/images/)
- 🔐 [Managing Docker Hub Secrets](https://docs.docker.com/engine/reference/commandline/login/)

### Documentos Relacionados:
- [Deployment Guide](./deployment-guide.md)
- [platform-gitops](https://github.com/GuAntunes/platform-gitops)
- [Jenkins CI/CD](../technologies/jenkins.md)

---

## Resumo Rápido

```bash
# Setup (uma vez)
docker login

# Workflow diário
make docker-release          # Build + Push
# Atualizar platform-gitops + sync Argo CD
```

🎉 **Pronto!** Sua imagem está no Docker Hub e pode ser referenciada pelo GitOps no platform-gitops.

---

**Autor:** Gustavo Antunes  
**Projeto:** BridalCover CRM  
**Última atualização:** Novembro 2024

