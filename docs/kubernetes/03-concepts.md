# Conceitos Fundamentais do Kubernetes

## Pod

### O que é?
- Menor unidade deployável no Kubernetes
- Grupo de um ou mais containers que compartilham storage e rede
- Containers em um pod compartilham o mesmo IP e namespace

### Características
- Ephemeral (não persistente por design)
- Geralmente gerenciado por controllers (Deployment, ReplicaSet)
- Pode ter múltiplos containers (sidecar pattern)

### Exemplo

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-pod
  labels:
    app: nginx
spec:
  containers:
  - name: nginx
    image: nginx:1.14.2
    ports:
    - containerPort: 80
```

## Service

### O que é?
- Abstração que define um conjunto lógico de pods
- Política de acesso aos pods (load balancing)
- Fornece um endpoint estável (IP/DNS) mesmo quando pods são recriados

### Tipos de Service

#### 1. ClusterIP (Padrão)
- Expõe o serviço **apenas dentro do cluster**
- Acessível por outros pods via DNS interno (`service-name.namespace.svc.cluster.local`)
- Não é acessível de fora do cluster

**Casos de uso:**
- Comunicação entre microserviços
- Bancos de dados internos (PostgreSQL, Redis)
- APIs backend que não precisam ser públicas

**Exemplo:**

```yaml
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
```

#### 2. NodePort
- Expõe o serviço em uma **porta estática em cada Node** do cluster
- Acessível externamente via `<NodeIP>:<NodePort>`
- Porta range: **30000-32767**
- Automaticamente cria um ClusterIP

**Casos de uso:**
- Ambiente de desenvolvimento e testes
- Clusters on-premises sem LoadBalancer
- Acesso temporário para debugging

**Exemplo:**

```yaml
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
```

#### 3. LoadBalancer
- Cria um **load balancer externo** (suportado por cloud providers)
- Fornece um **IP público único** para o serviço
- Roteia tráfego automaticamente para os NodePorts
- Automaticamente cria NodePort e ClusterIP

**Casos de uso:**
- Produção em cloud (AWS, GCP, Azure)
- Expor aplicações web publicamente
- APIs públicas que precisam de alta disponibilidade

**Exemplo:**

```yaml
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
```

#### 4. ExternalName
- Mapeia o serviço para um **nome DNS externo**
- Retorna um registro CNAME para o DNS especificado
- Não há proxying ou encaminhamento de portas
- Não possui selector de pods

**Casos de uso:**
- Integração com serviços externos ao cluster
- Migração gradual de serviços para Kubernetes
- Acesso a bancos de dados gerenciados (RDS, Cloud SQL)

**Exemplo:**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: external-database
spec:
  type: ExternalName
  externalName: database.external-service.com
```

## Deployment

### O que é?
- Fornece atualizações declarativas para pods e ReplicaSets
- Gerencia criação e scaling de pods
- Suporta rollback e rollout

### Características
- Define estado desejado para pods
- Controller garante que estado atual = estado desejado
- Gerencia ReplicaSets automaticamente
- Suporta estratégias de update (RollingUpdate, Recreate)

### Exemplo

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

## ReplicaSet

### O que é?
- Garante que um número específico de réplicas de pod esteja rodando
- Geralmente gerenciado por Deployments
- Usa selectors para identificar pods

### Características
- Mantém número de réplicas constante
- Cria novos pods se alguns falharem
- Deleta pods extras se houver mais que o desejado
- Raramente criado diretamente (use Deployments)

## Namespace

### O que é?
- Fornece isolamento lógico de recursos
- Múltiplos ambientes virtuais dentro do mesmo cluster

### Características
- Divide cluster em sub-clusters virtuais
- Resource quotas podem ser aplicadas por namespace
- Services podem se comunicar entre namespaces
- Alguns recursos são cluster-wide (nodes, PersistentVolumes)

### Namespaces Padrão
- `default`: Namespace padrão para recursos sem namespace especificado
- `kube-system`: Para recursos do sistema Kubernetes
- `kube-public`: Acessível por todos (incluindo não autenticados)
- `kube-node-lease`: Para objetos de lease relacionados a cada node

### Exemplo

```bash
# Criar namespace
kubectl create namespace desenvolvimento

# Listar namespaces
kubectl get namespaces

# Usar namespace
kubectl get pods -n desenvolvimento
```

## ConfigMap

### O que é?
- Armazena dados de configuração não-confidenciais
- Permite separar configuração do código

### Características
- Dados em formato key-value
- Pode ser montado como volume ou variáveis de ambiente
- Não é encriptado

### Exemplo

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  database_url: "postgresql://localhost:5432/mydb"
  log_level: "INFO"
```

## Secret

### O que é?
- Armazena informações sensíveis (senhas, tokens, chaves)
- Similar ao ConfigMap mas com propósito de segurança

### Características
- Dados são codificados em base64
- Pode ser encriptado at-rest (depende da configuração)
- Acesso pode ser controlado via RBAC

### Exemplo

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
type: Opaque
data:
  username: YWRtaW4=  # admin em base64
  password: cGFzc3dvcmQ=  # password em base64
```

## Volume

### O que é?
- Diretório acessível aos containers em um pod
- Persiste dados além do ciclo de vida do container

### Tipos Comuns
- **emptyDir**: Diretório vazio criado quando pod é atribuído ao node
- **hostPath**: Monta diretório do filesystem do node
- **persistentVolumeClaim**: Solicita armazenamento persistente
- **configMap/secret**: Monta ConfigMap ou Secret como volume

### Exemplo

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-with-volume
spec:
  containers:
  - name: app
    image: nginx
    volumeMounts:
    - name: data
      mountPath: /data
  volumes:
  - name: data
    emptyDir: {}
```

## Ingress

### O que é?
- Gerencia acesso externo aos serviços do cluster
- Fornece load balancing, SSL termination, name-based virtual hosting

### Características
- Requer Ingress Controller (nginx, traefik, etc.)
- Roteia tráfego HTTP/HTTPS baseado em regras
- Pode ter múltiplos backends
- Suporta SSL/TLS

### Exemplo

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
spec:
  rules:
  - host: myapp.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend-service
            port:
              number: 80
```

## StatefulSet

### O que é?
- Gerencia deployment de aplicações stateful
- Fornece identidades únicas e persistentes para pods

### Características
- Ordem de deployment e scaling garantida
- Nomes de pods estáveis e previsíveis
- Storage persistente por pod
- Útil para bancos de dados, sistemas de mensageria

## DaemonSet

### O que é?
- Garante que todos (ou alguns) nodes executem uma cópia de um pod
- Quando nodes são adicionados, pods são adicionados automaticamente

### Casos de Uso
- Agentes de logging (fluentd, logstash)
- Agentes de monitoring (node-exporter, datadog)
- Network plugins (kube-proxy, CNI)

## Job e CronJob

### Job
- Executa uma tarefa até completar com sucesso
- Pod não é reiniciado após conclusão

### CronJob
- Cria Jobs em horários agendados
- Similar ao cron do Linux

### Exemplo de CronJob

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: backup-job
spec:
  schedule: "0 2 * * *"  # Todos os dias às 2h
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: backup-tool:latest
            command: ["/bin/sh", "-c", "backup-script.sh"]
          restartPolicy: OnFailure
```

## Próximos Passos

Agora que você entende os conceitos fundamentais, você pode:

- **[Instalar Kubernetes](04-installation-ubuntu.md)** - Criar seu próprio cluster
- **[Aprender Comandos](08-essential-commands.md)** - Comandos kubectl essenciais

---

[← Anterior: Arquitetura](02-architecture.md) | [Voltar ao Índice](README.md) | [Próximo: Instalação Ubuntu →](04-installation-ubuntu.md)

