package com.mmg.testfortandem.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mmg.testfortandem.data.local.dao.LikedMemberDao
import com.mmg.testfortandem.data.local.entity.LikedMemberEntity

@Database(
    entities = [LikedMemberEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CommunityDatabase : RoomDatabase() {
    abstract fun likedMemberDao(): LikedMemberDao
}