package dev.mkao.weaver.data.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.mkao.weaver.data.BuildConfig
import dev.mkao.weaver.data.network.GdeltApi
import dev.mkao.weaver.data.network.NewsApi
import dev.mkao.weaver.data.network.YouTubeRssApi
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val CACHE_SIZE_BYTES = 5L * 1024 * 1024
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val CHUCKER_MAX_CONTENT_LENGTH = 250_000L

    @Singleton
    @Provides
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "http_cache"), CACHE_SIZE_BYTES))
            .addInterceptor(
                ChuckerInterceptor.Builder(context)
                    .collector(
                        ChuckerCollector(
                            context = context,
                            showNotification = true,
                            retentionPeriod = RetentionManager.Period.ONE_HOUR,
                        ),
                    )
                    .maxContentLength(CHUCKER_MAX_CONTENT_LENGTH)
                    .alwaysReadResponseBody(true)
                    .createShortcut(true)
                    .build(),
            )
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                },
            )
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(NewsApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Singleton
    @Provides
    fun provideNewsApi(retrofit: Retrofit): NewsApi = retrofit.create(NewsApi::class.java)

    @Singleton
    @Provides
    fun provideGdeltApi(okHttpClient: OkHttpClient, moshi: Moshi): GdeltApi =
        Retrofit.Builder()
            .baseUrl(GdeltApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GdeltApi::class.java)

    @Singleton
    @Provides
    fun provideYouTubeRssApi(okHttpClient: OkHttpClient): YouTubeRssApi =
        Retrofit.Builder()
            .baseUrl(YouTubeRssApi.BASE_URL)
            .client(okHttpClient)
            .build()
            .create(YouTubeRssApi::class.java)
}
