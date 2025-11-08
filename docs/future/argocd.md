# ArgoCD - GitOps para Kubernetes

## Índice
1. [Introdução](#introdução)
2. [O que é ArgoCD?](#o-que-é-argocd)
3. [Conceitos Fundamentais](#conceitos-fundamentais)
4. [Arquitetura](#arquitetura)
5. [Por que usar ArgoCD?](#por-que-usar-argocd)
6. [Instalação](#instalação)
   - [Pré-requisitos](#pré-requisitos)
   - [Instalação no Minikube](#instalação-no-minikube)
   - [Instalação em Cluster Completo](#instalação-em-cluster-completo)
7. [Configuração Inicial](#configuração-inicial)
8. [Deploy da Aplicação](#deploy-da-aplicação-bridal-cover-crm)
9. [Operações Comuns](#operações-comuns)
10. [Integração com Jenkins](#integração-com-jenkins)
11. [Troubleshooting](#troubleshooting)
12. [Boas Práticas](#boas-práticas)
13. [Referências](#referências)

---

## Introdução

Este documento fornece um guia completo sobre **ArgoCD**, uma ferramenta de entrega contínua (CD) declarativa para Kubernetes que segue os princípios de **GitOps**.

## O que é ArgoCD?

**ArgoCD** é um operador Kubernetes que automatiza o deploy de aplicações usando Git como fonte única da verdade (single source of truth). Ele monitora continuamente seu repositório Git e sincroniza automaticamente o estado do cluster Kubernetes com o estado desejado definido no Git.

### Principais Características

- **GitOps**: Git como fonte única da verdade
- **Continuous Deployment**: Sincronização automática
- **Multi-cluster**: Gerencia múltiplos clusters
- **Rollback Fácil**: Volta para qualquer commit do Git
- **Health Monitoring**: Monitora saúde das aplicações
- **SSO Integration**: Integração com GitHub, GitLab, Google, etc.
- **RBAC**: Controle de acesso granular
- **Web UI + CLI**: Interface gráfica e linha de comando

### ArgoCD vs Outras Ferramentas

| Ferramenta | Tipo | Propósito | Precisa K8s? |
|------------|------|-----------|--------------|
| **ArgoCD** | CD (GitOps) | Deploy automático | Sim |
| **Flux CD** | CD (GitOps) | Deploy automático | Sim |
| **Jenkins** | CI/CD | Build, test, deploy | Não |
| **GitHub Actions** | CI/CD | Build, test, deploy | Não |
| **Spinnaker** | CD | Multi-cloud CD | Não (mas suporta) |

---

## Conceitos Fundamentais

### 1. GitOps

**GitOps** é uma metodologia de operações onde:
- Git é a fonte única da verdade
- Mudanças são feitas via pull requests
- Automação garante que cluster = Git
- Auditável e reversível

```
Git Repository (Manifests)
         ↓
    ArgoCD observa
         ↓
    Detecta diferença
         ↓
    Aplica mudanças no K8s
         ↓
    Cluster sincronizado
```

### 2. Application

Uma **Application** no ArgoCD representa uma aplicação deployada no Kubernetes. Define:
- Repositório Git fonte
- Path dos manifestos
- Cluster/Namespace destino
- Política de sincronização

### 3. Sync

**Sync** é o processo de aplicar mudanças do Git no Kubernetes:
- **OutOfSync**: Git ≠ Cluster (precisa sincronizar)
- **Synced**: Git = Cluster (em sincronia)
- **Auto-sync**: Sincronização automática
- **Manual sync**: Sincronização manual

### 4. Health Status

**Health** indica se a aplicação está saudável:
- **Healthy**: Todos os recursos OK
- **Progressing**: Deploy em andamento
- **Degraded**: Algum problema
- **Missing**: Recurso não encontrado

### 5. Prune

**Prune** remove recursos do cluster que não existem mais no Git.

---

## Arquitetura

### Componentes do ArgoCD

```
┌─────────────────────────────────────────────────────────┐
│                    ARGOCD ARCHITECTURE                   │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │           ArgoCD Namespace                         │ │
│  │                                                    │ │
│  │  ┌──────────────────┐      ┌──────────────────┐  │ │
│  │  │  argocd-server   │      │ argocd-repo-server│ │
│  │  │  (API + UI)      │      │ (Git connector)   │ │
│  │  │  Port: 8080      │      │                   │ │
│  │  └──────────────────┘      └──────────────────┘  │ │
│  │                                                    │ │
│  │  ┌──────────────────┐      ┌──────────────────┐  │ │
│  │  │ application-     │      │ redis            │  │
│  │  │ controller       │      │ (Cache)          │  │
│  │  │ (Sync engine)    │      │                  │ │
│  │  └──────────────────┘      └──────────────────┘  │ │
│  │                                                    │ │
│  │  ┌──────────────────┐      ┌──────────────────┐  │ │
│  │  │ dex-server       │      │ notifications    │  │
│  │  │ (SSO)            │      │ (Webhooks)       │  │
│  │  └──────────────────┘      └──────────────────┘  │ │
│  └────────────────────────────────────────────────────┘ │
│                           ↓                             │
│                    Monitora e Aplica                    │
│                           ↓                             │
│  ┌────────────────────────────────────────────────────┐ │
│  │           Target Namespaces                        │ │
│  │   (bridal-crm, outros projetos...)                 │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Fluxo de Trabalho

```
Developer → Git Push → GitHub
                         ↓
                    ArgoCD detecta
                         ↓
                    Compara manifests
                         ↓
          Git ≠ Cluster? (OutOfSync)
                         ↓
                   Sincroniza (Sync)
                         ↓
              kubectl apply manifests
                         ↓
                 Cluster atualizado
                         ↓
              Health check (Healthy)
```

---

## Por que usar ArgoCD?

### Benefícios

#### 1. **Auditabilidade**
- Todo deploy está no Git
- História completa de mudanças
- Quem fez, quando e por quê

#### 2. **Disaster Recovery**
- Cluster destruído? Recrie e aponte ArgoCD para o Git
- Tudo volta como estava
- Backup natural via Git

#### 3. **Rollback Fácil**
- Volta para qualquer commit
- Um clique ou comando
- Sem complicação

#### 4. **Visibilidade**
- UI mostra estado de tudo
- Diff entre Git e Cluster
- Histórico de syncs

#### 5. **Automação**
- Sem kubectl manual
- Deploy automático
- Self-healing

#### 6. **Multi-ambiente**
- Dev, staging, prod
- Mesmos manifestos, diferentes configs
- Kustomize ou Helm

### Casos de Uso

- ✅ **CI/CD Completo**: Jenkins (CI) + ArgoCD (CD)
- ✅ **Multi-cluster**: Gerenciar dev, staging, prod
- ✅ **Multi-tenant**: Múltiplos times/projetos
- ✅ **Compliance**: Auditoria completa
- ✅ **Disaster Recovery**: Recreação rápida

---

## Instalação

### Pré-requisitos

- Kubernetes cluster rodando (Minikube, kubeadm, EKS, GKE, AKS)
- kubectl instalado e configurado
- Cluster com pelo menos:
  - 2 CPUs
  - 4GB RAM
  - Acesso à internet (para baixar imagens)

### Instalação no Minikube

#### Método 1: Instalação Rápida (Recomendado)

```bash
# 1. Criar namespace
kubectl create namespace argocd

# 2. Aplicar manifests oficiais
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# 3. Aguardar pods estarem prontos
kubectl wait --for=condition=Ready pods --all -n argocd --timeout=300s

# 4. Verificar instalação
kubectl get pods -n argocd
```

#### Método 2: Instalação com Script

Salve este script como `install-argocd.sh`:

```bash
#!/bin/bash

set -e

echo "🚀 Instalando ArgoCD no Minikube..."

# Criar namespace
echo "📦 Criando namespace argocd..."
kubectl create namespace argocd || echo "Namespace já existe"

# Instalar ArgoCD
echo "⬇️  Baixando e aplicando manifests..."
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Aguardar pods
echo "⏳ Aguardando pods ficarem prontos..."
kubectl wait --for=condition=Ready pods --all -n argocd --timeout=600s

# Verificar instalação
echo ""
echo "✅ ArgoCD instalado com sucesso!"
echo ""
echo "📊 Pods rodando:"
kubectl get pods -n argocd

echo ""
echo "🌐 Para acessar a UI:"
echo "   kubectl port-forward svc/argocd-server -n argocd 8080:443"
echo ""
echo "🔑 Para obter a senha inicial:"
echo "   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d"
echo ""
echo "👤 Usuário: admin"
```

Execute:

```bash
chmod +x install-argocd.sh
./install-argocd.sh
```

### Instalação em Cluster Completo

Para clusters em produção (AWS, GCP, Azure):

```bash
# 1. Criar namespace
kubectl create namespace argocd

# 2. Instalar ArgoCD (versão HA - High Availability)
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/ha/install.yaml

# 3. Aguardar
kubectl wait --for=condition=Ready pods --all -n argocd --timeout=600s
```

---

## Configuração Inicial

### 1. Acessar a UI

#### Opção A: Port-Forward (Desenvolvimento)

```bash
# Expor o serviço localmente
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Manter o terminal aberto e acessar:
# https://localhost:8080
```

#### Opção B: NodePort (Minikube)

```bash
# Alterar o service para NodePort
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "NodePort"}}'

# Obter URL
minikube service argocd-server -n argocd --url

# Acessar a URL retornada
```

#### Opção C: LoadBalancer (Cloud)

```bash
# Alterar para LoadBalancer
kubectl patch svc argocd-server -n argocd -p '{"spec": {"type": "LoadBalancer"}}'

# Obter IP externo
kubectl get svc argocd-server -n argocd
```

### 2. Obter Senha Inicial

```bash
# Obter senha
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d

# Salvar a senha!
```

### 3. Login

```
URL: https://localhost:8080
Usuário: admin
Senha: (obtida no passo anterior)
```

**Importante:** Aceite o certificado SSL auto-assinado no navegador.

### 4. Instalar ArgoCD CLI (Opcional)

```bash
# macOS
brew install argocd

# Linux
curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
chmod +x /usr/local/bin/argocd

# Windows (usando Chocolatey)
choco install argocd-cli

# Verificar instalação
argocd version
```

### 5. Login via CLI

```bash
# Port-forward em um terminal
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Em outro terminal, login
argocd login localhost:8080

# Usuário: admin
# Senha: (a senha obtida anteriormente)
```

### 6. Mudar Senha (Recomendado)

```bash
# Via CLI
argocd account update-password

# Ou via UI: User Info → Update Password
```

---

## Deploy da Aplicação Bridal Cover CRM

### Estrutura do Repositório

Seu repositório Git deve ter esta estrutura:

```
bridal-cover-crm/
├── k8s/
│   ├── 00-namespace.yaml
│   ├── 01-configmap.yaml
│   ├── 02-secret.yaml
│   ├── 03-postgres-pvc.yaml
│   ├── 04-postgres-statefulset.yaml
│   ├── 05-postgres-service.yaml
│   ├── 06-app-deployment.yaml
│   ├── 07-app-service.yaml
│   └── 08-ingress.yaml
├── src/
└── ...
```

### Método 1: Criar Application via UI

1. **Acesse ArgoCD UI** (https://localhost:8080)

2. **Click em "New App"**

3. **Preencha os campos:**
   ```
   Application Name: bridal-crm
   Project: default
   Sync Policy: Automatic (ou Manual)
   
   REPOSITORY
   Repository URL: https://github.com/seu-usuario/bridal-cover-crm
   Revision: main (ou master)
   Path: k8s
   
   DESTINATION
   Cluster URL: https://kubernetes.default.svc
   Namespace: bridal-crm
   
   SYNC OPTIONS
   ☑ Auto-Create Namespace
   ☑ Auto-Sync (opcional)
   ☑ Prune Resources (opcional)
   ☑ Self Heal (opcional)
   ```

4. **Click em "Create"**

5. **Sync** (se não habilitou auto-sync)
   - Click na aplicação
   - Click em "SYNC"
   - Click em "SYNCHRONIZE"

### Método 2: Criar Application via CLI

```bash
# Criar application
argocd app create bridal-crm \
  --repo https://github.com/seu-usuario/bridal-cover-crm \
  --path k8s \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace bridal-crm \
  --sync-policy automated \
  --auto-prune \
  --self-heal

# Verificar status
argocd app get bridal-crm

# Sincronizar (se necessário)
argocd app sync bridal-crm

# Ver logs
argocd app logs bridal-crm
```

### Método 3: Criar Application via Manifest YAML

Crie o arquivo `argocd/bridal-crm-app.yaml`:

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: bridal-crm
  namespace: argocd
  # Finalizer para garantir que ArgoCD limpe recursos quando app for deletada
  finalizers:
    - resources-finalizer.argocd.argoproj.io
spec:
  # Projeto (default ou crie um específico)
  project: default
  
  # Fonte (Git)
  source:
    repoURL: https://github.com/seu-usuario/bridal-cover-crm
    targetRevision: main
    path: k8s
    
  # Destino (Kubernetes)
  destination:
    server: https://kubernetes.default.svc
    namespace: bridal-crm
    
  # Política de sincronização
  syncPolicy:
    # Sincronização automática
    automated:
      # Remove recursos que não existem mais no Git
      prune: true
      # Corrige automaticamente se alguém mudar algo manualmente
      selfHeal: true
      # Permite recursos vazios
      allowEmpty: false
    
    # Opções de sync
    syncOptions:
      - CreateNamespace=true
      - PrunePropagationPolicy=foreground
      - PruneLast=true
    
    # Retry se falhar
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
```

Aplicar:

```bash
kubectl apply -f argocd/bridal-crm-app.yaml

# Verificar
kubectl get application -n argocd
argocd app get bridal-crm
```

### Verificar Deploy

```bash
# Via CLI
argocd app get bridal-crm
argocd app logs bridal-crm --follow

# Via kubectl
kubectl get all -n bridal-crm

# Via UI
# Abra https://localhost:8080 e veja a aplicação visualmente
```

---

## Operações Comuns

### Ver Status da Aplicação

```bash
# CLI
argocd app get bridal-crm

# Kubectl
kubectl get application bridal-crm -n argocd -o yaml

# UI
# Clique na aplicação
```

### Sincronizar Manualmente

```bash
# Sync completo
argocd app sync bridal-crm

# Sync de recurso específico
argocd app sync bridal-crm --resource Deployment:bridal-crm-app

# Sync com prune
argocd app sync bridal-crm --prune
```

### Ver Diferenças (Diff)

```bash
# Ver o que mudou entre Git e Cluster
argocd app diff bridal-crm

# Na UI: Click em "APP DIFF"
```

### Rollback

```bash
# Ver histórico
argocd app history bridal-crm

# Rollback para revisão específica
argocd app rollback bridal-crm <REVISION_ID>

# Na UI: HISTORY → Click em revisão → ROLLBACK
```

### Refresh (Recarregar do Git)

```bash
# Forçar ArgoCD a verificar o Git agora
argocd app get bridal-crm --refresh

# Hard refresh (limpa cache)
argocd app get bridal-crm --hard-refresh
```

### Deletar Aplicação

```bash
# Deletar app (mantém recursos no cluster)
argocd app delete bridal-crm --cascade=false

# Deletar app E recursos no cluster
argocd app delete bridal-crm --cascade=true

# Ou via kubectl
kubectl delete application bridal-crm -n argocd
```

### Pause/Resume Auto-sync

```bash
# Pausar auto-sync
argocd app set bridal-crm --sync-policy none

# Retomar auto-sync
argocd app set bridal-crm --sync-policy automated
```

---

## Integração com Jenkins

### Pipeline Completo: Jenkins + ArgoCD

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'your-registry'
        IMAGE_NAME = 'bridal-cover-crm'
        GIT_REPO = 'https://github.com/seu-usuario/bridal-cover-crm'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
        
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
        
        stage('Docker Build & Push') {
            steps {
                script {
                    def imageTag = "v${BUILD_NUMBER}"
                    sh """
                        docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${imageTag} .
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${imageTag}
                        docker tag ${DOCKER_REGISTRY}/${IMAGE_NAME}:${imageTag} ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                        docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:latest
                    """
                }
            }
        }
        
        stage('Update Kubernetes Manifests') {
            steps {
                script {
                    def imageTag = "v${BUILD_NUMBER}"
                    sh """
                        # Clone repo (ou use o já clonado)
                        git config user.name "Jenkins"
                        git config user.email "jenkins@example.com"
                        
                        # Atualizar manifest
                        sed -i 's|image: .*/${IMAGE_NAME}:.*|image: ${DOCKER_REGISTRY}/${IMAGE_NAME}:${imageTag}|' k8s/06-app-deployment.yaml
                        
                        # Commit e push
                        git add k8s/06-app-deployment.yaml
                        git commit -m "chore: update image to ${imageTag}"
                        git push origin main
                    """
                }
            }
        }
        
        stage('Trigger ArgoCD Sync') {
            steps {
                script {
                    // Opcional: Forçar sync imediato
                    sh """
                        argocd app sync bridal-crm --force
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline executado com sucesso!'
            echo 'ArgoCD irá deployar automaticamente'
        }
        failure {
            echo '❌ Pipeline falhou!'
        }
    }
}
```

### Fluxo Completo

```
1. Developer → Git Push
   ↓
2. Jenkins detecta (Webhook)
   ↓
3. Jenkins executa pipeline:
   • Build
   • Test
   • Docker build & push
   • Update manifest no Git
   ↓
4. ArgoCD detecta mudança no Git
   ↓
5. ArgoCD sincroniza automaticamente
   ↓
6. Aplicação atualizada no Kubernetes
```

---

## Troubleshooting

### Problema: Application Stuck em "Progressing"

```bash
# Ver eventos
argocd app get bridal-crm

# Ver logs
argocd app logs bridal-crm

# Ver recursos com problema
kubectl get events -n bridal-crm --sort-by='.lastTimestamp'

# Forçar refresh
argocd app get bridal-crm --hard-refresh
```

### Problema: OutOfSync mas não deveria

```bash
# Ver diff
argocd app diff bridal-crm

# Ignorar diferenças em campos específicos
# Adicione annotation no manifest:
argocd.argoproj.io/compare-options: IgnoreExtraneous
```

### Problema: "Permission Denied" ao sincronizar

```bash
# Verificar RBAC
kubectl get clusterrole argocd-application-controller -o yaml

# Verificar ServiceAccount
kubectl get serviceaccount argocd-application-controller -n argocd
```

### Problema: ArgoCD não detecta mudanças no Git

```bash
# Verificar webhook (se configurado)
# Ou forçar refresh manual
argocd app get bridal-crm --refresh

# Verificar se repo é acessível
argocd repo get https://github.com/seu-usuario/bridal-cover-crm
```

### Problema: Senha perdida

```bash
# Resetar senha do admin
kubectl -n argocd patch secret argocd-secret \
  -p '{"stringData": {
    "admin.password": "$2a$10$rRyBsGSHK6.uc8fntPwVIuLVHgsAhAX7TcdrqW/RADU0uh7CaChLa",
    "admin.passwordMtime": "'$(date +%FT%T%Z)'"
  }}'

# Nova senha: "password"
# Faça login e mude a senha imediatamente
```

### Ver Logs do ArgoCD

```bash
# Logs do application controller
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-application-controller -f

# Logs do server
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-server -f

# Logs do repo server
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-repo-server -f
```

---

## Boas Práticas

### 1. **Estrutura de Repositório**

```
repo/
├── apps/
│   ├── dev/
│   │   └── bridal-crm.yaml (Application manifest)
│   ├── staging/
│   └── production/
├── k8s/
│   ├── base/  (manifestos base)
│   └── overlays/
│       ├── dev/
│       ├── staging/
│       └── production/
```

### 2. **Use Projects**

```yaml
apiVersion: argoproj.io/v1alpha1
kind: AppProject
metadata:
  name: bridal-crm-project
  namespace: argocd
spec:
  description: Bridal CRM Project
  
  # Repositórios permitidos
  sourceRepos:
    - 'https://github.com/seu-usuario/bridal-cover-crm'
  
  # Clusters permitidos
  destinations:
    - namespace: 'bridal-crm-*'
      server: https://kubernetes.default.svc
  
  # Permissões
  clusterResourceWhitelist:
    - group: '*'
      kind: '*'
```

### 3. **Sync Windows**

Configure janelas de tempo para sincronização:

```yaml
spec:
  syncPolicy:
    syncOptions:
      - CreateNamespace=true
    automated:
      prune: true
      selfHeal: true
  # Permitir sync apenas em horários específicos
  syncWindows:
    - kind: allow
      schedule: '0 9-17 * * MON-FRI'  # Segunda a sexta, 9h-17h
      duration: 8h
      applications:
        - '*'
```

### 4. **Notifications**

Configure notificações para Slack, email, etc:

```bash
# Instalar ArgoCD Notifications
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj-labs/argocd-notifications/release-1.0/manifests/install.yaml
```

### 5. **Secrets Management**

Use Sealed Secrets ou External Secrets:

```bash
# Sealed Secrets
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.18.0/controller.yaml

# Criptografar secret
kubeseal --format yaml < secret.yaml > sealed-secret.yaml

# Commitar sealed-secret.yaml no Git (é seguro!)
```

### 6. **Health Checks Customizados**

```yaml
spec:
  source:
    helm:
      # Custom health checks
      parameters:
        - name: healthCheck.enabled
          value: "true"
```

### 7. **Multiple Environments**

```bash
# Dev
argocd app create bridal-crm-dev \
  --repo https://github.com/seu-usuario/bridal-cover-crm \
  --path k8s/overlays/dev \
  --dest-namespace bridal-crm-dev

# Staging
argocd app create bridal-crm-staging \
  --repo https://github.com/seu-usuario/bridal-cover-crm \
  --path k8s/overlays/staging \
  --dest-namespace bridal-crm-staging

# Production
argocd app create bridal-crm-prod \
  --repo https://github.com/seu-usuario/bridal-cover-crm \
  --path k8s/overlays/production \
  --dest-namespace bridal-crm-prod \
  --sync-policy none  # Manual sync em produção!
```

---

## Referências

### Documentação Oficial

- [ArgoCD Documentation](https://argo-cd.readthedocs.io/)
- [Getting Started Guide](https://argo-cd.readthedocs.io/en/stable/getting_started/)
- [Core Concepts](https://argo-cd.readthedocs.io/en/stable/core_concepts/)
- [Best Practices](https://argo-cd.readthedocs.io/en/stable/user-guide/best_practices/)

### Ferramentas Relacionadas

- [ArgoCD CLI](https://argo-cd.readthedocs.io/en/stable/cli_installation/)
- [ArgoCD Notifications](https://argocd-notifications.readthedocs.io/)
- [ArgoCD Image Updater](https://argocd-image-updater.readthedocs.io/)
- [ArgoCD Autopilot](https://argocd-autopilot.readthedocs.io/)

### GitOps

- [GitOps Principles](https://www.gitops.tech/)
- [CNCF GitOps Working Group](https://github.com/cncf/tag-app-delivery/tree/main/gitops-wg)

### Comunidade

- [ArgoCD GitHub](https://github.com/argoproj/argo-cd)
- [ArgoCD Slack](https://argoproj.github.io/community/join-slack)
- [ArgoCD Forum](https://github.com/argoproj/argo-cd/discussions)

---

## Resumo

ArgoCD é uma ferramenta poderosa que traz os benefícios do GitOps para Kubernetes:

✅ **Instalação simples**: 5 minutos no Minikube  
✅ **UI intuitiva**: Visualização clara do estado  
✅ **Automação**: Deploy automático a partir do Git  
✅ **Auditável**: Todo deploy registrado no Git  
✅ **Rollback fácil**: Volta para qualquer versão  
✅ **Multi-ambiente**: Dev, staging, prod  
✅ **Integração**: Funciona com Jenkins, GitHub Actions, etc.  

Para o projeto Bridal Cover CRM, ArgoCD complementa perfeitamente o pipeline CI/CD:
- **Jenkins** (Docker Compose): Build, test, push imagem
- **ArgoCD** (Kubernetes): Deploy, sync, monitoring

Juntos, formam um pipeline robusto e moderno! 🚀

