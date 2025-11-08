package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.repository

import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.entity.LeadEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LeadDataJdbcRepository : CrudRepository<LeadEntity, UUID>, PagingAndSortingRepository<LeadEntity, UUID> {
    fun findAllBy(pageable: Pageable): List<LeadEntity>
}


