package com.owensteel.starlingroundup.util

object SharedConstants {

    const val DATASTORE_NAME = "secure_token_store"

    object PreferenceKeys {
        const val ENCRYPTED_REFRESH_TOKEN = "encrypted_refresh_token"
        const val ENCRYPTED_ACCESS_TOKEN = "encrypted_access_token"
        const val REFRESH_TOKEN_IV = "refresh_token_iv"
        const val ACCESS_TOKEN_IV = "access_token_iv"
        const val REFRESH_TOKEN_EXPIRY = "refresh_token_expiry"
        const val ACCESS_TOKEN_EXPIRY = "access_token_expiry"
    }

    object ApiHeaders {
        const val AUTHORIZATION = "Authorization"
        const val ACCEPT = "Accept"
        const val USER_AGENT = "User-Agent"
    }

    object ApiConfig {
        const val BASE_URL = "https://api-sandbox.starlingbank.com"
        const val HOSTNAME = "api-sandbox.starlingbank.com"
        const val API_SERVER_CERT_HASH = "sha256/QX3C0KaQ0UUy8OZDUH+92efaoBtMSSBVuYXvXs+4ud4="
    }

    object Transactions {
        const val TRANSACTION_DIRECTION_IN = "IN"
        const val TRANSACTION_DIRECTION_OUT = "OUT"
    }

}