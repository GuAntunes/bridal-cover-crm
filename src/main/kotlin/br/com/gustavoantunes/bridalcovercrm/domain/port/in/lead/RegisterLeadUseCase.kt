package br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead

import br.com.gustavoantunes.bridalcovercrm.application.dto.lead.RegisterLeadCommand
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead

interface RegisterLeadUseCase {
    fun execute(command: RegisterLeadCommand): Lead
}


