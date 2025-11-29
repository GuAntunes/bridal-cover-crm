# 🔧 Solução Rápida - Problema de PVC

## Seu Problema

```
Warning  FailedScheduling  pod has unbound immediate PersistentVolumeClaims
```

Isso significa que o Kubernetes não consegue criar o volume para armazenar os dados do PostgreSQL.

---

## ✅ Solução Mais Rápida (1 comando)

Execute este comando no diretório `helm-chart/postgresql/`:

```bash
make fix-pvc-issues
```

Este comando vai:
1. ✅ Instalar o local-path-provisioner (gerenciador de volumes)
2. ✅ Remover a instalação atual com problema
3. ✅ Reinstalar o PostgreSQL corretamente

**Pronto! Problema resolvido!**

---

## 🔍 Alternativa: Diagnosticar primeiro

Se quiser entender o problema antes de corrigir:

```bash
# 1. Ver o que está acontecendo
make diagnose

# 2. Corrigir automaticamente
make fix-pvc-issues
```

---

## 🚀 Outras Opções

### Opção 1: Instalar sem persistência (teste rápido)

⚠️ **ATENÇÃO**: Os dados serão perdidos ao reiniciar o pod!

```bash
# Remover instalação atual
make uninstall-dev

# Instalar sem volume persistente
make install-dev-no-pvc

# Verificar
make get-pods-dev
```

### Opção 2: Instalar o provisioner manualmente

```bash
# 1. Instalar local-path-provisioner
make install-local-path-provisioner

# 2. Remover instalação atual
make uninstall-dev
kubectl delete pvc -n bridal-crm --all

# 3. Reinstalar usando local-path
make install-dev-local-path

# 4. Verificar
make get-pods-dev
```

---

## ✅ Verificar se Funcionou

Após aplicar qualquer solução, verifique:

```bash
# 1. Pod deve estar Running
make get-pods-dev
# Deve mostrar: 1/1 Running

# 2. Ver logs (deve mostrar PostgreSQL iniciado)
make logs-dev
# Deve mostrar: "database system is ready to accept connections"

# 3. Testar conexão
make psql-dev
```

---

## 📚 Documentação Completa

Para entender melhor o problema e ver todas as soluções possíveis:

👉 **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)**

---

## 🎯 Resumo dos Comandos

```bash
# SOLUÇÃO AUTOMÁTICA (recomendado)
make fix-pvc-issues

# OU PASSO A PASSO
make diagnose                          # Ver o problema
make install-local-path-provisioner    # Instalar provisioner
make uninstall-dev                     # Remover instalação atual
kubectl delete pvc -n bridal-crm --all # Limpar volumes
make install-dev-local-path            # Reinstalar

# OU SEM PERSISTÊNCIA (teste rápido)
make uninstall-dev
make install-dev-no-pvc
```

---

## 💡 Dica

Para desenvolvimento local, é normal precisar instalar um storage provisioner. O `local-path-provisioner` é perfeito para isso e o comando `make fix-pvc-issues` faz tudo automaticamente!

**Boa sorte!** 🚀

