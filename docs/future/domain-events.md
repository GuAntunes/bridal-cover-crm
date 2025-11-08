# 3. Eventos de Domínio

## Visão Geral

Os eventos de domínio são uma das ferramentas mais poderosas do Domain-Driven Design (DDD) para capturar e comunicar mudanças significativas que ocorrem no domínio do negócio. Eles representam fatos que já aconteceram e são fundamentais para criar sistemas desacoplados, auditáveis e extensíveis.

## O que são Eventos de Domínio?

Um evento de domínio é uma representação de algo importante que aconteceu no domínio do negócio. Eles possuem as seguintes características:

- **Imutáveis**: Uma vez criados, não podem ser alterados
- **Representam o passado**: Algo que já aconteceu (verbos no passado)
- **Ricos em informação**: Contêm dados suficientes para que outros contextos possam reagir
- **Identificáveis**: Possuem identificadores únicos e timestamps
- **Versionáveis**: Permitem evolução sem quebrar compatibilidade

## Arquitetura de Eventos

### Componentes Principais

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  AggregateRoot  │───▶│   DomainEvent    │───▶│ EventPublisher  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │ EventDispatcher  │───▶│  EventHandler   │
                       └──────────────────┘    └─────────────────┘
```

### Fluxo de Eventos

1. **Geração**: Agregados geram eventos quando mudanças significativas ocorrem
2. **Coleta**: Eventos são coletados no agregado até serem publicados
3. **Publicação**: Application Services publicam os eventos após persistir o agregado
4. **Distribuição**: Event Dispatcher distribui eventos para handlers apropriados
5. **Processamento**: Event Handlers processam os eventos e executam ações

## Implementação

### Classe Base DomainEvent

```kotlin
abstract class DomainEvent {
    /** Identificador único do evento */
    val eventId: String = UUID.randomUUID().toString()
    
    /** Timestamp UTC de quando o evento ocorreu */
    val occurredOn: Instant = Instant.now()
    
    /** Versão do evento para controle de evolução */
    val eventVersion: String = "1.0"
    
    /** ID do agregado que gerou o evento */
    abstract val aggregateId: String
    
    /** Tipo do evento (nome da classe por convenção) */
    abstract val eventType: String
    
    // Metadados para rastreabilidade
    open val userId: String? = null
    open val correlationId: String? = null
    open val causationId: String? = null
    open val metadata: Map<String, Any> = emptyMap()
}
```

### AggregateRoot com Eventos

```kotlin
abstract class AggregateRoot<T> {
    abstract val id: T
    
    private val _domainEvents = mutableListOf<DomainEvent>()
    val domainEvents: List<DomainEvent> get() = _domainEvents.toList()
    
    protected fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }
    
    fun clearDomainEvents() {
        _domainEvents.clear()
    }
}
```

### Exemplo de Evento Específico

```kotlin
class ClienteEmailAlteradoEvent(
    override val aggregateId: String,
    val emailAnterior: String,
    val novoEmail: String,
    override val userId: String? = null,
    override val correlationId: String? = null
) : DomainEvent() {
    override val eventType: String = "ClienteEmailAlterado"
}
```

## Metadados dos Eventos

### Metadados Essenciais

- **eventId**: Identificador único para cada evento (UUID)
- **occurredOn**: Timestamp UTC preciso de quando o evento ocorreu
- **eventVersion**: Versão do schema do evento para evolução
- **aggregateId**: ID do agregado que originou o evento
- **eventType**: Tipo/nome do evento para roteamento

### Metadados de Rastreabilidade

- **userId**: ID do usuário que iniciou a ação
  - Permite auditoria e rastreamento de responsabilidade
  - Útil para logs de segurança e compliance

- **correlationId**: ID que conecta eventos relacionados
  - Rastreia uma operação através de múltiplos serviços
  - Facilita debugging distribuído
  - Exemplo: todos os eventos de um processo de checkout têm o mesmo correlationId

- **causationId**: ID do evento ou comando que causou este evento
  - Cria uma cadeia de causalidade
  - Permite reconstruir o fluxo de eventos
  - Útil para análise de impacto

- **metadata**: Contexto adicional específico do evento
  - Informações que podem ser úteis para handlers
  - Dados de contexto que não fazem parte do evento principal
  - Flexibilidade para casos específicos

## Padrões de Uso

### 1. Event Sourcing

Os eventos são a fonte da verdade do sistema:

```kotlin
class ContaAggregate {
    private var saldo: BigDecimal = BigDecimal.ZERO
    
    fun depositar(valor: BigDecimal, userId: String) {
        // Validações...
        addDomainEvent(DepositoRealizadoEvent(id, valor, userId))
        aplicar(DepositoRealizadoEvent(id, valor, userId))
    }
    
    private fun aplicar(event: DepositoRealizadoEvent) {
        saldo = saldo.add(event.valor)
    }
}
```

### 2. Event-Driven Architecture

Eventos disparam ações em outros contextos:

```kotlin
class EmailAlteradoHandler : DomainEventHandler<ClienteEmailAlteradoEvent> {
    override fun handle(event: ClienteEmailAlteradoEvent) {
        // Atualizar sistema de notificações
        notificationService.updateEmail(event.aggregateId, event.novoEmail)
        
        // Sincronizar com CRM externo
        crmService.syncCustomerEmail(event.aggregateId, event.novoEmail)
        
        // Log de auditoria
        auditService.logEmailChange(event)
    }
}
```

### 3. CQRS (Command Query Responsibility Segregation)

Eventos atualizam modelos de leitura:

```kotlin
class ClienteProjectionHandler : DomainEventHandler<ClienteEmailAlteradoEvent> {
    override fun handle(event: ClienteEmailAlteradoEvent) {
        val projection = clienteProjectionRepository.findById(event.aggregateId)
        projection?.let {
            it.email = event.novoEmail
            it.lastUpdated = event.occurredOn
            clienteProjectionRepository.save(it)
        }
    }
}
```

## Benefícios

### 1. Desacoplamento
- Diferentes contextos podem reagir aos eventos sem conhecer uns aos outros
- Facilita a evolução independente dos bounded contexts
- Reduz dependências diretas entre módulos

### 2. Auditoria Natural
- Histórico completo de mudanças no domínio
- Rastreabilidade de todas as operações
- Compliance com regulamentações (LGPD, SOX, etc.)

### 3. Integração Entre Contextos
- Comunicação assíncrona entre bounded contexts
- Eventual consistency entre agregados
- Facilita arquiteturas distribuídas

### 4. Extensibilidade
- Novos handlers podem ser adicionados sem modificar código existente
- Facilita a implementação de funcionalidades como notificações, relatórios, etc.
- Suporte a múltiplos consumidores do mesmo evento

### 5. Testabilidade
- Eventos são objetos simples e fáceis de testar
- Comportamento do agregado pode ser verificado através dos eventos gerados
- Handlers podem ser testados independentemente

## Boas Práticas

### 1. Nomenclatura
- Use verbos no passado: `ClienteCriado`, `PedidoCancelado`
- Seja específico: `EmailAlterado` vs `ClienteAtualizado`
- Inclua o contexto quando necessário: `PagamentoAprovado` vs `Aprovado`

### 2. Conteúdo dos Eventos
- Inclua todas as informações necessárias para handlers
- Evite referências que podem se tornar inválidas
- Mantenha eventos pequenos mas completos

### 3. Versionamento
- Sempre versione seus eventos
- Mantenha compatibilidade com versões anteriores
- Use estratégias de migração para mudanças breaking

### 4. Idempotência
- Handlers devem ser idempotentes
- Mesmo evento processado múltiplas vezes deve ter o mesmo resultado
- Use identificadores únicos para detectar duplicatas

### 5. Ordenação
- Eventos do mesmo agregado devem ser processados em ordem
- Use timestamps e números de sequência quando necessário
- Considere particionamento por agregateId

## Exemplo Completo

### Agregado Cliente

```kotlin
class Cliente(
    override val id: String,
    var nome: String,
    var email: String
) : AggregateRoot<String>() {
    
    fun alterarEmail(novoEmail: String, userId: String, correlationId: String) {
        require(novoEmail.isNotBlank()) { "Email não pode estar vazio" }
        
        val emailAnterior = this.email
        if (emailAnterior != novoEmail) {
            this.email = novoEmail
            
            addDomainEvent(
                ClienteEmailAlteradoEvent(
                    aggregateId = this.id,
                    emailAnterior = emailAnterior,
                    novoEmail = novoEmail,
                    userId = userId,
                    correlationId = correlationId
                )
            )
        }
    }
}
```

### Application Service

```kotlin
@Service
class ClienteService(
    private val clienteRepository: ClienteRepository,
    private val eventPublisher: DomainEventPublisher
) {
    @Transactional
    fun alterarEmailCliente(clienteId: String, novoEmail: String, userId: String) {
        val correlationId = UUID.randomUUID().toString()
        val cliente = clienteRepository.findById(clienteId)
        
        cliente.alterarEmail(novoEmail, userId, correlationId)
        clienteRepository.save(cliente)
        
        eventPublisher.publishAndClear(cliente)
    }
}
```

### Event Handler

```kotlin
@Component
class ClienteEmailAlteradoHandler(
    private val emailService: EmailService,
    private val auditService: AuditService
) : DomainEventHandler<ClienteEmailAlteradoEvent> {
    
    override fun handle(event: ClienteEmailAlteradoEvent) {
        // Enviar email de confirmação
        emailService.enviarConfirmacaoAlteracaoEmail(
            event.novoEmail, 
            event.aggregateId
        )
        
        // Registrar auditoria
        auditService.registrarAlteracaoEmail(
            clienteId = event.aggregateId,
            emailAnterior = event.emailAnterior,
            novoEmail = event.novoEmail,
            userId = event.userId,
            timestamp = event.occurredOn
        )
    }
    
    override fun canHandle(event: DomainEvent): Boolean {
        return event is ClienteEmailAlteradoEvent
    }
    
    override fun getEventType(): Class<ClienteEmailAlteradoEvent> {
        return ClienteEmailAlteradoEvent::class.java
    }
}
```

## Considerações de Performance

### 1. Processamento Assíncrono
- Use filas para processamento não crítico
- Implemente retry policies para falhas temporárias
- Monitore latência e throughput dos handlers

### 2. Armazenamento de Eventos
- Considere event stores dedicados para alta performance
- Implemente estratégias de particionamento
- Use snapshots para agregados com muitos eventos

### 3. Serialização
- Use formatos eficientes (Avro, Protocol Buffers)
- Considere compressão para eventos grandes
- Cache schemas para melhor performance

## Monitoramento e Observabilidade

### 1. Métricas
- Número de eventos publicados por tipo
- Latência de processamento dos handlers
- Taxa de erro por handler
- Tamanho médio dos eventos

### 2. Logs
- Log estruturado com correlationId
- Trace de eventos através do sistema
- Logs de erro com contexto completo

### 3. Alertas
- Falhas de processamento de eventos críticos
- Aumento anormal no volume de eventos
- Latência alta em handlers importantes

## Conclusão

Os eventos de domínio são fundamentais para construir sistemas robustos, desacoplados e auditáveis. Eles permitem que diferentes partes do sistema reajam a mudanças no domínio de forma independente, facilitando a manutenção e evolução do software.

A implementação correta dos eventos de domínio, com metadados adequados e padrões bem definidos, proporciona uma base sólida para arquiteturas event-driven e facilita a implementação de funcionalidades como auditoria, integração entre contextos e eventual consistency.
