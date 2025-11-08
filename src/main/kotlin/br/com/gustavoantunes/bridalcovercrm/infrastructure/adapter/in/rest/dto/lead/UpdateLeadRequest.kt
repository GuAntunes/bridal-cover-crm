package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.`in`.rest.dto.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadStatus

data class UpdateLeadRequest(
    val companyName: String? = null,
    val cnpj: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val instagram: String? = null,
    val status: LeadStatus? = null
)

