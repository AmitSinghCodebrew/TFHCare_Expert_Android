package com.consultantvendor.di

import com.consultantvendor.pushNotifications.MessagingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun messagingService(): MessagingService

}