# Referências e Recursos

Coleção de links úteis, ferramentas e recursos para aprender e trabalhar com Kubernetes.

## 📚 Documentação Oficial

### Kubernetes
- **[Documentação Oficial](https://kubernetes.io/docs/)** - Documentação completa
- **[API Reference](https://kubernetes.io/docs/reference/)** - Referência da API
- **[kubectl Reference](https://kubernetes.io/docs/reference/kubectl/)** - Comandos kubectl
- **[kubeadm Documentation](https://kubernetes.io/docs/reference/setup-tools/kubeadm/)** - Ferramenta de instalação
- **[Kubernetes Blog](https://kubernetes.io/blog/)** - Novidades e artigos

### Conceitos e Tutoriais
- **[Kubernetes Concepts](https://kubernetes.io/docs/concepts/)** - Conceitos fundamentais
- **[Tutorials](https://kubernetes.io/docs/tutorials/)** - Tutoriais oficiais
- **[Tasks](https://kubernetes.io/docs/tasks/)** - Tarefas práticas
- **[Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)** - Melhores práticas

---

## 🛠️ Ferramentas Essenciais

### CLI Tools

#### kubectl
- **[kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)**
- **[kubectl Plugin Manager (krew)](https://krew.sigs.k8s.io/)**
```bash
brew install krew
kubectl krew install ctx ns tree get-all
```

#### K9s
Terminal UI para Kubernetes
- **[Site Oficial](https://k9scli.io/)**
- **[GitHub](https://github.com/derailed/k9s)**
```bash
brew install derailed/k9s/k9s
```

#### kubectx + kubens
Troca rápida de contextos e namespaces
- **[GitHub](https://github.com/ahmetb/kubectx)**
```bash
brew install kubectx
```

#### stern
Tail múltiplos pods simultaneamente
- **[GitHub](https://github.com/stern/stern)**
```bash
brew install stern
stern backend-api
```

#### kubetail
Alternativa ao stern
- **[GitHub](https://github.com/johanhaleby/kubetail)**
```bash
brew tap johanhaleby/kubetail
brew install kubetail
```

### Desktop GUI

#### Lens
IDE completo para Kubernetes
- **[Site Oficial](https://k8slens.dev/)**
- Interface gráfica poderosa
- Multi-cluster support
- Terminal integrado

#### Octant
Dashboard local da VMware
- **[Site Oficial](https://octant.dev/)**
- **[GitHub](https://github.com/vmware-archive/octant)**
```bash
brew install octant
```

#### Headlamp
Dashboard open-source moderno
- **[Site Oficial](https://headlamp.dev/)**
- **[GitHub](https://github.com/headlamp-k8s/headlamp)**

---

## 📦 Gerenciamento de Pacotes

### Helm
Gerenciador de pacotes para Kubernetes
- **[Site Oficial](https://helm.sh/)**
- **[Charts Hub](https://artifacthub.io/)**
- **[Documentação](https://helm.sh/docs/)**
```bash
brew install helm
```

### Kustomize
Gerenciamento de configuração sem templates
- **[Site Oficial](https://kustomize.io/)**
- **[GitHub](https://github.com/kubernetes-sigs/kustomize)**
```bash
brew install kustomize
```

### Helmfile
Gerenciamento declarativo de Helm charts
- **[GitHub](https://github.com/helmfile/helmfile)**
```bash
brew install helmfile
```

---

## 🔍 Validação e Linting

### kubeval
Validação de manifests YAML
- **[GitHub](https://github.com/instrumenta/kubeval)**
```bash
brew install kubeval
kubeval deployment.yaml
```

### kubeconform
Alternativa moderna ao kubeval
- **[GitHub](https://github.com/yannh/kubeconform)**
```bash
brew install kubeconform
```

### kube-score
Análise de boas práticas
- **[GitHub](https://github.com/zegl/kube-score)**
```bash
brew install kube-score/tap/kube-score
kube-score score deployment.yaml
```

### Polaris
Auditoria de best practices
- **[Site Oficial](https://www.fairwinds.com/polaris)**
- **[GitHub](https://github.com/FairwindsOps/polaris)**

---

## 🔒 Segurança

### Trivy
Scanner de vulnerabilidades
- **[GitHub](https://github.com/aquasecurity/trivy)**
```bash
brew install aquasecurity/trivy/trivy
trivy image nginx:latest
```

### Snyk
Segurança de containers e código
- **[Site Oficial](https://snyk.io/)**
```bash
brew install snyk/tap/snyk
```

### kubesec
Análise de segurança de YAML
- **[Site Oficial](https://kubesec.io/)**
```bash
brew install kubesec
kubesec scan deployment.yaml
```

### kube-bench
Verificação de segurança CIS Kubernetes Benchmark
- **[GitHub](https://github.com/aquasecurity/kube-bench)**

### Falco
Runtime security monitoring
- **[Site Oficial](https://falco.org/)**
- **[GitHub](https://github.com/falcosecurity/falco)**

---

## 📊 Monitoramento e Observabilidade

### Prometheus
Sistema de monitoramento e alertas
- **[Site Oficial](https://prometheus.io/)**
- **[Documentação](https://prometheus.io/docs/)**

### Grafana
Visualização de métricas
- **[Site Oficial](https://grafana.com/)**
- **[Dashboards Prontos](https://grafana.com/grafana/dashboards/)**

### Jaeger
Distributed tracing
- **[Site Oficial](https://www.jaegertracing.io/)**

### ELK Stack
Elasticsearch, Logstash, Kibana para logging
- **[Site Oficial](https://www.elastic.co/elastic-stack/)**

### Loki
Log aggregation system do Grafana
- **[Site Oficial](https://grafana.com/oss/loki/)**

### OpenTelemetry
Observabilidade padronizada
- **[Site Oficial](https://opentelemetry.io/)**

---

## 🚀 CI/CD e GitOps

### ArgoCD
GitOps continuous delivery
- **[Site Oficial](https://argo-cd.readthedocs.io/)**
- **[GitHub](https://github.com/argoproj/argo-cd)**

### Flux
GitOps operator
- **[Site Oficial](https://fluxcd.io/)**
- **[GitHub](https://github.com/fluxcd/flux2)**

### Tekton
Cloud-native CI/CD
- **[Site Oficial](https://tekton.dev/)**

### Jenkins X
CI/CD automation para Kubernetes
- **[Site Oficial](https://jenkins-x.io/)**

---

## 🌐 Service Mesh

### Istio
Service mesh completo
- **[Site Oficial](https://istio.io/)**
- **[Documentação](https://istio.io/latest/docs/)**

### Linkerd
Service mesh lightweight
- **[Site Oficial](https://linkerd.io/)**

### Consul
Service mesh e service discovery
- **[Site Oficial](https://www.consul.io/)**

---

## 🎓 Aprendizado

### Tutoriais Interativos

#### Katacoda (Descontinuado - Alternativas)
- **[KillerCoda](https://killercoda.com/)** - Sucessor do Katacoda
- **[Play with Kubernetes](https://labs.play-with-k8s.com/)** - Ambiente de teste gratuito

#### Kubernetes the Hard Way
Aprenda Kubernetes do zero
- **[GitHub](https://github.com/kelseyhightower/kubernetes-the-hard-way)**
- Tutorial detalhado de instalação manual

### Cursos Online

#### Gratuitos
- **[Kubernetes Basics](https://kubernetes.io/docs/tutorials/kubernetes-basics/)** - Tutorial oficial
- **[Introduction to Kubernetes (edX)](https://www.edx.org/course/introduction-to-kubernetes)** - Curso gratuito
- **[Kubernetes for Beginners (Udemy)](https://www.udemy.com/course/learn-kubernetes/)** - Alguns cursos gratuitos

#### Pagos
- **[Certified Kubernetes Administrator (CKA)](https://www.cncf.io/certification/cka/)**
- **[Certified Kubernetes Application Developer (CKAD)](https://www.cncf.io/certification/ckad/)**
- **[Kubernetes Mastery (Udemy)](https://www.udemy.com/course/kubernetesmastery/)**

### Livros

#### Gratuitos (PDF)
- **[Kubernetes Patterns](https://www.redhat.com/cms/managed-files/cm-oreilly-kubernetes-patterns-ebook-f19824-201910-en.pdf)** - O'Reilly
- **[Kubernetes Best Practices](https://www.nginx.com/resources/library/kubernetes-best-practices/)** - NGINX

#### Pagos
- **"Kubernetes in Action"** - Marko Luksa
- **"Kubernetes Up & Running"** - Kelsey Hightower
- **"The Kubernetes Book"** - Nigel Poulton
- **"Production Kubernetes"** - Josh Rosso

### Blogs e Artigos
- **[Kubernetes Blog](https://kubernetes.io/blog/)**
- **[CNCF Blog](https://www.cncf.io/blog/)**
- **[learnk8s.io](https://learnk8s.io/blog)** - Artigos técnicos excelentes
- **[Medium - Kubernetes](https://medium.com/tag/kubernetes)**

---

## 🎥 Canais do YouTube

- **[TechWorld with Nana](https://www.youtube.com/@TechWorldwithNana)** - Tutoriais práticos
- **[That DevOps Guy](https://www.youtube.com/@MarcelDempers)** - DevOps e Kubernetes
- **[Just me and Opensource](https://www.youtube.com/@wenkatn)** - Kubernetes avançado
- **[DevOps Toolkit](https://www.youtube.com/@DevOpsToolkit)** - Viktor Farcic

---

## 💬 Comunidades

### Oficiais
- **[Kubernetes Slack](https://slack.k8s.io/)** - Chat da comunidade
- **[Kubernetes Forum](https://discuss.kubernetes.io/)** - Fórum oficial
- **[Stack Overflow](https://stackoverflow.com/questions/tagged/kubernetes)** - Perguntas e respostas

### Redes Sociais
- **[Reddit r/kubernetes](https://www.reddit.com/r/kubernetes/)** - Discussões
- **[Twitter #kubernetes](https://twitter.com/search?q=%23kubernetes)**
- **[LinkedIn Kubernetes Group](https://www.linkedin.com/groups/6977712/)**

### Brasileiras
- **[Kubernetes Brasil (Telegram)](https://t.me/kubernetesbrasiloficial)**
- **[DevOps Brasil (Discord)](https://discord.gg/devopsbrasil)**

---

## 🏢 Plataformas Kubernetes Gerenciadas

### Cloud Providers
- **[Google Kubernetes Engine (GKE)](https://cloud.google.com/kubernetes-engine)**
- **[Amazon EKS](https://aws.amazon.com/eks/)**
- **[Azure Kubernetes Service (AKS)](https://azure.microsoft.com/en-us/services/kubernetes-service/)**
- **[DigitalOcean Kubernetes](https://www.digitalocean.com/products/kubernetes/)**
- **[Linode Kubernetes Engine](https://www.linode.com/products/kubernetes/)**

### Distribuições
- **[Rancher](https://rancher.com/)** - Plataforma de gerenciamento
- **[OpenShift](https://www.redhat.com/en/technologies/cloud-computing/openshift)** - Red Hat
- **[k3s](https://k3s.io/)** - Kubernetes lightweight
- **[MicroK8s](https://microk8s.io/)** - Canonical
- **[kind](https://kind.sigs.k8s.io/)** - Kubernetes in Docker

---

## 🧪 Ambientes de Desenvolvimento

### Local
- **[Minikube](https://minikube.sigs.k8s.io/)** - Cluster local
- **[kind](https://kind.sigs.k8s.io/)** - Kubernetes in Docker
- **[k3d](https://k3d.io/)** - k3s in Docker
- **[Docker Desktop](https://www.docker.com/products/docker-desktop)** - Kubernetes integrado

### Cloud
- **[Killercoda](https://killercoda.com/)** - Laboratórios interativos
- **[Play with Kubernetes](https://labs.play-with-k8s.com/)** - Ambiente temporário gratuito

---

## 📖 Especificações e Padrões

- **[12 Factor App](https://12factor.net/)** - Metodologia para aplicações cloud-native
- **[CNCF Landscape](https://landscape.cncf.io/)** - Ecosystem cloud-native
- **[CIS Kubernetes Benchmark](https://www.cisecurity.org/benchmark/kubernetes)** - Segurança
- **[Kubernetes Enhancement Proposals (KEP)](https://github.com/kubernetes/enhancements)** - Propostas de melhorias

---

## 🔧 Ferramentas Adicionais

### Networking
- **[Cilium](https://cilium.io/)** - CNI avançado com eBPF
- **[Calico](https://www.tigera.io/project-calico/)** - Networking e Network Policy
- **[Flannel](https://github.com/flannel-io/flannel)** - CNI simples

### Storage
- **[Rook](https://rook.io/)** - Storage orchestration
- **[Longhorn](https://longhorn.io/)** - Distributed block storage
- **[OpenEBS](https://openebs.io/)** - Container-attached storage

### Backup
- **[Velero](https://velero.io/)** - Backup e restore de clusters
- **[Kasten K10](https://www.kasten.io/)** - Data management

### Cost Management
- **[Kubecost](https://www.kubecost.com/)** - Análise de custos
- **[OpenCost](https://www.opencost.io/)** - Cost monitoring open-source

---

## 🎯 Certificações

### CNCF
- **[CKA](https://www.cncf.io/certification/cka/)** - Certified Kubernetes Administrator
- **[CKAD](https://www.cncf.io/certification/ckad/)** - Certified Kubernetes Application Developer
- **[CKS](https://www.cncf.io/certification/cks/)** - Certified Kubernetes Security Specialist

### Preparação
- **[killer.sh](https://killer.sh/)** - Simulados oficiais
- **[KodeKloud](https://kodekloud.com/)** - Cursos e labs

---

## 📚 Awesome Lists

- **[Awesome Kubernetes](https://github.com/ramitsurana/awesome-kubernetes)** - Coleção completa de recursos
- **[Awesome Kubernetes Security](https://github.com/magnologan/awesome-k8s-security)**
- **[Awesome Operators](https://github.com/operator-framework/awesome-operators)**

---

Voltar ao [Índice Principal](README.md)

