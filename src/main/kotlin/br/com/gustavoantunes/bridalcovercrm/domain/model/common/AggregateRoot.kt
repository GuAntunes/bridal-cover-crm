package br.com.gustavoantunes.bridalcovercrm.domain.model.common

/**
 * Abstract base class for aggregates in the domain.
 *
 * Defines the common structure for all aggregates, including:
 * - Unique identifier (preferably AggregateId)
 * - Domain event management
 * - Basic comparison and hash operations
 *
 * Follows DDD principles where each aggregate is a consistency unit
 * and encapsulates related business rules.
 */
abstract class AggregateRoot<T> {
    abstract val id: T

    private val _domainEvents = mutableListOf<DomainEvent>()
    val domainEvents: List<DomainEvent> get() = _domainEvents.toList()

    /**
     * Adds a domain event to the list of pending events
     */
    protected fun addDomainEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    /**
     * Clears all pending domain events
     */
    fun clearDomainEvents() {
        _domainEvents.clear()
    }

    /**
     * Checks if there are pending domain events
     */
    fun hasDomainEvents(): Boolean = _domainEvents.isNotEmpty()

    /**
     * Returns the number of pending domain events
     */
    fun getDomainEventsCount(): Int = _domainEvents.size

    /**
     * Compares aggregates based on ID
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateRoot<*>) return false
        return id == other.id
    }

    /**
     * Calculates hash code based on ID
     */
    override fun hashCode(): Int = id?.hashCode() ?: 0

    /**
     * String representation of the aggregate (shows type and ID)
     */
    override fun toString(): String {
        val className = this::class.simpleName
        val currentId = id
        val idValue = when (currentId) {
            is AggregateId -> currentId.getShort()
            else -> currentId.toString().take(8)
        }
        return "$className(id=$idValue)"
    }
}