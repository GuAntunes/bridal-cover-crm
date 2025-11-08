package br.com.gustavoantunes.bridalcovercrm.domain.model.lead

/**
 * Exception thrown when a Lead is not found.
 * 
 * This is a domain exception that indicates a business rule violation.
 */
class LeadNotFoundException(message: String) : RuntimeException(message)

