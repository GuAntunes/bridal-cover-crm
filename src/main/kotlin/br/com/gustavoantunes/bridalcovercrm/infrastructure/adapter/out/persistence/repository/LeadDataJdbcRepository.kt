package br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.repository

import br.com.gustavoantunes.bridalcovercrm.infrastructure.adapter.out.persistence.entity.LeadEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LeadDataJdbcRepository : CrudRepository<LeadEntity, UUID>


