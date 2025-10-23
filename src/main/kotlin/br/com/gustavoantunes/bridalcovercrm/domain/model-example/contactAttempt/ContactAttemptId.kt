package br.com.gustavoantunes.bridalcovercrm.domain.`model-example`.contactAttempt

import br.com.gustavoantunes.bridalcovercrm.domain.model.common.AggregateId
import java.util.UUID

/**
 * Value Object que representa o identificador único de uma tentativa de contato.
 * 
 * Este identificador é usado para distinguir uma tentativa de contato de outra
 * dentro do contexto de um Lead.
 * 
 * Herda de AggregateId para reutilizar as regras comuns de validação e
 * manipulação de identificadores baseados em UUID.
 */
class ContactAttemptId private constructor(value: String) : AggregateId(value) {
    
    companion object {
        /**
         * Gera um novo ContactAttemptId com UUID aleatório
         */
        fun generate(): ContactAttemptId = ContactAttemptId(generateUUID())
        
        /**
         * Cria um ContactAttemptId a partir de uma string, validando o formato
         */
        fun fromString(value: String): ContactAttemptId = ContactAttemptId(value)
        
        /**
         * Cria um ContactAttemptId a partir de um UUID
         */
        fun fromUUID(uuid: UUID): ContactAttemptId = ContactAttemptId(uuid.toString())
    }
}
