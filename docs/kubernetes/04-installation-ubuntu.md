# Instalação do Kubernetes no Ubuntu

Este guia mostra como instalar um cluster Kubernetes completo no Ubuntu usando **kubeadm**.

## Pré-requisitos

- **Ubuntu 20.04 ou superior**
- **Mínimo 2 GB de RAM** por máquina
- **2 CPUs** ou mais no control plane
- **Conectividade de rede** entre todas as máquinas
- **Hostname, MAC address e product_uuid únicos** para cada node
- **Swap desabilitado**

## Portas Necessárias

### Control Plane
- `6443`: Kubernetes API server
- `2379-2380`: etcd server client API
- `10250`: Kubelet API
- `10259`: kube-scheduler
- `10257`: kube-controller-manager

### Worker Nodes
- `10250`: Kubelet API
- `30000-32767`: NodePort Services

---

## Passo 1: Preparação do Sistema

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

---

## Passo 2: Instalar Container Runtime (containerd)

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

---

## Passo 3: Instalar kubeadm, kubelet e kubectl

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

---

## Passo 4: Inicializar Control Plane

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

**⚠️ Importante:** Salve o comando `kubeadm join` que aparece no final da inicialização. Você precisará dele para adicionar worker nodes.

Exemplo do comando:

```bash
kubeadm join 192.168.15.7:6443 --token nx67im.tdyi1gyac4hgu6a3 \
	--discovery-token-ca-cert-hash sha256:e63d9e945f5e8e5bb40f19f73c2bb7ee7e733655e1f0ba287945ab158b892da2
```

---

## Passo 5: Instalar Plugin de Rede (CNI)

Execute **no control plane**:

### Opção A: Flannel (Recomendado para iniciantes)

```bash
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml

# Aguarde os pods do kube-system estarem rodando
kubectl get pods -n kube-system
```

### Opção B: Calico (Mais recursos e políticas de rede)

```bash
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.26.1/manifests/tigera-operator.yaml
kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.26.1/manifests/custom-resources.yaml

# Verificar instalação
watch kubectl get pods -n calico-system
```

### Opção C: Weave Net

```bash
kubectl apply -f https://github.com/weaveworks/weave/releases/download/v2.8.1/weave-daemonset-k8s.yaml
```

---

## Passo 6: Adicionar Worker Nodes

Execute **nos worker nodes**:

```bash
# Use o comando fornecido pelo kubeadm init
sudo kubeadm join <CONTROL_PLANE_IP>:6443 --token <TOKEN> \
    --discovery-token-ca-cert-hash sha256:<HASH>
```

### Se Perdeu o Comando de Join

No control plane, você pode regenerá-lo:

```bash
# Gerar novo token e comando de join
kubeadm token create --print-join-command
```

---

## Passo 7: Verificar Cluster

Execute no **control plane**:

```bash
# Verificar nodes
kubectl get nodes

# Verificar pods do sistema
kubectl get pods --all-namespaces

# Verificar informações do cluster
kubectl cluster-info

# Verificar componentes
kubectl get componentstatuses
```

Todos os nodes devem aparecer com status `Ready`.

---

## Troubleshooting

### Nodes em NotReady

```bash
# Verificar logs do kubelet
sudo journalctl -u kubelet -f

# Verificar logs do containerd
sudo journalctl -u containerd -f

# Verificar pods do sistema
kubectl get pods -n kube-system
```

### Problemas com CNI

```bash
# Verificar pods do CNI
kubectl get pods -n kube-system | grep -E 'flannel|calico|weave'

# Ver logs do pod do CNI
kubectl logs -n kube-system <pod-name>
```

### Resetar Configuração

Se precisar recomeçar:

```bash
# Em todos os nodes
sudo kubeadm reset
sudo rm -rf /etc/cni/net.d
sudo rm -rf $HOME/.kube/config

# Depois siga os passos novamente
```

---

## Próximos Passos

Após instalar o cluster:

- **[Configuração Pós-Instalação](07-post-installation.md)** - Configurar autocompletion, Dashboard, Helm
- **[Kubernetes Dashboard](10-dashboard-setup.md)** - Instalar interface gráfica
- **[Comandos Essenciais](08-essential-commands.md)** - Aprender comandos kubectl

---

[← Anterior: Conceitos](03-concepts.md) | [Voltar ao Índice](README.md) | [Próximo: Instalação macOS →](05-installation-macos.md)

