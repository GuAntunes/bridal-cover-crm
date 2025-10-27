package br.com.gustavoantunes.bridalcovercrm.domain.model.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.AggregateId
import java.util.UUID

class LeadId private constructor(value: String) : AggregateId(value) {

    init {
        require(value.isNotBlank()) { "LeadId cannot be blank" }
        require(runCatching { UUID.fromString(value) }.isSuccess) { "LeadId must be a valid UUID" }
    }

    companion object {
        fun generate(): LeadId = LeadId(generateUUID())
        fun fromString(value: String): LeadId = LeadId(value.trim())
        fun fromUUID(uuid: UUID): LeadId = LeadId(uuid.toString())
    }
}