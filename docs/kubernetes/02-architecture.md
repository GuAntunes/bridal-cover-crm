# Arquitetura do Kubernetes

## Visão Geral

Kubernetes segue uma arquitetura master-worker, onde o **Control Plane** gerencia o cluster e os **Worker Nodes** executam as aplicações.

## Componentes do Control Plane (Master)

### 1. kube-apiserver
- **Função**: Expõe a API do Kubernetes, frontend do control plane
- **Características**: 
  - Ponto de entrada para todos os comandos kubectl
  - Valida e processa requisições REST
  - Único componente que fala diretamente com etcd
  - Pode ser escalado horizontalmente

### 2. etcd
- **Função**: Armazenamento de dados key-value consistente para todos os dados do cluster
- **Características**:
  - Armazena toda a configuração e estado do cluster
  - Altamente disponível e distribuído
  - Usa algoritmo de consenso Raft
  - **Crítico**: Backup regular é essencial

### 3. kube-scheduler
- **Função**: Atribui pods para nodes baseado em recursos disponíveis
- **Características**:
  - Avalia requisitos de recursos (CPU, memória)
  - Considera constraints e affinity rules
  - Analisa disponibilidade de nodes
  - Não executa pods, apenas decide onde executar

### 4. kube-controller-manager
- **Função**: Executa processos de controller
- **Controllers Principais**:
  - **Node Controller**: Monitora saúde dos nodes
  - **Replication Controller**: Mantém número correto de pods
  - **Endpoints Controller**: Popula objetos Endpoints
  - **Service Account Controller**: Cria service accounts padrão
  - **Namespace Controller**: Gerencia ciclo de vida de namespaces

### 5. cloud-controller-manager (Opcional)
- **Função**: Integração com APIs de cloud providers
- **Responsabilidades**:
  - Node Controller (cloud-specific)
  - Route Controller
  - Load Balancer Controller
  - Volume Controller

## Componentes do Node (Worker)

### 1. kubelet
- **Função**: Agente que garante que containers estão rodando em um pod
- **Características**:
  - Executa em cada node do cluster
  - Recebe PodSpecs do API server
  - Garante que containers descritos estão rodando e saudáveis
  - Reporta status de volta ao control plane
  - Gerencia volumes e secrets

### 2. kube-proxy
- **Função**: Mantém regras de rede nos nodes
- **Características**:
  - Implementa parte do conceito de Service
  - Mantém regras de rede para permitir comunicação com pods
  - Pode usar iptables, IPVS ou userspace mode
  - Permite acesso externo aos services

### 3. Container Runtime
- **Função**: Software responsável por rodar containers
- **Opções**:
  - **containerd** (recomendado)
  - **CRI-O**
  - **Docker** (via cri-dockerd)

## Diagrama da Arquitetura

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
│ │  Runtime  │ │  │  │  Runtime  │  │  │  Runtime  │ │
│ └───────────┘ │  │  └───────────┘ │  │ └───────────┘ │
│               │  │                 │  │               │
│  ┌─────────┐  │  │   ┌─────────┐  │  │  ┌─────────┐  │
│  │  Pods   │  │  │   │  Pods   │  │  │  │  Pods   │  │
│  └─────────┘  │  │   └─────────┘  │  │  └─────────┘  │
└───────────────┘  └─────────────────┘  └───────────────┘
```

## Fluxo de Criação de um Pod

1. **Usuário** executa `kubectl create deployment`
2. **kubectl** envia requisição para **kube-apiserver**
3. **kube-apiserver** valida e armazena no **etcd**
4. **kube-controller-manager** detecta novo deployment e cria ReplicaSet
5. **ReplicaSet Controller** cria pods necessários
6. **kube-scheduler** detecta pods sem node atribuído
7. **kube-scheduler** seleciona melhor node e atualiza pod spec
8. **kubelet** no node selecionado detecta novo pod atribuído
9. **kubelet** instrui **container runtime** a baixar imagem e iniciar container
10. **kubelet** reporta status de volta para **kube-apiserver**

## Comunicação entre Componentes

### Control Plane ↔ Nodes
- **API Server** é o hub central de comunicação
- Todos os componentes se comunicam através do API Server
- Kubelet inicia conexões para o API Server (pull-based)

### Nodes ↔ Nodes
- **kube-proxy** gerencia regras de rede para comunicação pod-to-pod
- Rede flat: todos os pods podem se comunicar sem NAT
- Network plugins (CNI) implementam networking entre nodes

## Alta Disponibilidade

### Control Plane HA
- Múltiplas réplicas do API Server (load balanced)
- etcd em cluster (3, 5 ou 7 nodes para quorum)
- Controller Manager e Scheduler com leader election

### Node HA
- Múltiplos worker nodes
- Pods distribuídos entre nodes
- Auto-recovery em caso de falha de node

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

## Próximos Passos

Agora que você entende a arquitetura, continue para:

- **[Conceitos Fundamentais](03-concepts.md)** - Aprender sobre Pods, Services, Deployments
- **[Instalação](04-installation-ubuntu.md)** - Começar a instalar seu cluster

---

[← Anterior: Introdução](01-introduction.md) | [Voltar ao Índice](README.md) | [Próximo: Conceitos →](03-concepts.md)

