package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.repository

import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.entity.LeadEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LeadDataJdbcRepository : CrudRepository<LeadEntity, String>


