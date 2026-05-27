package com.mmg.testfortandem.domain.model


import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LanguageTest {

    @Test
    fun `creates language with valid iso code`() {
        val language = Language("en")
        assertThat(language.isoCode).isEqualTo("en")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects blank iso code`() {
        Language("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects whitespace-only iso code`() {
        Language("   ")
    }
}