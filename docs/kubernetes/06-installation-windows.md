# Instalação do Kubernetes no Windows

Existem duas opções principais para executar Kubernetes no Windows.

## Comparação das Opções

| Opção | Complexidade | Recursos | Produção-like | Ideal Para |
|-------|--------------|----------|---------------|------------|
| **WSL2 + Multipass/Docker** | ⭐⭐ Médio | Médio | ⚠️ | Desenvolvimento |
| **Vagrant + VirtualBox/Hyper-V** | ⭐⭐⭐ Difícil | Alto | ✅ | Simulação de produção |

---

## Opção 1: WSL2 + Docker Desktop (Mais Simples)

Docker Desktop para Windows inclui Kubernetes integrado.

### Passo 1: Instalar WSL2

```powershell
# Execute no PowerShell como Administrador
wsl --install
wsl --set-default-version 2

# Instalar Ubuntu
wsl --install -d Ubuntu-22.04

# Reinicie o computador
```

### Passo 2: Instalar Docker Desktop

1. Baixe Docker Desktop: https://www.docker.com/products/docker-desktop
2. Instale e reinicie
3. Abra Docker Desktop
4. Vá em Settings → General
5. Marque "Use WSL 2 based engine"
6. Vá em Settings → Kubernetes
7. Marque "Enable Kubernetes"
8. Clique em "Apply & Restart"

### Passo 3: Instalar kubectl

**Opção A: Via Chocolatey**

```powershell
# Instalar Chocolatey (se não tiver)
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# Instalar kubectl
choco install kubernetes-cli
```

**Opção B: Download Manual**

```powershell
# Baixar kubectl
curl.exe -LO "https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe"

# Mover para C:\Windows\System32 ou adicionar ao PATH
```

### Passo 4: Verificar Instalação

```powershell
# Verificar cluster
kubectl cluster-info
kubectl get nodes

# Criar deployment de teste
kubectl create deployment hello-world --image=nginx
kubectl expose deployment hello-world --port=80 --type=NodePort
kubectl get services
```

### Limitações

- Single-node apenas
- Recursos limitados
- Não simula ambiente de produção real

---

## Opção 2: Vagrant + VirtualBox/Hyper-V (Cluster Completo)

### Usando Hyper-V (Windows 10/11 Pro)

#### Passo 1: Habilitar Hyper-V

```powershell
# Execute no PowerShell como Administrador
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All

# Reinicie o computador
```

#### Passo 2: Instalar Vagrant

```powershell
# Via Chocolatey
choco install vagrant

# Ou baixe de: https://www.vagrantup.com/downloads
```

#### Passo 3: Configurar Hyper-V

1. Abra Hyper-V Manager
2. Clique em "Virtual Switch Manager"
3. Crie um External Virtual Switch
4. Nomeie como "External Network"

#### Passo 4: Criar Vagrantfile

```powershell
# Criar diretório
mkdir k8s-cluster
cd k8s-cluster

# Criar Vagrantfile
@"
Vagrant.configure("2") do |config|
  config.vm.box = "generic/ubuntu2204"
  
  config.vm.provider "hyperv" do |h|
    h.enable_virtualization_extensions = true
    h.linked_clone = true
  end
  
  # Control Plane
  config.vm.define "k8s-control" do |control|
    control.vm.hostname = "k8s-control"
    control.vm.network "public_network", bridge: "External Network"
    control.vm.provider "hyperv" do |h|
      h.memory = 2048
      h.cpus = 2
    end
  end
  
  # Worker 1
  config.vm.define "k8s-worker1" do |worker|
    worker.vm.hostname = "k8s-worker1"
    worker.vm.network "public_network", bridge: "External Network"
    worker.vm.provider "hyperv" do |h|
      h.memory = 2048
      h.cpus = 2
    end
  end
  
  # Worker 2
  config.vm.define "k8s-worker2" do |worker|
    worker.vm.hostname = "k8s-worker2"
    worker.vm.network "public_network", bridge: "External Network"
    worker.vm.provider "hyperv" do |h|
      h.memory = 2048
      h.cpus = 2
    end
  end
end
"@ | Out-File -FilePath Vagrantfile -Encoding ASCII
```

### Usando VirtualBox (Todas as versões do Windows)

#### Passo 1: Instalar VirtualBox

```powershell
# Via Chocolatey
choco install virtualbox

# Ou baixe de: https://www.virtualbox.org/wiki/Downloads
```

**⚠️ Nota:** VirtualBox e Hyper-V não podem ser usados simultaneamente.

#### Passo 2: Instalar Vagrant

```powershell
# Via Chocolatey
choco install vagrant

# Ou baixe de: https://www.vagrantup.com/downloads
```

#### Passo 3: Criar Vagrantfile

```powershell
# Criar diretório
mkdir k8s-cluster
cd k8s-cluster

# Criar Vagrantfile
@"
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
"@ | Out-File -FilePath Vagrantfile -Encoding ASCII
```

### Passo 4: Iniciar VMs

```powershell
# Iniciar todas as VMs
vagrant up

# Conectar às VMs
vagrant ssh k8s-control
vagrant ssh k8s-worker1
vagrant ssh k8s-worker2
```

### Passo 5: Instalar Kubernetes nas VMs

Dentro de cada VM, siga os passos do **[Guia de Instalação Ubuntu](04-installation-ubuntu.md)**.

### Passo 6: Configurar kubectl no Windows

```powershell
# Instalar kubectl
choco install kubernetes-cli

# Criar diretório .kube
mkdir $HOME\.kube

# Copiar config do control plane (VirtualBox)
vagrant ssh k8s-control -c "sudo cat /etc/kubernetes/admin.conf" > $HOME\.kube\config

# Editar config se necessário (VirtualBox)
# Substituir IP por 192.168.56.10:6443
notepad $HOME\.kube\config

# Testar
kubectl get nodes
```

### Gerenciar VMs

```powershell
# Ver status
vagrant status

# Parar VMs
vagrant halt

# Iniciar VMs
vagrant up

# Deletar VMs
vagrant destroy -f
```

---

## Opção 3: Minikube no Windows

### Passo 1: Instalar Pré-requisitos

```powershell
# Instalar Docker Desktop (já explicado acima)
# Ou Hyper-V (Windows Pro)
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All
```

### Passo 2: Instalar Minikube e kubectl

```powershell
# Via Chocolatey
choco install minikube kubectl

# Ou download manual de:
# https://minikube.sigs.k8s.io/docs/start/
```

### Passo 3: Iniciar Cluster

```powershell
# Com Docker Desktop
minikube start --driver=docker

# Ou com Hyper-V
minikube start --driver=hyperv

# Verificar
minikube status
kubectl get nodes
```

---

## Troubleshooting

### Erro de Virtualização

```powershell
# Verificar se virtualização está habilitada na BIOS
systeminfo

# Procure por "Hyper-V Requirements"
```

### Conflito Hyper-V e VirtualBox

```powershell
# Desabilitar Hyper-V para usar VirtualBox
bcdedit /set hypervisorlaunchtype off
# Reinicie

# Reabilitar Hyper-V
bcdedit /set hypervisorlaunchtype auto
# Reinicie
```

### Problemas de Rede com Vagrant

```powershell
# Listar interfaces de rede
vagrant up --debug

# Especificar bridge manualmente no Vagrantfile
# config.vm.network "public_network", bridge: "Nome_da_Interface"
```

---

## Qual Opção Escolher?

### Use Docker Desktop + Kubernetes se:
- Você quer a forma mais simples
- Está desenvolvendo aplicações
- Não precisa de múltiplos nodes
- Tem Windows Home Edition

### Use Vagrant + Hyper-V se:
- Tem Windows Pro/Enterprise
- Quer cluster multi-node
- Precisa simular produção
- Já usa Hyper-V

### Use Vagrant + VirtualBox se:
- Tem Windows Home
- Quer cluster multi-node
- Não usa Hyper-V
- Quer portabilidade (funciona em Linux/macOS também)

### Use Minikube se:
- Quer algo intermediário
- Desenvolvimento rápido
- Pode usar Docker ou Hyper-V
- Não precisa de configuração complexa

---

## Próximos Passos

Após instalar o cluster:

- **[Configuração Pós-Instalação](07-post-installation.md)**
- **[Kubernetes Dashboard](10-dashboard-setup.md)**
- **[Comandos Essenciais](08-essential-commands.md)**

---

[← Anterior: Instalação macOS](05-installation-macos.md) | [Voltar ao Índice](README.md) | [Próximo: Pós-Instalação →](07-post-installation.md)

