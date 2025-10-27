package br.com.gustavoantunes.bridalcovercrm.application.service.lead

import br.com.gustavoantunes.bridalcovercrm.application.port.`in`.lead.RegisterLeadUseCase
import br.com.gustavoantunes.bridalcovercrm.application.port.out.repository.LeadRepository
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.LeadId
import org.springframework.stereotype.Service

@Service
class RegisterLeadService(
    private val leadRepository: LeadRepository
) : RegisterLeadUseCase {

    override fun registerLead(command: RegisterLeadUseCase.RegisterLeadCommand): Lead {
        val lead = Lead(
            id = LeadId.generate(),
            name = command.companyName,
            cnpj = command.cnpj,
            contactInfo = command.contactInfo,
            source = command.source
        )
        
        return leadRepository.save(lead)
    }
}
