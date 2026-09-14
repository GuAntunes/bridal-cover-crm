# Jenkins - CI/CD e Automação de Build

## 📖 O que é Jenkins?

**Jenkins** é uma ferramenta open-source de automação que permite implementar **Continuous Integration (CI)** e **Continuous Deployment (CD)**. Ele automatiza a construção, teste e deploy de aplicações, garantindo que mudanças no código sejam integradas e validadas continuamente.

### Características Principais

- **Automação Completa**: Build, teste, análise de código e deploy
- **Pipeline as Code**: Definição de pipelines em código (Jenkinsfile)
- **Extensível**: +1800 plugins disponíveis
- **Distribuído**: Suporte a agents/workers distribuídos
- **Multi-plataforma**: Suporte a múltiplas linguagens e tecnologias
- **Open Source**: Comunidade ativa e gratuito

## 🔧 Implementação no Projeto

### Versão Utilizada

Utilizamos a imagem oficial **jenkins/jenkins:lts-jdk17** (Long Term Support com JDK 17):

```yaml
jenkins:
  image: jenkins/jenkins:lts-jdk17
  ports:
    - "9090:8080"
    - "50000:50000"
```

### Por que JDK 17?

- ✅ Mesma versão JDK do projeto (consistência)
- ✅ LTS (Long Term Support) - suporte de longo prazo
- ✅ Compatibilidade com Gradle 8.5
- ✅ Suporte a features modernas do Java/Kotlin

## 🐳 Configuração Docker

### docker-compose.yml

```yaml
jenkins:
  image: jenkins/jenkins:lts-jdk17
  container_name: bridal-cover-crm-jenkins
  user: root
  environment:
    JAVA_OPTS: "-Djenkins.install.runSetupWizard=false"
    JENKINS_OPTS: "--httpPort=8080"
  ports:
    - "9090:8080"    # UI
    - "50000:50000"  # Agents
  volumes:
    - jenkins_data:/var/jenkins_home              # Persistência
    - /var/run/docker.sock:/var/run/docker.sock  # Docker-in-Docker
    - ./:/workspace                                # Código fonte
  networks:
    - bridal-network
  restart: unless-stopped
```

### Explicação das Configurações

#### **Portas**
- **9090:8080**: Interface web do Jenkins (9090 no host para evitar conflito com a aplicação)
- **50000:50000**: Comunicação com agents distribuídos

#### **Volumes**
- **jenkins_data**: Persistência de configurações, jobs, histórico de builds
- **/var/run/docker.sock**: Permite Jenkins executar comandos Docker do host
- **./:/workspace**: Acesso ao código fonte do projeto

#### **Variáveis de Ambiente**
- **JAVA_OPTS**: Desabilita wizard inicial (opcional)
- **JENKINS_OPTS**: Configurações do servidor Jenkins

#### **Network**
- **bridal-network**: Compartilhada com PostgreSQL e PgAdmin, permite comunicação entre containers

## 📋 Pipeline as Code (Jenkinsfile)

### O que é um Jenkinsfile?

Um **Jenkinsfile** é um arquivo de texto que contém a definição de um pipeline Jenkins usando a sintaxe do **Pipeline DSL** (Domain Specific Language), baseado em Groovy.

### Vantagens

- ✅ **Versionado**: Pipeline no Git junto com o código
- ✅ **Revisável**: Code review do pipeline
- ✅ **Reutilizável**: Compartilhamento entre projetos
- ✅ **Testável**: Mudanças no pipeline podem ser testadas em branches

### Estrutura do Pipeline

```groovy
pipeline {
    agent any
    
    tools {
        jdk 'JDK17'
    }
    
    environment {
        // Variáveis globais do pipeline
    }
    
    stages {
        stage('Nome') {
            steps {
                // Ações a executar
            }
            post {
                // Ações pós-execução
            }
        }
    }
    
    post {
        // Ações finais (sempre, sucesso, falha)
    }
}
```

## 🎯 Pipeline Implementado

### Stages do Pipeline

#### 1. **Checkout**
```groovy
stage('Checkout') {
    steps {
        checkout scm
    }
}
```
- Obtém o código fonte do repositório Git
- `scm` = Source Control Management (configurado no job)

#### 2. **Build**
```groovy
stage('Build') {
    steps {
        sh './gradlew clean build -x test'
    }
}
```
- Compila a aplicação Kotlin/Spring Boot
- `-x test`: Pula testes (executados em stage próprio)
- Valida que o código compila sem erros

#### 3. **Unit Tests**
```groovy
stage('Unit Tests') {
    steps {
        sh './gradlew test'
    }
    post {
        always {
            junit '**/build/test-results/test/*.xml'
            publishHTML([...])
        }
    }
}
```
- Executa todos os testes unitários
- Publica relatórios JUnit (XML)
- Gera relatório HTML visual
- **Sempre** executa, mesmo se testes falharem

#### 4. **Architecture Tests**
```groovy
stage('Architecture Tests') {
    steps {
        sh './gradlew test --tests "*ArchitectureTest"'
    }
}
```
- Executa testes de arquitetura (ArchUnit)
- Valida camadas hexagonais
- Garante dependências corretas

#### 5. **Code Quality Analysis**
```groovy
stage('Code Quality Analysis') {
    steps {
        sh './gradlew check'
    }
}
```
- Análise de qualidade de código
- Pronto para integrar SonarQube
- Verifica convenções de código

#### 6. **Package**
```groovy
stage('Package') {
    steps {
        sh './gradlew bootJar'
    }
}
```
- Gera JAR executável da aplicação
- Artefato pronto para deploy
- Localizado em `build/libs/`

#### 7. **Build Docker Image**
```groovy
stage('Build Docker Image') {
    when {
        branch 'main'
    }
    steps {
        script {
            def appVersion = sh(...)
            sh "docker build -t bridal-cover-crm:${appVersion} ."
            sh "docker tag bridal-cover-crm:${appVersion} bridal-cover-crm:latest"
        }
    }
}
```
- **Condicional**: Apenas na branch `main`
- Constrói imagem Docker da aplicação
- Usa versionamento do `build.gradle.kts`
- Cria tags versionada e `latest`

#### 8. **Deploy to Development**
```groovy
stage('Deploy to Development') {
    when {
        branch 'develop'
    }
    steps {
        // Deploy automático para dev
    }
}
```
- **Condicional**: Apenas na branch `develop`
- Deploy automático para ambiente de desenvolvimento
- Placeholder para implementação futura

#### 9. **Deploy to Production**
```groovy
stage('Deploy to Production') {
    when {
        branch 'main'
    }
    steps {
        input message: 'Deploy to production?', ok: 'Deploy'
        // Deploy para produção
    }
}
```
- **Condicional**: Apenas na branch `main`
- **Aprovação Manual**: Requer confirmação humana
- Deploy para ambiente de produção
- Placeholder para implementação futura

### Post Actions

```groovy
post {
    always {
        cleanWs()  // Limpa workspace
    }
    success {
        // Notificações de sucesso
    }
    failure {
        // Notificações de falha
    }
}
```

## 🔄 Fluxo de CI/CD

### Desenvolvimento Local
```
1. Developer faz commit/push
   ↓
2. Jenkins detecta mudança (poll ou webhook)
   ↓
3. Checkout do código
   ↓
4. Build + Testes
   ↓
5. Feedback ao desenvolvedor
```

### Branch Strategy

#### **Feature Branches**
```
feature/* → Build + Tests
```
- Validação básica
- Testes unitários e arquitetura
- Sem deploy

#### **Branch Develop**
```
develop → Build + Tests + Deploy Dev
```
- Validação completa
- Deploy automático para desenvolvimento
- Sem aprovação manual

#### **Branch Main**
```
main → Build + Tests + Docker + Deploy Prod (aprovação)
```
- Validação completa
- Build de imagem Docker
- Deploy para produção com aprovação manual

## 🛠️ Ferramentas e Plugins

### Plugins Essenciais (Já Instalados)

| Plugin | Função |
|--------|--------|
| **Git** | Integração com Git/GitHub |
| **Pipeline** | Suporte a Jenkinsfile |
| **JUnit** | Relatórios de testes |
| **HTML Publisher** | Publicação de relatórios HTML |
| **Gradle** | Suporte a builds Gradle |

### Plugins Recomendados (Opcional)

| Plugin | Função | Benefício |
|--------|--------|-----------|
| **Blue Ocean** | UI moderna para pipelines | Visualização melhorada |
| **Docker Pipeline** | Steps Docker no pipeline | Build/push de imagens |
| **SonarQube Scanner** | Análise de qualidade | Code quality detalhada |
| **Slack Notification** | Notificações no Slack | Alertas em tempo real |
| **Email Extension** | Emails avançados | Notificações personalizadas |
| **Credentials Binding** | Gestão de credenciais | Segurança aprimorada |
| **GitHub Integration** | Integração GitHub | Status checks, webhooks |

## 📊 Relatórios e Métricas

### JUnit Test Results

```groovy
junit '**/build/test-results/test/*.xml'
```

**Informações Disponíveis:**
- Total de testes executados
- Testes com sucesso
- Testes falhados
- Tempo de execução
- Tendência ao longo do tempo

### HTML Reports

```groovy
publishHTML([
    reportDir: 'build/reports/tests/test',
    reportFiles: 'index.html',
    reportName: 'Test Report'
])
```

**Visualização:**
- Relatório detalhado por classe
- Stack traces de falhas
- Cobertura de código (se configurado)

### Trends

Jenkins automaticamente rastreia:
- ✅ Taxa de sucesso dos builds
- ✅ Tempo médio de build
- ✅ Estabilidade dos testes
- ✅ Tamanho dos artefatos

## 🔐 Segurança e Boas Práticas

### Credenciais

**Nunca** coloque senhas/tokens no Jenkinsfile:

```groovy
// ❌ ERRADO
environment {
    API_KEY = "abc123"
}

// ✅ CORRETO
environment {
    API_KEY = credentials('api-key-id')
}
```

### Isolamento

```yaml
user: root  # Apenas para ambiente de desenvolvimento
```

**Produção**: Use usuário não-root e permissões adequadas

### Volumes Sensíveis

```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
```

**Atenção**: Permite Jenkins executar qualquer comando Docker no host

## 🚀 Comandos Úteis (Makefile)

### Gerenciamento do Jenkins

```bash
# Iniciar Jenkins
make jenkins-up

# Parar Jenkins
make jenkins-down

# Ver logs em tempo real
make jenkins-logs

# Obter senha inicial
make jenkins-password

# Reiniciar Jenkins
make jenkins-restart
```

### Operações Completas

```bash
# Iniciar todos os serviços
make start-all

# Parar todos os serviços
make stop-all

# Ver comandos disponíveis
make help
```

## 📈 Integração com Ferramentas

### SonarQube (Análise de Código)

```groovy
stage('SonarQube Analysis') {
    steps {
        withSonarQubeEnv('SonarQube') {
            sh './gradlew sonarqube'
        }
    }
}
```

### Docker Registry

```groovy
stage('Push Docker Image') {
    steps {
        script {
            docker.withRegistry('https://registry.exemplo.com', 'docker-credentials') {
                docker.image("app:${version}").push()
            }
        }
    }
}
```

### GitOps (platform-gitops)

Deploy em cluster não usa manifests neste repo. Após push da imagem Docker, atualize a tag no repositório **platform-gitops** e dispare sync no Argo CD (manual ou via API/CLI).

## 🔄 Webhooks e Triggers

### Poll SCM (Polling)

```groovy
triggers {
    pollSCM('H/5 * * * *')  // Verifica a cada 5 minutos
}
```

### GitHub Webhooks (Recomendado)

**Configuração no GitHub:**
1. Settings → Webhooks → Add webhook
2. URL: `http://jenkins:9090/github-webhook/`
3. Events: `push`, `pull_request`

**Vantagem**: Build instantâneo ao fazer push

### Cron Triggers

```groovy
triggers {
    cron('H 2 * * *')  // Executa diariamente às 2h
}
```

## 🧪 Testes de Pipeline

### Validação de Jenkinsfile

```bash
# Validar sintaxe localmente
curl -X POST -F "jenkinsfile=<Jenkinsfile" \
  http://localhost:9090/pipeline-model-converter/validate
```

### Testes em Branch

```groovy
// Jenkinsfile
if (env.BRANCH_NAME == 'test-pipeline') {
    // Mudanças experimentais
}
```

## 📋 Troubleshooting

### Build Falha - "Permission Denied"

```bash
# Dar permissão ao gradlew
chmod +x gradlew
git add gradlew
git commit -m "Add execute permission to gradlew"
```

### Jenkins não Inicia

```bash
# Verificar logs
make jenkins-logs

# Verificar porta em uso
lsof -i :9090

# Limpar e reiniciar
docker-compose down
docker volume rm bridal-cover-crm_jenkins_data
make jenkins-up
```

### Workspace Cheio

```bash
# Entrar no container
docker exec -it bridal-cover-crm-jenkins bash

# Limpar workspaces antigos
cd /var/jenkins_home/workspace
rm -rf */

# Ou via Jenkins UI
Manage Jenkins → Manage Nodes → master → Disk Usage
```

### Docker Commands Falham

```bash
# Instalar Docker CLI no container
docker exec -it bridal-cover-crm-jenkins bash
apt-get update && apt-get install -y docker.io

# Verificar socket
ls -la /var/run/docker.sock
```

## 📊 Métricas de Qualidade

### Build Health

- **Build Success Rate**: % de builds bem-sucedidos
- **Mean Time to Repair (MTTR)**: Tempo médio para corrigir build quebrado
- **Build Duration**: Tempo médio de build

### Test Health

- **Test Success Rate**: % de testes passando
- **Test Stability**: Consistência dos resultados
- **Test Coverage**: % de cobertura de código

### Deployment

- **Deployment Frequency**: Quantos deploys por dia/semana
- **Lead Time**: Tempo de commit até produção
- **Change Failure Rate**: % de deploys que falham

## 🎓 Boas Práticas

### ✅ DO's

1. **Versione o Jenkinsfile** junto com o código
2. **Use stages descritivos** para clareza
3. **Publique relatórios** para visibilidade
4. **Falhe rápido** - testes primeiro
5. **Paralelização** quando possível
6. **Notificações** para falhas
7. **Cleanup** do workspace

### ❌ DON'Ts

1. **Não** coloque credenciais no código
2. **Não** ignore falhas de teste
3. **Não** faça deploy sem testes
4. **Não** execute tudo em um único stage
5. **Não** mantenha artefatos desnecessários

## 🔗 Arquitetura do Sistema CI/CD

```
┌─────────────────────────────────────────────────────┐
│                     Developer                        │
│              (git push to branch)                    │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│                   Git Repository                     │
│         (GitHub/GitLab/Bitbucket)                    │
└──────────────────┬──────────────────────────────────┘
                   │ (webhook/poll)
                   ▼
┌─────────────────────────────────────────────────────┐
│                  Jenkins Master                      │
│               (localhost:9090)                       │
│                                                      │
│  ┌──────────────────────────────────────────┐      │
│  │          Jenkinsfile Pipeline            │      │
│  │                                          │      │
│  │  1. Checkout                             │      │
│  │  2. Build (Gradle)                       │      │
│  │  3. Unit Tests                           │      │
│  │  4. Architecture Tests                   │      │
│  │  5. Code Quality                         │      │
│  │  6. Package (JAR)                        │      │
│  │  7. Docker Build                         │      │
│  │  8. Deploy Dev (develop)                 │      │
│  │  9. Deploy Prod (main + approval)        │      │
│  └──────────────────────────────────────────┘      │
└──────────┬──────────────────┬────────────────────┬─┘
           │                  │                    │
           ▼                  ▼                    ▼
    ┌────────────┐     ┌────────────┐      ┌────────────┐
    │   Tests    │     │   Docker   │      │   Deploy   │
    │  Reports   │     │   Images   │      │   Targets  │
    └────────────┘     └────────────┘      └────────────┘
```

## 📚 Referências

- **Jenkins Documentation**: https://www.jenkins.io/doc/
- **Pipeline Syntax**: https://www.jenkins.io/doc/book/pipeline/syntax/
- **Pipeline Steps**: https://www.jenkins.io/doc/pipeline/steps/
- **Best Practices**: https://www.jenkins.io/doc/book/pipeline/pipeline-best-practices/
- **Groovy Documentation**: https://groovy-lang.org/documentation.html

## 🎯 Próximos Passos

### Melhorias Planejadas

1. **Notificações**
   - Integrar Slack/Email para alertas
   - Notificar em caso de falha

2. **Análise de Código**
   - Integrar SonarQube
   - Métricas de qualidade

3. **Deploy Automático**
   - Implementar deploy para K8s/Docker Swarm
   - Deploy staging automático

4. **Testes de Performance**
   - JMeter/Gatling no pipeline
   - Benchmarks automáticos

5. **Security Scanning**
   - OWASP Dependency Check
   - Container scanning

6. **Backup Automatizado**
   - Backup do volume jenkins_data
   - Restore procedure

## 🏁 Conclusão

Jenkins é a peça central da automação de CI/CD do projeto, garantindo:

- ✅ **Qualidade**: Testes automáticos em cada mudança
- ✅ **Velocidade**: Feedback rápido para desenvolvedores
- ✅ **Confiabilidade**: Builds consistentes e reproduzíveis
- ✅ **Rastreabilidade**: Histórico completo de builds e deploys
- ✅ **Escalabilidade**: Pronto para crescer com o projeto

A integração com Docker e a definição do pipeline como código (Jenkinsfile) garantem que todo o processo de build e deploy seja versionado, testável e facilmente replicável em qualquer ambiente.

