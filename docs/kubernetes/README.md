# Kubernetes - Documentação

Esta é a documentação completa sobre Kubernetes para o projeto Bridal Cover CRM.

## 📚 Índice de Documentos

### 1. Fundamentos
- **[Introdução ao Kubernetes](01-introduction.md)** - O que é Kubernetes, características e benefícios
- **[Arquitetura do Kubernetes](02-architecture.md)** - Componentes do Control Plane e Worker Nodes
- **[Conceitos Fundamentais](03-concepts.md)** - Pods, Services, Deployments, e outros recursos

### 2. Instalação
- **[Instalação no Ubuntu](04-installation-ubuntu.md)** - Guia completo de instalação com kubeadm
- **[Instalação no macOS](05-installation-macos.md)** - Opções com Multipass, Vagrant e Minikube
- **[Instalação no Windows](06-installation-windows.md)** - WSL2, Hyper-V e VirtualBox

### 3. Configuração e Uso
- **[Configuração Pós-Instalação](07-post-installation.md)** - Autocompletion, Helm, Ingress, Storage
- **[Comandos Essenciais](08-essential-commands.md)** - Referência rápida de comandos kubectl
- **[Kubernetes Dashboard](10-dashboard-setup.md)** - Instalação e acesso via NodePort

### 4. Boas Práticas
- **[Boas Práticas](09-best-practices.md)** - Organização, segurança, recursos e deployment

### 5. Referências
- **[Referências e Recursos](11-references.md)** - Links úteis, ferramentas e comunidade

---

## 🎯 Para Onde Ir?

**Novo no Kubernetes?**
→ Comece pela [Introdução](01-introduction.md) e [Conceitos Fundamentais](03-concepts.md)

**Quer instalar um cluster?**
→ Escolha seu sistema operacional: [Ubuntu](04-installation-ubuntu.md) | [macOS](05-installation-macos.md) | [Windows](06-installation-windows.md)

**Já tem um cluster?**
→ Veja a [Configuração Pós-Instalação](07-post-installation.md) e [Comandos Essenciais](08-essential-commands.md)

**Procurando o Dashboard?**
→ Acesse [Kubernetes Dashboard](10-dashboard-setup.md)

**Quer melhorar suas práticas?**
→ Consulte [Boas Práticas](09-best-practices.md)

---

## 🚀 Quick Start

Se você usa **macOS** e quer começar rapidamente:

```bash
# Instalar Minikube
brew install minikube kubectl

# Iniciar cluster
minikube start --driver=docker

# Verificar
kubectl get nodes
```

Para outros sistemas operacionais ou instalações mais complexas, consulte os guias específicos de instalação.

---

## 📖 Sobre esta Documentação

Esta documentação foi criada para facilitar o aprendizado e uso do Kubernetes no contexto do projeto Bridal Cover CRM. Ela está organizada de forma progressiva, começando pelos conceitos básicos até configurações avançadas.

Todos os exemplos e comandos foram testados e seguem as melhores práticas recomendadas pela comunidade Kubernetes e CNCF.

