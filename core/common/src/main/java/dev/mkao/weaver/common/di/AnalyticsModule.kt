package dev.mkao.weaver.common.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.analytics.CrashlyticsHelper
import dev.mkao.weaver.util.analytics.AppCrashlyticsHelper
import dev.mkao.weaver.util.analytics.FirebaseAnalyticsHelper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsHelper(
        firebaseAnalyticsHelper: FirebaseAnalyticsHelper,
    ): AnalyticsHelper

    @Binds
    @Singleton
    abstract fun bindCrashlyticsHelper(
        appCrashlyticsHelper: AppCrashlyticsHelper,
    ): CrashlyticsHelper

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAnalytics(
            @ApplicationContext context: Context,
        ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }
}
