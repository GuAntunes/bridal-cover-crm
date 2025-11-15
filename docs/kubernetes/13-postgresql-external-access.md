# 🗄️ Como Conectar no PostgreSQL do Kubernetes

Este guia ensina como conectar no banco de dados PostgreSQL rodando no Kubernetes a partir de outras máquinas.

## 📋 Informações do Banco

- **Host:** Varia conforme o método de acesso (veja abaixo)
- **Porta:** `5432`
- **Database:** `bridal_cover_crm_dev`
- **Username:** `postgres`
- **Password:** `postgres`

---

## 🔌 Métodos de Conexão

### **Método 1: Port Forward (Mais Simples)** ⭐

O mais fácil para desenvolvimento. Encaminha a porta do PostgreSQL para sua máquina local.

#### **Do cluster (máquina onde roda o K8s):**

```bash
# Expor PostgreSQL na porta 5432 local
kubectl port-forward svc/postgres-service 5432:5432

# Ou em outra porta se 5432 já estiver em uso
kubectl port-forward svc/postgres-service 5433:5432
```

#### **De outra máquina da rede:**

```bash
# SSH tunnel para o cluster
ssh -L 5432:localhost:5432 gustavo@192.168.15.7

# Em outra janela do terminal da máquina onde rodou o SSH
# Conectar via psql ou ferramenta gráfica em localhost:5432
```

**Depois conecte em:** `localhost:5432`

---

### **Método 2: NodePort (Acesso Direto pela Rede)**

Expor o PostgreSQL diretamente na rede através de uma porta no node.

#### **1. Criar Service NodePort**

Crie o arquivo `k8s/base/postgres-nodeport.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres-nodeport
  labels:
    app: postgres
spec:
  type: NodePort
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
    nodePort: 30432  # Porta que será exposta no node
    protocol: TCP
```

#### **2. Aplicar:**

```bash
kubectl apply -f k8s/base/postgres-nodeport.yaml
```

#### **3. Conectar de qualquer máquina da rede:**

```
Host: 192.168.15.7  (IP do node do Kubernetes)
Port: 30432
Database: bridal_cover_crm_dev
Username: postgres
Password: postgres
```

⚠️ **Atenção:** Só use em rede confiável! Para produção, use port-forward ou configure firewall.

---

### **Método 3: LoadBalancer (Cloud)**

Se estiver em cloud (AWS, GCP, Azure), pode usar LoadBalancer:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: postgres-lb
spec:
  type: LoadBalancer
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
```

Depois pegar o IP externo:
```bash
kubectl get svc postgres-lb
```

---

## 🛠️ Ferramentas Recomendadas

### **1. DBeaver (Free & Poderoso)** ⭐

**Download:** https://dbeaver.io/download/

#### **Configuração:**

1. Abrir DBeaver
2. **Database** → **New Database Connection**
3. Selecionar **PostgreSQL**
4. Configurar:
   ```
   Host: localhost (com port-forward)
   ou
   Host: 192.168.15.7 (com NodePort)
   
   Port: 5432 (port-forward) ou 30432 (NodePort)
   Database: bridal_cover_crm_dev
   Username: postgres
   Password: postgres
   ```
5. **Test Connection** → **Finish**

**Recursos:**
- ✅ Gratuito e open-source
- ✅ Multi-plataforma (Mac, Windows, Linux)
- ✅ Suporta dezenas de bancos
- ✅ Query editor poderoso
- ✅ ER Diagrams automáticos

---

### **2. DataGrip (JetBrains - Pago)**

**Download:** https://www.jetbrains.com/datagrip/

Melhor IDE de banco de dados, mas paga. Integra com IntelliJ IDEA.

---

### **3. TablePlus (Mac/Windows - Freemium)**

**Download:** https://tableplus.com/

Interface moderna e bonita. Versão gratuita limitada.

---

### **4. Postico (Mac only)**

**Download:** https://eggerapps.at/postico/

Interface nativa do macOS, muito bonita.

---

### **5. psql (CLI - Terminal)**

Já vem com PostgreSQL client.

#### **Instalar:**

**macOS:**
```bash
brew install postgresql@15
```

**Ubuntu/Debian:**
```bash
sudo apt-get install postgresql-client
```

**Windows:**
```bash
# Via WSL ou baixar: https://www.postgresql.org/download/windows/
```

#### **Conectar:**

**Com port-forward ativo:**
```bash
psql -h localhost -p 5432 -U postgres -d bridal_cover_crm_dev
# Senha: postgres
```

**Com NodePort:**
```bash
psql -h 192.168.15.7 -p 30432 -U postgres -d bridal_cover_crm_dev
```

#### **Comandos úteis no psql:**

```sql
-- Listar databases
\l

-- Conectar em outro database
\c bridal_cover_crm_dev

-- Listar tabelas
\dt

-- Descrever tabela
\d leads

-- Ver usuários
\du

-- Executar SQL
SELECT * FROM leads;

-- Sair
\q
```

---

## 🔐 Segurança - Melhores Práticas

### **Para Desenvolvimento:**
✅ Port Forward (mais seguro, não expõe na rede)

### **Para Produção:**

1. **NUNCA exponha PostgreSQL diretamente na internet!**

2. **Use senhas fortes:**
   ```bash
   # Alterar senha no Kubernetes
   kubectl edit secret db-credentials
   ```

3. **Configure SSL/TLS**

4. **Use VPN ou bastion host**

5. **Configure pg_hba.conf** para restringir IPs

6. **Considere usar banco gerenciado** (RDS, Cloud SQL)

---

## 📝 Passo a Passo Rápido

### **Para sua máquina de desenvolvimento:**

1. **Abrir terminal no cluster:**
   ```bash
   kubectl port-forward svc/postgres-service 5432:5432
   ```

2. **Instalar DBeaver** (se não tiver)

3. **Conectar no DBeaver:**
   - Host: `localhost`
   - Port: `5432`
   - Database: `bridal_cover_crm_dev`
   - User: `postgres`
   - Password: `postgres`

4. **Pronto!** 🎉

---

### **Para outra máquina da rede:**

#### **Opção A: SSH Tunnel (Mais Seguro)**

```bash
# Na sua máquina remota
ssh -L 5432:localhost:5432 gustavo@192.168.15.7

# Depois conectar em localhost:5432 com DBeaver
```

#### **Opção B: NodePort (Mais Direto)**

1. **No cluster, criar NodePort:**
   ```bash
   kubectl apply -f k8s/base/postgres-nodeport.yaml
   ```

2. **Na máquina remota, conectar no DBeaver:**
   - Host: `192.168.15.7`
   - Port: `30432`
   - Database: `bridal_cover_crm_dev`
   - User: `postgres`
   - Password: `postgres`

---

## 🐛 Troubleshooting

### **Problema: "Connection refused"**

```bash
# Verificar se o PostgreSQL está rodando
kubectl get pods -l app=postgres

# Ver logs
kubectl logs postgres-0

# Verificar service
kubectl get svc postgres-service
```

### **Problema: "Timeout"**

```bash
# Verificar se port-forward está ativo
# Deve estar rodando em um terminal

# Ou verificar se NodePort está acessível
telnet 192.168.15.7 30432
```

### **Problema: "Autenticação falhou"**

```bash
# Verificar credenciais no secret
kubectl get secret db-credentials -o jsonpath='{.data.username}' | base64 -d
kubectl get secret db-credentials -o jsonpath='{.data.password}' | base64 -d
```

### **Problema: Firewall bloqueando**

```bash
# Ubuntu - Abrir porta NodePort
sudo ufw allow 30432/tcp

# Verificar
sudo ufw status
```

---

## 📊 Comandos Úteis

```bash
# Ver todos os services
kubectl get svc

# Port forward em background
kubectl port-forward svc/postgres-service 5432:5432 &

# Matar port forward
pkill -f "port-forward"

# Ver conexões ativas no PostgreSQL
kubectl exec -it postgres-0 -- psql -U postgres -c "SELECT * FROM pg_stat_activity;"

# Backup do banco
kubectl exec -it postgres-0 -- pg_dump -U postgres bridal_cover_crm_dev > backup.sql

# Restore do banco
kubectl exec -i postgres-0 -- psql -U postgres bridal_cover_crm_dev < backup.sql
```

---

## 🎯 Recomendação Final

**Para você (desenvolvedor local):**

1. Use **port-forward** + **DBeaver** 
2. Simples, seguro e rápido
3. Não expõe nada na rede

**Comando:**
```bash
# Terminal 1: Port forward (deixar rodando)
kubectl port-forward svc/postgres-service 5432:5432

# Terminal 2: Ou sua máquina
# Conectar com DBeaver em localhost:5432
```

---

**Criado por:** BridalCover CRM Platform Team  
**Data:** 2025-11-15  
**Versão:** 1.0

