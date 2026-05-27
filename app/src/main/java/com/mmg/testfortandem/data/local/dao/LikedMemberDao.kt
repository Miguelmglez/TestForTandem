package com.mmg.testfortandem.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mmg.testfortandem.data.local.entity.LikedMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedMemberDao {

    @Query("SELECT memberId FROM liked_members")
    fun observeAllIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LikedMemberEntity)

    @Query("DELETE FROM liked_members WHERE memberId = :memberId")
    suspend fun delete(memberId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_members WHERE memberId = :memberId)")
    suspend fun exists(memberId: Long): Boolean

    /**
     * Atomic toggle: existence check and write happen within
     * the same transaction, eliminating the read-then-write race window.
     */
    @Transaction
    suspend fun toggleLike(memberId: Long) {
        if (exists(memberId)) {
            delete(memberId)
        } else {
            insert(LikedMemberEntity(memberId))
        }
    }
}