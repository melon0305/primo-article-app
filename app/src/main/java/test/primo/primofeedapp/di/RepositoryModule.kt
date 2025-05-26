package test.primo.primofeedapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import test.primo.primofeedapp.domain.repository.ArticleRepository
import test.primo.primofeedapp.data.repository.ArticleRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository
}