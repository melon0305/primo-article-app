package test.primo.primofeedapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import test.primo.primofeedapp.data.datasource.ArticleLocalDataSource
import test.primo.primofeedapp.data.datasource.ArticleLocalDataSourceImpl
import test.primo.primofeedapp.data.datasource.ArticleRemoteDataSource
import test.primo.primofeedapp.data.datasource.ArticleRemoteDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun provideArticleRemoteDataSource(impl: ArticleRemoteDataSourceImpl): ArticleRemoteDataSource

    @Binds
    @Singleton
    abstract fun provideArticleLocalDataSource(impl: ArticleLocalDataSourceImpl): ArticleLocalDataSource
}