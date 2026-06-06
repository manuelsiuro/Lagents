package com.msa.lagents.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ProviderError {
    abstract val message: String

    @Serializable
    data class Authentication(override val message: String) : ProviderError()

    @Serializable
    data class RateLimit(override val message: String) : ProviderError()

    @Serializable
    data class Safety(override val message: String) : ProviderError()

    @Serializable
    data class Network(override val message: String) : ProviderError()

    @Serializable
    data class Timeout(override val message: String) : ProviderError()

    @Serializable
    data class MalformedResponse(override val message: String) : ProviderError()

    @Serializable
    data class Unavailable(override val message: String) : ProviderError()

    @Serializable
    data class Unknown(override val message: String, val code: String? = null) : ProviderError()

    companion object {
        fun fromHttpCode(code: Int, message: String): ProviderError {
            return when (code) {
                401 -> Authentication(message)
                429 -> RateLimit(message)
                408, 504 -> Timeout(message)
                503 -> Unavailable(message)
                else -> Unknown(message, code.toString())
            }
        }
    }
}
