# Configurar Master Node para Executar Pods (Taints e Tolerations)

Este guia explica como permitir que pods sejam agendados no nó master/control-plane, algo essencial para clusters single-node ou ambientes de desenvolvimento.

## Índice

1. [O que são Taints e Tolerations](#o-que-são-taints-e-tolerations)
2. [Por que o Master tem Taints por Padrão](#por-que-o-master-tem-taints-por-padrão)
3. [Quando Remover Taints do Master](#quando-remover-taints-do-master)
4. [Como Remover Taints do Master](#como-remover-taints-do-master)
5. [Como Adicionar Taints de Volta](#como-adicionar-taints-de-volta)
6. [Configurar Tolerations em Pods](#configurar-tolerations-em-pods)
7. [Verificar Status de Taints](#verificar-status-de-taints)
8. [Troubleshooting](#troubleshooting)

---

## O que são Taints e Tolerations?

**Taints** (manchas) são propriedades aplicadas a nodes que repelem pods, a menos que esses pods tenham **tolerations** (tolerâncias) correspondentes.

### Analogia

Pense em taints como um "sinal de aviso" no node:
- 🚫 **Taint**: "Atenção! Este node tem restrições especiais"
- ✅ **Toleration**: "Tudo bem, eu aceito essas restrições"

### Componentes de um Taint

Um taint tem três componentes:

```
<key>=<value>:<effect>
```

- **Key**: Nome do taint (ex: `node-role.kubernetes.io/control-plane`)
- **Value**: Valor opcional (ex: `""`, `true`)
- **Effect**: O que acontece com pods sem toleration
  - `NoSchedule`: Não agenda novos pods
  - `PreferNoSchedule`: Tenta evitar agendar pods (soft)
  - `NoExecute`: Não agenda E remove pods existentes

### Exemplo

```yaml
# Taint do master
node-role.kubernetes.io/control-plane:NoSchedule

# Significa: "Não agende pods aqui, a menos que eles tolerem isso"
```

---

## Por que o Master tem Taints por Padrão?

O nó master/control-plane vem com taints por padrão por **razões de segurança e estabilidade**:

### Razões

1. **🔒 Isolamento**: Master roda componentes críticos do Kubernetes
   - etcd (banco de dados do cluster)
   - kube-apiserver
   - kube-controller-manager
   - kube-scheduler

2. **⚡ Performance**: Evitar que workloads de aplicação consumam recursos do master

3. **🛡️ Segurança**: Minimizar superfície de ataque no control plane

4. **📊 Confiabilidade**: Garantir que o cluster permaneça operacional

### Taints Padrão

Dependendo da versão do Kubernetes:

```bash
# Kubernetes 1.24+
node-role.kubernetes.io/control-plane:NoSchedule

# Kubernetes 1.23 e anteriores
node-role.kubernetes.io/master:NoSchedule
```

---

## Quando Remover Taints do Master?

### ✅ Remova Taints Quando:

1. **Cluster Single-Node**
   - Só tem um node (master)
   - Precisa executar workloads nele

2. **Ambiente de Desenvolvimento**
   - Cluster local para testes
   - Recursos limitados (laptop, VM pequena)

3. **Prototipagem Rápida**
   - Testando aplicações
   - POC (Proof of Concept)

4. **Laboratório de Estudos**
   - Aprendendo Kubernetes
   - Experimentando conceitos

### ❌ NÃO Remova Taints Quando:

1. **Ambiente de Produção**
   - Cluster multi-node
   - Alta disponibilidade necessária

2. **Workloads Críticos**
   - Aplicações que não podem falhar
   - Dados sensíveis

3. **Compliance e Segurança**
   - Regulamentações específicas
   - Políticas de isolamento

4. **Cluster Compartilhado**
   - Múltiplos usuários/equipes
   - Necessidade de isolamento

---

## Como Remover Taints do Master

### Passo 1: Identificar o Nome do Node

```bash
# Listar todos os nodes
kubectl get nodes

# Saída exemplo:
# NAME                 STATUS   ROLES           AGE   VERSION
# ubuntu-k8s-master    Ready    control-plane   1d    v1.28.0
```

### Passo 2: Verificar Taints Existentes

```bash
# Ver taints do node
kubectl describe node <nome-do-node> | grep Taints

# Exemplo:
kubectl describe node ubuntu-k8s-master | grep Taints

# Saída:
# Taints: node-role.kubernetes.io/control-plane:NoSchedule
```

### Passo 3: Remover Taint (Kubernetes 1.24+)

```bash
# Remover taint control-plane
kubectl taint nodes <nome-do-node> node-role.kubernetes.io/control-plane:NoSchedule-

# Exemplo:
kubectl taint nodes ubuntu-k8s-master node-role.kubernetes.io/control-plane:NoSchedule-

# Saída:
# node/ubuntu-k8s-master untainted
```

**Nota**: O `-` no final remove o taint!

### Passo 3 Alternativo: Remover Taint (Kubernetes 1.23-)

```bash
# Para versões antigas
kubectl taint nodes <nome-do-node> node-role.kubernetes.io/master:NoSchedule-

# Exemplo:
kubectl taint nodes ubuntu-k8s-master node-role.kubernetes.io/master:NoSchedule-
```

### Remover Todos os Taints de Uma Vez

```bash
# Remove todos os taints do node
kubectl taint nodes <nome-do-node> node-role.kubernetes.io/control-plane-
kubectl taint nodes <nome-do-node> node-role.kubernetes.io/master-

# Ou remover de todos os nodes (use com cuidado!)
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
kubectl taint nodes --all node-role.kubernetes.io/master-
```

### Passo 4: Verificar Remoção

```bash
# Verificar que taints foram removidos
kubectl describe node <nome-do-node> | grep Taints

# Saída esperada:
# Taints: <none>
```

---

## Como Adicionar Taints de Volta

Se você removeu o taint e quer adicioná-lo novamente:

### Adicionar Taint Control-Plane

```bash
# Kubernetes 1.24+
kubectl taint nodes <nome-do-node> node-role.kubernetes.io/control-plane:NoSchedule

# Verificar
kubectl describe node <nome-do-node> | grep Taints
```

### Adicionar Taint Master (versões antigas)

```bash
# Kubernetes 1.23-
kubectl taint nodes <nome-do-node> node-role.kubernetes.io/master:NoSchedule
```

### Adicionar Taint Customizado

```bash
# Taint customizado
kubectl taint nodes <nome-do-node> custom-key=custom-value:NoSchedule

# Com efeito PreferNoSchedule (mais suave)
kubectl taint nodes <nome-do-node> custom-key=custom-value:PreferNoSchedule

# Com efeito NoExecute (remove pods existentes)
kubectl taint nodes <nome-do-node> custom-key=custom-value:NoExecute
```

---

## Configurar Tolerations em Pods

Se você **não quer remover** o taint do master, mas quer que **pods específicos** rodem nele, use tolerations.

### Exemplo 1: Toleration Simples

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-tolerant
spec:
  containers:
  - name: nginx
    image: nginx
  tolerations:
  - key: "node-role.kubernetes.io/control-plane"
    operator: "Exists"
    effect: "NoSchedule"
```

### Exemplo 2: Deployment com Toleration

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-tolerante
spec:
  replicas: 3
  selector:
    matchLabels:
      app: app-tolerante
  template:
    metadata:
      labels:
        app: app-tolerante
    spec:
      tolerations:
      - key: "node-role.kubernetes.io/control-plane"
        operator: "Exists"
        effect: "NoSchedule"
      - key: "node-role.kubernetes.io/master"  # Para compatibilidade
        operator: "Exists"
        effect: "NoSchedule"
      containers:
      - name: app
        image: nginx
```

### Exemplo 3: Toleration com Valor Específico

```yaml
tolerations:
- key: "custom-taint"
  operator: "Equal"
  value: "special-node"
  effect: "NoSchedule"
```

### Exemplo 4: Tolerar Todos os Taints

```yaml
tolerations:
- operator: "Exists"  # Tolera todos os taints
```

### Exemplo 5: NodeSelector + Toleration

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: nginx-master-only
spec:
  nodeSelector:
    node-role.kubernetes.io/control-plane: ""
  tolerations:
  - key: "node-role.kubernetes.io/control-plane"
    operator: "Exists"
    effect: "NoSchedule"
  containers:
  - name: nginx
    image: nginx
```

Isso força o pod a rodar **apenas** no master.

---

## Verificar Status de Taints

### Ver Taints de Todos os Nodes

```bash
# Formato compacto
kubectl get nodes -o custom-columns=NAME:.metadata.name,TAINTS:.spec.taints

# Formato detalhado
kubectl get nodes -o json | jq '.items[] | {name: .metadata.name, taints: .spec.taints}'
```

### Ver Taints de Node Específico

```bash
# Usando describe
kubectl describe node <nome-do-node> | grep -A 5 Taints

# Usando jsonpath
kubectl get node <nome-do-node> -o jsonpath='{.spec.taints}' | jq
```

### Listar Nodes Sem Taints

```bash
# Nodes que podem receber qualquer pod
kubectl get nodes -o json | jq -r '.items[] | select(.spec.taints == null) | .metadata.name'
```

### Verificar se Pods Estão Rodando no Master

```bash
# Ver pods rodando no master
kubectl get pods --all-namespaces -o wide | grep <nome-do-master-node>

# Exemplo:
kubectl get pods --all-namespaces -o wide | grep ubuntu-k8s-master
```

---

## Troubleshooting

### Problema 1: Pods Ainda Ficam Pending Após Remover Taint

**Verificações:**

```bash
# 1. Confirmar que taint foi removido
kubectl describe node <nome-do-node> | grep Taints

# 2. Verificar eventos do pod
kubectl describe pod <nome-do-pod>

# 3. Ver eventos do cluster
kubectl get events --sort-by='.lastTimestamp'

# 4. Verificar se node está Ready
kubectl get nodes
```

**Causas Comuns:**

- Node não está em estado Ready
- Recursos insuficientes (CPU/Memory)
- Falta de network plugin (CNI)
- Outros taints não removidos

### Problema 2: Erro "node/xxx not found"

```bash
# Listar nome exato dos nodes
kubectl get nodes

# Use o nome exato (case-sensitive)
kubectl taint nodes <nome-exato-do-node> node-role.kubernetes.io/control-plane:NoSchedule-
```

### Problema 3: Erro "taint xxx not found"

```bash
# Ver taints atuais
kubectl describe node <nome-do-node> | grep Taints

# Remover apenas taints que existem
# Não é erro se taint já foi removido
```

### Problema 4: Removi Taint mas Quero Restringir Alguns Pods

Use **nodeSelector** ou **nodeAffinity** para controlar onde pods rodam:

```yaml
# Evitar que pods rodem no master
spec:
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: node-role.kubernetes.io/control-plane
            operator: DoesNotExist
```

### Problema 5: Cluster Multi-Node - Alguns Pods no Master, Outros Não

**Solução**: Use tolerations seletivamente em vez de remover taints:

```bash
# Manter taint no master
kubectl taint nodes <master> node-role.kubernetes.io/control-plane:NoSchedule

# Adicionar tolerations apenas em pods que precisam
# (ver exemplos em "Configurar Tolerations em Pods")
```

---

## Cenários Práticos

### Cenário 1: Cluster Single-Node (Minikube, Kind, k3s)

```bash
# Remover todos os taints
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
kubectl taint nodes --all node-role.kubernetes.io/master-

# Verificar
kubectl describe nodes | grep Taints
```

### Cenário 2: Permitir Apenas Monitoring no Master

```yaml
# deployment-monitoring.yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: node-monitoring
spec:
  selector:
    matchLabels:
      app: monitoring
  template:
    metadata:
      labels:
        app: monitoring
    spec:
      tolerations:
      - key: "node-role.kubernetes.io/control-plane"
        operator: "Exists"
        effect: "NoSchedule"
      containers:
      - name: monitoring
        image: prom/node-exporter
```

DaemonSets com toleration rodam em **todos** os nodes, incluindo master.

### Cenário 3: Cluster de Dev - Master + Workers

```bash
# Remover taint do master para aumentar capacidade
kubectl taint nodes <master> node-role.kubernetes.io/control-plane:NoSchedule-

# Mas adicionar label para identificação
kubectl label nodes <master> node-type=master

# Aplicações podem escolher onde rodar:
# - nodeSelector: {node-type: worker}  # Apenas workers
# - nodeSelector: {node-type: master}  # Apenas master
# - (sem selector)                      # Qualquer node
```

---

## Boas Práticas

### ✅ Faça

1. **Documente suas decisões**
   - Por que removeu taints?
   - Qual o impacto?

2. **Use tolerations em vez de remover taints**
   - Mais granular
   - Mais seguro

3. **Monitore recursos do master**
   ```bash
   kubectl top node <master>
   ```

4. **Configure resource limits**
   - Evite que apps consumam recursos do control-plane

5. **Use labels para identificar roles**
   ```bash
   kubectl label nodes <master> node-role=master
   kubectl label nodes <worker> node-role=worker
   ```

### ❌ Evite

1. **Remover taints em produção multi-node**
   - Pode afetar estabilidade do cluster

2. **Rodar workloads pesados no master**
   - Bancos de dados grandes
   - Machine learning
   - Processamento intensivo

3. **Esquecer de documentar mudanças**
   - Outros administradores podem se confundir

4. **Remover taints sem necessidade**
   - Se tem workers, use-os!

---

## Comandos Rápidos de Referência

```bash
# Ver taints de todos os nodes
kubectl get nodes -o custom-columns=NAME:.metadata.name,TAINTS:.spec.taints

# Remover taint control-plane
kubectl taint nodes <node> node-role.kubernetes.io/control-plane:NoSchedule-

# Remover taint master (versão antiga)
kubectl taint nodes <node> node-role.kubernetes.io/master:NoSchedule-

# Remover de todos os nodes
kubectl taint nodes --all node-role.kubernetes.io/control-plane-

# Adicionar taint de volta
kubectl taint nodes <node> node-role.kubernetes.io/control-plane:NoSchedule

# Ver pods rodando no master
kubectl get pods -A -o wide --field-selector spec.nodeName=<node-name>

# Verificar se node está sem taints
kubectl describe node <node> | grep Taints
# Saída esperada: Taints: <none>
```

---

## Próximos Passos

Agora que você entende taints e tolerations:

- **[Dashboard Setup](10-dashboard-setup.md)** - Configurar interface web
- **[Post Installation](07-post-installation.md)** - Outras configurações úteis
- **[Best Practices](09-best-practices.md)** - Melhorar seu cluster

---

## Recursos Adicionais

- [Documentação Oficial - Taints and Tolerations](https://kubernetes.io/docs/concepts/scheduling-eviction/taint-and-toleration/)
- [Node Selection](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/)
- [DaemonSet com Tolerations](https://kubernetes.io/docs/concepts/workloads/controllers/daemonset/)

---

[← Anterior: PostgreSQL External Access](13-postgresql-external-access.md) | [Voltar ao Índice](README.md)

