package br.com.gustavoantunes.bridalcovercrm.domain.model.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.AggregateId
import java.util.UUID

/**
 * Value Object representing a Lead's unique identifier.
 *
 * This identifier is used to uniquely distinguish one Lead from another
 * in the system. It is automatically generated as a UUID to ensure uniqueness.
 *
 * Inherits from AggregateId to reuse common validation rules and
 * handle UUID-based identifiers.
 */
class LeadId private constructor(value: String) : AggregateId(value) {

    companion object {
        /**
         * Generates a new LeadId with a random UUID
         */
        fun generate(): LeadId = LeadId(generateUUID())

        /**
         * Creates a LeadId from a string, validating the format
         */
        fun fromString(value: String): LeadId = LeadId(value)

        /**
         * Creates a LeadId from a UUID
         */
        fun fromUUID(uuid: UUID): LeadId = LeadId(uuid.toString())
    }
}