package test.primo.primofeedapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import test.primo.primofeedapp.core.provider.AndroidResourceProvider
import test.primo.primofeedapp.core.provider.AndroidResourceProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    @Binds
    @Singleton
    abstract fun provideAndroidResourceProvider(impl: AndroidResourceProviderImpl): AndroidResourceProvider
}