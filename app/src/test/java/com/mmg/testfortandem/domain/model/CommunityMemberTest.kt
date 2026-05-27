package com.mmg.testfortandem.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CommunityMemberTest {

    @Test
    fun `isNew is true when referenceCnt is zero`() {
        val member = sampleMember(referenceCnt = 0)
        assertThat(member.isNew).isTrue()
    }

    @Test
    fun `isNew is false when referenceCnt is positive`() {
        val member = sampleMember(referenceCnt = 1)
        assertThat(member.isNew).isFalse()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects non-positive id`() {
        sampleMember(id = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects blank firstName`() {
        sampleMember(firstName = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects negative referenceCnt`() {
        sampleMember(referenceCnt = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects empty natives`() {
        sampleMember(natives = emptyList())
    }

    private fun sampleMember(
        id: Long = 1L,
        firstName: String = "Tobi",
        topic: String = "topic",
        pictureUrl: String = "https://example.com/p.png",
        natives: List<Language> = listOf(Language("de")),
        learns: List<Language> = listOf(Language("en")),
        referenceCnt: Int = 5,
    ) = CommunityMember(
        id = id,
        firstName = firstName,
        topic = topic,
        pictureUrl = pictureUrl,
        natives = natives,
        learns = learns,
        referenceCnt = referenceCnt,
    )
}