package com.example.appio_test_2.di

import androidx.fragment.app.Fragment
import com.example.appio_test_2.utils.CustomDialogCreator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
object DialogModule {

    @Provides
    @FragmentScoped
    fun provideCustomDialogCreator(fragment: Fragment): Lazy<CustomDialogCreator> {
        return lazy { CustomDialogCreator(fragment) }
    }
}

