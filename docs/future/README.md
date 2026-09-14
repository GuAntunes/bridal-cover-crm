# 🔮 Future Technologies & Advanced Topics

Esta pasta contém documentação sobre tecnologias e conceitos que serão implementados quando o projeto estiver mais maduro.

## 📋 Conteúdo

### **domain-events.md**
- Eventos de domínio para comunicação entre bounded contexts
- **Quando implementar:** Quando tiver múltiplos agregados e precisar de comunicação assíncrona

### **bounded-contexts.md**
- Divisão do domínio em múltiplos contextos
- **Quando implementar:** Quando o Lead Management estiver completo e surgir necessidade de outros contextos

### Deploy em Kubernetes / GitOps

Manifests Helm e Argo CD vivem no repositório **[platform-gitops](https://github.com/GuAntunes/platform-gitops)** (fora deste repo).

---

## 🎯 Status Atual do Projeto

**Fase Atual:** Foundation & Core Lead Management

**Prioridades:**
1. ✅ Arquitetura Hexagonal implementada
2. 🔄 CRUD completo de Leads
3. 🔄 Testes unitários e de integração
4. ⏳ Contact Management
5. ⏳ Dashboards e Relatórios

**Quando avançar para tecnologias futuras:**
- Quando tiver pelo menos 2 agregados completos
- Quando tiver testes cobrindo >80% do código
- Quando o frontend estiver funcional
- Quando tiver necessidade real de escalar

---

## 📚 Aprendizado

Estes documentos foram criados como parte do processo de aprendizado sobre arquitetura de software moderna. Mantê-los aqui serve como referência para quando o projeto crescer e essas tecnologias se tornarem necessárias.

**Princípio:** *Implement when needed, not because it's cool* 🚀
