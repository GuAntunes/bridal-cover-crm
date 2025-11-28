# Conectar kubectl a Clusters Remotos

Este guia explica como configurar o kubectl para executar comandos locais contra clusters Kubernetes remotos, permitindo gerenciar múltiplos clusters a partir da sua máquina local.

## 📋 Índice

- [Conceitos Fundamentais](#conceitos-fundamentais)
- [Kubeconfig e Contexts](#kubeconfig-e-contexts)
- [Obtendo Credenciais de Acesso](#obtendo-credenciais-de-acesso)
- [Configuração Manual](#configuração-manual)
- [Gerenciando Múltiplos Clusters](#gerenciando-múltiplos-clusters)
- [Verificação e Troubleshooting](#verificação-e-troubleshooting)
- [Segurança e Boas Práticas](#segurança-e-boas-práticas)

---

## Conceitos Fundamentais

### O que é Kubeconfig?

O arquivo kubeconfig é um arquivo YAML que contém:
- **Clusters**: Informações sobre os clusters Kubernetes (endereço da API, certificados)
- **Users**: Credenciais de autenticação (certificados, tokens, etc.)
- **Contexts**: Combinações de cluster + user + namespace
- **Current-context**: O contexto ativo no momento

### Localização Padrão

```bash
# Localização padrão do kubeconfig
~/.kube/config

# Verificar qual kubeconfig está sendo usado
echo $KUBECONFIG

# Usar kubeconfig alternativo
export KUBECONFIG=/path/to/custom/config
```

---

## Kubeconfig e Contexts

### Estrutura do Kubeconfig

```yaml
apiVersion: v1
kind: Config
current-context: production-cluster

clusters:
- cluster:
    certificate-authority-data: LS0tLS...
    server: https://api.production.example.com:6443
  name: production-cluster

- cluster:
    certificate-authority-data: LS0tLS...
    server: https://api.staging.example.com:6443
  name: staging-cluster

users:
- name: production-admin
  user:
    client-certificate-data: LS0tLS...
    client-key-data: LS0tLS...

- name: staging-admin
  user:
    token: eyJhbGciOiJSUzI1NiIsImtpZCI6...

contexts:
- context:
    cluster: production-cluster
    namespace: bridal-cover
    user: production-admin
  name: production

- context:
    cluster: staging-cluster
    namespace: bridal-cover
    user: staging-admin
  name: staging
```

### Comandos Básicos de Context

```bash
# Listar todos os contexts configurados
kubectl config get-contexts

# Ver o context atual
kubectl config current-context

# Mudar para outro context
kubectl config use-context staging

# Ver a configuração completa
kubectl config view

# Ver configuração sem dados sensíveis mascarados
kubectl config view --raw
```

---

## Obtendo Credenciais de Acesso

### 1. Cloud Providers

#### AWS EKS

```bash
# Instalar AWS CLI
brew install awscli  # macOS
# ou
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Configurar credenciais AWS
aws configure

# Obter credenciais do cluster EKS
aws eks update-kubeconfig --name cluster-name --region us-east-1

# Com profile específico
aws eks update-kubeconfig --name cluster-name --region us-east-1 --profile production

# Com role ARN
aws eks update-kubeconfig --name cluster-name --region us-east-1 --role-arn arn:aws:iam::123456789:role/eks-admin
```

#### Google GKE

```bash
# Instalar gcloud CLI
brew install google-cloud-sdk  # macOS
# ou
curl https://sdk.cloud.google.com | bash

# Autenticar
gcloud auth login

# Listar clusters
gcloud container clusters list

# Obter credenciais
gcloud container clusters get-credentials cluster-name --zone us-central1-a --project project-id

# Com região ao invés de zona
gcloud container clusters get-credentials cluster-name --region us-central1 --project project-id
```

#### Azure AKS

```bash
# Instalar Azure CLI
brew install azure-cli  # macOS
# ou
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

# Autenticar
az login

# Listar clusters
az aks list

# Obter credenciais
az aks get-credentials --resource-group myResourceGroup --name cluster-name

# Com usuário admin
az aks get-credentials --resource-group myResourceGroup --name cluster-name --admin

# Sobrescrever context existente
az aks get-credentials --resource-group myResourceGroup --name cluster-name --overwrite-existing
```

#### DigitalOcean Kubernetes

```bash
# Instalar doctl
brew install doctl  # macOS
# ou
cd ~
wget https://github.com/digitalocean/doctl/releases/download/v1.94.0/doctl-1.94.0-linux-amd64.tar.gz
tar xf ~/doctl-1.94.0-linux-amd64.tar.gz
sudo mv ~/doctl /usr/local/bin

# Autenticar
doctl auth init

# Listar clusters
doctl kubernetes cluster list

# Obter credenciais
doctl kubernetes cluster kubeconfig save cluster-name
```

### 2. Cluster On-Premise ou Self-Managed

#### Método 1: Copiar kubeconfig do Master Node

```bash
# No servidor master do cluster
sudo cat /etc/kubernetes/admin.conf

# Na sua máquina local, copiar o conteúdo para
mkdir -p ~/.kube
nano ~/.kube/config
# Cole o conteúdo e salve

# Ou usar scp diretamente
scp user@master-node:/etc/kubernetes/admin.conf ~/.kube/config-remote

# Mesclar com config existente
export KUBECONFIG=~/.kube/config:~/.kube/config-remote
kubectl config view --flatten > ~/.kube/config.new
mv ~/.kube/config.new ~/.kube/config
```

#### Método 2: Criar Service Account com RBAC

No cluster remoto:

```bash
# Criar namespace (se necessário)
kubectl create namespace remote-access

# Criar service account
kubectl create serviceaccount remote-admin -n remote-access

# Criar ClusterRoleBinding
kubectl create clusterrolebinding remote-admin-binding \
  --clusterrole=cluster-admin \
  --serviceaccount=remote-access:remote-admin

# Para Kubernetes 1.24+, criar token manualmente
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: remote-admin-token
  namespace: remote-access
  annotations:
    kubernetes.io/service-account.name: remote-admin
type: kubernetes.io/service-account-token
EOF

# Obter o token
kubectl get secret remote-admin-token -n remote-access -o jsonpath='{.data.token}' | base64 -d

# Obter o CA certificate
kubectl get secret remote-admin-token -n remote-access -o jsonpath='{.data.ca\.crt}'
```

Na sua máquina local, use esses dados para configurar o kubeconfig (veja seção de Configuração Manual).

### 3. Rancher

```bash
# No Rancher UI:
# 1. Vá para o cluster desejado
# 2. Clique em "Kubeconfig File" no canto superior direito
# 3. Copie o conteúdo

# Cole no seu kubeconfig local
nano ~/.kube/config
```

### 4. K3s

```bash
# No servidor K3s
sudo cat /etc/rancher/k3s/k3s.yaml

# Copiar para máquina local e editar
# Trocar 'server: https://127.0.0.1:6443' pelo IP público do servidor
scp user@k3s-server:/etc/rancher/k3s/k3s.yaml ~/.kube/k3s-config

# Editar e mudar o server
sed -i 's/127.0.0.1/<IP-DO-SERVIDOR>/g' ~/.kube/k3s-config

# Adicionar ao KUBECONFIG
export KUBECONFIG=~/.kube/config:~/.kube/k3s-config
kubectl config view --flatten > ~/.kube/config.new
mv ~/.kube/config.new ~/.kube/config
```

---

## Configuração Manual

### Adicionar um Cluster Manualmente

```bash
# 1. Adicionar informações do cluster
kubectl config set-cluster production-cluster \
  --server=https://api.production.example.com:6443 \
  --certificate-authority=/path/to/ca.crt

# Ou com certificado embarcado (base64)
kubectl config set-cluster production-cluster \
  --server=https://api.production.example.com:6443 \
  --certificate-authority-data=$(cat /path/to/ca.crt | base64 -w 0)

# Ou sem validação de certificado (NÃO RECOMENDADO para produção)
kubectl config set-cluster production-cluster \
  --server=https://api.production.example.com:6443 \
  --insecure-skip-tls-verify=true
```

### Adicionar Credenciais de Usuário

```bash
# Com certificado cliente
kubectl config set-credentials production-admin \
  --client-certificate=/path/to/client.crt \
  --client-key=/path/to/client.key

# Com token
kubectl config set-credentials production-admin \
  --token=eyJhbGciOiJSUzI1NiIsImtpZCI6...

# Com usuário e senha (basic auth - deprecated)
kubectl config set-credentials production-admin \
  --username=admin \
  --password=password123
```

### Criar um Context

```bash
# Criar context combinando cluster + user + namespace
kubectl config set-context production \
  --cluster=production-cluster \
  --user=production-admin \
  --namespace=bridal-cover

# Usar o novo context
kubectl config use-context production
```

### Exemplo Completo de Configuração Manual

```bash
#!/bin/bash

CLUSTER_NAME="production"
API_SERVER="https://192.168.1.100:6443"
CA_CERT="/path/to/ca.crt"
CLIENT_CERT="/path/to/client.crt"
CLIENT_KEY="/path/to/client.key"
NAMESPACE="bridal-cover"

# Adicionar cluster
kubectl config set-cluster ${CLUSTER_NAME}-cluster \
  --server=${API_SERVER} \
  --certificate-authority=${CA_CERT} \
  --embed-certs=true

# Adicionar usuário
kubectl config set-credentials ${CLUSTER_NAME}-admin \
  --client-certificate=${CLIENT_CERT} \
  --client-key=${CLIENT_KEY} \
  --embed-certs=true

# Criar context
kubectl config set-context ${CLUSTER_NAME} \
  --cluster=${CLUSTER_NAME}-cluster \
  --user=${CLUSTER_NAME}-admin \
  --namespace=${NAMESPACE}

# Usar o context
kubectl config use-context ${CLUSTER_NAME}

echo "✅ Context '${CLUSTER_NAME}' configurado e ativado!"
```

---

## Gerenciando Múltiplos Clusters

### Visualizar e Alternar Contexts

```bash
# Listar todos os contexts com detalhes
kubectl config get-contexts

# Output exemplo:
# CURRENT   NAME         CLUSTER            AUTHINFO         NAMESPACE
# *         production   production-cluster production-admin bridal-cover
#           staging      staging-cluster    staging-admin    bridal-cover
#           local        minikube           minikube         default

# Alternar para outro context
kubectl config use-context staging

# Alternar e executar comando em um único passo
kubectl --context=production get pods
kubectl --context=staging get pods

# Ver qual context está ativo
kubectl config current-context
```

### Renomear Context

```bash
# Renomear um context
kubectl config rename-context old-name new-name

# Exemplo
kubectl config rename-context gke_project_us-central1-a_cluster gke-production
```

### Deletar Context, Cluster ou User

```bash
# Deletar um context
kubectl config delete-context staging

# Deletar um cluster
kubectl config delete-cluster staging-cluster

# Deletar um usuário
kubectl config delete-user staging-admin
```

### Alterar Namespace Padrão de um Context

```bash
# Alterar namespace do context atual
kubectl config set-context --current --namespace=kube-system

# Alterar namespace de um context específico
kubectl config set-context production --namespace=bridal-cover
```

### Usar Múltiplos Arquivos Kubeconfig

```bash
# Mesclar múltiplos arquivos kubeconfig
export KUBECONFIG=~/.kube/config:~/.kube/config-aws:~/.kube/config-gcp

# Ver configuração mesclada
kubectl config view

# Consolidar em um único arquivo
kubectl config view --flatten > ~/.kube/config.merged
mv ~/.kube/config.merged ~/.kube/config
unset KUBECONFIG
```

### Plugins Úteis para Gerenciar Contexts

#### kubectx e kubens

```bash
# Instalar kubectx e kubens
brew install kubectx  # macOS

# Linux
sudo git clone https://github.com/ahmetb/kubectx /opt/kubectx
sudo ln -s /opt/kubectx/kubectx /usr/local/bin/kubectx
sudo ln -s /opt/kubectx/kubens /usr/local/bin/kubens

# Listar e alternar contexts
kubectx                    # Listar contexts
kubectx production         # Mudar para production
kubectx -                  # Voltar para context anterior

# Listar e alternar namespaces
kubens                     # Listar namespaces
kubens kube-system         # Mudar para kube-system
kubens -                   # Voltar para namespace anterior
```

#### kube-ps1 (Mostrar context no prompt)

```bash
# Instalar kube-ps1
brew install kube-ps1  # macOS

# Linux
git clone https://github.com/jonmosco/kube-ps1.git ~/.kube-ps1

# Adicionar ao ~/.zshrc ou ~/.bashrc
source ~/.kube-ps1/kube-ps1.sh
PS1='[\u@\h \W $(kube_ps1)]\$ '

# Reload do shell
source ~/.zshrc  # ou source ~/.bashrc

# Seu prompt agora mostra: [user@host dir (context|namespace)]$
```

---

## Verificação e Troubleshooting

### Verificar Conectividade

```bash
# Testar conexão com o cluster
kubectl cluster-info

# Verificar nodes
kubectl get nodes

# Verificar se API está acessível
kubectl version

# Testar com verbosidade
kubectl get pods --v=8

# Verificar certificados
kubectl config view --raw

# Testar com curl
kubectl proxy &
curl http://localhost:8001/api/v1/namespaces/default/pods
```

### Problemas Comuns e Soluções

#### 1. Unable to connect to the server

```bash
# Erro: Unable to connect to the server: dial tcp: lookup api.cluster.com: no such host

# Verificações:
# 1. Conferir se o server URL está correto
kubectl config view | grep server

# 2. Testar conectividade de rede
ping api.cluster.com
telnet api.cluster.com 6443
nc -zv api.cluster.com 6443

# 3. Verificar DNS
nslookup api.cluster.com

# 4. Verificar firewall/security groups
# Certifique-se que a porta 6443 está liberada
```

#### 2. x509: certificate signed by unknown authority

```bash
# Erro indica problema com certificado CA

# Solução 1: Obter o certificado CA correto
# No servidor master:
cat /etc/kubernetes/pki/ca.crt

# Atualizar no kubeconfig
kubectl config set-cluster cluster-name \
  --certificate-authority=/path/to/correct/ca.crt \
  --embed-certs=true

# Solução 2: Temporariamente desabilitar validação (DEV ONLY!)
kubectl config set-cluster cluster-name \
  --insecure-skip-tls-verify=true
```

#### 3. Forbidden: User cannot list resource

```bash
# Erro indica falta de permissões RBAC

# Verificar permissões do usuário atual
kubectl auth can-i get pods
kubectl auth can-i create deployments
kubectl auth can-i '*' '*' --all-namespaces

# Ver quais recursos você pode acessar
kubectl auth can-i --list

# Administrador deve conceder permissões adequadas via RoleBinding/ClusterRoleBinding
```

#### 4. The connection was refused

```bash
# Erro: The connection to the server <IP>:6443 was refused

# Possíveis causas:
# 1. API server não está rodando
ssh user@master-node
sudo systemctl status kubelet
sudo systemctl status docker

# 2. Firewall bloqueando
sudo ufw status
sudo iptables -L -n | grep 6443

# 3. IP/Porta errados no kubeconfig
kubectl config view | grep server
```

#### 5. Context não muda

```bash
# Limpar cache do kubectl
rm -rf ~/.kube/cache
rm -rf ~/.kube/http-cache

# Verificar se não há KUBECONFIG sobrescrevendo
echo $KUBECONFIG
unset KUBECONFIG

# Verificar permissões do arquivo
chmod 600 ~/.kube/config
```

### Debug Avançado

```bash
# Aumentar nível de verbosidade (0-10)
kubectl get pods --v=6   # Mostra headers HTTP
kubectl get pods --v=8   # Mostra request/response
kubectl get pods --v=10  # Dump completo

# Ver raw API requests
kubectl get pods --raw /api/v1/namespaces/default/pods | jq

# Validar kubeconfig
kubectl config view --validate

# Testar autenticação
kubectl auth whoami
```

---

## Segurança e Boas Práticas

### 1. Proteger o Kubeconfig

```bash
# Permissões restritas
chmod 600 ~/.kube/config

# Nunca commitar kubeconfig no git
echo '.kube/' >> ~/.gitignore

# Usar secrets manager
# AWS Secrets Manager, HashiCorp Vault, etc.

# Rotacionar credenciais regularmente
# Especialmente tokens e service accounts
```

### 2. Princípio do Menor Privilégio

```bash
# NÃO usar cluster-admin para tudo
# Criar roles específicas com permissões mínimas

# Exemplo: Role apenas para leitura
kubectl create role pod-reader \
  --verb=get,list,watch \
  --resource=pods

kubectl create rolebinding dev-pod-reader \
  --role=pod-reader \
  --user=developer@example.com \
  --namespace=development
```

### 3. Usar Namespaces

```bash
# Isolar ambientes e equipes
kubectl create namespace team-a
kubectl create namespace team-b

# Configurar contexts com namespace
kubectl config set-context team-a \
  --cluster=production-cluster \
  --user=team-a-user \
  --namespace=team-a
```

### 4. Auditoria e Logging

```bash
# Habilitar audit logging no cluster
# Adicionar ao kube-apiserver:
--audit-log-path=/var/log/kubernetes/audit.log
--audit-policy-file=/etc/kubernetes/audit-policy.yaml

# Revisar ações regularmente
kubectl get events --all-namespaces
```

### 5. Expiração de Tokens

```bash
# Para tokens de service account, definir TTL
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: temp-access-token
  namespace: default
  annotations:
    kubernetes.io/service-account.name: temp-user
    kubernetes.io/service-account.token-ttl: "3600"
type: kubernetes.io/service-account-token
EOF
```

### 6. Contexts Seguros

```bash
# Marcar contexts de produção claramente
kubectl config rename-context prod-cluster "🔴 PRODUCTION"

# Usar aliases com confirmação
alias kubectl-prod='kubectl --context=production'

# Adicionar prompt de confirmação
kubectl-prod() {
    echo "⚠️  Executando em PRODUÇÃO. Confirmar? (y/n)"
    read confirm
    if [ "$confirm" = "y" ]; then
        kubectl --context=production "$@"
    fi
}
```

### 7. Network Security

```bash
# Usar VPN para acessar clusters privados
# Configurar bastion host/jump server

# Exemplo com SSH tunnel
ssh -L 6443:localhost:6443 user@bastion-host

# No kubeconfig, apontar para localhost
kubectl config set-cluster private-cluster \
  --server=https://localhost:6443
```

### 8. Backup do Kubeconfig

```bash
# Fazer backup regular
cp ~/.kube/config ~/.kube/config.backup.$(date +%Y%m%d)

# Ou usar git (em repositório privado)
cd ~/.kube
git init
git add config
git commit -m "Backup kubeconfig $(date)"
git remote add origin git@private-repo.com:configs/kube.git
git push
```

### 9. Certificados com Data de Validade

```bash
# Verificar validade dos certificados
openssl x509 -in /path/to/client.crt -text -noout | grep "Not After"

# Renovar certificados antes de expirar
# No cluster com kubeadm:
sudo kubeadm certs renew all

# Atualizar kubeconfig com novos certs
sudo kubeadm init phase kubeconfig admin
cp /etc/kubernetes/admin.conf ~/.kube/config
```

### 10. Monitorar Acesso

```bash
# Instalar e usar ferramentas de monitoramento
# - Falco (runtime security)
# - OPA/Gatekeeper (policy enforcement)
# - Kube-bench (CIS benchmark)

# Exemplo com kubectl audit
kubectl get events --all-namespaces --sort-by='.lastTimestamp' | grep -i "forbidden\|unauthorized"
```

---

## Checklist de Configuração

Ao configurar acesso a um novo cluster remoto, siga este checklist:

- [ ] Obter credenciais do cluster (kubeconfig, certificados, token)
- [ ] Adicionar cluster ao kubeconfig local
- [ ] Testar conectividade: `kubectl cluster-info`
- [ ] Verificar permissões: `kubectl auth can-i --list`
- [ ] Configurar namespace padrão
- [ ] Renomear context para nome descritivo
- [ ] Definir permissões mínimas necessárias
- [ ] Proteger arquivo kubeconfig (chmod 600)
- [ ] Fazer backup do kubeconfig
- [ ] Documentar credenciais em local seguro
- [ ] Configurar rotação de credenciais (se aplicável)
- [ ] Testar em namespace não-produtivo primeiro

---

## Scripts Úteis

### Script para Adicionar Novo Cluster

```bash
#!/bin/bash
# add-cluster.sh - Script para adicionar novo cluster ao kubeconfig

set -e

read -p "Nome do cluster: " CLUSTER_NAME
read -p "URL da API (ex: https://api.cluster.com:6443): " API_SERVER
read -p "Caminho para CA cert: " CA_CERT
read -p "Caminho para client cert: " CLIENT_CERT
read -p "Caminho para client key: " CLIENT_KEY
read -p "Namespace padrão [default]: " NAMESPACE
NAMESPACE=${NAMESPACE:-default}

echo "➡️  Adicionando cluster ${CLUSTER_NAME}..."

kubectl config set-cluster ${CLUSTER_NAME} \
  --server=${API_SERVER} \
  --certificate-authority=${CA_CERT} \
  --embed-certs=true

kubectl config set-credentials ${CLUSTER_NAME}-admin \
  --client-certificate=${CLIENT_CERT} \
  --client-key=${CLIENT_KEY} \
  --embed-certs=true

kubectl config set-context ${CLUSTER_NAME} \
  --cluster=${CLUSTER_NAME} \
  --user=${CLUSTER_NAME}-admin \
  --namespace=${NAMESPACE}

echo "✅ Cluster ${CLUSTER_NAME} adicionado com sucesso!"
echo ""
echo "Para usar: kubectl config use-context ${CLUSTER_NAME}"
echo "Para testar: kubectl --context=${CLUSTER_NAME} cluster-info"
```

### Script para Validar Acesso a Todos os Clusters

```bash
#!/bin/bash
# validate-clusters.sh - Valida acesso a todos os clusters configurados

echo "🔍 Validando acesso aos clusters..."
echo ""

# Obter todos os contexts
CONTEXTS=$(kubectl config get-contexts -o name)

for context in $CONTEXTS; do
    echo "📍 Testing context: $context"
    
    if kubectl --context=$context cluster-info &> /dev/null; then
        echo "  ✅ OK - Cluster acessível"
        
        # Tentar listar nodes
        NODE_COUNT=$(kubectl --context=$context get nodes --no-headers 2>/dev/null | wc -l)
        echo "  📊 Nodes: $NODE_COUNT"
        
        # Verificar versão
        VERSION=$(kubectl --context=$context version --short 2>/dev/null | grep Server || echo "N/A")
        echo "  🔖 $VERSION"
    else
        echo "  ❌ ERRO - Não foi possível conectar"
    fi
    echo ""
done

echo "✅ Validação concluída!"
```

---

## Referências

### Documentação Oficial
- [Organizing Cluster Access Using kubeconfig Files](https://kubernetes.io/docs/concepts/configuration/organize-cluster-access-kubeconfig/)
- [Configure Access to Multiple Clusters](https://kubernetes.io/docs/tasks/access-application-cluster/configure-access-multiple-clusters/)
- [Kubectl Config Commands](https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands#config)

### Ferramentas Úteis
- [kubectx/kubens](https://github.com/ahmetb/kubectx) - Alternador rápido de contexts e namespaces
- [kube-ps1](https://github.com/jonmosco/kube-ps1) - Mostrar context/namespace no prompt
- [k9s](https://k9scli.io/) - Terminal UI para Kubernetes
- [lens](https://k8slens.dev/) - IDE para Kubernetes
- [kubeconfig-merge](https://github.com/kubeconfig/kubeconfig-merge) - Mesclar múltiplos kubeconfigs

### Cloud Provider CLIs
- [AWS CLI](https://aws.amazon.com/cli/)
- [gcloud CLI](https://cloud.google.com/sdk/gcloud)
- [Azure CLI](https://docs.microsoft.com/cli/azure/)
- [doctl (DigitalOcean)](https://docs.digitalocean.com/reference/doctl/)

---

**Próximos passos:**
- ← Voltar para [Helm e Tiller Guide](15-helm-tiller-guide.md)
- → Ver [Índice completo](README.md)


