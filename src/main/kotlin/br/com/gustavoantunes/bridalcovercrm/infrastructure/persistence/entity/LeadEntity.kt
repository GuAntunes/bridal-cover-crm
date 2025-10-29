package br.com.gustavoantunes.bridalcovercrm.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("leads")
data class LeadEntity(
    @Id
    val id: String,
    
    @Column("company_name")
    val companyName: String,
    
    @Column("cnpj")
    val cnpj: String? = null,
    
    @Column("contact_info")
    val contactInfo: String,
    
    @Column("status")
    val status: String,
    
    @Column("source")
    val source: String,
    
    @Column("created_at")
    val createdAt: LocalDateTime,
    
    @Column("updated_at")
    val updatedAt: LocalDateTime
)

