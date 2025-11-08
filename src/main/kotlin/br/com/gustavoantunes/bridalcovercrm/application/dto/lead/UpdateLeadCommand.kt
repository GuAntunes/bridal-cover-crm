package br.com.gustavoantunes.bridalcovercrm.application.dto.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadStatus

data class UpdateLeadCommand(
    val id: String,
    val companyName: String? = null,
    val cnpj: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val instagram: String? = null,
    val status: LeadStatus? = null
)

