package android.template.core.network.retrofit

import android.template.core.network.BuildConfig
import android.template.core.network.GreetingNetworkDataSource
import android.template.core.network.model.NetworkGreeting
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Retrofit API declaration for the backend.
 */
private interface RetrofitGreetingNetworkApi {
    @GET(value = "greetings")
    suspend fun fetchGreetings(): List<NetworkGreeting>
}

private const val BASE_URL = BuildConfig.BACKEND_URL

/**
 * [Retrofit] backed [GreetingNetworkDataSource].
 */
@Singleton
internal class RetrofitGreetingNetwork @Inject constructor(
    networkJson: Json,
    okhttpCallFactory: dagger.Lazy<Call.Factory>,
) : GreetingNetworkDataSource {

    private val networkApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        // We use a callFactory lambda with dagger.Lazy<Call.Factory>
        // to prevent initializing OkHttp on the main thread.
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .addConverterFactory(
            networkJson.asConverterFactory("application/json".toMediaType()),
        )
        .build()
        .create(RetrofitGreetingNetworkApi::class.java)

    override suspend fun fetchGreetings(): List<NetworkGreeting> =
        networkApi.fetchGreetings()
}
