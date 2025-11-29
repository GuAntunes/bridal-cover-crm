# 🚀 Instruções de Deploy - PostgreSQL com Helm

## ✅ O que foi criado

Foi criado um Helm chart completo para deploy do PostgreSQL no Kubernetes com:

- ✅ Deployment do PostgreSQL 15 Alpine
- ✅ Service (NodePort para dev, ClusterIP para prod)
- ✅ PersistentVolumeClaim para dados
- ✅ Secret para credenciais
- ✅ ConfigMap com script de inicialização
- ✅ Health checks (liveness e readiness probes)
- ✅ Valores específicos por ambiente (dev, staging, prod)
- ✅ Makefile com comandos úteis
- ✅ Documentação completa
- ✅ Exemplos de conexão

## 📁 Estrutura de Arquivos

```
helm-chart/postgresql/
├── Chart.yaml                      # Metadados do chart
├── values.yaml                     # Valores padrão
├── values-dev.yaml                 # Valores para desenvolvimento
├── values-staging.yaml             # Valores para staging
├── values-prod.yaml                # Valores para produção
├── Makefile                        # Comandos úteis
├── README.md                       # Documentação completa
├── QUICK-START.md                  # Guia rápido
├── DEPLOY-INSTRUCTIONS.md          # Este arquivo
├── .helmignore                     # Arquivos a ignorar no pacote
├── templates/
│   ├── _helpers.tpl               # Templates helpers
│   ├── deployment.yaml            # Deployment do PostgreSQL
│   ├── service.yaml               # Service
│   ├── pvc.yaml                   # PersistentVolumeClaim
│   ├── secret.yaml                # Secret com credenciais
│   ├── configmap.yaml             # Script de inicialização
│   └── NOTES.txt                  # Notas pós-instalação
└── examples/
    ├── backend-connection.yaml    # Exemplo de conexão do backend
    └── connection-test.yaml       # Pod para testar conexão
```

## 🎯 Quando estiver no servidor Kubernetes

### Passo 1: Navegar até o diretório

```bash
cd /caminho/para/bridal-cover-crm/helm-chart/postgresql
```

### Passo 2: Verificar o cluster

```bash
# Verificar se está conectado ao cluster correto
kubectl cluster-info

# Ver nodes disponíveis
kubectl get nodes

# Verificar namespaces
kubectl get namespaces
```

### Passo 3: Instalar PostgreSQL

#### Opção A: Desenvolvimento (recomendado para começar)

```bash
# Usando Makefile (mais fácil)
make install-dev

# OU usando Helm diretamente
helm install postgresql-dev . \
  --namespace bridal-crm \
  --create-namespace \
  --values values-dev.yaml
```

#### Opção B: Staging

```bash
make install-staging

# OU
helm install postgresql-staging . \
  --namespace bridal-crm-staging \
  --create-namespace \
  --values values-staging.yaml
```

#### Opção C: Produção

```bash
# ⚠️ IMPORTANTE: Antes de instalar em produção
# 1. Edite values-prod.yaml e altere a senha:
nano values-prod.yaml
# Procure por: postgresql.password e altere de "CHANGE_ME_IN_PRODUCTION" para uma senha forte

# 2. Depois instale:
make install-prod

# OU
helm install postgresql-prod . \
  --namespace bridal-crm-prod \
  --create-namespace \
  --values values-prod.yaml
```

### Passo 4: Verificar a instalação

```bash
# Ver o status do Helm release
helm status postgresql-dev -n bridal-crm

# Ver os pods (deve mostrar 1/1 Running)
kubectl get pods -n bridal-crm

# Ver os serviços
kubectl get svc -n bridal-crm

# Ver o PVC
kubectl get pvc -n bridal-crm

# Ver logs em tempo real
kubectl logs -n bridal-crm -l app.kubernetes.io/name=bridal-cover-crm-postgresql -f
```

### Passo 5: Testar a conexão

#### Teste 1: De dentro do cluster

```bash
# Criar pod de teste
kubectl apply -f examples/connection-test.yaml

# Esperar o pod ficar pronto
kubectl wait --for=condition=ready pod/postgres-test -n bridal-crm --timeout=60s

# Testar conectividade
kubectl exec -it postgres-test -n bridal-crm -- pg_isready

# Conectar ao psql
kubectl exec -it postgres-test -n bridal-crm -- psql

# Listar bancos de dados
kubectl exec -it postgres-test -n bridal-crm -- psql -c "\l"

# Limpar o pod de teste
kubectl delete pod postgres-test -n bridal-crm
```

#### Teste 2: Port Forward (para acessar localmente)

```bash
# Em um terminal, criar port-forward
kubectl port-forward -n bridal-crm svc/postgresql-dev-bridal-cover-crm-postgresql 5432:5432

# Em outro terminal, conectar
psql -h localhost -p 5432 -U postgres -d bridal_cover_crm_dev
# Senha: postgres (em dev)
```

#### Teste 3: NodePort (acesso externo em dev)

```bash
# Pegar o IP do node
kubectl get nodes -o wide
# Anote o INTERNAL-IP ou EXTERNAL-IP

# Conectar de fora do cluster
psql -h <NODE_IP> -p 30432 -U postgres -d bridal_cover_crm_dev
```

## 🔧 Comandos Úteis (via Makefile)

```bash
# Ver todos os comandos disponíveis
make help

# Status e monitoramento
make status-dev          # Ver status do release
make get-pods-dev        # Listar pods
make logs-dev            # Ver logs em tempo real

# Conectar ao banco
make psql-dev           # Conectar ao psql
make port-forward-dev   # Criar port-forward
make shell-dev          # Abrir shell no container

# Gerenciamento
make upgrade-dev        # Atualizar o release
make uninstall-dev      # Desinstalar (mantém PVC)
make reinstall-dev      # Reinstalar do zero

# Backup
make backup-dev         # Criar backup do banco
make list-backups       # Listar backups

# Validação
make lint               # Validar templates
make template-dev       # Ver manifestos renderizados
make dry-run-dev        # Simular instalação
```

## 📊 Bancos de Dados Criados

Após a instalação, os seguintes bancos estarão disponíveis:

| Banco de Dados | Descrição |
|----------------|-----------|
| `bridal_cover_crm` | Banco principal |
| `bridal_cover_crm_dev` | Desenvolvimento |
| `bridal_cover_crm_test` | Testes |
| `bridal_cover_crm_prod` | Produção |

### Usuários criados:

| Usuário | Senha (dev) | Permissões |
|---------|-------------|------------|
| `postgres` | `postgres` | Superuser |
| `bridal_user` | `bridal_pass` | Acesso a todos os bancos |

## 🔗 Configurar Backend para Conectar

### Service Name do PostgreSQL

O nome do service que o backend deve usar para conectar:

```
# Desenvolvimento
postgresql-dev-bridal-cover-crm-postgresql.bridal-crm.svc.cluster.local:5432

# Ou simplesmente (se estiver no mesmo namespace)
postgresql-dev-bridal-cover-crm-postgresql:5432
```

### Exemplo de configuração Spring Boot

No `src/main/resources/application-dev.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgresql-dev-bridal-cover-crm-postgresql:5432/bridal_cover_crm_dev
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: true
```

Ou no deployment Kubernetes, usando variáveis de ambiente:

```yaml
env:
- name: SPRING_DATASOURCE_URL
  value: jdbc:postgresql://postgresql-dev-bridal-cover-crm-postgresql:5432/bridal_cover_crm_dev
- name: SPRING_DATASOURCE_USERNAME
  valueFrom:
    secretKeyRef:
      name: postgresql-dev-bridal-cover-crm-postgresql
      key: POSTGRES_USER
- name: SPRING_DATASOURCE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: postgresql-dev-bridal-cover-crm-postgresql
      key: POSTGRES_PASSWORD
```

Arquivo completo de exemplo: `examples/backend-connection.yaml`

## 🔄 Atualizar o PostgreSQL

Se precisar mudar alguma configuração:

```bash
# 1. Editar o arquivo de valores
nano values-dev.yaml

# 2. Aplicar as mudanças
make upgrade-dev

# OU
helm upgrade postgresql-dev . \
  --namespace bridal-crm \
  --values values-dev.yaml
```

## 💾 Backup e Restore

### Criar Backup

```bash
# Usando Makefile
make backup-dev

# O backup será salvo em: backups/backup-dev-YYYYMMDD-HHMMSS.sql
```

### Restaurar Backup

```bash
# Restaurar um backup específico
cat backups/backup-dev-20241129-150000.sql | \
  kubectl exec -i -n bridal-crm \
  $(kubectl get pods -n bridal-crm -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}') \
  -- psql -U postgres -d bridal_cover_crm_dev
```

## 🗑️ Desinstalar

### Remover apenas o deployment (mantém os dados)

```bash
make uninstall-dev

# OU
helm uninstall postgresql-dev -n bridal-crm
```

### Remover tudo incluindo os dados (⚠️ CUIDADO!)

```bash
# Isso remove o deployment E os dados permanentemente!
make clean-all-dev

# OU manualmente
helm uninstall postgresql-dev -n bridal-crm
kubectl delete pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data
```

## 🐛 Troubleshooting

### Pod não está iniciando

```bash
# Ver detalhes do pod
kubectl describe pod -n bridal-crm $(kubectl get pods -n bridal-crm -l app.kubernetes.io/name=bridal-cover-crm-postgresql -o jsonpath='{.items[0].metadata.name}')

# Ver logs
make logs-dev

# Ver eventos
kubectl get events -n bridal-crm --sort-by='.lastTimestamp'
```

### Erro de persistência (PVC)

```bash
# Ver status do PVC
kubectl get pvc -n bridal-crm

# Ver detalhes
kubectl describe pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data

# Ver se há PV disponível
kubectl get pv
```

### Erro de conexão

```bash
# Testar de dentro do cluster
kubectl run -it --rm test --image=postgres:15-alpine --restart=Never -n bridal-crm -- \
  psql -h postgresql-dev-bridal-cover-crm-postgresql -U postgres -d bridal_cover_crm_dev
```

### Resetar tudo

```bash
# Desinstalar
make uninstall-dev

# Deletar PVC
kubectl delete pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data

# Reinstalar
make install-dev
```

## 📈 Próximos Passos

1. ✅ PostgreSQL instalado e funcionando
2. 🔄 Configurar backend Spring Boot para conectar
3. 🔄 Testar a aplicação completa
4. 🔄 Configurar backups automáticos
5. 🔄 Configurar monitoramento
6. 🔄 Fazer deploy em staging/produção

## 📚 Documentação Adicional

- **README.md** - Documentação completa do chart
- **QUICK-START.md** - Guia rápido de início

## 💡 Dicas

1. **Sempre verifique os logs** se algo não funcionar: `make logs-dev`
2. **Use dry-run** para testar antes de aplicar: `make dry-run-dev`
3. **Faça backup** antes de upgrades importantes: `make backup-dev`
4. **Em produção**, sempre use senhas fortes e diferentes
5. **Monitore os recursos** do pod: `kubectl top pods -n bridal-crm`

## ❓ Perguntas Frequentes

**P: Como sei se o PostgreSQL está funcionando?**
```bash
kubectl get pods -n bridal-crm
# Deve mostrar: 1/1 Running
```

**P: Como conecto meu backend ao PostgreSQL?**
```bash
# Use o service name:
postgresql-dev-bridal-cover-crm-postgresql:5432
# Veja: examples/backend-connection.yaml
```

**P: Os dados são persistentes?**
```bash
# Sim! Use este comando para ver o PVC:
kubectl get pvc -n bridal-crm
```

**P: Como faço backup?**
```bash
make backup-dev
```

**P: Como mudo a senha do PostgreSQL?**
```bash
# Edite values-dev.yaml e depois:
make upgrade-dev
```

## 🎉 Pronto!

Seu PostgreSQL está pronto para uso! O Helm chart já está completamente configurado e você pode começar a usar assim que estiver no servidor Kubernetes.

Boa sorte com o deployment! 🚀

