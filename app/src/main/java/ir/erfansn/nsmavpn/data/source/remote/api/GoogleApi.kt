package ir.erfansn.nsmavpn.data.source.remote.api

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import javax.inject.Inject

abstract class GoogleApi<T : AbstractGoogleJsonClient> {

    protected abstract val credential: GoogleAccountCredential
    @Inject protected lateinit var httpTransport: NetHttpTransport
    @Inject protected lateinit var gsonFactory: GsonFactory

    protected abstract fun getService(): T

    fun selectAccount(emailAddress: String): T {
        credential.selectedAccountName = emailAddress
        return getService()
    }
}
