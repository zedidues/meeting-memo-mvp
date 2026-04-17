package com.example.meetingmemo.di

import android.content.Context
import androidx.room.Room
import com.example.meetingmemo.BuildConfig
import com.example.meetingmemo.data.local.AppDatabase
import com.example.meetingmemo.data.local.MemoDao
import com.example.meetingmemo.data.remote.EmailApi
import com.example.meetingmemo.data.remote.SummaryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "meeting_memo.db",
        ).build()
    }

    @Provides
    fun provideMemoDao(database: AppDatabase): MemoDao = database.memoDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSummaryApi(retrofit: Retrofit): SummaryApi = retrofit.create(SummaryApi::class.java)

    @Provides
    @Singleton
    fun provideEmailApi(retrofit: Retrofit): EmailApi = retrofit.create(EmailApi::class.java)
}
