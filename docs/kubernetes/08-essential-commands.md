# Comandos Essenciais do Kubernetes

Referência rápida dos comandos kubectl mais utilizados.

## Informações do Cluster

```bash
# Informações do cluster
kubectl cluster-info

# Versão do Kubernetes
kubectl version
kubectl version --short

# Configuração atual
kubectl config view

# Contextos disponíveis
kubectl config get-contexts

# Contexto atual
kubectl config current-context

# Mudar contexto
kubectl config use-context <context-name>

# Mudar namespace padrão
kubectl config set-context --current --namespace=<namespace>
```

---

## Gerenciamento de Recursos

### Criar Recursos

```bash
# Criar a partir de arquivo
kubectl create -f <arquivo.yaml>
kubectl apply -f <arquivo.yaml>

# Criar a partir de diretório
kubectl apply -f <diretório>/

# Criar a partir de URL
kubectl apply -f https://example.com/resource.yaml

# Criar deployment imperativo
kubectl create deployment <name> --image=<image>

# Criar service
kubectl expose deployment <name> --port=80 --target-port=8080

# Criar namespace
kubectl create namespace <name>

# Criar configmap
kubectl create configmap <name> --from-file=<file>
kubectl create configmap <name> --from-literal=key=value

# Criar secret
kubectl create secret generic <name> --from-literal=password=secret
kubectl create secret docker-registry <name> \
  --docker-server=<server> \
  --docker-username=<user> \
  --docker-password=<pass>
```

### Listar Recursos

```bash
# Listar pods
kubectl get pods
kubectl get pods -o wide
kubectl get pods -A                    # Todos os namespaces
kubectl get pods -n <namespace>        # Namespace específico
kubectl get pods --show-labels
kubectl get pods -l app=nginx          # Filtrar por label

# Listar services
kubectl get services
kubectl get svc                        # Abreviação

# Listar deployments
kubectl get deployments
kubectl get deploy                     # Abreviação

# Listar nodes
kubectl get nodes
kubectl get nodes -o wide

# Listar todos os recursos
kubectl get all
kubectl get all -A

# Listar com watch (atualização automática)
kubectl get pods -w

# Formatos de output
kubectl get pods -o yaml
kubectl get pods -o json
kubectl get pods -o jsonpath='{.items[*].metadata.name}'
```

### Descrever Recursos

```bash
# Descrever pod (detalhes completos)
kubectl describe pod <pod-name>
kubectl describe pod <pod-name> -n <namespace>

# Descrever outros recursos
kubectl describe service <name>
kubectl describe deployment <name>
kubectl describe node <name>

# Ver eventos
kubectl get events
kubectl get events -n <namespace>
kubectl get events --sort-by=.metadata.creationTimestamp
```

### Editar Recursos

```bash
# Editar recurso (abre editor)
kubectl edit deployment <name>
kubectl edit service <name>
kubectl edit pod <name>

# Atualizar imagem de deployment
kubectl set image deployment/<name> <container>=<new-image>

# Exemplo
kubectl set image deployment/nginx nginx=nginx:1.16.1

# Atualizar variável de ambiente
kubectl set env deployment/<name> KEY=value

# Adicionar label
kubectl label pods <pod-name> env=prod
kubectl label pods <pod-name> env=prod --overwrite

# Remover label
kubectl label pods <pod-name> env-

# Adicionar annotation
kubectl annotate pods <pod-name> description="My pod"
```

### Deletar Recursos

```bash
# Deletar recurso específico
kubectl delete pod <pod-name>
kubectl delete service <service-name>
kubectl delete deployment <deployment-name>

# Deletar a partir de arquivo
kubectl delete -f <arquivo.yaml>

# Deletar todos os pods de um deployment
kubectl delete pods -l app=nginx

# Deletar todos os recursos de um namespace
kubectl delete all --all -n <namespace>

# Deletar namespace (e todos os recursos dentro)
kubectl delete namespace <name>

# Forçar deleção (use com cuidado!)
kubectl delete pod <pod-name> --force --grace-period=0
```

---

## Trabalhar com Pods

### Logs

```bash
# Ver logs de pod
kubectl logs <pod-name>

# Ver logs em tempo real (follow)
kubectl logs -f <pod-name>

# Ver logs de container específico
kubectl logs <pod-name> -c <container-name>

# Ver logs de todos os containers
kubectl logs <pod-name> --all-containers=true

# Ver últimas N linhas
kubectl logs <pod-name> --tail=100

# Ver logs desde timestamp
kubectl logs <pod-name> --since=1h
kubectl logs <pod-name> --since-time=2024-01-01T00:00:00Z

# Ver logs de pod anterior (se reiniciou)
kubectl logs <pod-name> --previous
```

### Executar Comandos

```bash
# Executar comando em pod
kubectl exec <pod-name> -- <comando>

# Exemplo
kubectl exec nginx-pod -- ls /usr/share/nginx/html

# Shell interativo
kubectl exec -it <pod-name> -- /bin/bash
kubectl exec -it <pod-name> -- /bin/sh

# Container específico
kubectl exec -it <pod-name> -c <container> -- /bin/bash

# Executar comando em múltiplos pods
kubectl exec -it deployment/<name> -- /bin/bash
```

### Port Forward

```bash
# Encaminhar porta de pod local
kubectl port-forward <pod-name> <local-port>:<pod-port>

# Exemplo
kubectl port-forward nginx-pod 8080:80

# Port forward de service
kubectl port-forward service/<service-name> 8080:80

# Port forward de deployment
kubectl port-forward deployment/<name> 8080:80

# Permitir acesso de qualquer IP (não apenas localhost)
kubectl port-forward --address 0.0.0.0 pod/<name> 8080:80
```

### Copiar Arquivos

```bash
# Copiar arquivo para pod
kubectl cp <local-file> <pod-name>:<path>

# Exemplo
kubectl cp app.jar nginx-pod:/tmp/

# Copiar arquivo de pod
kubectl cp <pod-name>:<path> <local-file>

# Exemplo
kubectl cp nginx-pod:/var/log/nginx/access.log ./access.log

# Container específico
kubectl cp <file> <pod>:<path> -c <container>
```

---

## Scaling e Recursos

### Scaling

```bash
# Escalar deployment
kubectl scale deployment <name> --replicas=5

# Escalar replicaset
kubectl scale rs <name> --replicas=3

# Escalar statefulset
kubectl scale statefulset <name> --replicas=3

# Autoscaling
kubectl autoscale deployment <name> --min=2 --max=10 --cpu-percent=80
```

### Recursos (CPU/Memória)

```bash
# Ver uso de recursos dos nodes
kubectl top nodes

# Ver uso de recursos dos pods
kubectl top pods
kubectl top pods -A
kubectl top pods -n <namespace>

# Ordenar por CPU
kubectl top pods --sort-by=cpu

# Ordenar por memória
kubectl top pods --sort-by=memory

# Ver uso de um pod específico
kubectl top pod <pod-name>
```

---

## Deployments e Rollouts

### Gerenciar Deployments

```bash
# Ver status de rollout
kubectl rollout status deployment/<name>

# Ver histórico de rollout
kubectl rollout history deployment/<name>

# Ver detalhes de revisão específica
kubectl rollout history deployment/<name> --revision=2

# Pausar rollout
kubectl rollout pause deployment/<name>

# Retomar rollout
kubectl rollout resume deployment/<name>

# Rollback para revisão anterior
kubectl rollout undo deployment/<name>

# Rollback para revisão específica
kubectl rollout undo deployment/<name> --to-revision=2

# Reiniciar deployment (recria todos os pods)
kubectl rollout restart deployment/<name>
```

---

## Namespaces

```bash
# Listar namespaces
kubectl get namespaces
kubectl get ns

# Criar namespace
kubectl create namespace <name>

# Deletar namespace
kubectl delete namespace <name>

# Ver recursos de namespace específico
kubectl get all -n <namespace>

# Alternar namespace padrão
kubectl config set-context --current --namespace=<namespace>

# Ver configuração de um namespace
kubectl describe namespace <name>
```

---

## Labels e Selectors

```bash
# Listar pods com labels
kubectl get pods --show-labels

# Filtrar por label
kubectl get pods -l app=nginx
kubectl get pods -l env=prod,tier=frontend
kubectl get pods -l 'env in (prod,staging)'
kubectl get pods -l 'env notin (dev,test)'

# Adicionar label
kubectl label pods <pod> env=prod

# Atualizar label
kubectl label pods <pod> env=staging --overwrite

# Remover label
kubectl label pods <pod> env-

# Listar todos os recursos com label
kubectl get all -l app=myapp
```

---

## ConfigMaps e Secrets

### ConfigMaps

```bash
# Criar configmap
kubectl create configmap <name> --from-file=config.txt
kubectl create configmap <name> --from-literal=key1=value1

# Ver configmaps
kubectl get configmaps
kubectl get cm

# Ver conteúdo de configmap
kubectl describe configmap <name>
kubectl get configmap <name> -o yaml

# Editar configmap
kubectl edit configmap <name>

# Deletar configmap
kubectl delete configmap <name>
```

### Secrets

```bash
# Criar secret
kubectl create secret generic <name> --from-literal=password=secret
kubectl create secret generic <name> --from-file=ssh-key=~/.ssh/id_rsa

# Ver secrets
kubectl get secrets

# Ver conteúdo de secret (base64 encoded)
kubectl get secret <name> -o yaml

# Decodificar secret
kubectl get secret <name> -o jsonpath='{.data.password}' | base64 --decode

# Editar secret
kubectl edit secret <name>

# Deletar secret
kubectl delete secret <name>
```

---

## Debug e Troubleshooting

```bash
# Criar pod de debug temporário
kubectl run debug --rm -it --image=busybox -- /bin/sh
kubectl run debug --rm -it --image=nicolaka/netshoot -- /bin/bash

# Debug de rede
kubectl run curl --rm -it --image=curlimages/curl -- sh

# Ver eventos do cluster
kubectl get events --sort-by=.metadata.creationTimestamp
kubectl get events -w

# Ver logs de componentes do sistema
kubectl logs -n kube-system <pod-name>

# Verificar certificados
kubectl get csr

# Ver resource quotas
kubectl get resourcequotas
kubectl describe resourcequota <name>

# Ver limit ranges
kubectl get limitranges
kubectl describe limitrange <name>
```

---

## Contextos e Configuração

```bash
# Ver configuração do kubectl
kubectl config view

# Listar contextos
kubectl config get-contexts

# Contexto atual
kubectl config current-context

# Mudar contexto
kubectl config use-context <context>

# Renomear contexto
kubectl config rename-context <old> <new>

# Deletar contexto
kubectl config delete-context <context>

# Configurar cluster
kubectl config set-cluster <name> --server=https://...

# Configurar credenciais
kubectl config set-credentials <name> --token=...

# Configurar contexto
kubectl config set-context <name> --cluster=<cluster> --user=<user>
```

---

## Atalhos e Aliases Úteis

```bash
# Atalhos padrão do kubectl
k get po          # pods
k get svc         # services
k get ns          # namespaces
k get no          # nodes
k get deploy      # deployments
k get rs          # replicasets
k get cm          # configmaps
k get pv          # persistentvolumes
k get pvc         # persistentvolumeclaims
k get ing         # ingresses

# Comando completo
kubectl get <resource> -n <namespace> -o <output> -w
```

---

## Dicas de Produtividade

```bash
# Ver definição de recurso
kubectl explain pods
kubectl explain pods.spec
kubectl explain pods.spec.containers

# Dry-run (não cria o recurso)
kubectl create deployment nginx --image=nginx --dry-run=client -o yaml

# Gerar YAML de recursos existentes
kubectl get deployment nginx -o yaml > nginx-deployment.yaml

# Comparar diferenças antes de aplicar
kubectl diff -f deployment.yaml

# Aguardar condição
kubectl wait --for=condition=ready pod -l app=nginx
kubectl wait --for=delete pod/nginx --timeout=60s

# Plugin kubectl-tree (instalar separadamente)
kubectl tree deployment nginx
```

---

## Próximos Passos

Agora que você domina os comandos essenciais:

- **[Ver Boas Práticas](09-best-practices.md)** - Usar kubectl de forma eficiente
- **[Kubernetes Dashboard](10-dashboard-setup.md)** - Interface gráfica
- **[Referências](11-references.md)** - Ferramentas e recursos adicionais

---

[← Anterior: Pós-Instalação](07-post-installation.md) | [Voltar ao Índice](README.md) | [Próximo: Boas Práticas →](09-best-practices.md)

