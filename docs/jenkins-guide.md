# Guia de Configuração do Jenkins

Este guia descreve como configurar e usar o Jenkins no projeto Bridal Cover CRM.

## 📋 Índice

- [Iniciando o Jenkins](#iniciando-o-jenkins)
- [Configuração Inicial](#configuração-inicial)
- [Configuração do Pipeline](#configuração-do-pipeline)
- [Executando o Pipeline](#executando-o-pipeline)
- [Troubleshooting](#troubleshooting)

## 🚀 Iniciando o Jenkins

### 1. Iniciar o Jenkins

```bash
make jenkins-up
```

Ou diretamente com Docker Compose:

```bash
docker-compose up -d jenkins
```

### 2. Acessar o Jenkins

Abra o navegador em: http://localhost:9090

### 3. Obter a senha inicial

A senha inicial é exibida automaticamente pelo comando `make jenkins-up`, ou você pode obtê-la com:

```bash
make jenkins-password
```

## ⚙️ Configuração Inicial

### 1. Primeiro Acesso

1. Cole a senha inicial obtida no passo anterior
2. Escolha "Install suggested plugins"
3. Aguarde a instalação dos plugins
4. Crie o primeiro usuário administrador
5. Confirme a URL do Jenkins (http://localhost:9090)

### 2. Instalar Plugins Adicionais (Recomendado)

Vá em: **Gerenciar Jenkins > Gerenciar Plugins > Disponíveis**

Instale os seguintes plugins:
- **Docker Pipeline** - Para build de imagens Docker
- **Blue Ocean** - Interface moderna para pipelines
- **Gradle Plugin** - Para builds Gradle
- **HTML Publisher** - Para publicar relatórios HTML
- **JUnit Plugin** - Para relatórios de testes (geralmente já instalado)

### 3. Configurar JDK

Vá em: **Gerenciar Jenkins > Global Tool Configuration**

1. Role até **JDK**
2. Clique em "Add JDK"
3. Nome: `JDK17`
4. Marque "Install automatically"
5. Escolha "Install from adoptium.net"
6. Versão: `jdk-17+35`
7. Salve

## 🔧 Configuração do Pipeline

### Opção 1: Pipeline SCM (Recomendado)

1. Clique em **New Item**
2. Digite o nome: `bridal-cover-crm-pipeline`
3. Escolha **Pipeline**
4. Clique **OK**

Na configuração:

#### General
- Marque "GitHub project" (se estiver usando GitHub)
- URL do projeto: seu repositório

#### Build Triggers
- Marque "Poll SCM" para verificar mudanças
- Schedule: `H/5 * * * *` (verifica a cada 5 minutos)
  
Ou configure Webhook se preferir builds automáticos no push.

#### Pipeline
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: seu repositório
- Credentials: adicione se necessário
- Branch: `*/main` (ou a branch desejada)
- Script Path: `Jenkinsfile`

Salve a configuração.

### Opção 2: Pipeline Local (Para Desenvolvimento)

1. Na seção Pipeline, escolha **Pipeline script**
2. Cole o conteúdo do arquivo `Jenkinsfile`
3. Salve

## 🎯 Executando o Pipeline

### Executar Manualmente

1. Acesse o job criado
2. Clique em **Build Now**
3. Acompanhe o progresso na lista de builds ou no Blue Ocean

### Stages do Pipeline

O pipeline configurado possui os seguintes stages:

1. **Checkout** - Obtém o código do repositório
2. **Build** - Compila a aplicação
3. **Unit Tests** - Executa os testes unitários
4. **Architecture Tests** - Valida a arquitetura (ArchUnit)
5. **Code Quality Analysis** - Análise de qualidade de código
6. **Package** - Gera o JAR da aplicação
7. **Build Docker Image** - Cria imagem Docker (apenas na branch main)
8. **Deploy to Development** - Deploy em desenvolvimento (branch develop)
9. **Deploy to Production** - Deploy em produção (branch main, requer aprovação)

## 📊 Relatórios

Após a execução, você terá acesso a:

- **Test Results** - Relatório JUnit com resultados dos testes
- **Test Report HTML** - Relatório visual detalhado dos testes
- **Console Output** - Log completo da execução

## 🐳 Integração com Docker

O Jenkins está configurado para acessar o Docker do host:

```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

Isso permite que o Jenkins construa imagens Docker dentro do pipeline.

## 🔄 Comandos Úteis

```bash
# Iniciar Jenkins
make jenkins-up

# Parar Jenkins
make jenkins-down

# Ver logs do Jenkins
make jenkins-logs

# Reiniciar Jenkins
make jenkins-restart

# Obter senha inicial
make jenkins-password

# Iniciar todos os serviços (DB + Jenkins)
make start-all

# Parar todos os serviços
make stop-all
```

## 🛠 Troubleshooting

### Jenkins não inicia

```bash
# Verificar logs
make jenkins-logs

# Verificar status do container
docker ps -a | grep jenkins

# Reiniciar Jenkins
make jenkins-restart
```

### Senha inicial não aparece

```bash
# Aguardar Jenkins inicializar completamente
sleep 30

# Tentar obter novamente
make jenkins-password
```

### Erro de permissão no Docker

Se o Jenkins não conseguir executar comandos Docker:

```bash
# Entrar no container
docker exec -it bridal-cover-crm-jenkins bash

# Instalar Docker CLI (se necessário)
apt-get update && apt-get install -y docker.io

# Verificar permissões
ls -la /var/run/docker.sock
```

### Build falha no Gradle

Certifique-se de que:
1. JDK17 está configurado corretamente
2. O arquivo `Jenkinsfile` está no root do projeto
3. O Jenkins tem acesso ao workspace (`./:/workspace` no docker-compose)

### Pipeline não encontra o Jenkinsfile

Verifique:
1. O arquivo `Jenkinsfile` existe no root do repositório
2. O "Script Path" está configurado como `Jenkinsfile`
3. A branch configurada está correta

## 📝 Próximos Passos

1. **Configurar Webhooks** - Para builds automáticos no push
2. **Adicionar SonarQube** - Para análise de qualidade de código
3. **Configurar Notificações** - Email, Slack, etc.
4. **Implementar Deploy Automático** - Para ambientes de staging/produção
5. **Adicionar Testes de Integração** - Com Testcontainers no pipeline
6. **Configurar Backup** - Do volume `jenkins_data`

## 🔗 Recursos Adicionais

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Blue Ocean Documentation](https://www.jenkins.io/doc/book/blueocean/)
- [Docker Pipeline Plugin](https://plugins.jenkins.io/docker-workflow/)

## 📞 Suporte

Para problemas ou dúvidas:
1. Verifique os logs: `make jenkins-logs`
2. Consulte a documentação do Jenkins
3. Verifique os issues do projeto

