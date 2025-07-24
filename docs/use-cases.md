# Casos de Uso - BridalCover CRM

Os **Casos de Uso** descrevem as principais funcionalidades do sistema do ponto de vista do usuário, definindo como o sistema deve se comportar em diferentes cenários.

---

## 👥 Atores do Sistema

### **Vendedor**
- Usuário principal do sistema
- Responsável por prospecção e contatos com leads
- Utiliza scripts e registra interações

### **Gerente de Vendas**
- Supervisiona equipe de vendedores
- Analisa métricas e relatórios
- Define estratégias territoriais

### **Sistema Externo (Google Places API)**
- Fornece dados de estabelecimentos
- Automatiza processo de prospecção
- Alimenta base de leads

---

## 📋 UC01 - Cadastrar Lead Manualmente

### **Ator Principal**: Vendedor
### **Objetivo**: Registrar novo prospect no sistema

### **Pré-condições**:
- Vendedor autenticado no sistema
- Informações básicas do lead disponíveis

### **Fluxo Principal**:
1. Vendedor acessa formulário de cadastro de lead
2. Sistema apresenta campos obrigatórios e opcionais
3. Vendedor preenche dados do lead:
   - Nome da loja
   - CNPJ (opcional)
   - Telefone
   - Email (opcional)
   - Endereço completo
4. Sistema valida formato dos dados
5. Sistema gera ID único para o lead
6. Sistema define status inicial como "NOVO"
7. Sistema registra fonte como "CADASTRO_MANUAL"
8. Sistema salva lead na base de dados
9. Sistema dispara evento `LeadCreated`

### **Pós-condições**:
- Lead cadastrado com status "NOVO"
- Disponível para tentativas de contato
- Incluído nas análises territoriais

### **Fluxos Alternativos**:
**3a. CNPJ já existe no sistema**
- Sistema apresenta aviso de duplicação
- Oferece opção de visualizar lead existente
- Cancela operação de cadastro

**4a. Dados inválidos**
- Sistema apresenta mensagens de erro específicas
- Vendedor corrige informações
- Retorna ao passo 4

---

## 🔍 UC02 - Importar Leads via Google Places

### **Ator Principal**: Vendedor
### **Objetivo**: Buscar e importar leads automaticamente via Google Places API

### **Pré-condições**:
- Vendedor autenticado no sistema
- Integração com Google Places configurada
- Limite diário de importações não excedido

### **Fluxo Principal**:
1. Vendedor acessa função de importação
2. Sistema solicita parâmetros de busca:
   - Cidade/região
   - Raio de busca (km)
   - Palavras-chave ("aluguel vestidos noiva")
3. Vendedor define critérios e confirma busca
4. Sistema consulta Google Places API
5. Sistema filtra resultados por tipo de negócio
6. Sistema apresenta lista de estabelecimentos encontrados
7. Vendedor seleciona quais leads importar
8. Para cada lead selecionado:
   - Sistema mapeia dados do Google para modelo interno
   - Sistema verifica duplicação por nome/endereço
   - Sistema cria lead com status "NOVO"
   - Sistema define fonte como "GOOGLE_PLACES"
9. Sistema apresenta resumo da importação
10. Sistema dispara eventos `LeadCreated` para cada lead

### **Pós-condições**:
- Novos leads disponíveis no sistema
- Leads marcados com origem Google Places
- Incluídos nas análises de densidade

### **Fluxos Alternativos**:
**4a. Limite de API excedido**
- Sistema apresenta mensagem de limite atingido
- Solicita aguardar próximo período
- Cancela operação

**6a. Nenhum resultado encontrado**
- Sistema informa que não há estabelecimentos na região
- Sugere ampliar raio de busca
- Retorna ao passo 2

---

## 📞 UC03 - Realizar Tentativa de Contato

### **Ator Principal**: Vendedor
### **Objetivo**: Registrar tentativa de comunicação com lead

### **Pré-condições**:
- Lead existe no sistema
- Vendedor autenticado
- Informação de contato disponível (telefone/email)

### **Fluxo Principal**:
1. Vendedor seleciona lead para contato
2. Sistema apresenta histórico de contatos anteriores
3. Sistema sugere script baseado no contexto do lead
4. Vendedor escolhe canal de contato (telefone, email, WhatsApp)
5. Vendedor seleciona script a ser utilizado
6. Vendedor realiza o contato
7. Vendedor registra resultado do contato:
   - Data/hora da tentativa
   - Canal utilizado
   - Resultado (interessado, não atende, etc.)
   - Observações detalhadas
   - Próximo follow-up (se aplicável)
8. Sistema salva log de contato
9. Sistema atualiza métricas do script utilizado
10. Sistema dispara evento `ContactAttempted`
11. Se resultado positivo, sistema agenda follow-up automático

### **Pós-condições**:
- Contato registrado no histórico do lead
- Métricas de script atualizadas
- Follow-up agendado (se necessário)

### **Fluxos Alternativos**:
**7a. Contato resultou em conversão**
- Sistema solicita confirmação de conversão
- Vendedor confirma dados do novo cliente
- Sistema converte lead em cliente
- Sistema dispara evento `LeadConverted`

**7b. Lead não demonstrou interesse**
- Sistema pergunta sobre tentativas futuras
- Se "não", marca lead como "PERDIDO"
- Sistema dispara evento `LeadLost`

---

## 📊 UC04 - Analisar Densidade Territorial

### **Ator Principal**: Gerente de Vendas
### **Objetivo**: Visualizar distribuição geográfica de leads para otimizar estratégias

### **Pré-condições**:
- Gerente autenticado no sistema
- Leads cadastrados com informações de localização
- Dados geográficos processados

### **Fluxo Principal**:
1. Gerente acessa módulo de análise territorial
2. Sistema apresenta opções de visualização:
   - Mapa de calor por cidade
   - Ranking de regiões por densidade
   - Análise de conversão por território
3. Gerente seleciona tipo de análise
4. Sistema processa dados de localização dos leads
5. Sistema calcula densidade por região (leads/km²)
6. Sistema apresenta visualização escolhida:
   - Mapa com cores indicando densidade
   - Lista ordenada por prioridade
   - Gráficos de performance regional
7. Gerente pode filtrar por:
   - Status do lead
   - Período de criação
   - Fonte do lead
8. Sistema permite exportar dados para relatório

### **Pós-condições**:
- Análise territorial disponível
- Territórios priorizados para ação
- Dados disponíveis para planejamento estratégico

### **Fluxos Alternativos**:
**4a. Dados insuficientes para análise**
- Sistema informa necessidade de mais leads
- Sugere importação via Google Places
- Retorna à tela inicial

---

## 📝 UC05 - Gerenciar Scripts de Vendas

### **Ator Principal**: Gerente de Vendas
### **Objetivo**: Criar e gerenciar roteiros de comunicação para equipe

### **Pré-condições**:
- Gerente autenticado no sistema
- Permissão para gerenciar scripts

### **Fluxo Principal**:
1. Gerente acessa módulo de scripts
2. Sistema apresenta lista de scripts existentes com métricas
3. Gerente pode:
   - Criar novo script
   - Editar script existente
   - Visualizar efetividade
   - Ativar/desativar script
4. Para criação de novo script:
   - Gerente define nome e categoria
   - Escreve conteúdo do script
   - Define situações de uso
   - Sistema salva e ativa script
5. Sistema calcula efetividade baseado em usos históricos
6. Sistema apresenta ranking por performance

### **Pós-condições**:
- Scripts atualizados no sistema
- Disponíveis para uso pela equipe
- Métricas de efetividade atualizadas

---

## 🔄 UC06 - Converter Lead em Cliente

### **Ator Principal**: Vendedor
### **Objetivo**: Finalizar processo de vendas convertendo prospect em cliente

### **Pré-condições**:
- Lead qualificado no sistema
- Negociação avançada
- Aprovação para conversão

### **Fluxo Principal**:
1. Vendedor acessa lead qualificado
2. Sistema verifica se lead atende critérios de conversão:
   - Pelo menos um contato bem-sucedido
   - Status "NEGOCIANDO" ou superior
   - Informações de contato completas
3. Vendedor solicita conversão em cliente
4. Sistema apresenta formulário de cliente com dados pré-preenchidos
5. Vendedor confirma/ajusta informações:
   - Dados contratuais
   - Informações de faturamento
   - Data de início do relacionamento
6. Sistema valida completude dos dados
7. Sistema cria registro de cliente
8. Sistema marca lead como "CONVERTIDO"
9. Sistema dispara evento `LeadConverted`
10. Sistema apresenta confirmação de conversão

### **Pós-condições**:
- Cliente criado no sistema
- Lead marcado como convertido
- Métricas de conversão atualizadas
- Relacionamento comercial iniciado

---

## 📈 UC07 - Visualizar Dashboard de Vendas

### **Ator Principal**: Vendedor / Gerente de Vendas
### **Objetivo**: Acompanhar métricas e performance de vendas

### **Pré-condições**:
- Usuário autenticado no sistema
- Dados de vendas disponíveis

### **Fluxo Principal**:
1. Usuário acessa dashboard principal
2. Sistema apresenta métricas principais:
   - Total de leads por status
   - Taxa de conversão atual
   - Contatos realizados no período
   - Ranking de scripts por efetividade
   - Mapa de densidade territorial
3. Sistema permite filtros por:
   - Período (última semana, mês, trimestre)
   - Território
   - Vendedor (para gerentes)
   - Fonte de leads
4. Sistema atualiza visualizações conforme filtros
5. Sistema oferece opção de exportar relatórios

### **Pós-condições**:
- Métricas visualizadas
- Performance acompanhada
- Insights para tomada de decisão

---

## 🎯 Casos de Uso Secundários

### **UC08 - Agendar Follow-up**
- Programar próximo contato com lead
- Definir data, canal e observações
- Integrar com agenda do vendedor

### **UC09 - Exportar Relatórios**
- Gerar relatórios de performance
- Incluir métricas detalhadas
- Formatos PDF e Excel

### **UC10 - Qualificar Lead**
- Avaliar potencial de compra
- Definir critérios de qualificação
- Atualizar status para "QUALIFICADO"

### **UC11 - Gerenciar Territórios**
- Definir regiões de atuação
- Atribuir vendedores por território
- Calcular performance regional

---

## 📊 Matriz de Rastreabilidade

| Caso de Uso | Bounded Context | Aggregates Envolvidos | Domain Events |
|-------------|-----------------|----------------------|---------------|
| UC01 | Lead Management | Lead | LeadCreated |
| UC02 | External Integration, Lead Management | GooglePlacesData, Lead | PlacesImported, LeadCreated |
| UC03 | Sales Execution, Lead Management | ContactLog, Script, Lead | ContactAttempted |
| UC04 | Geographic Analytics | Territory, LeadDensity | TerritoryAnalyzed |
| UC05 | Sales Execution | Script | ScriptUpdated |
| UC06 | Lead Management | Lead, Client | LeadConverted |
| UC07 | Todos os contextos | Dashboard | - |

---

**Nota**: Estes casos de uso representam as funcionalidades principais do sistema. Casos de uso detalhados adicionais podem ser criados conforme necessário durante o desenvolvimento. 