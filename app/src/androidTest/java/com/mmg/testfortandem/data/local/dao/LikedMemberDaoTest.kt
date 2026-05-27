package com.mmg.testfortandem.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mmg.testfortandem.data.local.CommunityDatabase
import com.mmg.testfortandem.data.local.entity.LikedMemberEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LikedMemberDaoTest {

    private lateinit var database: CommunityDatabase
    private lateinit var dao: LikedMemberDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CommunityDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.likedMemberDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeAllIds_emitsEmptyInitially() = runTest {
        dao.observeAllIds().test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggleLike_addsId_whenMissing() = runTest {
        dao.toggleLike(42L)
        assertThat(dao.exists(42L)).isTrue()
    }

    @Test
    fun toggleLike_removesId_whenPresent() = runTest {
        dao.insert(LikedMemberEntity(42L))
        dao.toggleLike(42L)
        assertThat(dao.exists(42L)).isFalse()
    }

    @Test
    fun observeAllIds_emitsUpdates_onToggle() = runTest {
        dao.observeAllIds().test {
            assertThat(awaitItem()).isEmpty()

            dao.toggleLike(1L)
            assertThat(awaitItem()).containsExactly(1L)

            dao.toggleLike(2L)
            assertThat(awaitItem()).containsExactly(1L, 2L)

            dao.toggleLike(1L)
            assertThat(awaitItem()).containsExactly(2L)

            cancelAndIgnoreRemainingEvents()
        }
    }
}