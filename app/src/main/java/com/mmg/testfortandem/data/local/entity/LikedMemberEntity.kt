package com.mmg.testfortandem.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a "like" of a community member by the user. Only the member's id is stored, since that's all we need to know.
 * We could store in the future other metadata about the like (e.g. timestamp, note), but that would be YAGNI until a real product requirement arises.
 * we would need to add a Room migration to add the new fields, but that's a simple and well-known process.
 */
@Entity(tableName = "liked_members")
data class LikedMemberEntity(
    @PrimaryKey val memberId: Long,
)