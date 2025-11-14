# Kubernetes Dashboard - Instalação e Acesso via NodePort

Este guia mostra como instalar e acessar o Kubernetes Dashboard através de NodePort, permitindo acesso direto via navegador sem necessidade de `kubectl proxy`.

## Índice

1. [O que é o Kubernetes Dashboard](#o-que-é-o-kubernetes-dashboard)
2. [Instalação](#instalação)
3. [Configurar Acesso via NodePort](#configurar-acesso-via-nodeport)
4. [Criar Usuário Admin](#criar-usuário-admin)
5. [Acessar o Dashboard](#acessar-o-dashboard)
6. [Configurar Usuário com Permissões Limitadas](#configurar-usuário-com-permissões-limitadas)
7. [Troubleshooting](#troubleshooting)
8. [Considerações de Segurança](#considerações-de-segurança)

---

## O que é o Kubernetes Dashboard?

Kubernetes Dashboard é uma interface web de propósito geral para clusters Kubernetes. Permite gerenciar aplicações rodando no cluster e diagnosticar problemas, além de gerenciar o próprio cluster.

### Recursos Principais

- ✅ Visualizar recursos do cluster (Deployments, Pods, Services, etc.)
- ✅ Criar, editar e deletar recursos
- ✅ Ver logs de containers
- ✅ Executar shell em containers
- ✅ Escalar aplicações
- ✅ Ver métricas de recursos (com Metrics Server)
- ✅ Visualizar eventos do cluster

---

## Instalação

### Passo 1: Instalar Dashboard

```bash
# Instalar a versão mais recente do Dashboard
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml

# Verificar instalação
kubectl get pods -n kubernetes-dashboard
kubectl get services -n kubernetes-dashboard
```

**Aguarde todos os pods estarem no estado `Running`:**

```bash
# Verificar status
kubectl get pods -n kubernetes-dashboard -w
```

Você deve ver algo como:

```
NAME                                         READY   STATUS    RESTARTS   AGE
dashboard-metrics-scraper-5cb4f4bb9c-xxxxx   1/1     Running   0          1m
kubernetes-dashboard-6967859bff-xxxxx        1/1     Running   0          1m
```

---

## Configurar Acesso via NodePort

Por padrão, o Dashboard é exposto como ClusterIP (apenas interno). Vamos alterá-lo para NodePort.

### Opção 1: Editar Service Existente (Recomendado)

```bash
# Editar o service do Dashboard
kubectl edit service kubernetes-dashboard -n kubernetes-dashboard
```

No editor que abrir, localize a linha `type: ClusterIP` e altere para `type: NodePort`:

```yaml
spec:
  type: NodePort  # Alterar de ClusterIP para NodePort
  ports:
  - port: 443
    targetPort: 8443
    nodePort: 30443  # Adicionar esta linha (opcional)
  selector:
    k8s-app: kubernetes-dashboard
```

**Salvar e sair** (`:wq` no vim, `Ctrl+O` e `Ctrl+X` no nano).

### Opção 2: Usar kubectl patch

```bash
# Alterar para NodePort em uma linha
kubectl patch service kubernetes-dashboard -n kubernetes-dashboard \
  -p '{"spec":{"type":"NodePort"}}'

# Definir NodePort específico (opcional)
kubectl patch service kubernetes-dashboard -n kubernetes-dashboard \
  -p '{"spec":{"type":"NodePort","ports":[{"port":443,"targetPort":8443,"nodePort":30443}]}}'
```

### Opção 3: Deletar e Recriar Service

```bash
# Salvar o service atual
kubectl get service kubernetes-dashboard -n kubernetes-dashboard -o yaml > dashboard-service.yaml

# Deletar service existente
kubectl delete service kubernetes-dashboard -n kubernetes-dashboard

# Criar novo service com NodePort
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Service
metadata:
  name: kubernetes-dashboard
  namespace: kubernetes-dashboard
  labels:
    k8s-app: kubernetes-dashboard
spec:
  type: NodePort
  ports:
  - port: 443
    targetPort: 8443
    nodePort: 30443  # Porta que será exposta (30000-32767)
  selector:
    k8s-app: kubernetes-dashboard
EOF
```

### Verificar NodePort

```bash
# Ver porta alocada
kubectl get service kubernetes-dashboard -n kubernetes-dashboard

# Output esperado:
# NAME                   TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)         AGE
# kubernetes-dashboard   NodePort   10.96.123.45    <none>        443:30443/TCP   5m
```

A porta após os dois pontos (`30443` no exemplo) é a porta NodePort.

---

## Criar Usuário Admin

O Dashboard requer autenticação. Vamos criar um usuário admin com acesso completo.

### Passo 1: Criar ServiceAccount

```bash
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kubernetes-dashboard
EOF
```

### Passo 2: Criar ClusterRoleBinding

```bash
cat <<EOF | kubectl apply -f -
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
```

### Passo 3: Obter Token de Acesso

**Para Kubernetes 1.24 e superior:**

```bash
# Criar token (válido por 24 horas)
kubectl -n kubernetes-dashboard create token admin-user

# Criar token com validade maior (30 dias)
kubectl -n kubernetes-dashboard create token admin-user --duration=720h
```

**Para Kubernetes 1.23 e inferior:**

```bash
# Obter token do secret
kubectl -n kubernetes-dashboard get secret $(kubectl -n kubernetes-dashboard get sa/admin-user -o jsonpath="{.secrets[0].name}") -o go-template="{{.data.token | base64decode}}"
```

**Salve este token!** Você precisará dele para fazer login no Dashboard.

### Alternativa: Criar Secret com Token Permanente (K8s 1.24+)

```bash
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: admin-user-token
  namespace: kubernetes-dashboard
  annotations:
    kubernetes.io/service-account.name: admin-user
type: kubernetes.io/service-account-token
EOF

# Obter token permanente
kubectl get secret admin-user-token -n kubernetes-dashboard -o jsonpath='{.data.token}' | base64 --decode
```

---

## Acessar o Dashboard

### Passo 1: Obter IP do Node

Você precisa do IP de qualquer node do cluster.

```bash
# Ver IPs dos nodes
kubectl get nodes -o wide

# Ou obter IP do primeiro node
kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}'
```

### Passo 2: Construir URL

A URL será: `https://<NODE_IP>:<NODE_PORT>`

Por exemplo:
- Se NODE_IP = `192.168.1.10`
- Se NODE_PORT = `30443`
- URL = `https://192.168.1.10:30443`

### Passo 3: Acessar no Navegador

1. Abra o navegador e acesse: `https://<NODE_IP>:<NODE_PORT>`
2. Você verá um aviso de segurança (certificado auto-assinado)
3. **Chrome/Edge:** Clique em "Avançado" → "Prosseguir para..."
4. **Firefox:** Clique em "Avançado" → "Aceitar o risco e continuar"
5. **Safari:** Clique em "Exibir o certificado" → "Continuar"

### Passo 4: Fazer Login

1. Selecione **"Token"** como método de autenticação
2. Cole o token obtido anteriormente
3. Clique em **"Sign In"**

![Dashboard Login](https://kubernetes.io/images/docs/ui-dashboard-login.png)

---

## Configurar Usuário com Permissões Limitadas

Para ambientes compartilhados, crie usuários com permissões restritas.

### Usuário Read-Only

```bash
# Criar ServiceAccount
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: readonly-user
  namespace: kubernetes-dashboard
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: readonly-role
rules:
- apiGroups: ["", "apps", "batch"]
  resources: ["*"]
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: readonly-user-binding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: readonly-role
subjects:
- kind: ServiceAccount
  name: readonly-user
  namespace: kubernetes-dashboard
EOF

# Obter token
kubectl -n kubernetes-dashboard create token readonly-user
```

### Usuário com Acesso a Namespace Específico

```bash
# Criar ServiceAccount
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: developer-user
  namespace: kubernetes-dashboard
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: developer-role
  namespace: development
rules:
- apiGroups: ["", "apps", "batch"]
  resources: ["*"]
  verbs: ["*"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: developer-binding
  namespace: development
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: developer-role
subjects:
- kind: ServiceAccount
  name: developer-user
  namespace: kubernetes-dashboard
EOF

# Obter token
kubectl -n kubernetes-dashboard create token developer-user
```

---

## Troubleshooting

### Erro: "Not enough data to create auth info structure"

**Causa:** Token inválido ou expirado.

**Solução:**
```bash
# Gerar novo token
kubectl -n kubernetes-dashboard create token admin-user
```

### Erro: "Forbidden: User cannot list resource"

**Causa:** Usuário não tem permissões necessárias.

**Solução:**
```bash
# Verificar permissões
kubectl auth can-i --list --as=system:serviceaccount:kubernetes-dashboard:admin-user

# Recriar ClusterRoleBinding
kubectl delete clusterrolebinding admin-user
kubectl create clusterrolebinding admin-user \
  --clusterrole=cluster-admin \
  --serviceaccount=kubernetes-dashboard:admin-user
```

### Não Consigo Acessar via Navegador

**Verificações:**

```bash
# 1. Verificar se pods estão rodando
kubectl get pods -n kubernetes-dashboard

# 2. Verificar service
kubectl get svc -n kubernetes-dashboard

# 3. Verificar logs
kubectl logs -n kubernetes-dashboard deployment/kubernetes-dashboard

# 4. Testar conectividade (de dentro do cluster)
kubectl run curl --rm -it --image=curlimages/curl -- sh
curl -k https://kubernetes-dashboard.kubernetes-dashboard.svc.cluster.local
```

### Firewall Bloqueando Porta

```bash
# Ubuntu/Debian
sudo ufw allow 30443/tcp

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=30443/tcp
sudo firewall-cmd --reload

# Verificar porta está aberta
sudo netstat -tlnp | grep 30443
```

### Dashboard Não Carrega Métricas

```bash
# Instalar Metrics Server (se ainda não tiver)
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# Verificar
kubectl top nodes
kubectl top pods -A
```

---

## Considerações de Segurança

### ⚠️ Avisos Importantes

1. **NodePort Expõe o Dashboard Externamente**
   - Qualquer um que tenha acesso à rede pode acessar a URL
   - Use apenas em ambientes controlados
   - Considere restringir acesso por firewall

2. **Use HTTPS Sempre**
   - Dashboard usa certificado auto-assinado
   - Nunca desabilite HTTPS
   - Para produção, configure certificado válido

3. **Tokens Devem Ser Protegidos**
   - Não compartilhe tokens publicamente
   - Rotacione tokens regularmente
   - Use tokens com tempo de expiração

4. **RBAC é Essencial**
   - Não dê acesso cluster-admin para todos
   - Use princípio do menor privilégio
   - Crie roles específicas por necessidade

### Melhores Práticas para Produção

#### 1. Use Ingress ao Invés de NodePort

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: dashboard-ingress
  namespace: kubernetes-dashboard
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - dashboard.company.com
    secretName: dashboard-tls
  rules:
  - host: dashboard.company.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: kubernetes-dashboard
            port:
              number: 443
```

#### 2. Configure Autenticação Externa (OIDC)

```bash
# Integrar com Google, GitHub, ou Azure AD
# Documentação: https://kubernetes.io/docs/reference/access-authn-authz/authentication/
```

#### 3. Use Network Policies

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: dashboard-network-policy
  namespace: kubernetes-dashboard
spec:
  podSelector:
    matchLabels:
      k8s-app: kubernetes-dashboard
  policyTypes:
  - Ingress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8443
```

#### 4. Monitore Acessos

```bash
# Ver logs de acesso
kubectl logs -n kubernetes-dashboard deployment/kubernetes-dashboard

# Configurar auditoria
# https://kubernetes.io/docs/tasks/debug/debug-cluster/audit/
```

### Alternativas ao Dashboard

Para ambientes de produção, considere:

- **[Lens](https://k8slens.dev/)** - IDE desktop para Kubernetes
- **[K9s](https://k9scli.io/)** - Terminal UI
- **[Octant](https://octant.dev/)** - Dashboard local
- **[Rancher](https://rancher.com/)** - Plataforma completa de gerenciamento

---

## Comandos Rápidos de Referência

```bash
# Ver status do Dashboard
kubectl get all -n kubernetes-dashboard

# Ver NodePort
kubectl get svc kubernetes-dashboard -n kubernetes-dashboard

# Gerar novo token
kubectl -n kubernetes-dashboard create token admin-user

# Ver logs do Dashboard
kubectl logs -n kubernetes-dashboard deployment/kubernetes-dashboard

# Reiniciar Dashboard
kubectl rollout restart deployment kubernetes-dashboard -n kubernetes-dashboard

# Desinstalar Dashboard
kubectl delete ns kubernetes-dashboard
```

---

## Próximos Passos

Agora que o Dashboard está configurado:

- **[Comandos Essenciais](08-essential-commands.md)** - Dominar kubectl
- **[Boas Práticas](09-best-practices.md)** - Melhorar segurança e configuração
- **[Referências](11-references.md)** - Ferramentas adicionais

---

## Recursos Adicionais

- [Documentação Oficial do Dashboard](https://kubernetes.io/docs/tasks/access-application-cluster/web-ui-dashboard/)
- [GitHub do Kubernetes Dashboard](https://github.com/kubernetes/dashboard)
- [Guia de Acesso e Autenticação](https://github.com/kubernetes/dashboard/blob/master/docs/user/access-control/README.md)

---

[← Anterior: Boas Práticas](09-best-practices.md) | [Voltar ao Índice](README.md) | [Próximo: Referências →](11-references.md)

