package br.com.gustavoantunes.bridalcovercrm.domain.model.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.model.common.AggregateRootWithId
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.ContactInfo
import java.time.LocalDateTime

class Lead(
    override val id: LeadId,
    var name: CompanyName,
    var cnpj: CNPJ? = null,
    var contactInfo: ContactInfo,
    var status: LeadStatus = LeadStatus.NEW,
    val source: LeadSource,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) : AggregateRootWithId<LeadId>()