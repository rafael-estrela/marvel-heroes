package br.eti.rafaelcouto.marvelheroes.network.config

import br.eti.rafaelcouto.marvelheroes.BuildConfig
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthenticatorClientTest {
    // mocks
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        this.mockWebServer = MockWebServer()
    }

    @Test
    fun `when build interceptor requested then should build interceptor correctly`() {
        mockWebServer.start()
        mockWebServer.enqueue(MockResponse())

        val client = AuthenticatorClient.build()

        assertThat(client.interceptors.filterIsInstance<HttpLoggingInterceptor>(), hasSize(1))

        client.newCall(Request.Builder().url(mockWebServer.url("/")).build()).execute()

        val url = mockWebServer.takeRequest().requestUrl

        assertThat(url, notNullValue())

        assertThat(
            url?.queryParameterValues("apikey")?.first(),
            equalTo(BuildConfig.PUBLIC_KEY)
        )
        assertThat(url?.queryParameterValues("ts")?.first(), notNullValue())

        val hash = url?.queryParameterValues("hash")?.first()
        assertThat(hash, notNullValue())
        assertThat(hash?.length, equalTo(32))
    }
}
