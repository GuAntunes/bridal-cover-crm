package br.com.gustavoantunes.bridalcovercrm.domain.model

abstract class DomainObject<T> {
    abstract val id: T

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DomainObject<*>) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
