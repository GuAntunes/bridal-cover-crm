# ArgoCD - Bridal Cover CRM

GitOps para deploy do PostgreSQL e Backend no namespace `dev`.

## Estrutura

```
argocd/
├── bootstrap/root-app.yaml       # App of Apps (aplicar 1x via kubectl)
├── applications/
│   ├── postgres-dev.yaml         # Helm: helm-chart/postgresql
│   └── backend-dev.yaml          # Helm: helm-chart/bridal-cover-crm
├── projects/bridal-cover-crm.yaml
└── Makefile
```

## Pré-requisitos

- Cluster Kubernetes (Rancher Desktop / k3s)
- `kubectl` configurado
- Repositório GitHub privado com Deploy Key configurada no ArgoCD

## Ordem de bootstrap

```bash
# 1. Instalar ArgoCD
make install

# 2. Configurar repo SSH no ArgoCD (Etapa 3)
make setup-repo-secret    # gera chave + cria Secret no cluster
make show-deploy-key      # adicionar no GitHub Deploy Keys
make verify-repo          # validar conexao Successful

# 3. Aplicar AppProject
make bootstrap-project

# 4. Aplicar root-app (uma vez)
make bootstrap-root

# 5. Na UI: Sync root-app → postgres-dev → backend-dev (manual)
```

## Comandos úteis

```bash
make help            # Ver comandos
make port-forward    # UI em https://localhost:8080
make password        # Senha do admin
make validate        # Validar YAMLs
make status          # Pods do ArgoCD
```

## Sync manual

Este ambiente usa **sync manual**. Após mudanças no Git:

1. ArgoCD detecta `OutOfSync` (~3 min)
2. Revise o diff na UI
3. Clique **Sync**

Ordem obrigatória: `postgres-dev` antes de `backend-dev`.

## Migração do Helm manual

Antes do primeiro sync das applications, remova as releases manuais:

```bash
cd ../helm-chart && make uninstall
```

Depois faça sync via ArgoCD UI.
