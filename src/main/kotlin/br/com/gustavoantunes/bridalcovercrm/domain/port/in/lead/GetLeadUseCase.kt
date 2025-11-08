package br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead

import br.com.gustavoantunes.bridalcovercrm.application.dto.lead.GetLeadQuery
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead

interface GetLeadUseCase {
    fun execute(query: GetLeadQuery): Lead?
}


