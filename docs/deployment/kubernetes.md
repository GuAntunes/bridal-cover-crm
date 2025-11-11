# Kubernetes

## Índice
1. [Introdução](#introdução)
2. [O que é Kubernetes?](#o-que-é-kubernetes)
3. [Arquitetura do Kubernetes](#arquitetura-do-kubernetes)
4. [Conceitos Fundamentais](#conceitos-fundamentais)
5. [Como Utilizar Kubernetes](#como-utilizar-kubernetes)
6. [Instalação de Cluster Kubernetes](#instalação-de-cluster-kubernetes)
   - [Ubuntu](#instalação-no-ubuntu)
   - [macOS](#instalação-no-macos)
   - [Windows](#instalação-no-windows)
7. [Configuração Pós-Instalação](#configuração-pós-instalação)
8. [Comandos Essenciais](#comandos-essenciais)
9. [Boas Práticas](#boas-práticas)
10. [Referências](#referências)

---

## Introdução

Este documento fornece uma visão abrangente sobre Kubernetes (K8s), incluindo seus conceitos fundamentais, arquitetura e instruções detalhadas para instalação de um cluster completo em diferentes sistemas operacionais.

## O que é Kubernetes?

**Kubernetes** (também conhecido como K8s) é uma plataforma open-source de orquestração de containers que automatiza a implantação, dimensionamento e gerenciamento de aplicações containerizadas. Originalmente desenvolvido pelo Google e agora mantido pela Cloud Native Computing Foundation (CNCF).

### Principais Características

- **Orquestração Automática**: Gerencia containers em múltiplos hosts
- **Auto-recuperação**: Reinicia containers que falham, substitui containers, mata containers que não respondem
- **Escalabilidade**: Escala aplicações horizontal e verticalmente
- **Balanceamento de Carga**: Distribui tráfego de rede automaticamente
- **Rollouts e Rollbacks**: Gerencia atualizações de aplicações sem downtime
- **Gerenciamento de Configuração**: Gerencia secrets e configurações de forma segura
- **Service Discovery**: Descobre serviços automaticamente usando DNS ou IP

### Benefícios

- **Portabilidade**: Funciona em qualquer cloud provider ou on-premises
- **Extensibilidade**: Altamente modular e plugável
- **Alta Disponibilidade**: Garante que aplicações estejam sempre rodando
- **Eficiência de Recursos**: Otimiza uso de recursos computacionais

---

## Arquitetura do Kubernetes

### Componentes do Control Plane (Master)

1. **kube-apiserver**: Expõe a API do Kubernetes, frontend do control plane
2. **etcd**: Armazenamento de dados key-value consistente para todos os dados do cluster
3. **kube-scheduler**: Atribui pods para nodes baseado em recursos disponíveis
4. **kube-controller-manager**: Executa processos de controller (Node Controller, Replication Controller, etc.)
5. **cloud-controller-manager**: Integração com APIs de cloud providers (opcional)

### Componentes do Node (Worker)

1. **kubelet**: Agente que garante que containers estão rodando em um pod
2. **kube-proxy**: Mantém regras de rede nos nodes
3. **Container Runtime**: Software responsável por rodar containers (containerd, CRI-O, Docker)

### Diagrama Simplificado

```
┌─────────────────────────────────────────────────────────┐
│                    CONTROL PLANE                         │
│  ┌──────────┐  ┌──────┐  ┌───────────┐  ┌────────────┐ │
│  │ API      │  │ etcd │  │ Scheduler │  │ Controller │ │
│  │ Server   │  │      │  │           │  │ Manager    │ │
│  └──────────┘  └──────┘  └───────────┘  └────────────┘ │
└─────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼───────┐  ┌────────▼────────┐  ┌──────▼────────┐
│   NODE 1      │  │    NODE 2       │  │   NODE 3      │
│               │  │                 │  │               │
│ ┌───────────┐ │  │  ┌───────────┐ │  │ ┌───────────┐ │
│ │  kubelet  │ │  │  │  kubelet  │ │  │ │  kubelet  │ │
│ ├───────────┤ │  │  ├───────────┤ │  │ ├───────────┤ │
│ │kube-proxy │ │  │  │kube-proxy │ │  │ │kube-proxy │ │
│ ├───────────┤ │  │  ├───────────┤ │  │ ├───────────┤ │
│ │ Container │ │  │  │ Container │ │  │ │ Container │ │
│ │  Runtime  │ │  │  │  Runtime  │ │  │ │  Runtime  │ │
│ └───────────┘ │  │  └───────────┘ │  │ └───────────┘ │
│               │  │                 │  │               │
│  ┌─────────┐  │  │   ┌─────────┐  │  │  ┌─────────┐  │
│  │  Pods   │  │  │   │  Pods   │  │  │  │  Pods   │  │
│  └─────────┘  │  │   └─────────┘  │  │  └─────────┘  │
└───────────────┘  └─────────────────┘  └───────────────┘
```

---

## Conceitos Fundamentais

### Pod
- Menor unidade deployável no Kubernetes
- Grupo de um ou mais containers que compartilham storage e rede
- Containers em um pod compartilham o mesmo IP e namespace

### Service

- Abstração que define um conjunto lógico de pods
- Política de acesso aos pods (load balancing)
- Fornece um endpoint estável (IP/DNS) mesmo quando pods são recriados

#### Tipos de Service

**1. ClusterIP (Padrão)**
- Expõe o serviço **apenas dentro do cluster**
- Acessível por outros pods via DNS interno (`service-name.namespace.svc.cluster.local`)
- Não é acessível de fora do cluster
- **Casos de uso:**
  - Comunicação entre microserviços
  - Bancos de dados internos (PostgreSQL, Redis)
  - APIs backend que não precisam ser públicas

**2. NodePort**
- Expõe o serviço em uma **porta estática em cada Node** do cluster
- Acessível externamente via `<NodeIP>:<NodePort>`
- Porta range: **30000-32767**
- Automaticamente cria um ClusterIP
- **Casos de uso:**
  - Ambiente de desenvolvimento e testes
  - Clusters on-premises sem LoadBalancer
  - Acesso temporário para debugging

**3. LoadBalancer**
- Cria um **load balancer externo** (suportado por cloud providers)
- Fornece um **IP público único** para o serviço
- Roteia tráfego automaticamente para os NodePorts
- Automaticamente cria NodePort e ClusterIP
- **Casos de uso:**
  - Produção em cloud (AWS, GCP, Azure)
  - Expor aplicações web publicamente
  - APIs públicas que precisam de alta disponibilidade

**4. ExternalName**
- Mapeia o serviço para um **nome DNS externo**
- Retorna um registro CNAME para o DNS especificado
- Não há proxying ou encaminhamento de portas
- Não possui selector de pods
- **Casos de uso:**
  - Integração com serviços externos ao cluster
  - Migração gradual de serviços para Kubernetes
  - Acesso a bancos de dados gerenciados (RDS, Cloud SQL)

#### Exemplo Comparativo

```yaml
# ClusterIP - Interno apenas
apiVersion: v1
kind: Service
metadata:
  name: backend-service
spec:
  type: ClusterIP
  selector:
    app: backend
  ports:
  - port: 8080
    targetPort: 8080

---
# NodePort - Acesso externo via Node
apiVersion: v1
kind: Service
metadata:
  name: backend-nodeport
spec:
  type: NodePort
  selector:
    app: backend
  ports:
  - port: 8080
    targetPort: 8080
    nodePort: 30080  # Opcional, se omitido será alocado automaticamente

---
# LoadBalancer - IP público
apiVersion: v1
kind: Service
metadata:
  name: frontend-lb
spec:
  type: LoadBalancer
  selector:
    app: frontend
  ports:
  - port: 80
    targetPort: 3000

---
# ExternalName - DNS externo
apiVersion: v1
kind: Service
metadata:
  name: external-database
spec:
  type: ExternalName
  externalName: database.external-service.com
```

### Deployment
- Fornece atualizações declarativas para pods e ReplicaSets
- Gerencia criação e scaling de pods
- Suporta rollback e rollout

### ReplicaSet
- Garante que um número específico de réplicas de pod esteja rodando
- Geralmente gerenciado por Deployments

### Namespace
- Fornece isolamento lógico de recursos
- Múltiplos ambientes virtuais dentro do mesmo cluster

### ConfigMap e Secret
- **ConfigMap**: Armazena dados de configuração não-confidenciais
- **Secret**: Armazena informações sensíveis (senhas, tokens, chaves)

### Volume
- Diretório acessível aos containers em um pod
- Persiste dados além do ciclo de vida do container

### Ingress
- Gerencia acesso externo aos serviços do cluster
- Fornece load balancing, SSL termination, name-based virtual hosting

---

## Como Utilizar Kubernetes

### 1. Definição de Recursos (Manifests YAML)

Exemplo de um Deployment simples:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  labels:
    app: nginx
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:1.14.2
        ports:
        - containerPort: 80
```

### 2. Aplicando Configurações

```bash
# Aplicar configuração
kubectl apply -f deployment.yaml

# Verificar status
kubectl get deployments
kubectl get pods

# Descrever recurso
kubectl describe deployment nginx-deployment
```

### 3. Escalando Aplicações

```bash
# Escalar manualmente
kubectl scale deployment nginx-deployment --replicas=5

# Auto-scaling
kubectl autoscale deployment nginx-deployment --min=2 --max=10 --cpu-percent=80
```

### 4. Atualizando Aplicações

```bash
# Atualizar imagem
kubectl set image deployment/nginx-deployment nginx=nginx:1.16.1

# Verificar rollout
kubectl rollout status deployment/nginx-deployment

# Rollback
kubectl rollout undo deployment/nginx-deployment
```

### 5. Expondo Aplicações

```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx-service
spec:
  selector:
    app: nginx
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
  type: LoadBalancer
```

---

## Instalação de Cluster Kubernetes

A instalação manual de um cluster Kubernetes envolve configurar o control plane e os worker nodes usando **kubeadm**, uma ferramenta oficial que facilita a criação de clusters seguindo as melhores práticas.

### Pré-requisitos Gerais

- **Mínimo 2 GB de RAM** por máquina
- **2 CPUs** ou mais no control plane
- **Conectividade de rede** entre todas as máquinas
- **Hostname, MAC address e product_uuid únicos** para cada node
- **Portas específicas abertas** no firewall
- **Swap desabilitado**

### Portas Necessárias

**Control Plane:**
- 6443: Kubernetes API server
- 2379-2380: etcd server client API
- 10250: Kubelet API
- 10259: kube-scheduler
- 10257: kube-controller-manager

**Worker Nodes:**
- 10250: Kubelet API
- 30000-32767: NodePort Services

---

## Instalação no Ubuntu

### Passo 1: Preparação do Sistema

Execute em **todos os nodes** (control plane e workers):

```bash
# Atualizar sistema
sudo apt-get update
sudo apt-get upgrade -y

# Desabilitar swap
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# Carregar módulos do kernel necessários
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

# Configurar parâmetros sysctl
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

sudo sysctl --system

# Verificar módulos
lsmod | grep br_netfilter
lsmod | grep overlay
```

### Passo 2: Instalar Container Runtime (containerd)

```bash
# Instalar dependências
sudo apt-get install -y ca-certificates curl gnupg lsb-release

# Adicionar repositório do Docker
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar containerd
sudo apt-get update
sudo apt-get install -y containerd.io

# Configurar containerd
sudo mkdir -p /etc/containerd
containerd config default | sudo tee /etc/containerd/config.toml

# Configurar cgroup driver para systemd
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml

# Reiniciar containerd
sudo systemctl restart containerd
sudo systemctl enable containerd
sudo systemctl status containerd
```

### Passo 3: Instalar kubeadm, kubelet e kubectl

```bash
# Instalar dependências
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl gpg

# Adicionar chave GPG do Kubernetes
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

# Adicionar repositório do Kubernetes
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

# Instalar kubelet, kubeadm e kubectl
sudo apt-get update
sudo apt-get install -y kubelet kubeadm kubectl

# Bloquear atualizações automáticas
sudo apt-mark hold kubelet kubeadm kubectl

# Habilitar kubelet
sudo systemctl enable kubelet
```

### Passo 4: Inicializar Control Plane

Execute **apenas no node do control plane**:

```bash
# Inicializar cluster
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=<SEU_IP>

# Após a inicialização, configure kubectl para seu usuário
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

# Verificar nodes
kubectl get nodes
```

**Importante:** Salve o comando `kubeadm join` que aparece no final da inicialização. Você precisará dele para adicionar worker nodes.
kubeadm join 192.168.15.7:6443 --token 2pzpct.vxq3069uwlehx6kx \
--discovery-token-ca-cert-hash sha256:86892c23aec7ea1f07865ba175dc0f878d0f2e7970cb7b5504b99a36314ba8f8
### Passo 5: Instalar Plugin de Rede (CNI)

Execute **no control plane**:

```bash
# Instalando Flannel (alternativa: Calico, Weave Net)
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml

# Aguarde os pods do kube-system estarem rodando
kubectl get pods -n kube-system
```

### Passo 6: Adicionar Worker Nodes

Execute **nos worker nodes**:

```bash
# Use o comando fornecido pelo kubeadm init
sudo kubeadm join <CONTROL_PLANE_IP>:6443 --token <TOKEN> \
    --discovery-token-ca-cert-hash sha256:<HASH>
```

Se perdeu o comando de join, você pode regenerá-lo no control plane:

```bash
kubeadm token create --print-join-command
```

### Passo 7: Verificar Cluster

```bash
# No control plane
kubectl get nodes
kubectl get pods --all-namespaces
```

---

## Instalação no macOS

Como o macOS não pode rodar kubelet nativamente, você precisa usar máquinas virtuais Linux ou containers para criar um cluster real.

### Opção 1: Usando Multipass (Recomendado)

Multipass permite criar VMs Ubuntu rapidamente.

#### Passo 1: Instalar Multipass

```bash
# Instalar via Homebrew
brew install multipass

# Verificar instalação
multipass version
```

#### Passo 2: Criar VMs para o Cluster

```bash
# Criar VM para control plane
multipass launch --name k8s-control --cpus 2 --memory 2G --disk 20G

# Criar VMs para workers
multipass launch --name k8s-worker1 --cpus 2 --memory 2G --disk 20G
multipass launch --name k8s-worker2 --cpus 2 --memory 2G --disk 20G

# Listar VMs
multipass list

# Obter IP das VMs
multipass info k8s-control
multipass info k8s-worker1
multipass info k8s-worker2
```

#### Passo 3: Configurar e Instalar Kubernetes nas VMs

Para cada VM, conecte e siga os passos da instalação do Ubuntu:

```bash
# Conectar ao control plane
multipass shell k8s-control

# Dentro da VM, siga todos os passos da seção "Instalação no Ubuntu"
# (Passos 1-5)
```

```bash
# Conectar aos workers
multipass shell k8s-worker1
# Siga os passos 1-3 da seção Ubuntu, depois o passo 6 (join)

multipass shell k8s-worker2
# Repita o processo
```

#### Passo 4: Configurar kubectl no macOS

```bash
# Instalar kubectl no macOS
brew install kubectl

# Copiar config do control plane
multipass exec k8s-control -- sudo cat /etc/kubernetes/admin.conf > ~/.kube/config

# Editar o config para usar o IP externo da VM
# Substitua o IP 'server: https://...' pelo IP da VM k8s-control
vim ~/.kube/config

# Testar conexão
kubectl get nodes
```

### Opção 2: Usando VirtualBox + Vagrant

#### Passo 1: Instalar VirtualBox e Vagrant

```bash
# Instalar via Homebrew
brew install --cask virtualbox
brew install --cask vagrant
```

#### Passo 2: Criar Vagrantfile

```bash
# Criar diretório do projeto
mkdir k8s-cluster
cd k8s-cluster

# Criar Vagrantfile
cat <<EOF > Vagrantfile
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

#### Passo 3: Iniciar VMs

```bash
# Iniciar todas as VMs
vagrant up

# Conectar às VMs
vagrant ssh k8s-control
vagrant ssh k8s-worker1
vagrant ssh k8s-worker2
```

#### Passo 4: Configurar Kubernetes

Dentro de cada VM, siga os passos da seção "Instalação no Ubuntu".

#### Passo 5: Configurar kubectl no macOS

```bash
# Instalar kubectl
brew install kubectl

# Copiar config do control plane
vagrant ssh k8s-control -c "sudo cat /etc/kubernetes/admin.conf" > ~/.kube/config

# Editar o config
# Substitua o IP 'server: https://...' por 192.168.56.10:6443
vim ~/.kube/config

# Testar
kubectl get nodes
```

### Opção 3: Usando Minikube (Ambiente de Desenvolvimento)

Minikube é uma ferramenta que facilita a execução de um cluster Kubernetes local de nó único, ideal para desenvolvimento e testes. Esta opção é a mais simples e rápida para começar.

#### Passo 1: Instalar Pré-requisitos

Minikube requer um driver de virtualização ou container:

**Opção A: Docker Desktop (Recomendado)**

```bash
# Instalar Docker Desktop via Homebrew
brew install --cask docker

# Ou baixar manualmente de https://www.docker.com/products/docker-desktop

# Iniciar Docker Desktop a partir de Applications
# Aguarde o Docker estar rodando (ícone na barra de menu)
```

**Opção B: Hyperkit (driver nativo do macOS)**

```bash
brew install hyperkit
```

**Opção C: VirtualBox**

```bash
brew install --cask virtualbox
```

#### Passo 2: Instalar kubectl

```bash
# Instalar kubectl
brew install kubectl

# Verificar instalação
kubectl version --client
```

#### Passo 3: Instalar Minikube

```bash
# Instalar Minikube via Homebrew
brew install minikube

# Verificar instalação
minikube version
```

#### Passo 4: Iniciar Cluster Minikube

**Com Docker (recomendado):**

```bash
# Iniciar Minikube com Docker driver
minikube start --driver=docker

# Definir Docker como driver padrão (opcional)
minikube config set driver docker
```

**Com Hyperkit:**

```bash
# Iniciar Minikube com Hyperkit driver
minikube start --driver=hyperkit

# Definir Hyperkit como driver padrão (opcional)
minikube config set driver hyperkit
```

**Com VirtualBox:**

```bash
# Iniciar Minikube com VirtualBox driver
minikube start --driver=virtualbox
```

**Opções Avançadas de Inicialização:**

```bash
# Iniciar com mais recursos
minikube start --driver=docker \
  --cpus=4 \
  --memory=8192 \
  --disk-size=40g \
  --kubernetes-version=v1.28.0

# Iniciar com múltiplos nodes (cluster multi-node)
minikube start --driver=docker --nodes=3

# Iniciar com addons específicos
minikube start --driver=docker \
  --addons=ingress \
  --addons=metrics-server \
  --addons=dashboard
```

#### Passo 5: Verificar Instalação

```bash
# Verificar status do cluster
minikube status

# Verificar nodes
kubectl get nodes

# Informações do cluster
kubectl cluster-info

# Verificar todos os pods do sistema
kubectl get pods -A
```

#### Passo 6: Habilitar Addons Úteis

Minikube vem com vários addons que podem ser habilitados facilmente:

```bash
# Listar addons disponíveis
minikube addons list

# Habilitar Dashboard
minikube addons enable dashboard

# Habilitar Metrics Server (para kubectl top)
minikube addons enable metrics-server

# Habilitar Ingress Controller
minikube addons enable ingress

# Habilitar Storage Provisioner (geralmente já habilitado)
minikube addons enable storage-provisioner

# Habilitar Registry (registry Docker local)
minikube addons enable registry
```

#### Passo 7: Acessar o Dashboard

```bash
# Abrir dashboard no navegador
minikube dashboard

# Ou obter a URL sem abrir o navegador
minikube dashboard --url
```

#### Passo 8: Configurar Acesso ao Docker do Minikube (Opcional)

Para usar o Docker daemon dentro do Minikube (útil para desenvolvimento):

```bash
# Configurar shell para usar Docker do Minikube
eval $(minikube docker-env)

# Agora você pode construir imagens diretamente no Minikube
docker build -t my-app:latest .

# Para voltar ao Docker local
eval $(minikube docker-env -u)
```

#### Comandos Úteis do Minikube

**Gerenciamento do Cluster:**

```bash
# Parar o cluster (preserva dados)
minikube stop

# Iniciar cluster existente
minikube start

# Pausar o cluster (economiza recursos)
minikube pause

# Retomar cluster pausado
minikube unpause

# Deletar o cluster
minikube delete

# Deletar todos os clusters
minikube delete --all
```

**Informações e Diagnóstico:**

```bash
# Ver logs do Minikube
minikube logs

# Ver IP do cluster
minikube ip

# SSH no node do Minikube
minikube ssh

# Ver configuração atual
minikube config view

# Verificar status dos addons
minikube addons list
```

**Networking:**

```bash
# Expor um serviço (abre no navegador)
minikube service <service-name>

# Obter URL de um serviço
minikube service <service-name> --url

# Criar tunnel para LoadBalancer services
minikube tunnel
# (Mantenha este comando rodando em um terminal separado)
```

**Gerenciamento de Recursos:**

```bash
# Ajustar recursos do cluster existente (requer restart)
minikube config set cpus 4
minikube config set memory 8192
minikube delete
minikube start

# Ver uso de recursos
minikube ssh -- df -h
minikube ssh -- free -h
```

#### Passo 9: Testar o Cluster

Vamos criar uma aplicação de exemplo para testar:

```bash
# Criar namespace de teste
kubectl create namespace test-app

# Criar um deployment
kubectl create deployment hello-minikube \
  --image=kicbase/echo-server:1.0 \
  --namespace=test-app

# Expor o deployment como serviço
kubectl expose deployment hello-minikube \
  --type=NodePort \
  --port=8080 \
  --namespace=test-app

# Acessar o serviço
minikube service hello-minikube --namespace=test-app

# Verificar pods
kubectl get pods -n test-app

# Limpar recursos de teste
kubectl delete namespace test-app
```

#### Passo 10: Configurar LoadBalancer Local (Opcional)

Para usar serviços do tipo LoadBalancer no Minikube:

```bash
# Em um terminal separado, execute:
minikube tunnel

# Este comando cria rotas de rede para serviços LoadBalancer
# Mantenha-o rodando enquanto precisar acessar LoadBalancers
# Pode solicitar senha de administrador
```

Exemplo de uso:

```yaml
# service-loadbalancer.yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: my-app
```

```bash
# Aplicar o serviço
kubectl apply -f service-loadbalancer.yaml

# Com minikube tunnel rodando, o serviço receberá um EXTERNAL-IP
kubectl get service my-service
```

#### Dicas e Melhores Práticas do Minikube

**1. Performance:**
- Use o driver Docker para melhor performance no macOS
- Aloque recursos adequados (4 CPUs, 8GB RAM é um bom ponto de partida)
- Use SSD para melhor performance de I/O

**2. Desenvolvimento:**
- Use `minikube docker-env` para construir imagens diretamente no cluster
- Habilite addons necessários desde o início
- Use `imagePullPolicy: IfNotPresent` para imagens locais

**3. Networking:**
- Use `minikube service` para acessar serviços facilmente
- Use `minikube tunnel` para LoadBalancers
- Configure `/etc/hosts` para acessar serviços via hostname

**4. Depuração:**
- Use `minikube logs` para ver logs do sistema
- Use `minikube ssh` para investigar o node
- Use dashboard para visualização gráfica

**5. Profiles (Múltiplos Clusters):**

```bash
# Criar cluster com profile específico
minikube start -p dev-cluster
minikube start -p staging-cluster

# Listar profiles
minikube profile list

# Mudar de profile
minikube profile dev-cluster

# Deletar profile específico
minikube delete -p staging-cluster
```

#### Limitações do Minikube

É importante entender as limitações do Minikube:

- **Single-node por padrão**: Embora suporte multi-node, não simula totalmente um cluster de produção
- **Recursos limitados**: Limitado aos recursos da máquina local
- **Networking simplificado**: Algumas features de rede avançadas podem não funcionar como em produção
- **Performance**: Pode ser lento comparado a clusters nativos
- **Não é para produção**: Apenas para desenvolvimento e testes

#### Quando Usar Minikube vs. Cluster Completo

**Use Minikube quando:**
- Estiver desenvolvendo e testando aplicações Kubernetes
- Precisar de um ambiente rápido para aprender Kubernetes
- Quiser testar manifests e configurações
- Estiver trabalhando sozinho no macOS
- Recursos da máquina forem limitados

**Use Cluster Completo (Opções 1 ou 2) quando:**
- Precisar simular ambiente de produção
- Quiser testar alta disponibilidade e failover
- Precisar de múltiplos nodes reais
- Quiser entender a arquitetura completa do Kubernetes
- Estiver preparando para certificações (CKA, CKAD)

#### Migração de Minikube para Cluster Completo

Quando estiver pronto para migrar:

```bash
# Exportar todos os recursos do Minikube
kubectl get all --all-namespaces -o yaml > minikube-backup.yaml

# Iniciar cluster completo (usando Opção 1 ou 2)

# Aplicar recursos no novo cluster (revisar antes!)
kubectl apply -f minikube-backup.yaml

# Ou migrar namespace por namespace
kubectl get all -n my-namespace -o yaml > my-namespace.yaml
kubectl apply -f my-namespace.yaml
```

---

## Instalação no Windows

### Opção 1: Usando WSL2 + Multipass

#### Passo 1: Habilitar WSL2

```powershell
# Execute no PowerShell como Administrador
wsl --install
wsl --set-default-version 2

# Instalar distribuição Ubuntu
wsl --install -d Ubuntu-22.04

# Reinicie o computador
```

#### Passo 2: Configurar Ubuntu no WSL2

```bash
# Abra o Ubuntu do WSL2
# Configure usuário e senha quando solicitado

# Atualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Multipass (alternativa: usar VMs no Hyper-V)
# Nota: No WSL2, é melhor usar VirtualBox no Windows ou Hyper-V
```

#### Passo 3: Usar Hyper-V para VMs

```powershell
# Habilitar Hyper-V (PowerShell como Admin)
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All

# Reinicie o computador
```

#### Passo 4: Criar VMs no Hyper-V

1. Abra o Hyper-V Manager
2. Crie 3 VMs com Ubuntu Server 22.04
3. Configure rede (usar Internal Switch ou External)
4. Alocar recursos mínimos (2GB RAM, 2 CPUs cada)

#### Passo 5: Instalar Kubernetes nas VMs

Conecte via SSH ou console em cada VM e siga os passos da seção "Instalação no Ubuntu".

#### Passo 6: Configurar kubectl no Windows

```powershell
# Instalar kubectl usando Chocolatey
choco install kubernetes-cli

# Ou baixar manualmente
curl.exe -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"

# Criar diretório .kube
mkdir $HOME\.kube

# Copiar config do control plane (use SSH ou WinSCP)
scp user@control-plane-ip:/home/user/.kube/config $HOME\.kube\config

# Editar config se necessário
notepad $HOME\.kube\config

# Testar
kubectl get nodes
```

### Opção 2: Usando Vagrant + VirtualBox

#### Passo 1: Instalar VirtualBox e Vagrant

```powershell
# Baixe e instale VirtualBox
# https://www.virtualbox.org/wiki/Downloads

# Baixe e instale Vagrant
# https://www.vagrantup.com/downloads

# Ou use Chocolatey
choco install virtualbox vagrant
```

#### Passo 2: Criar Vagrantfile

```powershell
# Criar diretório
mkdir k8s-cluster
cd k8s-cluster

# Criar Vagrantfile (mesmo conteúdo da seção macOS)
```

Use o mesmo Vagrantfile mostrado na seção macOS.

#### Passo 3: Iniciar e Configurar

```powershell
# Iniciar VMs
vagrant up

# Conectar
vagrant ssh k8s-control
vagrant ssh k8s-worker1
vagrant ssh k8s-worker2
```

Siga os passos de instalação do Ubuntu em cada VM.

---

## Configuração Pós-Instalação

### 1. Configurar Autocompletion para kubectl

**Linux/macOS (bash):**
```bash
echo 'source <(kubectl completion bash)' >>~/.bashrc
echo 'alias k=kubectl' >>~/.bashrc
echo 'complete -o default -F __start_kubectl k' >>~/.bashrc
source ~/.bashrc
```

**macOS (zsh):**
```bash
echo 'source <(kubectl completion zsh)' >>~/.zshrc
echo 'alias k=kubectl' >>~/.zshrc
echo 'compdef __start_kubectl k' >>~/.zshrc
source ~/.zshrc
```

### 2. Instalar Dashboard Kubernetes (Opcional)

```bash
# Instalar dashboard
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml

# Criar usuário admin
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kubernetes-dashboard
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: admin-user
  namespace: kubernetes-dashboard
EOF

# Obter token
kubectl -n kubernetes-dashboard create token admin-user

# Acessar dashboard
kubectl proxy
# Abra: http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/
```

#### Expor Dashboard via NodePort

Para acessar o Dashboard diretamente via navegador (sem `kubectl proxy`):

```bash
# Expor Dashboard via NodePort
kubectl expose deployment kubernetes-dashboard \
  --name=kubernetes-dashboard-nodeport \
  --target-port=8443 \
  --type=NodePort \
  -n kubernetes-dashboard

# Verificar porta alocada
kubectl get service kubernetes-dashboard-nodeport -n kubernetes-dashboard

# Acessar via: https://<NODE_IP>:<NODE_PORT>
```

### 3. Instalar Helm (Gerenciador de Pacotes)

```bash
# Linux
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# macOS
brew install helm

# Windows (PowerShell)
choco install kubernetes-helm

# Verificar instalação
helm version
```

### 4. Configurar Ingress Controller (NGINX)

```bash
# Usando kubectl
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# Ou usando Helm
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace

# Verificar instalação
kubectl get pods -n ingress-nginx
```

### 5. Configurar Storage Class (Opcional)

Para ambientes on-premises, você pode usar **Local Path Provisioner**:

```bash
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml

# Verificar
kubectl get storageclass
```

---

## Comandos Essenciais

### Gerenciamento de Recursos

```bash
# Criar recursos
kubectl create -f <arquivo.yaml>
kubectl apply -f <arquivo.yaml>

# Listar recursos
kubectl get <resource>
kubectl get pods
kubectl get services
kubectl get deployments
kubectl get nodes

# Detalhes de um recurso
kubectl describe <resource> <name>
kubectl describe pod nginx-pod

# Deletar recursos
kubectl delete <resource> <name>
kubectl delete -f <arquivo.yaml>

# Editar recurso
kubectl edit <resource> <name>
```

### Logs e Debug

```bash
# Ver logs
kubectl logs <pod-name>
kubectl logs -f <pod-name>  # Follow
kubectl logs <pod-name> -c <container-name>  # Container específico

# Executar comandos em pod
kubectl exec <pod-name> -- <comando>
kubectl exec -it <pod-name> -- /bin/bash

# Port-forward
kubectl port-forward <pod-name> <local-port>:<pod-port>
kubectl port-forward service/<service-name> <local-port>:<service-port>

# Top (uso de recursos)
kubectl top nodes
kubectl top pods
```

### Gerenciamento de Cluster

```bash
# Informações do cluster
kubectl cluster-info
kubectl version

# Contextos
kubectl config get-contexts
kubectl config use-context <context-name>

# Namespaces
kubectl get namespaces
kubectl create namespace <name>
kubectl config set-context --current --namespace=<name>

# Drain e Cordon (manutenção)
kubectl drain <node-name> --ignore-daemonsets
kubectl cordon <node-name>
kubectl uncordon <node-name>
```

### Labels e Selectors

```bash
# Adicionar label
kubectl label pods <pod-name> <label-key>=<label-value>

# Remover label
kubectl label pods <pod-name> <label-key>-

# Buscar por label
kubectl get pods -l <label-key>=<label-value>
kubectl get pods --selector=<label-key>=<label-value>
```

---

## Boas Práticas

### 1. Organização

- Use **namespaces** para separar ambientes (dev, staging, prod)
- Aplique **labels** consistentes em todos os recursos
- Use **annotations** para metadados adicionais

### 2. Segurança

- Não rode containers como root (use `securityContext`)
- Use **RBAC** (Role-Based Access Control) para controlar permissões
- Armazene informações sensíveis em **Secrets**, não em ConfigMaps
- Escaneie imagens de containers por vulnerabilidades
- Use **Network Policies** para controlar tráfego entre pods

### 3. Recursos

- Defina **requests e limits** de CPU e memória para todos os containers
- Use **HorizontalPodAutoscaler** para scaling automático
- Configure **readiness e liveness probes** para health checks

### 4. Configuração

- Use **ConfigMaps** para configurações
- Mantenha configurações separadas do código
- Use **Helm Charts** para aplicações complexas
- Versione seus manifestos YAML em Git

### 5. Observabilidade

- Implemente logging centralizado (ELK, Loki)
- Use ferramentas de monitoramento (Prometheus, Grafana)
- Configure alertas para eventos críticos
- Use tracing distribuído (Jaeger, Zipkin) para aplicações complexas

### 6. Deployment

- Use **Deployments** em vez de pods diretamente
- Configure **rolling updates** para zero-downtime deployments
- Sempre teste rollbacks antes de ir para produção
- Use **canary deployments** ou **blue-green deployments** para mudanças críticas

### 7. Backup e Disaster Recovery

- Faça backup regular do etcd
- Documente procedimentos de recuperação
- Teste restauração de backups periodicamente
- Use **Velero** ou ferramentas similares para backup de cluster

### 8. Performance

- Use **node affinity** e **pod affinity/anti-affinity** para otimizar placement
- Configure **PodDisruptionBudgets** para alta disponibilidade
- Use **Resource Quotas** para prevenir uso excessivo de recursos
- Implemente **caching** onde apropriado

---

## Referências

### Documentação Oficial

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Kubernetes API Reference](https://kubernetes.io/docs/reference/)
- [kubeadm Documentation](https://kubernetes.io/docs/reference/setup-tools/kubeadm/)
- [kubectl Reference](https://kubernetes.io/docs/reference/kubectl/)

### Ferramentas

- [Helm](https://helm.sh/) - Package Manager
- [K9s](https://k9scli.io/) - Terminal UI
- [Lens](https://k8slens.dev/) - Desktop IDE
- [Kubectx/Kubens](https://github.com/ahmetb/kubectx) - Context/Namespace switcher
- [kustomize](https://kustomize.io/) - Configuration management

### Aprendizado

- [Kubernetes the Hard Way](https://github.com/kelseyhightower/kubernetes-the-hard-way)
- [Kubernetes Patterns](https://www.redhat.com/cms/managed-files/cm-oreilly-kubernetes-patterns-ebook-f19824-201910-en.pdf)
- [12 Factor App](https://12factor.net/)
- [CNCF Landscape](https://landscape.cncf.io/)

### Comunidade

- [Kubernetes Slack](https://slack.k8s.io/)
- [Kubernetes Forum](https://discuss.kubernetes.io/)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/kubernetes)
- [Reddit r/kubernetes](https://www.reddit.com/r/kubernetes/)

---

## Conclusão

Este documento fornece uma base sólida para começar com Kubernetes, desde conceitos fundamentais até instalação completa de clusters em diferentes sistemas operacionais. Para o projeto Bridal Cover CRM, Kubernetes pode ser utilizado para:

- **Orquestrar** a aplicação Spring Boot em múltiplas instâncias
- **Gerenciar** o banco de dados PostgreSQL com alta disponibilidade
- **Escalar** automaticamente baseado em demanda
- **Implementar** estratégias de deployment sem downtime
- **Gerenciar** configurações e secrets de forma segura

À medida que o projeto cresce, Kubernetes fornecerá a flexibilidade e escalabilidade necessárias para suportar a demanda crescente.

