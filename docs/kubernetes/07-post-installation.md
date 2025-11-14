# Configuração Pós-Instalação

Após instalar o Kubernetes, estas configurações melhoram a experiência de uso e adicionam funcionalidades importantes.

## 1. Configurar Autocompletion para kubectl

O autocompletion facilita muito o uso do kubectl, completando comandos e nomes de recursos.

### Linux (bash)

```bash
# Adicionar ao .bashrc
echo 'source <(kubectl completion bash)' >>~/.bashrc
echo 'alias k=kubectl' >>~/.bashrc
echo 'complete -o default -F __start_kubectl k' >>~/.bashrc
source ~/.bashrc
```

### macOS (bash)

```bash
# Instalar bash-completion
brew install bash-completion@2

# Adicionar ao .bash_profile
echo 'source <(kubectl completion bash)' >>~/.bash_profile
echo 'alias k=kubectl' >>~/.bash_profile
echo 'complete -o default -F __start_kubectl k' >>~/.bash_profile
source ~/.bash_profile
```

### macOS (zsh)

```bash
# Adicionar ao .zshrc
echo 'source <(kubectl completion zsh)' >>~/.zshrc
echo 'alias k=kubectl' >>~/.zshrc
echo 'compdef __start_kubectl k' >>~/.zshrc
source ~/.zshrc
```

### Windows (PowerShell)

```powershell
# Adicionar ao perfil do PowerShell
kubectl completion powershell | Out-String | Invoke-Expression

# Tornar permanente
kubectl completion powershell >> $PROFILE
```

### Testar

```bash
# Agora você pode usar Tab para completar
k get po<TAB>
k get pods -n kube-<TAB>
```

---

## 2. Instalar Helm (Gerenciador de Pacotes)

Helm é o gerenciador de pacotes para Kubernetes, facilitando instalação de aplicações complexas.

### Linux

```bash
# Instalar Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Verificar instalação
helm version
```

### macOS

```bash
# Instalar via Homebrew
brew install helm

# Verificar instalação
helm version
```

### Windows

```powershell
# Via Chocolatey
choco install kubernetes-helm

# Via Scoop
scoop install helm

# Verificar instalação
helm version
```

### Configurar Helm

```bash
# Adicionar repositórios populares
helm repo add stable https://charts.helm.sh/stable
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx

# Atualizar repositórios
helm repo update

# Listar repositórios
helm repo list

# Buscar charts
helm search repo nginx
```

---

## 3. Instalar Ingress Controller (NGINX)

Ingress Controller permite expor serviços HTTP/HTTPS com roteamento avançado.

### Usando kubectl

```bash
# Instalar NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# Para ambientes bare-metal (sem LoadBalancer)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/baremetal/deploy.yaml

# Verificar instalação
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
```

### Usando Helm (Recomendado)

```bash
# Instalar via Helm
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace

# Verificar instalação
helm list -n ingress-nginx
kubectl get all -n ingress-nginx
```

### Aguardar Controller Estar Pronto

```bash
# Aguardar pods estarem rodando
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
```

### Testar Ingress

Crie um exemplo de teste:

```yaml
# test-ingress.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hello-world
spec:
  replicas: 2
  selector:
    matchLabels:
      app: hello
  template:
    metadata:
      labels:
        app: hello
    spec:
      containers:
      - name: hello
        image: hashicorp/http-echo
        args:
        - "-text=Hello from Kubernetes!"
        ports:
        - containerPort: 5678
---
apiVersion: v1
kind: Service
metadata:
  name: hello-service
spec:
  selector:
    app: hello
  ports:
  - port: 80
    targetPort: 5678
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: hello-ingress
spec:
  ingressClassName: nginx
  rules:
  - host: hello.local
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: hello-service
            port:
              number: 80
```

```bash
# Aplicar
kubectl apply -f test-ingress.yaml

# Adicionar ao /etc/hosts (Linux/macOS) ou C:\Windows\System32\drivers\etc\hosts (Windows)
# <INGRESS_IP> hello.local

# Testar
curl http://hello.local

# Limpar
kubectl delete -f test-ingress.yaml
```

---

## 4. Instalar Metrics Server

Metrics Server coleta métricas de recursos (CPU/memória) para usar com `kubectl top` e HPA.

### Instalação

```bash
# Instalar Metrics Server
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### Para Ambientes de Desenvolvimento (sem TLS)

Se você está usando Minikube ou cluster de desenvolvimento com certificados auto-assinados:

```bash
# Baixar manifest
curl -LO https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Editar para adicionar --kubelet-insecure-tls
# Na seção containers, em args, adicionar:
# - --kubelet-insecure-tls

# Ou aplicar este patch
kubectl patch deployment metrics-server -n kube-system --type='json' \
  -p='[{"op": "add", "path": "/spec/template/spec/containers/0/args/-", "value": "--kubelet-insecure-tls"}]'
```

### Verificar

```bash
# Verificar pods
kubectl get pods -n kube-system | grep metrics-server

# Testar (aguardar alguns minutos para coletar métricas)
kubectl top nodes
kubectl top pods -A
```

---

## 5. Configurar Storage Class

Storage Class permite provisionamento dinâmico de volumes.

### Cloud Providers

Cloud providers geralmente já têm Storage Classes configuradas:

```bash
# Ver Storage Classes disponíveis
kubectl get storageclass
```

### Ambiente On-Premises: Local Path Provisioner

```bash
# Instalar Local Path Provisioner
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml

# Verificar
kubectl get storageclass

# Definir como padrão (opcional)
kubectl patch storageclass local-path \
  -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

### Testar Storage

```yaml
# test-pvc.yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: test-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1Gi
---
apiVersion: v1
kind: Pod
metadata:
  name: test-pod
spec:
  containers:
  - name: test
    image: nginx
    volumeMounts:
    - name: data
      mountPath: /data
  volumes:
  - name: data
    persistentVolumeClaim:
      claimName: test-pvc
```

```bash
# Aplicar
kubectl apply -f test-pvc.yaml

# Verificar
kubectl get pvc
kubectl get pv

# Limpar
kubectl delete -f test-pvc.yaml
```

---

## 6. Instalar K9s (Terminal UI)

K9s é uma interface de terminal moderna para Kubernetes.

### Instalação

```bash
# Linux/macOS via Homebrew
brew install derailed/k9s/k9s

# Linux via script
curl -sS https://webinstall.dev/k9s | bash

# Windows via Chocolatey
choco install k9s

# Ou download de releases:
# https://github.com/derailed/k9s/releases
```

### Usar K9s

```bash
# Iniciar K9s
k9s

# Atalhos úteis:
# :pods       - Ver pods
# :svc        - Ver services
# :deploy     - Ver deployments
# :ns         - Mudar namespace
# /           - Buscar
# l           - Ver logs
# d           - Descrever
# e           - Editar
# ctrl+d      - Deletar
# :q          - Sair
```

---

## 7. Configurar Aliases Úteis

```bash
# Adicionar ao .bashrc ou .zshrc

# Alias principal
alias k='kubectl'

# Contexto e namespace
alias kx='kubectl config use-context'
alias kn='kubectl config set-context --current --namespace'

# Get
alias kg='kubectl get'
alias kgp='kubectl get pods'
alias kgs='kubectl get services'
alias kgd='kubectl get deployments'

# Describe
alias kd='kubectl describe'
alias kdp='kubectl describe pod'

# Logs
alias kl='kubectl logs'
alias klf='kubectl logs -f'

# Exec
alias ke='kubectl exec -it'

# Apply e Delete
alias ka='kubectl apply -f'
alias kdel='kubectl delete'

# Namespace
alias kgns='kubectl get namespaces'

# Contextos
alias kgc='kubectl config get-contexts'
```

---

## 8. Verificação Final

Execute estes comandos para verificar que tudo está funcionando:

```bash
# 1. Cluster está saudável
kubectl get nodes
kubectl get pods -A

# 2. Componentes do sistema
kubectl get componentstatuses

# 3. Helm está funcionando
helm version
helm repo list

# 4. Ingress Controller
kubectl get pods -n ingress-nginx

# 5. Metrics Server
kubectl top nodes

# 6. Storage Class
kubectl get storageclass

# 7. Criar recursos de teste
kubectl create deployment nginx --image=nginx
kubectl get pods
kubectl delete deployment nginx
```

---

## Próximos Passos

Agora seu cluster está totalmente configurado! Você pode:

- **[Instalar Kubernetes Dashboard](10-dashboard-setup.md)** - Interface gráfica
- **[Aprender Comandos Essenciais](08-essential-commands.md)** - Dominar kubectl
- **[Ver Boas Práticas](09-best-practices.md)** - Melhorar suas configurações

---

[← Anterior: Instalação Windows](06-installation-windows.md) | [Voltar ao Índice](README.md) | [Próximo: Comandos Essenciais →](08-essential-commands.md)

