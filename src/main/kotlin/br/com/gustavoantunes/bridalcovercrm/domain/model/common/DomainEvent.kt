package br.com.gustavoantunes.bridalcovercrm.domain.model.common

import java.time.Instant
import java.util.UUID

/**
 * Base class for all domain events.
 * 
 * Domain events represent something significant that happened in the business domain.
 * They are immutable and capture important state changes that other contexts 
 * or application components may need to know about.
 */
abstract class DomainEvent {
    /** Unique identifier of the event */
    val eventId: String = UUID.randomUUID().toString()
    
    /** UTC timestamp of when the event occurred */
    val occurredOn: Instant = Instant.now()
    
    /** Event version for evolution control */
    val eventVersion: String = "1.0"
    
    /** ID of the aggregate that generated the event */
    abstract val aggregateId: String
    
    /** Event type (class name by convention) */
    abstract val eventType: String
    
    // Additional metadata for traceability and correlation
    
    /** ID of the user who initiated the action that generated the event */
    open val userId: String? = null
    
    /** Correlation ID to track an operation across multiple services */
    open val correlationId: String? = null
    
    /** Causation ID - reference to the event or command that caused this event */
    open val causationId: String? = null
    
    /** Additional context or event-specific metadata */
    open val metadata: Map<String, Any> = emptyMap()
} 