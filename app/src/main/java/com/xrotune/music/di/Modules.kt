package com.xrotune.music.di

import android.content.Context
import com.xrotune.music.core.common.DeviceInfo
import com.xrotune.music.data.local.db.XroTuneDatabase
import com.xrotune.music.data.local.db.dao.DownloadDao
import com.xrotune.music.data.local.db.dao.HistoryDao
import com.xrotune.music.data.local.db.dao.PlaylistDao
import com.xrotune.music.data.local.db.dao.SearchHistoryDao
import com.xrotune.music.data.local.db.dao.SongDao
import com.xrotune.music.data.local.prefs.PrefsManager
import com.xrotune.music.core.network.SessionProvider
import com.xrotune.music.data.remote.ApiService
import com.xrotune.music.data.remote.interceptor.DecryptInterceptor
import com.xrotune.music.data.remote.interceptor.SignInterceptor
import com.xrotune.music.domain.repository.DownloadRepository
import com.xrotune.music.domain.repository.LibraryRepository
import com.xrotune.music.domain.repository.MusicRepository
import com.xrotune.music.domain.repository.UserRepository
import com.xrotune.music.data.repository.impl.DownloadRepositoryImpl
import com.xrotune.music.data.repository.impl.LibraryRepositoryImpl
import com.xrotune.music.data.repository.impl.MusicRepositoryImpl
import com.xrotune.music.data.repository.impl.UserRepositoryImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideSessionProvider(prefs: PrefsManager): SessionProvider = prefs

    @Provides
    @Singleton
    fun provideOkHttp(
        signInterceptor: SignInterceptor,
        decryptInterceptor: DecryptInterceptor,
        deviceInfo: DeviceInfo,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", deviceInfo.ua)
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(signInterceptor)
            .addInterceptor(decryptInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(com.xrotune.music.core.crypto.Secrets.baseHost)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): XroTuneDatabase =
        androidx.room.Room.databaseBuilder(context, XroTuneDatabase::class.java, XroTuneDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideSongDao(db: XroTuneDatabase): SongDao = db.songDao()
    @Provides fun providePlaylistDao(db: XroTuneDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideDownloadDao(db: XroTuneDatabase): DownloadDao = db.downloadDao()
    @Provides fun provideHistoryDao(db: XroTuneDatabase): HistoryDao = db.historyDao()
    @Provides fun provideSearchHistoryDao(db: XroTuneDatabase): SearchHistoryDao = db.searchHistoryDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
