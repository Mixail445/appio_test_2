package com.example.appio_test_2.di

import android.content.Context
import com.example.appio_test_2.data.repository.CurrentLocationRepositoryImpl
import com.example.appio_test_2.data.repository.local.PlaceDao
import com.example.appio_test_2.data.repository.local.PlaceLocalSourceImpl
import com.example.appio_test_2.domain.LocationRepository
import com.example.appio_test_2.domain.PlaceLocalSource
import com.example.appio_test_2.utils.DispatchersProvider
import com.example.appio_test_2.utils.DispatchersProviderImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    @Singleton
    fun provideLocalSource(
        dao: PlaceDao,
        dispatchersProvider: DispatchersProvider
    ): PlaceLocalSource = PlaceLocalSourceImpl(dao, dispatchersProvider)

    @Singleton
    @Provides
    fun provideDispatcher(): DispatchersProvider {
        return DispatchersProviderImpl
    }

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideCurrentLocationRepository(fusedLocationClient: FusedLocationProviderClient): LocationRepository {
        return CurrentLocationRepositoryImpl(fusedLocationClient)
    }
}