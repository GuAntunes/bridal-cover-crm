# Instalação do Kubernetes no macOS

Como o macOS não pode rodar kubelet nativamente, existem três opções principais para executar Kubernetes.

## Comparação das Opções

| Opção | Complexidade | Recursos | Produção-like | Ideal Para |
|-------|--------------|----------|---------------|------------|
| **Minikube** | ⭐ Fácil | Baixo | ❌ | Desenvolvimento rápido |
| **Multipass** | ⭐⭐ Médio | Médio | ✅ | Aprendizado completo |
| **Vagrant + VirtualBox** | ⭐⭐⭐ Difícil | Alto | ✅ | Simulação de produção |

---

## Opção 1: Minikube (Recomendado para Desenvolvimento)

Minikube é a forma mais rápida e fácil de começar com Kubernetes no macOS.

### Passo 1: Instalar Pré-requisitos

**Instalar Docker Desktop (Recomendado)**

```bash
# Instalar Docker Desktop via Homebrew
brew install --cask docker

# Ou baixar de: https://www.docker.com/products/docker-desktop
# Iniciar Docker Desktop e aguardar estar rodando
```

**Alternativas:**

```bash
# Hyperkit (driver nativo do macOS)
brew install hyperkit

# Ou VirtualBox
brew install --cask virtualbox
```

### Passo 2: Instalar kubectl e Minikube

```bash
# Instalar kubectl
brew install kubectl

# Instalar Minikube
brew install minikube

# Verificar instalação
kubectl version --client
minikube version
```

### Passo 3: Iniciar Cluster

```bash
# Iniciar com Docker (recomendado)
minikube start --driver=docker

# Definir Docker como driver padrão
minikube config set driver docker

# Verificar cluster
minikube status
kubectl get nodes
```

### Configurações Avançadas

```bash
# Iniciar com mais recursos
minikube start --driver=docker \
  --cpus=4 \
  --memory=8192 \
  --disk-size=40g \
  --kubernetes-version=v1.28.0

# Cluster multi-node
minikube start --driver=docker --nodes=3

# Com addons
minikube start --driver=docker \
  --addons=ingress \
  --addons=metrics-server \
  --addons=dashboard
```

### Comandos Úteis do Minikube

```bash
# Gerenciamento
minikube stop         # Parar cluster
minikube start        # Iniciar cluster
minikube pause        # Pausar cluster
minikube unpause      # Retomar cluster
minikube delete       # Deletar cluster

# Informações
minikube status       # Status do cluster
minikube ip           # IP do cluster
minikube logs         # Logs do Minikube
minikube ssh          # SSH no node

# Addons
minikube addons list  # Listar addons
minikube addons enable dashboard
minikube addons enable metrics-server
minikube addons enable ingress

# Dashboard
minikube dashboard    # Abrir dashboard no navegador

# Serviços
minikube service <service-name>  # Acessar serviço
minikube tunnel      # Criar tunnel para LoadBalancers
```

### Usar Docker do Minikube

```bash
# Configurar shell para usar Docker do Minikube
eval $(minikube docker-env)

# Construir imagens diretamente no Minikube
docker build -t my-app:latest .

# Voltar ao Docker local
eval $(minikube docker-env -u)
```

### Teste Rápido

```bash
# Criar deployment de teste
kubectl create deployment hello-minikube \
  --image=kicbase/echo-server:1.0

# Expor como serviço
kubectl expose deployment hello-minikube \
  --type=NodePort \
  --port=8080

# Acessar serviço
minikube service hello-minikube

# Limpar
kubectl delete deployment hello-minikube
kubectl delete service hello-minikube
```

### Limitações do Minikube

- Single-node por padrão
- Recursos limitados à máquina local
- Networking simplificado
- Não é para produção

---

## Opção 2: Multipass + kubeadm (Recomendado para Aprendizado)

Multipass cria VMs Ubuntu rapidamente, permitindo um cluster real multi-node.

### Passo 1: Instalar Multipass

```bash
# Instalar via Homebrew
brew install multipass

# Verificar instalação
multipass version
```

### Passo 2: Criar VMs

```bash
# Criar VM para control plane
multipass launch --name k8s-control --cpus 2 --memory 2G --disk 20G

# Criar VMs para workers
multipass launch --name k8s-worker1 --cpus 2 --memory 2G --disk 20G
multipass launch --name k8s-worker2 --cpus 2 --memory 2G --disk 20G

# Listar VMs
multipass list

# Obter IPs das VMs
multipass info k8s-control
multipass info k8s-worker1
multipass info k8s-worker2
```

### Passo 3: Instalar Kubernetes nas VMs

```bash
# Conectar ao control plane
multipass shell k8s-control

# Dentro da VM, seguir passos do guia Ubuntu:
# 1. Preparação do sistema
# 2. Instalar containerd
# 3. Instalar kubeadm, kubelet, kubectl
# 4. Inicializar control plane
# 5. Instalar CNI

# Repetir passos 1-3 nos workers
multipass shell k8s-worker1
# (executar passos 1-3 do guia Ubuntu)

multipass shell k8s-worker2
# (executar passos 1-3 do guia Ubuntu)
```

### Passo 4: Configurar kubectl no macOS

```bash
# Instalar kubectl
brew install kubectl

# Copiar config do control plane
multipass exec k8s-control -- sudo cat /etc/kubernetes/admin.conf > ~/.kube/config

# Editar config para usar IP externo da VM
# Substituir 'server: https://...' pelo IP da VM k8s-control
vim ~/.kube/config

# Testar conexão
kubectl get nodes
```

### Gerenciar VMs

```bash
# Parar VMs
multipass stop k8s-control k8s-worker1 k8s-worker2

# Iniciar VMs
multipass start k8s-control k8s-worker1 k8s-worker2

# Deletar VMs
multipass delete k8s-control k8s-worker1 k8s-worker2
multipass purge
```

---

## Opção 3: Vagrant + VirtualBox (Cluster Completo)

Vagrant com VirtualBox permite criar um cluster completo com configuração reproduzível.

### Passo 1: Instalar VirtualBox e Vagrant

```bash
# Instalar via Homebrew
brew install --cask virtualbox
brew install --cask vagrant
```

### Passo 2: Criar Vagrantfile

```bash
# Criar diretório do projeto
mkdir k8s-cluster
cd k8s-cluster

# Criar Vagrantfile
cat <<'EOF' > Vagrantfile
Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/jammy64"
  
  # Control Plane
  config.vm.define "k8s-control" do |control|
    control.vm.hostname = "k8s-control"
    control.vm.network "private_network", ip: "192.168.56.10"
    control.vm.provider "virtualbox" do |vb|
      vb.memory = "2048"
      vb.cpus = 2
    end
  end
  
  # Worker 1
  config.vm.define "k8s-worker1" do |worker|
    worker.vm.hostname = "k8s-worker1"
    worker.vm.network "private_network", ip: "192.168.56.11"
    worker.vm.provider "virtualbox" do |vb|
      vb.memory = "2048"
      vb.cpus = 2
    end
  end
  
  # Worker 2
  config.vm.define "k8s-worker2" do |worker|
    worker.vm.hostname = "k8s-worker2"
    worker.vm.network "private_network", ip: "192.168.56.12"
    worker.vm.provider "virtualbox" do |vb|
      vb.memory = "2048"
      vb.cpus = 2
    end
  end
end
EOF
```

### Passo 3: Iniciar VMs

```bash
# Iniciar todas as VMs
vagrant up

# Conectar às VMs
vagrant ssh k8s-control
vagrant ssh k8s-worker1
vagrant ssh k8s-worker2
```

### Passo 4: Instalar Kubernetes

Dentro de cada VM, siga os passos do **[Guia de Instalação Ubuntu](04-installation-ubuntu.md)**.

### Passo 5: Configurar kubectl no macOS

```bash
# Instalar kubectl
brew install kubectl

# Copiar config do control plane
vagrant ssh k8s-control -c "sudo cat /etc/kubernetes/admin.conf" > ~/.kube/config

# Editar config
# Substituir IP 'server: https://...' por 192.168.56.10:6443
vim ~/.kube/config

# Testar
kubectl get nodes
```

### Gerenciar VMs Vagrant

```bash
# Status
vagrant status

# Parar VMs
vagrant halt

# Iniciar VMs
vagrant up

# Deletar VMs
vagrant destroy -f
```

---

## Comparação de Performance

### Minikube
- ✅ Início rápido (< 2 minutos)
- ✅ Baixo consumo de recursos
- ❌ Não simula produção
- ✅ Ideal para desenvolvimento

### Multipass
- ✅ VMs leves e rápidas
- ✅ Simula ambiente real
- ⚠️ Configuração manual necessária
- ✅ Bom para aprendizado

### Vagrant + VirtualBox
- ⚠️ Início mais lento (5-10 minutos)
- ❌ Maior consumo de recursos
- ✅ Configuração reproduzível
- ✅ Mais próximo de produção

---

## Qual Escolher?

### Use Minikube se:
- Você quer começar rapidamente
- Está desenvolvendo e testando apps
- Recursos da máquina são limitados
- Não precisa de multi-node

### Use Multipass se:
- Quer aprender Kubernetes completo
- Precisa de cluster multi-node
- Quer simular ambiente de produção
- Está estudando para certificações

### Use Vagrant se:
- Precisa de configuração reproduzível
- Trabalha em equipe
- Quer simular exatamente produção
- Tem recursos de hardware disponíveis

---

## Próximos Passos

Após instalar o cluster:

- **[Configuração Pós-Instalação](07-post-installation.md)**
- **[Kubernetes Dashboard](10-dashboard-setup.md)**
- **[Comandos Essenciais](08-essential-commands.md)**

---

[← Anterior: Instalação Ubuntu](04-installation-ubuntu.md) | [Voltar ao Índice](README.md) | [Próximo: Instalação Windows →](06-installation-windows.md)

