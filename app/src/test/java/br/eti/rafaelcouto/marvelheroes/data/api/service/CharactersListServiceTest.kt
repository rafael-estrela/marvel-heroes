package br.eti.rafaelcouto.marvelheroes.data.api.service

import android.os.Build
import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.model.general.DataWrapper
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.data.api.INetworkAPI
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CharactersListServiceTest {
    // sut
    private lateinit var sut: CharactersListService

    // mocks
    @Mock
    private lateinit var mockApi: INetworkAPI

    // dummies
    private var dummyId: Int = 0

    private val dummyCharacter: Character
        get() = Character().apply {
            id = ++dummyId
            name = "Marvel character #$id"
        }

    private val dummyResult: ResponseBody<Character>
        get() = ResponseBody(
            200,
            "ok",
            "copyright",
            DataWrapper(0, 10, 100, listOf(
                dummyCharacter,
                dummyCharacter,
                dummyCharacter,
                dummyCharacter,
                dummyCharacter,
                dummyCharacter,
                dummyCharacter,
                dummyCharacter,
                dummyCharacter,
                dummyCharacter
            ))
        )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        this.sut = CharactersListService(mockApi)
        this.dummyId = 0
    }

    @Test
    fun `when characters list requested then api should return initial characters list`() = runBlocking {
        val expected = dummyResult

        whenever(sut.loadCharacters(0)) doReturn expected

        // when

        val result = sut.loadCharacters(0)

        // then

        verify(mockApi).getPublicCharacters(20, 0)
        assertThat(expected, equalTo(result))
    }

    @Test(expected = Throwable::class)
    fun `when characters list requested then api should fail`() = runBlocking {
        val expected = Throwable("dummy exception")

        whenever(sut.loadCharacters(0)) doThrow expected

        // when

        try {
            sut.loadCharacters(0)
        } catch (e: Exception) {
            // then

            verify(mockApi).getPublicCharacters(20, 0)
            assertThat(e.message, equalTo("dummy exception"))

            throw e
        }

        Unit
    }

    @Test
    fun `when filtered list requested then api should return initial characters list`() = runBlocking {
        val expected = dummyResult

        whenever(sut.filterCharacters(0, "dummy")) doReturn expected

        // when

        val result = sut.filterCharacters(0, "dummy")

        // then

        verify(mockApi).getPublicCharacters(20, 0, "dummy")
        assertThat(expected, equalTo(result))
    }

    @Test(expected = Throwable::class)
    fun `when filtered list requested then api should fail`() = runBlocking {
        val expected = Throwable("dummy exception")

        whenever(sut.filterCharacters(0, "dummy")) doThrow expected

        // when

        try {
            sut.filterCharacters(0, "dummy")
        } catch (e: Exception) {
            // then

            verify(mockApi).getPublicCharacters(20, 0, "dummy")
            assertThat(e.message, equalTo("dummy exception"))

            throw e
        }

        Unit
    }
}
