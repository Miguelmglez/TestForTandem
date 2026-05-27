package com.mmg.testfortandem.data.remote.mapper

import com.mmg.testfortandem.data.remote.dto.CommunityMemberDto
import com.mmg.testfortandem.domain.model.CommunityMember
import com.mmg.testfortandem.domain.model.Language

/**
 * Maps a remote DTO into a domain entity, filtering out any blank languages and trimming whitespace from the first name and topic.
 * This ensures that the domain model only contains valid, clean data, and that any blank language entries from the API are not included in the domain model.
 */
fun CommunityMemberDto.toDomain(): CommunityMember = CommunityMember(
    id = id,
    firstName = firstName.trim(),
    topic = topic.trim(),
    pictureUrl = pictureUrl,
    natives = natives.filter { it.isNotBlank() }.map(::Language),
    learns = learns.filter { it.isNotBlank() }.map(::Language),
    referenceCnt = referenceCnt,
)