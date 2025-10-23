package br.com.gustavoantunes.bridalcovercrm.domain.model.common

/**
 * Abstract base class for domain objects (entities).
 * 
 * Defines the common structure for all domain entities,
 * including unique identification and basic comparison operations.
 * 
 * Unlike aggregates, entities are objects that have identity
 * but are not aggregate roots and do not manage domain events.
 */
abstract class DomainObject<T> {
    abstract val id: T

    /**
     * Compares entities based on ID
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DomainObject<*>) return false
        return id == other.id
    }

    /**
     * Calculates hash code based on ID
     */
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
    
    /**
     * String representation of the entity (shows type and ID)
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
    
    /**
     * Checks if the entity has a valid ID
     */
    fun hasValidId(): Boolean {
        val currentId = id
        return when (currentId) {
            is AggregateId -> currentId.isNotEmpty()
            else -> currentId != null
        }
    }
}
