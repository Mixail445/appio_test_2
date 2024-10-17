package com.example.appio_test_2.di

import androidx.fragment.app.Fragment
import com.example.appio_test_2.utils.LocationPermissionManager
import com.example.appio_test_2.utils.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class PermissionModule {

    companion object {
        @Provides
        fun providePermissionManager(
            fragment: Fragment
        ): PermissionManager {
            return LocationPermissionManager(fragment)
        }
    }
}

