package br.com.gustavoantunes.bridalcovercrm.domain.model.common

import java.util.UUID

/**
 * Abstract base class for aggregates that use AggregateId as identifier.
 *
 * This is a more specific version of AggregateRoot that enforces the use of
 * AggregateId as identifier type, ensuring consistency and
 * leveraging all functionalities of the base ID class.
 *
 * Use this class when you want to ensure that the aggregate uses an identifier
 * based on UUID with all standard validations and functionalities.
 */
abstract class AggregateRootWithId<T : AggregateId> : AggregateRoot<T>() {

    /**
     * Returns the string value of the ID
     */
    fun getIdValue(): String = id.value

    /**
     * Returns the UUID of the ID
     */
    fun getIdAsUUID(): UUID = id.toUUID()

    /**
     * Returns a masked version of the ID for logs
     */
    fun getMaskedId(): String = id.getMasked()

    /**
     * Returns a short version of the ID
     */
    fun getShortId(): String = id.getShort()

    /**
     * Checks if the ID is not empty
     */
    fun hasValidId(): Boolean = id.isNotEmpty()

    /**
     * Improved string representation of the aggregate
     */
    override fun toString(): String {
        val className = this::class.simpleName
        return "$className(id=${id.getShort()})"
    }
}