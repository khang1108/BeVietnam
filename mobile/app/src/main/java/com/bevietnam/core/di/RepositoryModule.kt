package com.bevietnam.core.di

import com.bevietnam.core.data.repository.AuthRepository
import com.bevietnam.core.data.repository.PlaceRepository
import com.bevietnam.core.data.repository.TaskRepository
import com.bevietnam.core.data.repository.UserRepository
import com.bevietnam.core.domain.repository.IAuthRepository
import com.bevietnam.core.domain.repository.IPlaceRepository
import com.bevietnam.core.domain.repository.ITaskRepository
import com.bevietnam.core.domain.repository.IUserRepository
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
        placeRepository: PlaceRepository
    ): IPlaceRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepository: TaskRepository
    ): ITaskRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: AuthRepository
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepository: UserRepository
    ): IUserRepository
}
