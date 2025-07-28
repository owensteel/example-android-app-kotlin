package com.owensteel.starlingroundup.di

import com.owensteel.starlingroundup.domain.repository.AccountRepository
import com.owensteel.starlingroundup.domain.repository.AccountRepositoryImpl
import com.owensteel.starlingroundup.domain.repository.TransactionsRepository
import com.owensteel.starlingroundup.domain.repository.TransactionsRepositoryImpl
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
    abstract fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransactionsRepository(
        impl: TransactionsRepositoryImpl
    ): TransactionsRepository

}