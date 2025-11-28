# 📚 Documentação Helm Chart - Índice Completo

Bem-vindo à documentação do Helm Chart do Bridal Cover CRM! Este índice vai te ajudar a encontrar rapidamente o que você precisa.

## 🎯 Começando

### Nunca usou Helm?
👉 Comece aqui: **[Guia Completo Helm e Tiller](../docs/kubernetes/15-helm-tiller-guide.md)**
- O que é Helm e Tiller
- Como funciona
- Instalação e configuração
- Conceitos fundamentais
- Boas práticas

### Quer instalar o Chart?
👉 Vá para: **[Getting Started](GETTING-STARTED.md)**
- Instalação rápida em 5 minutos
- Pré-requisitos
- Primeiro deploy
- Troubleshooting básico

### Precisa de comandos rápidos?
👉 Veja: **[Quick Reference](QUICK-REFERENCE.md)**
- Comandos mais usados
- Atalhos do Makefile
- Troubleshooting
- Cenários comuns

---

## 📖 Documentação Detalhada

### 1. [Guia Completo Helm e Tiller](../docs/kubernetes/15-helm-tiller-guide.md)

**O QUE É:** Documentação completa sobre Helm, conceitos, instalação e uso.

**QUANDO LER:**
- Você nunca usou Helm antes
- Quer entender como Helm funciona
- Precisa migrar de Helm 2 para Helm 3
- Quer aprender boas práticas

**CONTEÚDO:**
- ✅ Introdução ao Helm
- ✅ O que é Tiller (Helm 2)
- ✅ Arquitetura Helm 2 vs Helm 3
- ✅ Conceitos fundamentais (Charts, Releases, Values)
- ✅ Instalação detalhada (macOS, Linux, Windows)
- ✅ Criando Charts personalizados
- ✅ Boas práticas
- ✅ Troubleshooting avançado
- ✅ Migração Helm 2 → 3

**TAMANHO:** ~800 linhas | **TEMPO DE LEITURA:** ~30-45 min

---

### 2. [Getting Started](GETTING-STARTED.md)

**O QUE É:** Guia prático de instalação e primeiros passos.

**QUANDO LER:**
- Você quer instalar o chart pela primeira vez
- Precisa fazer deploy rápido em dev
- Quer configurar diferentes ambientes
- Precisa customizar valores

**CONTEÚDO:**
- ✅ Pré-requisitos e instalação de ferramentas
- ✅ Instalação rápida (5 minutos)
- ✅ Validação do chart
- ✅ Deploy em dev/staging/prod
- ✅ Customização de valores
- ✅ Workflow de desenvolvimento
- ✅ Gerenciamento de secrets
- ✅ Monitoramento básico
- ✅ Testing
- ✅ Troubleshooting prático
- ✅ Próximos passos

**TAMANHO:** ~450 linhas | **TEMPO DE LEITURA:** ~20-30 min

---

### 3. [Quick Reference](QUICK-REFERENCE.md)

**O QUE É:** Cheat sheet com comandos prontos para usar.

**QUANDO USAR:**
- Você já conhece Helm e quer comandos rápidos
- Precisa lembrar sintaxe de um comando
- Quer resolver um problema específico rapidamente
- Está fazendo troubleshooting

**CONTEÚDO:**
- ✅ Comandos Make (atalhos)
- ✅ Comandos Helm diretos
- ✅ Debug e troubleshooting
- ✅ Gerenciamento de secrets
- ✅ Monitoramento (HPA, logs, métricas)
- ✅ Cenários comuns (update, scale, restart)
- ✅ Troubleshooting rápido
- ✅ CI/CD snippets

**TAMANHO:** ~480 linhas | **TEMPO DE CONSULTA:** ~2-5 min

---

### 4. [Chart README](bridal-cover-crm/README.md)

**O QUE É:** Documentação específica do chart Bridal Cover CRM.

**QUANDO LER:**
- Quer entender os parâmetros específicos do chart
- Precisa customizar configurações
- Quer ver exemplos de valores para cada ambiente
- Precisa integrar com banco externo

**CONTEÚDO:**
- ✅ Visão geral do chart
- ✅ Tabela completa de parâmetros
- ✅ Exemplos de configuração (dev/staging/prod)
- ✅ Integração com PostgreSQL
- ✅ Banco de dados externo
- ✅ Secrets management
- ✅ Monitoramento com Prometheus
- ✅ Atualização e rollback

**TAMANHO:** ~280 linhas | **TEMPO DE LEITURA:** ~15-20 min

---

### 5. [Helm Chart README Geral](README.md)

**O QUE É:** Overview da estrutura de helm charts do projeto.

**QUANDO LER:**
- Primeira vez explorando o diretório helm-chart
- Quer entender a estrutura do projeto
- Precisa de instruções gerais de uso
- Quer contribuir para o projeto

**CONTEÚDO:**
- ✅ Estrutura do diretório
- ✅ Quick start
- ✅ Deployment por ambiente
- ✅ Atualização e rollback
- ✅ Debugging
- ✅ Customização
- ✅ Gerenciamento de secrets
- ✅ Testing
- ✅ Empacotamento
- ✅ CI/CD integration
- ✅ Ferramentas úteis

**TAMANHO:** ~420 linhas | **TEMPO DE LEITURA:** ~20 min

---

## 🛠️ Arquivos de Configuração

### Values Files

| Arquivo | Ambiente | Uso |
|---------|----------|-----|
| `values.yaml` | Padrão | Valores base e defaults |
| `values-dev.yaml` | Desenvolvimento | 1 réplica, NodePort, recursos mínimos |
| `values-staging.yaml` | Staging | 2 réplicas, Ingress, recursos médios |
| `values-prod.yaml` | Produção | 5+ réplicas, HA, recursos altos, DB externo |

### Templates

| Template | Descrição |
|----------|-----------|
| `deployment.yaml` | Deployment principal da aplicação |
| `service.yaml` | Service para expor a aplicação |
| `ingress.yaml` | Ingress para acesso externo |
| `configmap.yaml` | ConfigMap com configurações da app |
| `serviceaccount.yaml` | ServiceAccount para o pod |
| `hpa.yaml` | HorizontalPodAutoscaler |
| `pdb.yaml` | PodDisruptionBudget |
| `_helpers.tpl` | Funções auxiliares reutilizáveis |
| `NOTES.txt` | Notas exibidas após instalação |

### Outros Arquivos

| Arquivo | Descrição |
|---------|-----------|
| `Chart.yaml` | Metadados do chart (nome, versão, dependências) |
| `.helmignore` | Arquivos a ignorar no package |
| `Makefile` | Atalhos para comandos comuns |
| `.gitignore` | Arquivos a não versionar |

---

## 🎯 Fluxos de Trabalho Comuns

### 1️⃣ Primeiro Deploy (Dev)

```
1. Ler: Getting Started
2. Instalar pré-requisitos
3. Rodar: make deploy-dev
4. Verificar: make status-dev
```

### 2️⃣ Deploy em Produção

```
1. Ler: Chart README → seção Produção
2. Configurar secrets
3. Customizar values-prod.yaml
4. Fazer dry-run: make dry-run-prod
5. Deploy: make deploy-prod
6. Monitorar: make status-prod
```

### 3️⃣ Atualizar Aplicação

```
1. Consultar: Quick Reference → "Atualização"
2. Testar em dev: make upgrade-dev
3. Verificar: make status-dev
4. Deploy staging: make upgrade-staging
5. Deploy prod: make upgrade-prod
```

### 4️⃣ Troubleshooting

```
1. Consultar: Quick Reference → "Troubleshooting"
2. Ver logs: make logs-dev
3. Descrever pods: kubectl describe pod ...
4. Se necessário: make rollback-dev
```

### 5️⃣ Customização

```
1. Ler: Chart README → "Parâmetros"
2. Copiar values para ambiente
3. Editar valores
4. Validar: make template-dev
5. Aplicar: make upgrade-dev
```

---

## 📊 Quando Usar Cada Documento

### Cenário: Nunca usei Helm

```
1. Guia Helm e Tiller (completo)
2. Getting Started
3. Quick Reference (bookmark)
```

### Cenário: Já uso Helm, novo no projeto

```
1. Chart README
2. Getting Started (seção deploy)
3. Quick Reference
```

### Cenário: Desenvolvedor do dia-a-dia

```
Use principalmente:
- Quick Reference
- Makefile (make help)

Consulte quando necessário:
- Chart README (parâmetros)
- Getting Started (workflows)
```

### Cenário: DevOps/SRE

```
1. Chart README (completo)
2. values-prod.yaml (customizar)
3. Quick Reference (CI/CD, monitoramento)
4. Guia Helm (boas práticas avançadas)
```

### Cenário: Troubleshooting

```
1. Quick Reference → "Troubleshooting Rápido"
2. Getting Started → "Troubleshooting"
3. Guia Helm → "Troubleshooting Avançado"
```

---

## 🎓 Progressão de Aprendizado

### Nível 1: Iniciante

**Objetivo:** Conseguir fazer deploy básico

**Ler:**
1. ✅ Guia Helm e Tiller (seções 1-5)
2. ✅ Getting Started (completo)

**Praticar:**
```bash
make deploy-dev
make status-dev
make upgrade-dev
```

### Nível 2: Intermediário

**Objetivo:** Customizar e gerenciar múltiplos ambientes

**Ler:**
1. ✅ Chart README (completo)
2. ✅ Guia Helm (seções 6-9)
3. ✅ Quick Reference (completo)

**Praticar:**
```bash
# Customizar valores
# Deploy staging/prod
# Gerenciar secrets
# Troubleshooting básico
```

### Nível 3: Avançado

**Objetivo:** Criar charts, CI/CD, produção

**Ler:**
1. ✅ Guia Helm (completo, incluindo migração)
2. ✅ Helm Best Practices (oficial)
3. ✅ Quick Reference (CI/CD)

**Praticar:**
```bash
# Criar charts customizados
# Implementar CI/CD
# Sealed Secrets
# Monitoramento avançado
# Network Policies
```

---

## 🔗 Links Rápidos

### Documentação Local

- [Guia Completo Helm](../docs/kubernetes/15-helm-tiller-guide.md)
- [Getting Started](GETTING-STARTED.md)
- [Quick Reference](QUICK-REFERENCE.md)
- [Chart README](bridal-cover-crm/README.md)
- [Helm Chart README](README.md)

### Arquivos de Configuração

- [values.yaml](bridal-cover-crm/values.yaml)
- [values-dev.yaml](bridal-cover-crm/values-dev.yaml)
- [values-staging.yaml](bridal-cover-crm/values-staging.yaml)
- [values-prod.yaml](bridal-cover-crm/values-prod.yaml)

### Templates

- [deployment.yaml](bridal-cover-crm/templates/deployment.yaml)
- [service.yaml](bridal-cover-crm/templates/service.yaml)
- [ingress.yaml](bridal-cover-crm/templates/ingress.yaml)

### Recursos Externos

- [Helm Official Docs](https://helm.sh/docs/)
- [Kubernetes Docs](https://kubernetes.io/docs/)
- [Chart Best Practices](https://helm.sh/docs/chart_best_practices/)
- [Artifact Hub](https://artifacthub.io/)

---

## 💡 Dicas de Navegação

1. **Use Ctrl+F** para buscar palavras-chave neste índice
2. **Bookmark** o Quick Reference para consultas rápidas
3. **Imprima** ou salve como PDF as seções que mais usa
4. **Contribua** melhorando a documentação que achar confusa

---

## 🆘 Ainda Perdido?

### Perguntas Comuns → Onde Encontrar Respostas

**Como instalo o Helm?**
→ Guia Helm e Tiller, seção "Instalação"

**Como faço meu primeiro deploy?**
→ Getting Started, seção "Instalação Rápida"

**Quais parâmetros posso customizar?**
→ Chart README, seção "Principais Parâmetros"

**Como faço upgrade da aplicação?**
→ Quick Reference, seção "Atualização"

**Como faço rollback?**
→ Quick Reference, seção "History & Rollback"

**Como gerencio secrets?**
→ Getting Started, seção "Gerenciamento de Secrets"

**Meus pods não sobem, e agora?**
→ Quick Reference, seção "Troubleshooting Rápido"

**Como integro com CI/CD?**
→ Quick Reference, seção "CI/CD Integration"

**Como uso banco de dados externo?**
→ Chart README, seção "Banco de Dados Externo"

**Quais as boas práticas?**
→ Guia Helm e Tiller, seção "Boas Práticas"

---

## 📞 Suporte

Não encontrou o que procurava?

1. **Pesquise** nos documentos (use Ctrl+F)
2. **Consulte** o Quick Reference primeiro
3. **Leia** a seção de troubleshooting relevante
4. **Abra uma issue** no GitHub se o problema persistir

---

**Boa leitura e bom deploy! 🚀**

_Última atualização: Novembro 2025_


