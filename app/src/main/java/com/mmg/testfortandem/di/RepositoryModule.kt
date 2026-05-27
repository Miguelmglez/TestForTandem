package com.mmg.testfortandem.di

import com.mmg.testfortandem.data.repository.CommunityRepositoryImpl
import com.mmg.testfortandem.domain.repository.CommunityRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        impl: CommunityRepositoryImpl,
    ): CommunityRepository
}