package br.eti.rafaelcouto.marvelheroes.network.config

import br.eti.rafaelcouto.marvelheroes.BuildConfig
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Date
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

object AuthenticatorClient {
    private const val APIKEY_FIELD = "apikey"
    private const val TIMESTAMP_FIELD = "ts"
    private const val HASH_FIELD = "hash"

    fun build(): OkHttpClient = OkHttpClient.Builder().apply {
        addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val ts = Date().time.toString()

                val original = chain.request()
                val originalUrl = original.url

                val newUrl = originalUrl.newBuilder()
                    .addQueryParameter(APIKEY_FIELD, BuildConfig.PUBLIC_KEY)
                    .addQueryParameter(TIMESTAMP_FIELD, ts)
                    .addQueryParameter(HASH_FIELD, generateHash(ts))
                    .build()

                val builder = original.newBuilder().url(newUrl)

                return chain.proceed(builder.build())
            }
        })

        if (BuildConfig.DEBUG) addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
    }.build()

    private fun generateHash(ts: String): String {
        val publicKey = BuildConfig.PUBLIC_KEY
        val privateKey = BuildConfig.PRIVATE_KEY

        val digest = MessageDigest.getInstance("MD5")

        val hashedString = "$ts$privateKey$publicKey"

        digest.update(hashedString.toByteArray(), 0, hashedString.length)

        return BigInteger(1, digest.digest()).toString(16)
    }
}
