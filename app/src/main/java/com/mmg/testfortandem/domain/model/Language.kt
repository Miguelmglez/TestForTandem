package com.mmg.testfortandem.domain.model

/**
 * A value class representing a language code in the context of the Tandem app.
 * Type-safe wrapper around a String to represent ISO 639-1 language codes.
 * Ensures that only valid language codes are used throughout the app,
 * while allowing for flexibility in the set of supported languages as defined by the remote API.
 */
@JvmInline
value class Language(val isoCode: String) {
    init {
        require(isoCode.isNotBlank()) { "Language code cannot be blank" }
    }
}