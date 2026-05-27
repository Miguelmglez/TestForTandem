package com.mmg.testfortandem.data.remote.mapper


import com.google.common.truth.Truth.assertThat
import com.mmg.testfortandem.data.remote.dto.CommunityMemberDto
import com.mmg.testfortandem.domain.model.Language
import org.junit.Test

class CommunityMemberMapperTest {

    @Test
    fun `maps all fields preserving values`() {
        val dto = sampleDto()
        val domain = dto.toDomain()

        assertThat(domain.id).isEqualTo(1L)
        assertThat(domain.firstName).isEqualTo("Tobi")
        assertThat(domain.referenceCnt).isEqualTo(5)
        assertThat(domain.natives).containsExactly(Language("de"), Language("ja"))
        assertThat(domain.learns).containsExactly(Language("en"), Language("pt"))
    }

    @Test
    fun `trims whitespace in firstName`() {
        val dto = sampleDto(firstName = "  Tobi  ")
        assertThat(dto.toDomain().firstName).isEqualTo("Tobi")
    }

    @Test
    fun `filters out blank language codes`() {
        val dto = sampleDto(natives = listOf("de", "", "  ", "ja"))
        assertThat(dto.toDomain().natives)
            .containsExactly(Language("de"), Language("ja"))
            .inOrder()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws when domain validation fails`() {
        // Blank firstName fails the CommunityMember init block.
        sampleDto(firstName = "").toDomain()
    }

    private fun sampleDto(
        id: Long = 1L,
        firstName: String = "Tobi",
        topic: String = "topic",
        pictureUrl: String = "https://example.com/p.png",
        natives: List<String> = listOf("de", "ja"),
        learns: List<String> = listOf("en", "pt"),
        referenceCnt: Int = 5,
    ) = CommunityMemberDto(
        id = id,
        firstName = firstName,
        topic = topic,
        pictureUrl = pictureUrl,
        natives = natives,
        learns = learns,
        referenceCnt = referenceCnt,
    )
}