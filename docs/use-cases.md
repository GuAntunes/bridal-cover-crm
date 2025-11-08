# 📋 Casos de Uso - BridalCover CRM

Funcionalidades do sistema do ponto de vista do usuário.

---

## 👥 Atores

**Vendedor** - Usuário principal que gerencia leads e contatos

---

## ✅ Casos de Uso Implementados

### UC01 - Cadastrar Lead Manualmente

**Objetivo:** Registrar novo prospect no sistema

**Fluxo:**
1. Vendedor acessa formulário de cadastro
2. Preenche dados obrigatórios:
   - Nome da loja
   - Telefone OU Email (pelo menos um)
   - Fonte (ex: MANUAL_ENTRY)
3. Sistema valida dados (formato de email, CNPJ se fornecido)
4. Sistema cria lead com status NEW
5. Sistema retorna lead cadastrado com ID único

**Validações:**
- Nome da loja: mínimo 2 caracteres
- Email: formato válido
- CNPJ: formato e dígitos verificadores válidos
- Pelo menos um contato (email ou telefone)

**Endpoint:** `POST /api/v1/leads`

**Exemplo:**
```json
{
  "companyName": "Ateliê Elegante",
  "email": "contato@elegante.com.br",
  "phone": "+55 11 98765-4321",
  "cnpj": "12.345.678/0001-90",
  "source": "MANUAL_ENTRY"
}
```

---

### UC02 - Buscar Lead por ID

**Objetivo:** Recuperar informações de um lead específico

**Fluxo:**
1. Vendedor fornece ID do lead
2. Sistema busca no banco de dados
3. Sistema retorna dados completos do lead

**Retorno:**
- Dados completos do lead
- Status atual
- Data de criação e última atualização

**Endpoint:** `GET /api/v1/leads/{id}`

---

### UC03 - Verificar Saúde do Sistema

**Objetivo:** Confirmar que a aplicação está rodando

**Fluxo:**
1. Sistema (ou monitoramento) acessa endpoint de health
2. Aplicação responde com status UP

**Endpoint:** `GET /health`

---

## 🔄 Casos de Uso Em Desenvolvimento

### UC04 - Atualizar Lead

**Objetivo:** Modificar informações de um lead existente

**Fluxo proposto:**
1. Vendedor fornece ID e campos a atualizar
2. Sistema valida dados
3. Sistema atualiza lead
4. Sistema retorna lead atualizado

**Campos atualizáveis:** nome, email, telefone, CNPJ, Instagram

---

### UC05 - Listar Leads

**Objetivo:** Ver todos os leads com paginação

**Fluxo proposto:**
1. Vendedor solicita lista (com página e tamanho)
2. Sistema retorna leads ordenados por data de criação
3. Sistema inclui informações de paginação

**Parâmetros:**
- page (padrão: 0)
- size (padrão: 20, máximo: 100)

---

### UC06 - Mudar Status do Lead

**Objetivo:** Avançar lead no funil de vendas

**Fluxo proposto:**
1. Vendedor seleciona lead e novo status
2. Sistema valida transição de status
3. Sistema atualiza lead
4. Sistema registra data da mudança

**Transições válidas:**
- NEW → CONTACTED
- CONTACTED → QUALIFIED
- QUALIFIED → PROPOSAL_SENT
- PROPOSAL_SENT → NEGOTIATING
- NEGOTIATING → CONVERTED
- Qualquer → LOST

---

## 📅 Roadmap de Casos de Uso

### Fase 1: CRUD Completo (Atual)
- [x] UC01 - Cadastrar lead
- [x] UC02 - Buscar lead por ID
- [ ] UC04 - Atualizar lead
- [ ] UC05 - Listar leads
- [ ] UC06 - Mudar status
- [ ] UC07 - Deletar lead

### Fase 2: Gestão de Contatos
- [ ] UC08 - Registrar tentativa de contato
- [ ] UC09 - Ver histórico de contatos
- [ ] UC10 - Agendar follow-up
- [ ] UC11 - Listar follow-ups pendentes

### Fase 3: Relatórios
- [ ] UC12 - Dashboard de vendas
- [ ] UC13 - Taxa de conversão por período
- [ ] UC14 - Leads por status
- [ ] UC15 - Performance por fonte

### Fase 4: Integrações
- [ ] UC16 - Importar leads do Google Places
- [ ] UC17 - Buscar lojas por cidade
- [ ] UC18 - Deduplicar leads importados

---

## 🎯 Matriz de Rastreabilidade

| Caso de Uso | Status | Endpoint | Agregado |
|-------------|--------|----------|----------|
| UC01 - Cadastrar Lead | ✅ Implementado | POST /api/v1/leads | Lead |
| UC02 - Buscar Lead | ✅ Implementado | GET /api/v1/leads/{id} | Lead |
| UC03 - Health Check | ✅ Implementado | GET /health | - |
| UC04 - Atualizar Lead | ⏳ Planejado | PUT /api/v1/leads/{id} | Lead |
| UC05 - Listar Leads | ⏳ Planejado | GET /api/v1/leads | Lead |

---

## 📝 Template para Novos Casos de Uso

Ao documentar novos casos de uso, siga este formato:

```markdown
### UCXX - Nome do Caso de Uso

**Objetivo:** Uma frase descrevendo o objetivo

**Ator:** Quem executa

**Pré-condições:** O que precisa existir antes

**Fluxo Principal:**
1. Passo 1
2. Passo 2
3. ...

**Pós-condições:** Estado do sistema após sucesso

**Validações:** Regras de negócio aplicadas

**Endpoint:** Rota da API (se aplicável)
```

---

**Nota:** Este documento evolui com o projeto. Casos de uso detalhados são criados conforme necessário durante o desenvolvimento.
