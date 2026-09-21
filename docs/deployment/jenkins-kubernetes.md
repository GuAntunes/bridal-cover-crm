# Jenkins no Kubernetes (GitOps)

Este guia explica como o **Jenkins** entra no fluxo do **bridal-cover-crm** quando o controller roda no cluster, gerenciado pelo repositório **[platform-gitops](https://github.com/GuAntunes/platform-gitops)** e pelo **Argo CD**.

Para Jenkins apenas no Docker Compose da máquina local, veja também [technologies/jenkins.md](../technologies/jenkins.md).

---

## Visão geral: quem faz o quê

```text
┌─────────────────┐     push      ┌──────────────────┐
│ bridal-cover-crm│ ────────────► │ GitHub (código)  │
│  + Jenkinsfile  │               └────────┬─────────┘
└─────────────────┘                        │
                                           │ webhook / poll
                                           ▼
┌──────────────────────────────────────────────────────────┐
│ Cluster Kubernetes                                        │
│  namespace ci     → Jenkins (build + testes)              │
│  namespace dev    → PostgreSQL (dados)                    │
│  namespace argocd → Argo CD (deploy GitOps)               │
└──────────────────────────────────────────────────────────┘
                                           │
                    imagem Docker + tag    │
                                           ▼
┌─────────────────┐     commit      ┌──────────────────┐
│  Docker Hub     │ ◄────────────── │ Jenkins (futuro) │
└────────┬────────┘                 └────────┬─────────┘
         │                                   │
         │         atualiza tag no Git       │
         └──────────────────► platform-gitops ──► Argo sync ──► app no cluster
```

| Etapa | Ferramenta | Onde está definido |
|-------|------------|-------------------|
| Instalar Jenkins no cluster | Argo CD + Helm | `platform-gitops/charts/jenkins`, Application `jenkins-dev` |
| Receita de CI (build/test) | Jenkins Pipeline | `Jenkinsfile` na raiz deste repo |
| Banco de dev | Argo + Helm | `postgres-dev` → namespace `dev` |
| Deploy da API no cluster | Argo CD | `platform-gitops` (chart da app no futuro) |

**CI** (integração contínua) = compilar, testar, gerar artefato/imagem.  
**CD** (entrega contínua) = colocar a versão certa no cluster; aqui o caminho preferido é **GitOps** (mudar tag no Git e sincronizar no Argo), não `kubectl apply` manual no Jenkins.

---

## Etapa 1 — Subir o Jenkins via Argo CD

No **platform-gitops** já existem:

- Chart: `charts/jenkins`
- Application: `argocd/applications/example-project/dev/jenkins-dev.yaml`
- Destino: namespace **`ci`**, NodePort **30090** (delta em `jenkins-values.yaml`)

### Pré-requisitos

- Argo CD instalado e secret do repo Git (`make setup-repo-secret`)
- `AppProject` com destino `ci` (já em `example-project.yaml`)

### Comandos (uma vez ou após mudança no Git)

```bash
# No platform-gitops — se ainda não aplicou o projeto atualizado:
make bootstrap-projects

# Na UI do Argo ou CLI:
# 1) Sync root-app
# 2) Sync jenkins-dev
```

### Conferir no cluster

```bash
kubectl get pods -n ci
kubectl get svc -n ci jenkins
```

### Acessar a UI

**NodePort (dev):** `http://<IP-do-nó>:30090`

**Port-forward (qualquer ambiente):**

```bash
kubectl port-forward -n ci svc/jenkins 9090:8080
# http://localhost:9090
```

### Senha inicial do admin

```bash
kubectl exec -n ci deploy/jenkins -- cat /var/jenkins_home/secrets/initialAdminPassword
```

Usuário padrão: **admin**.

---

## Etapa 2 — Configurar o Jenkins (primeira vez na UI)

O chart instala o **controller** e RBAC para agents no namespace `ci`. Plugins, JDK nomeado e o job Pipeline são configurados no Jenkins.

### 2.1 Plugins recomendados

**Manage Jenkins → Plugins → Available** (instale e reinicie se pedido):

| Plugin | Uso |
|--------|-----|
| Pipeline | `Jenkinsfile` |
| Git | SCM |
| JUnit | Relatórios de teste |
| HTML Publisher | Relatório HTML dos testes |
| Kubernetes | Agents em Pods no cluster |

### 2.2 JDK 17 (obrigatório para o Jenkinsfile atual)

**Manage Jenkins → Tools → JDK installations**

- Name: **`JDK17`** (exatamente esse nome)
- Install automatically (Temurin 17) ou caminho manual

O `Jenkinsfile` referencia:

```groovy
tools { jdk 'JDK17' }
```

### 2.3 Kubernetes cloud (agents no cluster)

**Manage Jenkins → Clouds → New cloud → Kubernetes**

| Campo | Valor sugerido |
|-------|----------------|
| Kubernetes URL | `https://kubernetes.default.svc` |
| Credentials | None (in-cluster) ou kubeconfig se necessário |
| Jenkins URL | `http://jenkins.ci.svc.cluster.local:8080` |
| Jenkins tunnel | `jenkins.ci.svc.cluster.local:50000` |
| Namespace | `ci` |

O ServiceAccount do release (`jenkins`) já tem Role para criar Pods no namespace `ci`.

Depois, ajuste o `Jenkinsfile` para usar agent Kubernetes (fase seguinte); enquanto usar `agent any`, o build roda **no próprio Pod do controller** (funciona para Gradle, mas não escala bem).

### 2.4 Credenciais Git

Se o repositório **bridal-cover-crm** é privado:

**Manage Jenkins → Credentials → Global → Add**

- Tipo: SSH Username with private key ou Username with password (HTTPS)
- ID sugerido: `github-bridal-cover-crm`

Use esse ID na configuração do job (SCM).

---

## Etapa 3 — Criar o job Pipeline

1. **New Item** → nome `bridal-cover-crm` → **Pipeline**
2. **Pipeline → Definition:** Pipeline script from SCM
3. **SCM:** Git
4. **Repository URL:** URL do repo bridal-cover-crm
5. **Credentials:** se privado
6. **Branches:** `*/main` ou multibranch depois
7. **Script Path:** `Jenkinsfile`
8. **Save → Build Now**

### Disparar builds

| Método | Quando usar |
|--------|-------------|
| **Build Now** | Testes manuais |
| **Poll SCM** (`H/5 * * * *`) | Dev local sem webhook |
| **GitHub webhook** | Servidor com URL pública HTTPS |

Webhook path: `https://<jenkins-host>/github-webhook/`

---

## Etapa 4 — O que o Jenkinsfile executa (stages)

Arquivo: `Jenkinsfile` na raiz do **bridal-cover-crm**.

| Stage | Ação | Branch |
|-------|------|--------|
| Checkout | Clona o repo | todas |
| Build | `./gradlew clean build -x test` | todas |
| Unit Tests | `./gradlew test` + JUnit/HTML | todas |
| Architecture Tests | `*ArchitectureTest` | todas |
| Code Quality | `./gradlew check` | todas |
| Package | `bootJar` | todas |
| Build Docker Image | `docker build` / tag | **main** apenas |
| Deploy to Development | placeholder | **develop** |
| Deploy to Production | aprovação manual + placeholder | **main** |

Stages de **deploy** ainda não aplicam no cluster; o deploy real deve atualizar o **platform-gitops** e sincronizar no Argo.

### Build Docker no Kubernetes

O chart **não** monta Docker socket no controller (diferente do Compose). Para `docker build` no cluster use depois uma destas opções:

- **Kaniko** ou **BuildKit** no pipeline
- Build na máquina/Actions e Jenkins só dispara GitOps
- Runner dedicado com Docker (menos recomendado)

Até lá, stages Gradle funcionam no controller; o stage Docker em `main` pode falhar até você adotar Kaniko ou build externo.

---

## Etapa 5 — CD com Argo CD (fluxo alvo)

```text
1. Jenkins: build + testes OK
2. Jenkins: build/push imagem (ex.: guantunes/bridal-cover-crm:dev-<sha>)
3. Commit no platform-gitops: nova image.tag no values da Application
4. Argo CD: sync da app backend-dev (quando existir)
5. Cluster roda a nova versão
```

Vantagem: o que está no cluster é sempre o que está no Git; Jenkins não precisa de `kubectl` com permissões amplas.

---

## Etapa 6 — Ordem de sync no Argo (dev)

| sync-wave | Application | Namespace |
|-----------|-------------|-----------|
| 0 | `postgres-dev` | `dev` |
| 1 | `jenkins-dev` | `ci` |

Sync **root-app** primeiro para registrar `jenkins-dev`, depois sync da app Jenkins.

---

## Comandos úteis

```bash
# Renderizar manifestos Jenkins localmente (platform-gitops)
make helm-template-jenkins ENV=dev PROJECT=example-project

# Logs do controller
kubectl logs -n ci deploy/jenkins -f

# Reiniciar após problema de volume (cuidado: não apague PVC em prod)
kubectl rollout restart -n ci deploy/jenkins
```

---

## Troubleshooting

| Problema | Solução |
|----------|---------|
| `JDK17` not found | Configurar JDK com nome exato em Tools |
| Repo não clona | Credencial Git no Jenkins |
| UI não abre no NodePort | `kubectl get nodes -o wide`, testar port-forward |
| Docker build falha no K8s | Esperado sem Kaniko; ver seção Build Docker |
| App Argo `jenkins-dev` negada | `bootstrap-projects` com destino `ci` no AppProject |

---

## Referências

- [platform-gitops — charts/jenkins](https://github.com/GuAntunes/platform-gitops/tree/main/charts/jenkins)
- [technologies/jenkins.md](../technologies/jenkins.md) — conceitos e Compose
- [deployment-guide.md](deployment-guide.md) — visão geral de deploy
- [docker-hub-guide.md](docker-hub-guide.md) — credenciais Docker Hub no Jenkins
