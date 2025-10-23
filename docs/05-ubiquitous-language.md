# Linguagem Ubíqua - BridalCover CRM

A **Linguagem Ubíqua** define o vocabulário comum utilizado por toda a equipe (desenvolvedores, analistas de negócio, especialistas de domínio) para comunicação sobre o domínio do BridalCover CRM.

---

## 📋 Glossário de Termos

### **Core Business**

**Bridal Cover (Capa de Vestido de Noiva)**
- Produto principal do negócio: capas protetoras para vestidos de noiva e smokings
- Utilizadas por lojas de aluguel para proteger as peças durante transporte e armazenamento

**Loja de Aluguel de Vestidos**
- Cliente-alvo do CRM: estabelecimentos que alugam vestidos de noiva e smokings
- Também conhecidas como "lojas de trajes para eventos" ou "ateliês de aluguel"

---

### **Gestão de Prospects**

**Lead (Prospect)**
- Loja de aluguel potencial que ainda não é cliente
- Pode estar em diferentes estágios do funil de vendas
- Origem pode ser manual, Google Places API, indicação ou website

**Cliente**
- Loja de aluguel que já comprou capas e mantém relacionamento comercial ativo
- Representa um lead convertido com sucesso

**Conversão de Lead**
- Processo de transformar um prospect em cliente através de vendas bem-sucedidas
- Marca a transição do status "Lead" para "Cliente"

**Qualificação de Lead**
- Processo de avaliar se um prospect tem potencial real de compra
- Considera fatores como porte da loja, localização e necessidade do produto

---

### **Gestão de Contatos**

**Tentativa de Contato**
- Qualquer ação de comunicação direcionada a um lead ou cliente
- Registrada no sistema com data, canal, resultado e observações

**Canal de Contato**
- Meio utilizado para comunicação: telefone, email, WhatsApp, visita presencial ou website
- Cada canal pode ter diferentes taxas de sucesso

**Resultado de Contato**
- Outcome de uma tentativa de contato: sem resposta, interessado, não interessado, 
  solicitação de retorno, reunião agendada, proposta solicitada ou conversão

**Follow-up (Acompanhamento)**
- Contato subsequente agendado baseado no resultado de uma interação anterior
- Essencial para manter o relacionamento e avançar no funil de vendas

---

### **Scripts de Vendas**

**Script de Vendas**
- Roteiro estruturado de comunicação para diferentes situações de contato
- Pode ser para cold call, follow-up, apresentação de proposta, tratamento de objeções ou fechamento

**Efetividade do Script**
- Métrica que mede o sucesso de um script baseado na taxa de conversão
- Calculada pela razão entre usos bem-sucedidos e total de usos

**Cold Call**
- Primeiro contato telefônico com um lead que não conhece a empresa
- Requer script específico para quebrar o gelo e despertar interesse

---

### **Análise Geográfica**

**Densidade de Leads**
- Concentração de prospects em uma determinada região (cidade/estado)
- Utilizada para priorizar visitas presenciais e estratégias regionais

**Heatmap (Mapa de Calor)**
- Visualização que mostra regiões com maior concentração de leads
- Ajuda na tomada de decisão para expansão geográfica

**Território de Vendas**
- Região geográfica específica designada para prospecção intensiva
- Baseada na densidade de leads e potencial de mercado

---

### **Integração Externa**

**Google Places API**
- Serviço utilizado para buscar automaticamente lojas de aluguel de vestidos
- Fonte de dados para importação de novos leads com informações básicas

**Prospecção Inteligente**
- Processo automatizado de buscar e importar leads via Google Places
- Filtra apenas estabelecimentos relevantes (lojas de aluguel de trajes)

---

### **Estados e Status**

**Status do Lead**
- Estado atual no funil de vendas:
  - **Novo**: Recém-cadastrado, aguarda primeiro contato
  - **Contatado**: Já houve tentativa de comunicação
  - **Qualificado**: Confirmado como prospect válido
  - **Proposta Enviada**: Recebeu orçamento formal
  - **Negociando**: Em processo de negociação ativa
  - **Convertido**: Transformado em cliente
  - **Perdido**: Não demonstrou interesse ou escolheu concorrente

**Fonte do Lead**
- Origem de um prospect:
  - **Cadastro Manual**: Inserido diretamente por usuário
  - **Google Places**: Importado via API
  - **Indicação**: Recomendado por cliente existente
  - **Website**: Gerado através do site da empresa

---

### **Métricas e KPIs**

**Taxa de Conversão**
- Percentual de leads que se tornam clientes
- Métrica principal para avaliar efetividade das vendas

**Ciclo de Vendas**
- Tempo médio entre primeiro contato e conversão em cliente
- Varia conforme porte da loja e complexidade da negociação

**ROI de Território**
- Retorno sobre investimento em uma região específica
- Considera custos de prospecção versus receita gerada

---

### **Contextos Técnicos**

**Aggregate Root (Raiz de Agregado)**
- Entidade principal que controla acesso a um conjunto de objetos relacionados
- No sistema: Lead, Cliente e Script são aggregate roots

**Value Object (Objeto de Valor)**
- Objeto imutável definido por seus atributos, sem identidade própria
- Exemplos: Endereço, Coordenadas, IDs tipados

**Domain Event (Evento de Domínio)**
- Acontecimento significativo no domínio que outras partes do sistema precisam saber
- Exemplos: "Lead Convertido", "Contato Realizado", "Script Utilizado"

---

## 🔄 Fluxos de Negócio

### **Fluxo de Prospecção**
1. **Identificação** → Encontrar lojas de aluguel via Google Places ou cadastro manual
2. **Qualificação** → Avaliar se o lead tem potencial de compra
3. **Contato Inicial** → Primeira abordagem usando script apropriado
4. **Follow-up** → Acompanhamento baseado no resultado do contato
5. **Proposta** → Envio de orçamento para leads qualificados
6. **Negociação** → Ajustes de proposta e condições
7. **Conversão** → Fechamento da venda e transformação em cliente

### **Fluxo de Análise Territorial**
1. **Coleta de Dados** → Gathering de leads por região
2. **Análise de Densidade** → Identificação de concentrações
3. **Priorização** → Ranking de territórios por potencial
4. **Planejamento de Visitas** → Agenda de visitas presenciais
5. **Execução** → Realização de contatos na região
6. **Avaliação** → Análise de resultados e ROI

---

## 🎯 Regras de Negócio

### **Qualificação de Leads**
- Loja deve ter foco em aluguel de vestidos de noiva ou smokings
- Localização deve estar em território de atuação da empresa
- Deve haver informação de contato válida (telefone ou email)

### **Gestão de Contatos**
- Máximo de 3 tentativas de contato sem resposta antes de marcar como "Não Responsivo"
- Follow-up obrigatório para leads que demonstraram interesse
- Registro detalhado de todas as interações para histórico

### **Scripts de Vendas**
- Script deve ser escolhido baseado no tipo de contato e histórico do lead
- Efetividade deve ser calculada automaticamente após cada uso
- Scripts com baixa efetividade devem ser revisados ou desativados

---

## 📊 Métricas do Domínio

### **Indicadores de Performance**
- **Taxa de Resposta**: % de leads que respondem ao primeiro contato
- **Taxa de Qualificação**: % de leads que passam pelo processo de qualificação
- **Taxa de Conversão**: % de leads qualificados que se tornam clientes
- **Tempo Médio de Conversão**: Dias entre primeiro contato e fechamento
- **ROI por Território**: Receita gerada vs. investimento em prospecção

### **Indicadores de Qualidade**
- **Precisão da Fonte**: % de leads válidos por origem (Google vs. Manual)
- **Efetividade de Scripts**: Taxa de sucesso por tipo de script
- **Densidade Territorial**: Concentração de leads por região
- **Retenção de Clientes**: % de clientes que permanecem ativos

---

## 🚀 Eventos de Domínio

### **Eventos de Lead**
- `LeadCriado`: Novo prospect adicionado ao sistema
- `LeadQualificado`: Lead passou pelo processo de qualificação
- `LeadConvertido`: Lead transformado em cliente
- `LeadPerdido`: Lead marcado como sem interesse

### **Eventos de Contato**
- `ContatoRealizado`: Nova tentativa de contato registrada
- `FollowUpAgendado`: Próximo contato programado
- `PropostaEnviada`: Orçamento formal enviado ao lead

### **Eventos de Script**
- `ScriptUtilizado`: Script usado em uma tentativa de contato
- `EfetividadeCalculada`: Métrica de sucesso atualizada
- `ScriptDesativado`: Script removido de uso ativo

---

**Nota**: Esta linguagem ubíqua deve ser utilizada em todas as conversas, documentações, código e reuniões relacionadas ao projeto BridalCover CRM. Qualquer novo termo deve ser adicionado a este glossário para manter a consistência da comunicação. 