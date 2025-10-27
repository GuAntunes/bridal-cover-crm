package br.com.gustavoantunes.bridalcovercrm.domain.model.shared

/**
 * Value Object that represents a phone number.
 * 
 * Encapsulates validation and formatting rules for Brazilian phone numbers,
 * supporting both landline and mobile phones.
 */
data class Phone(
    val value: String,
    val countryCode: String = "55", // Brazil by default
    val areaCode: String? = null
) {
    
    init {
        require(value.isNotBlank()) { "Phone number cannot be empty" }
        require(countryCode.isNotBlank()) { "Country code cannot be empty" }
        require(isValid()) { "Invalid phone number: $value" }
    }
    
    companion object {
        /**
         * Cria um Phone a partir de uma string, extraindo automaticamente DDD e número
         */
        fun fromBrazilianNumber(phoneNumber: String): Phone {
            val digits = phoneNumber.replace(Regex("[^0-9]"), "")
            
            return when {
                // Formato: +5511999999999 (13 dígitos)
                digits.length == 13 && digits.startsWith("55") -> {
                    val areaCode = digits.substring(2, 4)
                    val number = digits.substring(4)
                    Phone(number, "55", areaCode)
                }
                // Formato: 11999999999 (11 dígitos)
                digits.length == 11 -> {
                    val areaCode = digits.substring(0, 2)
                    val number = digits.substring(2)
                    Phone(number, "55", areaCode)
                }
                // Formato: 999999999 (9 dígitos - sem DDD)
                digits.length == 9 -> {
                    Phone(digits, "55", null)
                }
                // Formato: 99999999 (8 dígitos - telefone fixo sem DDD)
                digits.length == 8 -> {
                    Phone(digits, "55", null)
                }
                else -> throw IllegalArgumentException("Formato de telefone brasileiro inválido: $phoneNumber")
            }
        }
        
        /**
         * Valida se um número de telefone brasileiro é válido
         */
        fun isValidBrazilianPhone(phone: String): Boolean {
            val digits = phone.replace(Regex("[^0-9]"), "")
            
            return when (digits.length) {
                8 -> true // Telefone fixo sem DDD
                9 -> digits.startsWith("9") // Celular sem DDD (deve começar com 9)
                10 -> !digits.substring(2, 3).equals("9") // Fixo com DDD (3º dígito não pode ser 9)
                11 -> digits.substring(2, 3).equals("9") // Celular com DDD (3º dígito deve ser 9)
                13 -> digits.startsWith("55") && isValidBrazilianPhone(digits.substring(2))
                else -> false
            }
        }
    }
    
    /**
     * Valida se o telefone é válido
     */
    private fun isValid(): Boolean {
        return if (countryCode == "55") {
            isValidBrazilianPhone(getFullNumber())
        } else {
            // Para outros países, validação básica
            val digits = value.replace(Regex("[^0-9]"), "")
            digits.length in 7..15
        }
    }
    
    /**
     * Retorna o número completo com código do país e DDD
     */
    fun getFullNumber(): String {
        return buildString {
            append(countryCode)
            areaCode?.let { append(it) }
            append(getDigits())
        }
    }
    
    /**
     * Retorna apenas os dígitos do número
     */
    fun getDigits(): String {
        return value.replace(Regex("[^0-9]"), "")
    }
    
    /**
     * Retorna o telefone formatado para exibição
     */
    fun format(): String {
        val digits = getDigits()
        
        return if (countryCode == "55" && areaCode != null) {
            when (digits.length) {
                9 -> "($areaCode) ${digits.substring(0, 5)}-${digits.substring(5)}" // Celular
                8 -> "($areaCode) ${digits.substring(0, 4)}-${digits.substring(4)}" // Fixo
                else -> "+$countryCode ($areaCode) $digits"
            }
        } else {
            "+$countryCode $digits"
        }
    }
    
    /**
     * Verifica se é um número de celular (no Brasil, começa com 9)
     */
    fun isMobile(): Boolean {
        return if (countryCode == "55") {
            val digits = getDigits()
            digits.length == 9 && digits.startsWith("9")
        } else {
            false // Para outros países, não temos como determinar
        }
    }
    
    /**
     * Verifica se é um número de telefone fixo
     */
    fun isLandline(): Boolean {
        return if (countryCode == "55") {
            val digits = getDigits()
            digits.length == 8 || (digits.length == 9 && !digits.startsWith("9"))
        } else {
            false // Para outros países, não temos como determinar
        }
    }
    
    /**
     * Retorna o número no formato internacional
     */
    fun getInternationalFormat(): String {
        return "+${getFullNumber()}"
    }
    
    /**
     * Retorna o número mascarado para exibição (ex: (11) 9****-1234)
     */
    fun getMasked(): String {
        val digits = getDigits()
        
        return if (countryCode == "55" && areaCode != null) {
            when (digits.length) {
                9 -> "($areaCode) ${digits.substring(0, 1)}****-${digits.substring(5)}"
                8 -> "($areaCode) ****-${digits.substring(4)}"
                else -> "+$countryCode ($areaCode) ****"
            }
        } else {
            "+$countryCode ****"
        }
    }
    
    override fun toString(): String = format()
}

