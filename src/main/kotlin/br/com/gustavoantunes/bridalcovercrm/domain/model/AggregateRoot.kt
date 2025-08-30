package br.com.gustavoantunes.bridalcovercrm.domain.model

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