package br.com.gustavoantunes.bridalcovercrm.domain.model.common

import java.util.UUID

/**
 * Abstract base class for aggregate identifiers.
 *
 * Encapsulates common rules for unique identifiers based on UUID,
 * ensuring consistency and code reuse throughout the domain.
 *
 * Follows DDD principles where each aggregate must have a
 * unique and immutable identifier that distinguishes it from other aggregates of the same type.
 */
abstract class AggregateId(val value: String) {

    init {
        require(value.isNotBlank()) { "Aggregate ID cannot be empty" }
        require(isValidUUID(value)) { "Aggregate ID must be a valid UUID: $value" }
    }

    companion object {
        /**
         * Validates if a string represents a valid UUID
         */
        fun isValidUUID(value: String): Boolean {
            return try {
                UUID.fromString(value)
                true
            } catch (e: IllegalArgumentException) {
                false
            }
        }

        /**
         * Generates a new UUID as string
         */
        fun generateUUID(): String = UUID.randomUUID().toString()

        /**
         * Converts a UUID to string
         */
        fun fromUUID(uuid: UUID): String = uuid.toString()
    }

    /**
     * Returns the UUID as UUID object
     */
    fun toUUID(): UUID = UUID.fromString(value)

    /**
     * Checks if this ID equals another ID (value comparison)
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateId) return false
        return value == other.value
    }

    /**
     * Calculates hash code based on UUID value
     */
    override fun hashCode(): Int = value.hashCode()

    /**
     * Returns the string representation of the ID
     */
    override fun toString(): String = value

    /**
     * Checks if the ID is null or empty (for special cases)
     */
    fun isEmpty(): Boolean = value.isBlank()

    /**
     * Checks if the ID is not null nor empty
     */
    fun isNotEmpty(): Boolean = value.isNotBlank()

    /**
     * Returns a masked version of the ID for logs (first 8 characters + ***)
     */
    fun getMasked(): String {
        return if (value.length > 8) {
            "${value.take(8)}***"
        } else {
            "***"
        }
    }

    /**
     * Returns a short version of the ID (first 8 characters)
     */
    fun getShort(): String = value.take(8)

    /**
     * Compares this ID with another ID lexicographically
     */
    fun compareTo(other: AggregateId): Int = value.compareTo(other.value)
}