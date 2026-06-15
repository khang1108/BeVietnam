package com.bevietnam.core.di

import com.bevietnam.core.data.mock.MockPlaceRepository
import com.bevietnam.core.data.mock.MockUserRepository
import com.bevietnam.core.data.mock.MockTaskRepository
import com.bevietnam.core.data.mock.MockAuthRepository
import com.bevietnam.core.data.mock.MockFeedRepository
import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.domain.repository.IUserRepository
import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.domain.repository.IFeedRepository
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
    abstract fun bindPlaceRepository(
        mockPlaceRepository: MockPlaceRepository
    ): IPlaceRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        mockTaskRepository: MockTaskRepository
    ): ITaskRepository
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        mockAuthRepository: MockAuthRepository
    ): IAuthRepository
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        mockUserRepository: MockUserRepository
    ): IUserRepository

    @Binds
    @Singleton
    abstract fun bindFeedRepository(
        mockFeedRepository: MockFeedRepository
    ): IFeedRepository
}
