package com.example.appio_test_2.di

import android.content.Context
import com.example.appio_test_2.data.repository.local.PlaceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaceDatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = PlaceDatabase.getDatabase(context)

    @Singleton
    @Provides
    fun providePlaceDao(database: PlaceDatabase) =
        database.getPlace()
}