# 🆘 Troubleshooting - PostgreSQL Helm Chart

## ❌ Problema: PVC não consegue bind (PersistentVolumeClaim unbound)

### Erro:
```
Warning  FailedScheduling  pod has unbound immediate PersistentVolumeClaims
```

### Causa:
O cluster Kubernetes não tem um PersistentVolume (PV) disponível ou não tem um storage provisioner configurado.

### Soluções:

---

## Solução 1: Instalar Local Path Provisioner (Recomendado para Dev) ⭐

Este é um provisionador de armazenamento que cria volumes automaticamente.

```bash
# Instalar local-path-provisioner
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml

# Tornar o local-path a storageClass padrão
kubectl patch storageclass local-path -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'

# Verificar
kubectl get storageclass

# Reinstalar o PostgreSQL
make uninstall-dev
kubectl delete pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data
make install-dev
```

---

## Solução 2: Usar valores específicos para local storage

Já criei um arquivo de valores específico para usar com o local-path provisioner:

```bash
# Desinstalar instalação atual
make uninstall-dev
kubectl delete pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data

# Instalar com storageClass local-path
helm install postgresql-dev . \
  --namespace bridal-crm \
  --create-namespace \
  --values values-dev.yaml \
  --set persistence.storageClass=local-path
```

---

## Solução 3: Criar PersistentVolume manualmente

Se você não pode instalar o provisioner, crie um PV manualmente:

```bash
# 1. Criar o PV
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: PersistentVolume
metadata:
  name: postgresql-dev-pv
spec:
  capacity:
    storage: 2Gi
  accessModes:
    - ReadWriteOnce
  persistentVolumeReclaimPolicy: Retain
  storageClassName: ""
  hostPath:
    path: /tmp/postgresql-data
    type: DirectoryOrCreate
EOF

# 2. Verificar PV criado
kubectl get pv

# 3. Reinstalar PostgreSQL
make uninstall-dev
kubectl delete pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data
make install-dev
```

---

## Solução 4: Desabilitar persistência (apenas para testes rápidos)

⚠️ **CUIDADO**: Os dados serão perdidos quando o pod for deletado!

```bash
# Desinstalar
make uninstall-dev

# Reinstalar sem persistência
helm install postgresql-dev . \
  --namespace bridal-crm \
  --create-namespace \
  --values values-dev.yaml \
  --set persistence.enabled=false
```

---

## Solução 5: Usar Minikube com addon (se estiver usando Minikube)

```bash
# Habilitar storage provisioner do Minikube
minikube addons enable storage-provisioner
minikube addons enable default-storageclass

# Verificar
kubectl get storageclass

# Reinstalar
make uninstall-dev
kubectl delete pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data
make install-dev
```

---

## Diagnóstico: Comandos para verificar o problema

```bash
# 1. Ver PVC
kubectl get pvc -n bridal-crm

# 2. Descrever PVC (ver eventos)
kubectl describe pvc -n bridal-crm postgresql-dev-bridal-cover-crm-postgresql-data

# 3. Ver PV disponíveis
kubectl get pv

# 4. Ver StorageClasses
kubectl get storageclass

# 5. Ver eventos
kubectl get events -n bridal-crm --sort-by='.lastTimestamp'

# 6. Ver status do pod
kubectl get pods -n bridal-crm
kubectl describe pod -n bridal-crm <pod-name>
```

---

## Verificar qual solução funcionou

```bash
# Após aplicar uma solução, verificar:

# 1. PVC deve estar "Bound"
kubectl get pvc -n bridal-crm
# STATUS deve mostrar: Bound

# 2. Pod deve estar "Running"
kubectl get pods -n bridal-crm
# STATUS deve mostrar: 1/1 Running

# 3. Logs devem mostrar PostgreSQL iniciado
make logs-dev
# Deve mostrar: "database system is ready to accept connections"
```

---

## Recomendação por Ambiente

### Desenvolvimento Local (Minikube/Kind)
✅ **Solução 1** - Instalar local-path-provisioner (melhor opção)  
✅ **Solução 5** - Usar addon do Minikube (se usando Minikube)

### Cluster de Desenvolvimento Real
✅ **Solução 1** - Instalar local-path-provisioner  
✅ **Solução 2** - Especificar storageClass adequada

### Produção
⚠️ Use storage provisioner adequado do seu cloud provider:
- AWS: EBS (gp3)
- GCP: PD-SSD
- Azure: Azure Disk

---

## Outros Problemas Comuns

### Pod está em CrashLoopBackOff

```bash
# Ver logs
kubectl logs -n bridal-crm <pod-name>

# Ver logs do container anterior
kubectl logs -n bridal-crm <pod-name> --previous

# Verificar permissões
kubectl describe pod -n bridal-crm <pod-name>
```

### Erro de permissão no volume

```bash
# Ver eventos do pod
kubectl describe pod -n bridal-crm <pod-name>

# Verificar security context
kubectl get pod -n bridal-crm <pod-name> -o yaml | grep -A 10 securityContext
```

### ImagePullBackOff

```bash
# Ver detalhes
kubectl describe pod -n bridal-crm <pod-name>

# A imagem postgres:15-alpine deve estar disponível
# Verificar se há problemas de rede
```

---

## Limpeza Completa (Reset)

Se nada funcionar, limpe tudo e comece de novo:

```bash
# 1. Desinstalar Helm release
helm uninstall postgresql-dev -n bridal-crm

# 2. Deletar PVC
kubectl delete pvc -n bridal-crm --all

# 3. Deletar PV (se criou manualmente)
kubectl delete pv postgresql-dev-pv

# 4. Deletar namespace (opcional)
kubectl delete namespace bridal-crm

# 5. Recriar namespace
kubectl create namespace bridal-crm

# 6. Aplicar solução escolhida
# (Exemplo: Instalar local-path-provisioner)
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.24/deploy/local-path-storage.yaml

# 7. Reinstalar PostgreSQL
make install-dev
```

---

## Suporte Adicional

Se o problema persistir:

1. Verifique os logs: `make logs-dev`
2. Veja eventos: `kubectl get events -n bridal-crm --sort-by='.lastTimestamp'`
3. Descreva o pod: `kubectl describe pod -n bridal-crm <pod-name>`
4. Verifique StorageClass: `kubectl get storageclass`
5. Verifique PV: `kubectl get pv`

---

**Dica**: Para desenvolvimento local, a **Solução 1** (local-path-provisioner) é a mais robusta e funciona na maioria dos casos!

