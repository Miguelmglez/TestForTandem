package com.mmg.testfortandem.presentation.community

import com.google.common.truth.Truth.assertThat
import com.mmg.testfortandem.presentation.components.joinHumanReadable
import org.junit.Test

class LanguageDisplayTest {

    @Test
    fun `empty list returns empty string`() {
        assertThat(joinHumanReadable(emptyList(), ", ", " and ")).isEmpty()
    }

    @Test
    fun `single item is returned as-is`() {
        assertThat(joinHumanReadable(listOf("English"), ", ", " and "))
            .isEqualTo("English")
    }

    @Test
    fun `two items are joined with last separator`() {
        assertThat(joinHumanReadable(listOf("English", "Spanish"), ", ", " and "))
            .isEqualTo("English and Spanish")
    }

    @Test
    fun `three items use comma then and`() {
        assertThat(
            joinHumanReadable(listOf("English", "Spanish", "German"), ", ", " and ")
        ).isEqualTo("English, Spanish and German")
    }

    @Test
    fun `four items still use one and at the end`() {
        assertThat(
            joinHumanReadable(
                listOf("English", "Spanish", "German", "French"),
                ", ",
                " and ",
            )
        ).isEqualTo("English, Spanish, German and French")
    }
}