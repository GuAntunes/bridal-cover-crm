package br.com.gustavoantunes.bridalcovercrm.application.usecase.lead

import br.com.gustavoantunes.bridalcovercrm.domain.model.lead.Lead
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.ListLeadsUseCase
import br.com.gustavoantunes.bridalcovercrm.domain.port.`in`.lead.PageResult
import br.com.gustavoantunes.bridalcovercrm.domain.port.out.repository.LeadRepository
import org.springframework.stereotype.Service

@Service
class ListLeadsService(
    private val leadRepository: LeadRepository
) : ListLeadsUseCase {

    override fun execute(page: Int, size: Int): PageResult<Lead> {
        require(page >= 0) { "Page number must be greater than or equal to 0" }
        require(size > 0 && size <= 100) { "Page size must be between 1 and 100" }

        val leads = leadRepository.findAll(page, size)
        val totalElements = leadRepository.count()

        return PageResult.of(
            content = leads,
            page = page,
            size = size,
            totalElements = totalElements
        )
    }
}

