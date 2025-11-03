# Swagger / OpenAPI - Documentação Interativa da API (API-First)

## 📖 O que é Swagger/OpenAPI?

**Swagger** (agora oficialmente conhecido como **OpenAPI**) é uma especificação para documentação de APIs REST que permite:

- **Documentação automática** da API baseada no código
- **Interface interativa** para testar endpoints diretamente no navegador
- **Geração automática** de esquemas JSON/YAML da API
- **Contratos bem definidos** entre frontend e backend

## 🔧 Implementação no Projeto

### Biblioteca Utilizada

Utilizamos o **SpringDoc OpenAPI** (versão 2.3.0) e **Swagger Parser** (versão 2.1.19):

```kotlin
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
implementation("io.swagger.parser.v3:swagger-parser:2.1.19")
```

### 🎯 Estratégia: **API-First Design**

**Importante:** Neste projeto, adotamos a estratégia de **API-First Design**, onde o contrato da API é definido ANTES da implementação do código.

Toda a especificação da API está no arquivo:
```
src/main/resources/openapi.yaml
```

### Arquivo Principal

**openapi.yaml** - Especificação completa da API
- Informações gerais (título, descrição, versão)
- Servidores disponíveis (dev, prod)
- Definição de todos os endpoints
- Schemas de requisição e resposta
- Exemplos de uso
- Códigos de resposta HTTP

**OpenApiConfig.kt** - Configuração minimalista
- Apenas carrega o arquivo YAML
- Não contém lógica de documentação
- Mantém código limpo

## 🎯 Vantagens da Abordagem API-First

### ✅ Contrato Definido Primeiro
- API é especificada antes da implementação
- Permite discussão e aprovação do design antes de codificar
- Evita retrabalho

### ✅ Desenvolvimento Paralelo
- Frontend pode começar usando mocks baseados no contrato
- Backend implementa seguindo a especificação
- Equipes trabalham simultaneamente

### ✅ Documentação como Fonte da Verdade
- Arquivo YAML é a documentação oficial
- Implementação deve seguir o contrato
- Fácil de revisar mudanças (diff no Git)

### ✅ Geração de Código
- Pode gerar clientes automaticamente (TypeScript, Java, Python, etc.)
- Pode gerar servidores stub
- Garante consistência entre cliente e servidor

### ✅ Validação Automática
- Ferramentas podem validar requisições contra o contrato
- Testes de contrato automatizados
- Detecta quebras de contrato antes de produção

### ✅ Centralização Total
- Uma única fonte de verdade
- Fácil de versionar
- Simples de compartilhar com stakeholders

### ✅ Alinhamento com Arquitetura Hexagonal
- Código permanece limpo, sem anotações
- Documentação como artefato separado
- Separação de responsabilidades

## 🌐 Acessando a Documentação

### Swagger UI (Interface Interativa)
```
http://localhost:8080/swagger-ui.html
```

A interface do Swagger UI permite:
- ✅ Visualizar todos os endpoints disponíveis
- ✅ Ver detalhes de cada endpoint (parâmetros, respostas, exemplos)
- ✅ **Testar endpoints diretamente no navegador**
- ✅ Ver modelos de dados (schemas)

### OpenAPI JSON (Especificação)
```
http://localhost:8080/v3/api-docs
```

Retorna a especificação completa da API em formato JSON.

### OpenAPI YAML (Especificação)
```
http://localhost:8080/v3/api-docs.yaml
```

Retorna a especificação em formato YAML (mesmo conteúdo do arquivo fonte).

## 📝 Estrutura do openapi.yaml

### Seções Principais

```yaml
openapi: 3.0.3
info:                    # Informações gerais da API
  title: ...
  version: ...
  description: ...
  contact: ...
  license: ...

servers:                 # Lista de servidores
  - url: http://localhost:8080
    description: Dev
  
tags:                    # Categorias de endpoints
  - name: Leads
    description: ...

paths:                   # Definição dos endpoints
  /api/v1/leads:
    post:
      tags: [Leads]
      summary: ...
      requestBody: ...
      responses: ...
      
  /api/v1/leads/{id}:
    get: ...

components:              # Schemas reutilizáveis
  schemas:
    LeadRequest: ...
    LeadResponse: ...
    ErrorResponse: ...
```

### Exemplo de Endpoint

```yaml
/api/v1/leads:
  post:
    tags:
      - Leads
    summary: Cadastrar novo lead
    description: |
      Descrição detalhada com markdown
    operationId: registerLead
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/LeadRequest'
          examples:
            leadCompleto:
              summary: Lead Completo
              value:
                companyName: Vestidos Elegantes
                email: contato@exemplo.com
                source: MANUAL_ENTRY
    responses:
      '201':
        description: Sucesso
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LeadResponse'
```

### Exemplo de Schema

```yaml
components:
  schemas:
    LeadRequest:
      type: object
      required:
        - companyName
        - source
      properties:
        companyName:
          type: string
          minLength: 2
          maxLength: 200
          example: Vestidos Elegantes
        email:
          type: string
          format: email
          example: contato@exemplo.com
```

## 🔍 Fluxo de Trabalho API-First

### 1. **Design da API** (Primeiro)
```bash
# Criar/editar openapi.yaml
vim src/main/resources/openapi.yaml
```

### 2. **Validação do Contrato**
```bash
# Validar YAML
swagger-cli validate src/main/resources/openapi.yaml
```

### 3. **Gerar Mocks** (Frontend pode começar)
```bash
# Gerar servidor mock
prism mock src/main/resources/openapi.yaml
```

### 4. **Implementar Backend** (Seguindo o contrato)
```kotlin
// Implementar controllers conforme especificação
@PostMapping
fun registerLead(@RequestBody request: LeadRequest): ResponseEntity<LeadResponse>
```

### 5. **Validar Implementação**
```bash
# Testes de contrato verificam se implementação segue spec
./gradlew test
```

## 🛠️ Ferramentas Úteis

### Validação de OpenAPI
```bash
# Instalar swagger-cli
npm install -g @apidevtools/swagger-cli

# Validar arquivo
swagger-cli validate src/main/resources/openapi.yaml
```

### Geração de Clientes
```bash
# Instalar openapi-generator
npm install -g @openapitools/openapi-generator-cli

# Gerar cliente TypeScript
openapi-generator-cli generate \
  -i src/main/resources/openapi.yaml \
  -g typescript-axios \
  -o frontend/src/api
```

### Mock Server
```bash
# Instalar Prism
npm install -g @stoplight/prism-cli

# Executar mock server
prism mock src/main/resources/openapi.yaml
```

### Visualização e Edição
- **Swagger Editor**: https://editor.swagger.io/
- **Stoplight Studio**: https://stoplight.io/studio
- **VSCode Extension**: OpenAPI (Swagger) Editor

## 📋 Como Adicionar um Novo Endpoint

### 1. Editar openapi.yaml

```yaml
paths:
  /api/v1/leads/{id}:
    put:
      tags:
        - Leads
      summary: Atualizar lead
      operationId: updateLead
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateLeadRequest'
      responses:
        '200':
          description: Lead atualizado
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LeadResponse'
```

### 2. Adicionar Schema (se necessário)

```yaml
components:
  schemas:
    UpdateLeadRequest:
      type: object
      properties:
        companyName:
          type: string
        email:
          type: string
          format: email
```

### 3. Implementar no Controller

```kotlin
@PutMapping("/{id}")
fun updateLead(
    @PathVariable id: String,
    @RequestBody request: UpdateLeadRequest
): ResponseEntity<LeadResponse> {
    // Implementação
}
```

**Importante**: A implementação deve seguir exatamente o que está no YAML!

## 🔄 Versionamento da API

Quando precisar criar uma nova versão:

```yaml
# openapi-v2.yaml
openapi: 3.0.3
info:
  title: BridalCover CRM API
  version: 2.0.0
  
paths:
  /api/v2/leads:    # Nova versão dos endpoints
    post: ...
```

## 🎨 Recursos Implementados

### ✅ Endpoints Documentados
- `POST /api/v1/leads` - Cadastrar lead
- `GET /api/v1/leads/{id}` - Buscar lead por ID

### ✅ Schemas Definidos
- `LeadRequest` - Requisição de cadastro
- `LeadResponse` - Resposta com dados do lead
- `ContactInfoResponse` - Informações de contato
- `ErrorResponse` - Resposta de erro padrão

### ✅ Exemplos Completos
- Exemplo de lead completo
- Exemplo de lead simples
- Exemplos de erros

### ✅ Validações Documentadas
- Tipos de dados
- Formatos (email, uuid, date-time)
- Enums (LeadSource, LeadStatus)
- Restrições (minLength, maxLength, pattern)

## 🚀 Próximos Passos

### Implementar CI/CD com Validação
```yaml
# .github/workflows/api-validation.yml
- name: Validate OpenAPI
  run: swagger-cli validate src/main/resources/openapi.yaml
```

### Contract Testing
```kotlin
@Test
fun `API implementation should match OpenAPI spec`() {
    // Usar ferramentas como Atlassian Swagger Request Validator
}
```

### Geração Automática de DTOs
```bash
# Gerar DTOs a partir do YAML
openapi-generator-cli generate \
  -i src/main/resources/openapi.yaml \
  -g kotlin-spring \
  --additional-properties=interfaceOnly=true
```

## 📚 Referências

- **OpenAPI Specification**: https://swagger.io/specification/
- **SpringDoc OpenAPI**: https://springdoc.org/
- **Swagger Editor**: https://editor.swagger.io/
- **API-First Design**: https://swagger.io/resources/articles/adopting-an-api-first-approach/
- **OpenAPI Generator**: https://openapi-generator.tech/

## 🎓 API-First vs Code-First

| Aspecto | API-First | Code-First |
|---------|-----------|------------|
| **Documentação** | YAML escrito primeiro | Gerado do código |
| **Desenvolvimento** | Frontend e Backend paralelo | Backend primeiro |
| **Contrato** | Arquivo YAML | Código + Anotações |
| **Mudanças** | Visíveis no Git diff | Espalhadas no código |
| **Mocking** | Fácil (antes da implementação) | Difícil |
| **Validação** | Automática via ferramentas | Manual |

**No projeto**: Usamos **API-First** para melhor colaboração e qualidade.

## 🏗️ Arquitetura da Documentação

```
openapi.yaml (Source of Truth)
  ↓
OpenApiConfig.kt (Loader)
  ↓
SpringDoc (Runtime)
  ↓
Swagger UI (Browser)
```

### Fluxo de Desenvolvimento

```
1. Design API (openapi.yaml)
   ↓
2. Review & Approve
   ↓
3. Generate Mocks
   ↓
4. Frontend Development (usando mocks)
   ↓
5. Backend Development (seguindo spec)
   ↓
6. Contract Tests
   ↓
7. Integration
```

Essa abordagem garante que a **API é desenhada pensando nos consumidores** (frontend, apps móveis) e não apenas na implementação backend.
