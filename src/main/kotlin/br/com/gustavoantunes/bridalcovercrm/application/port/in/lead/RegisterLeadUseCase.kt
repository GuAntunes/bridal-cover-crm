package br.com.gustavoantunes.bridalcovercrm.application.port.`in`.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadSource
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CNPJ
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.CompanyName
import br.com.gustavoantunes.bridalcovercrm.domain.model.shared.ContactInfo

interface RegisterLeadUseCase {
    fun registerLead(command: RegisterLeadCommand): Lead

    data class RegisterLeadCommand(
        val companyName: CompanyName,
        val cnpj: CNPJ? = null,
        val contactInfo: ContactInfo,
        val source: LeadSource
    )
}