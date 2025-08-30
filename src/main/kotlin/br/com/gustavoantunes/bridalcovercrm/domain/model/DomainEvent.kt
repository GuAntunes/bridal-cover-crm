package br.com.gustavoantunes.bridalcovercrm.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Classe base para todos os eventos de domínio.
 * 
 * Os eventos de domínio representam algo significativo que aconteceu no domínio do negócio.
 * Eles são imutáveis e capturam mudanças de estado importantes que outros contextos 
 * ou componentes da aplicação podem precisar saber.
 */
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
    
    // Metadados adicionais para rastreabilidade e correlação
    
    /** ID do usuário que iniciou a ação que gerou o evento */
    open val userId: String? = null
    
    /** ID de correlação para rastrear uma operação através de múltiplos serviços */
    open val correlationId: String? = null
    
    /** ID de causação - referência ao evento ou comando que causou este evento */
    open val causationId: String? = null
    
    /** Contexto adicional ou metadados específicos do evento */
    open val metadata: Map<String, Any> = emptyMap()
} 