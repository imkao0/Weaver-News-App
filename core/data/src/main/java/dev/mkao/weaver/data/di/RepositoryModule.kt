package dev.mkao.weaver.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.mkao.weaver.data.repository.BookmarksRepositoryImpl
import dev.mkao.weaver.data.repository.HeadlinesRepositoryImpl
import dev.mkao.weaver.data.repository.RepositoryImpl
import dev.mkao.weaver.data.repository.SearchRepositoryImpl
import dev.mkao.weaver.data.repository.VideoRepositoryImpl
import dev.mkao.weaver.domain.repository.BookmarksRepository
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.domain.repository.Repository
import dev.mkao.weaver.domain.repository.SearchRepository
import dev.mkao.weaver.domain.repository.VideoRepository
import javax.inject.Singleton

/**
 * Binds the repository interfaces from `core:domain` to their concrete
 * implementations in `core:data` so Hilt can inject them across feature
 * modules.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindHeadlinesRepository(impl: HeadlinesRepositoryImpl): HeadlinesRepository

    @Singleton
    @Binds
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Singleton
    @Binds
    abstract fun bindBookmarksRepository(impl: BookmarksRepositoryImpl): BookmarksRepository

    @Singleton
    @Binds
    abstract fun bindVideoRepository(impl: VideoRepositoryImpl): VideoRepository

    @Singleton
    @Binds
    abstract fun bindRepository(impl: RepositoryImpl): Repository
}
