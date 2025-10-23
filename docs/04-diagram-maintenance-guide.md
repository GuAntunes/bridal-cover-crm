# Guia de Manutenção dos Diagramas

## Visão Geral

Este documento explica como manter os diagramas de arquitetura atualizados conforme o desenvolvimento progride, garantindo que sempre reflitam o estado real do sistema.

## Sistema de Status dos Componentes

### Cores e Marcações

- **🟢 Verde (`<<implemented>>`)**: Componente existe no código
- **🔵 Azul (`<<planned>>`)**: Planejado para implementação futura
- **🟡 Amarelo (`<<in_progress>>`)**: Em desenvolvimento ativo
- **🔴 Vermelho (`<<deprecated>>`)**: Descontinuado ou removido

### Bordas

- **Sólida**: Componente implementado
- **Tracejada**: Componente planejado

## Processo de Atualização

### 1. Ao Implementar um Novo Componente

Quando você implementar um componente que estava marcado como `<<planned>>`:

1. **Mude o status** de `<<planned>>` para `<<implemented>>`
2. **Atualize a cor** nos diagramas relevantes
3. **Adicione detalhes** específicos da implementação se necessário
4. **Verifique dependências** e atualize componentes relacionados

**Exemplo:**
```plantuml
// ANTES
AGGREGATE_ROOT Lead <<planned>> {
    +id: LeadId
    +name: String
    // ...
}

// DEPOIS
AGGREGATE_ROOT Lead <<implemented>> {
    +id: LeadId
    +name: String
    +status: LeadStatus
    // ... métodos implementados
}
```

### 2. Ao Iniciar o Desenvolvimento

Quando começar a trabalhar em um componente:

1. **Mude** de `<<planned>>` para `<<in_progress>>`
2. **Use cor amarela** temporariamente
3. **Adicione notas** sobre o progresso se necessário

### 3. Ao Remover ou Deprecar

Quando um componente não for mais necessário:

1. **Marque** como `<<deprecated>>`
2. **Use cor vermelha**
3. **Adicione nota** explicando o motivo
4. **Remova** do diagrama na próxima versão

## Diagramas por Tipo (Estrutura Simplificada)

### `overview.puml`
- **Atualização**: A cada fase completada
- **Foco**: Status geral do projeto (Done → Next → Future)
- **Frequência**: Semanal ou a cada milestone
- **Como atualizar**: Mover itens entre as seções conforme progresso

### `domain.puml`
- **Atualização**: A cada implementação de classe/evento
- **Foco**: Modelo de domínio detalhado com status de implementação
- **Frequência**: A cada implementação de domínio
- **Como atualizar**: Mudar `<<planned>>` → `<<in_progress>>` → `<<implemented>>`

### `roadmap.puml`
- **Atualização**: Quando cronograma muda
- **Foco**: Fases de desenvolvimento e timeline
- **Frequência**: Mensal ou quando prioridades mudam
- **Como atualizar**: Ajustar estimativas e marcar fases como completas

## Checklist de Atualização

### Ao Completar uma Feature

- [ ] Atualizar status em `domain.puml` (`<<planned>>` → `<<implemented>>`)
- [ ] Verificar se novas dependências foram criadas
- [ ] Atualizar `overview.puml` se mudou de fase
- [ ] Documentar mudanças no commit
- [ ] Testar se diagramas renderizam corretamente

### Ao Finalizar um Sprint

- [ ] Revisar todos os 3 diagramas
- [ ] Atualizar `domain.puml` com progresso das classes
- [ ] Ajustar `roadmap.puml` se cronograma mudou
- [ ] Verificar consistência entre os diagramas
- [ ] Atualizar README se necessário

### Ao Finalizar uma Fase

- [ ] Mover componentes de "planned" para "implemented" em `domain.puml`
- [ ] Atualizar `overview.puml` (Done → Next → Future)
- [ ] Marcar fase como completa em `roadmap.puml`
- [ ] Revisar prioridades da próxima fase
- [ ] Celebrar o progresso! 🎉

## Ferramentas e Automação

### PlantUML Preview

Use extensões do VS Code para visualizar diagramas:
- PlantUML (oficial)
- PlantUML Previewer

### Scripts de Validação

Considere criar scripts que:
- Verificam consistência entre diagramas
- Validam que componentes implementados estão marcados corretamente
- Geram relatórios de progresso

### Integração com CI/CD

Possível automação futura:
- Validar diagramas em PRs
- Gerar diagramas automaticamente do código
- Alertar sobre inconsistências

## Exemplos Práticos

### Implementando o Lead Aggregate

**1. Estado Inicial (Planejado)**
```plantuml
class Lead <<planned>> {
    +id: LeadId
    +name: String
    +email: String?
    +phone: String?
    +address: Address
    +status: LeadStatus
    +source: LeadSource
    --
    +convertToClient(): Client
    +updateStatus(status: LeadStatus): void
}
```

**2. Durante Desenvolvimento**
```plantuml
class Lead <<in_progress>> {
    +id: LeadId
    +name: String
    +email: String?
    +phone: String?
    +address: Address
    +status: LeadStatus
    +source: LeadSource
    --
    +convertToClient(): Client  // TODO
    +updateStatus(status: LeadStatus): void  // DONE
}
```

**3. Implementação Completa**
```plantuml
class Lead <<implemented>> {
    +id: LeadId
    +name: String
    +email: String?
    +phone: String?
    +address: Address
    +status: LeadStatus
    +source: LeadSource
    +createdAt: LocalDateTime
    +updatedAt: LocalDateTime
    --
    +convertToClient(): Client
    +updateStatus(status: LeadStatus): void
    +addContact(contact: ContactLog): void
    +updateContactInfo(email: String?, phone: String?): void
}
```

### Implementando Value Objects

**Exemplo: Address Value Object**
```plantuml
// Estado inicial
class Address <<planned>> {
    +city: String
    +state: String
    --
    +isComplete(): Boolean
}

// Após implementação
class Address <<implemented>> {
    +street: String?
    +city: String
    +state: String
    +postalCode: String?
    +latitude: Double?
    +longitude: Double?
    +country: String
    --
    +isComplete(): Boolean
    +getCoordinates(): Coordinates?
    +distanceTo(other: Address): Double?
}
```

### Mudança de Arquitetura

Se decidir usar MongoDB ao invés de PostgreSQL:

1. **Marque PostgreSQL** como `<<deprecated>>`
2. **Adicione MongoDB** como `<<planned>>` ou `<<implemented>>`
3. **Atualize dependências** nos diagramas
4. **Documente a mudança** no ADR correspondente

## Responsabilidades

### Desenvolvedor
- Atualizar diagramas ao implementar features
- Marcar componentes com status correto
- Documentar mudanças significativas

### Tech Lead
- Revisar consistência dos diagramas
- Aprovar mudanças arquiteturais
- Manter roadmap atualizado

### Equipe
- Usar diagramas como referência
- Reportar inconsistências encontradas
- Sugerir melhorias no processo

## Benefícios

### Para Novos Desenvolvedores
- **Clareza** sobre o que existe vs. o que está planejado
- **Roadmap** visual do desenvolvimento
- **Contexto** histórico das decisões

### Para a Equipe
- **Alinhamento** sobre prioridades
- **Visibilidade** do progresso
- **Documentação** viva da arquitetura

### Para Stakeholders
- **Transparência** sobre o desenvolvimento
- **Expectativas** realistas sobre timelines
- **Confiança** na qualidade da documentação

## Conclusão

Manter os diagramas atualizados é um investimento que paga dividendos em:
- Comunicação mais clara
- Onboarding mais rápido
- Decisões arquiteturais mais informadas
- Redução de débito técnico

Lembre-se: **diagramas desatualizados são piores que nenhum diagrama**. Mantenha-os simples, precisos e atualizados!
