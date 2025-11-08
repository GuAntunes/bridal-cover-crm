# 📖 Glossário - BridalCover CRM

Vocabulário comum usado no projeto. Mantenha este glossário atualizado conforme o sistema evolui.

---

## 🎯 Domínio do Negócio

### Lead (Prospect)
Loja de aluguel de vestidos que é cliente potencial, mas ainda não comprou.

**Exemplo:** "Ateliê Elegante" cadastrada no sistema mas ainda não fez pedido.

### Cliente
Loja que já comprou capas e mantém relacionamento comercial ativo.

### Conversão
Processo de transformar um lead em cliente através de venda bem-sucedida.

### Qualificação
Avaliar se um lead tem potencial real de compra (porte, localização, necessidade).

---

## 📊 Status do Lead

| Status | Significado | Quando usar |
|--------|-------------|-------------|
| **NEW** | Recém-cadastrado | Lead acabou de entrar no sistema |
| **CONTACTED** | Já houve tentativa de comunicação | Após primeiro contato (mesmo sem sucesso) |
| **QUALIFIED** | Confirmado como prospect válido | Lead tem potencial e interesse |
| **PROPOSAL_SENT** | Recebeu orçamento formal | Proposta enviada, aguardando resposta |
| **NEGOTIATING** | Em negociação ativa | Discutindo preços, condições, etc |
| **CONVERTED** | Transformado em cliente | Venda fechada! 🎉 |
| **LOST** | Perdido (sem interesse) | Não comprou / escolheu concorrente |

---

## 📍 Fonte do Lead

Como o lead chegou ao sistema:

- **MANUAL_ENTRY** - Cadastrado manualmente pelo vendedor
- **GOOGLE_PLACES** - Importado via Google Places API
- **REFERRAL** - Indicação de cliente existente  
- **WEBSITE** - Formulário do site da empresa
- **COLD_CALL** - Descoberto via prospecção ativa

---

## 📞 Gestão de Contatos

### Tentativa de Contato
Ação de comunicação com um lead (telefone, email, WhatsApp).

**Registra:** data, canal, resultado, observações.

### Canal de Contato
Meio usado para comunicação:
- Telefone
- Email
- WhatsApp
- Visita presencial

### Follow-up
Contato programado baseado em interação anterior.

**Exemplo:** "Ligar novamente segunda-feira às 14h após enviar proposta"

---

## 🏢 Value Objects (Objetos de Valor)

### CNPJ
Documento fiscal brasileiro. Obrigatório para empresas.

**Formato:** `12.345.678/0001-90`

### Email
Endereço de email válido.

**Validação:** Formato padrão de email

### Phone (Telefone)
Número de telefone brasileiro.

**Formato:** `(11) 98765-4321` ou `11987654321`

### ContactInfo
Agrupa informações de contato (email, telefone, Instagram).

**Regra:** Lead deve ter pelo menos um meio de contato.

---

## 📐 Arquitetura (Termos Técnicos)

### Aggregate Root
Entidade principal que controla acesso a objetos relacionados.

**No projeto:** `Lead` é um aggregate root.

### Value Object
Objeto imutável definido por seus atributos, sem identidade própria.

**Exemplos:** Email, CNPJ, Phone, ContactInfo

### Port (Porta)
Interface que define contrato entre camadas.

**Tipos:**
- **Port IN** (entrada): Casos de uso - ex: `RegisterLeadUseCase`
- **Port OUT** (saída): Dependências externas - ex: `LeadRepository`

### Adapter (Adaptador)
Implementação concreta de uma porta.

**Exemplos:**
- `LeadController` - adapta HTTP para casos de uso
- `LeadRepositoryAdapter` - adapta domínio para banco de dados

---

## 🔄 Fluxo de Prospecção

```
1. Identificação
   Encontrar lojas via cadastro manual ou Google Places
   ↓
2. Contato Inicial
   Primeira abordagem (telefone/email)
   ↓
3. Qualificação
   Avaliar potencial de compra
   ↓
4. Proposta
   Enviar orçamento
   ↓
5. Negociação
   Ajustar condições
   ↓
6. Conversão
   Fechar venda! 🎉
```

---

## 🎯 Regras de Negócio Principais

### Validações de Lead

✅ Lead deve ter ao menos um contato (email OU telefone)  
✅ CNPJ deve ser válido (formato + dígitos verificadores)  
✅ Email deve ter formato válido  
✅ Leads de WEBSITE devem ter email obrigatório  
✅ Leads de COLD_CALL devem ter telefone obrigatório

### Transições de Status

✅ NEW → CONTACTED (após primeiro contato)  
✅ CONTACTED → QUALIFIED (após qualificação)  
✅ QUALIFIED → PROPOSAL_SENT (após enviar proposta)  
✅ PROPOSAL_SENT → NEGOTIATING (ao iniciar negociação)  
✅ NEGOTIATING → CONVERTED (ao fechar venda)  
✅ Qualquer status → LOST (quando desiste)

❌ CONVERTED → LOST (cliente convertido não pode ser perdido)

---

## 💡 Dica de Uso

Ao adicionar novos termos ao código ou documentação:
1. Verifique se já existe neste glossário
2. Se não, adicione aqui com definição clara
3. Use o termo consistentemente em todo o código

**Objetivo:** Todos (desenvolvedores + negócio) falam a mesma língua!

---

**Última atualização:** Versão inicial - apenas Lead Management implementado
