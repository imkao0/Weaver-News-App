package dev.mkao.weaver.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.mkao.weaver.data.database.MIGRATION_1_2
import dev.mkao.weaver.data.database.MIGRATION_2_3
import dev.mkao.weaver.data.database.MIGRATION_3_4
import dev.mkao.weaver.data.database.MIGRATION_4_5
import dev.mkao.weaver.data.database.NewsDao
import dev.mkao.weaver.data.database.NewsDatabase
import dev.mkao.weaver.data.database.RecentSearchDao
import dev.mkao.weaver.data.database.TableUpdateDao
import dev.mkao.weaver.data.database.VideoDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideNewsDatabase(@ApplicationContext context: Context): NewsDatabase {
        return Room.databaseBuilder(
            context,
            NewsDatabase::class.java,
            NewsDatabase.DATABASE_NAME,
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    @Singleton
    @Provides
    fun provideNewsDao(newsDatabase: NewsDatabase): NewsDao = newsDatabase.articleDao()

    @Singleton
    @Provides
    fun provideRecentSearchDao(newsDatabase: NewsDatabase): RecentSearchDao =
        newsDatabase.recentSearchDao()

    @Singleton
    @Provides
    fun provideTableUpdateDao(newsDatabase: NewsDatabase): TableUpdateDao =
        newsDatabase.tableUpdateDao()

    @Singleton
    @Provides
    fun provideVideoDao(newsDatabase: NewsDatabase): VideoDao =
        newsDatabase.videoDao()
}
