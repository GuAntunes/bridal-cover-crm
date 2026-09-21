# Guia Jenkins — por onde começar

O **bridal-cover-crm** usa **Pipeline as Code** (`Jenkinsfile` na raiz do repositório). Este guia indica qual documentação seguir conforme o ambiente.

---

## Escolha o ambiente

| Onde o Jenkins roda | Documentação |
|---------------------|--------------|
| **Kubernetes** (recomendado com Argo CD) | **[deployment/jenkins-kubernetes.md](deployment/jenkins-kubernetes.md)** |
| **Docker Compose** (máquina local) | **[technologies/jenkins.md](technologies/jenkins.md)** |

---

## Resumo do fluxo no Kubernetes

1. **platform-gitops** — Argo sync da app `jenkins-dev` → namespace `ci`
2. Acessar UI (NodePort `30090` ou port-forward)
3. Plugins + JDK **JDK17** + (opcional) Kubernetes cloud
4. Job Pipeline apontando ao Git deste repo, script `Jenkinsfile`
5. CI no Jenkins; CD via **platform-gitops** + Argo CD

Detalhes de cada etapa: [jenkins-kubernetes.md](deployment/jenkins-kubernetes.md).

---

## Docker Compose (local)

```bash
docker compose up -d jenkins
# UI: http://localhost:9090
docker exec bridal-cover-crm-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

O Compose monta o socket Docker do host para `docker build` no pipeline; no cluster isso é tratado de outra forma (Kaniko/build externo).

---

## Links relacionados

- [deployment-guide.md](deployment/deployment-guide.md)
- [docker-hub-guide.md](deployment/docker-hub-guide.md)
