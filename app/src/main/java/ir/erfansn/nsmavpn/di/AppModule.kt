package ir.erfansn.nsmavpn.di

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.erfansn.nsmavpn.data.source.local.DefaultVpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.VpnProviderLocalDataSource
import ir.erfansn.nsmavpn.data.source.local.datastore.UserPreferencesSerializer
import ir.erfansn.nsmavpn.data.source.local.datastore.VpnProviderSerializer
import ir.erfansn.nsmavpn.data.source.remote.DefaultVpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.VpnGateMessagesRemoteDataSource
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApi
import ir.erfansn.nsmavpn.data.source.remote.api.GmailApiImpl
import ir.erfansn.nsmavpn.data.util.*
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindsGmailApi(gmailApiImpl: GmailApiImpl): GmailApi

    @Binds
    abstract fun bindsVpnGateMessagesRemoteDataSource(
        defaultVpnGateMessagesRemoteDataSource: DefaultVpnGateMessagesRemoteDataSource,
    ): VpnGateMessagesRemoteDataSource

    @Binds
    abstract fun bindsVpnGateContentExtractor(
        defaultVpnGateContentExtractor: DefaultVpnGateContentExtractor,
    ): VpnGateContentExtractor

    @Binds
    abstract fun bindsVpnProviderLocalDataSource(
        defaultVpnProviderLocalDataSource: DefaultVpnProviderLocalDataSource,
    ): VpnProviderLocalDataSource

    @Binds
    abstract fun bindsLinkAvailabilityChecker(
        defaultLinkAvailabilityChecker: DefaultLinkAvailabilityChecker,
    ): LinkAvailabilityChecker

    @Binds
    abstract fun bindsPingChecker(
        defaultPingChecker: DefaultPingChecker,
    ): PingChecker

    companion object {

        @[Provides Singleton]
        fun providesVpnProviderDataStore(@ApplicationContext context: Context) =
            DataStoreFactory.create(
                serializer = VpnProviderSerializer,
                produceFile = { context.dataStoreFile("vpn_provider") }
            )

        @[Provides Singleton]
        fun providesUserPreferencesDataStore(@ApplicationContext context: Context) =
            DataStoreFactory.create(
                serializer = UserPreferencesSerializer,
                produceFile = { context.dataStoreFile("user_preferences") }
            )

        @Provides
        fun providesIoDispatcher() = Dispatchers.IO
    }
}