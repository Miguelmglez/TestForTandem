package com.mmg.testfortandem.domain.model


/**
 * A member of the language learning community, as returned by the remote API.
 * Single source of truth for all member data, including like state (see [LikedMember]).
 */
data class CommunityMember(
    val id: Long,
    val firstName: String,
    val topic: String,
    val pictureUrl: String,
    val natives: List<Language>,
    val learns: List<Language>,
    val referenceCnt: Int,
) {
    /**
     * A member is considered "new" when they have no references.
     * This is a derived property to avoid duplicating state.
     */
    val isNew: Boolean
        get() = referenceCnt == 0

    init {
        require(id > 0) { "Member id must be positive, got $id" }
        require(firstName.isNotBlank()) { "Member firstName cannot be blank" }
        require(referenceCnt >= 0) { "referenceCnt cannot be negative" }
        require(natives.isNotEmpty()) { "A member must speak at least one native language" }
    }
}