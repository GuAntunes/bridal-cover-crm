package br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead

import br.com.gustavoantunes.bridalcovercrm.application.dto.lead.UpdateLeadCommand
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead

interface UpdateLeadUseCase {
    fun execute(command: UpdateLeadCommand): Lead
}

