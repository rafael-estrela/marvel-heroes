package br.eti.rafaelcouto.marvelheroes.network.service

import android.os.Build
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.model.general.DataWrapper
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.config.INetworkAPI
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CharacterDetailsServiceTest {
    // sut
    private lateinit var sut: CharacterDetailsService

    // mocks
    @Mock
    private lateinit var mockApi: INetworkAPI

    // comics dummies
    private val dummyComicsListDataWrapper: DataWrapper<Comic>
        get() = DataWrapper(0, 10, 100, listOf())

    private val dummyComicsList: ResponseBody<Comic>
        get() = ResponseBody(200, "ok", "copyright", dummyComicsListDataWrapper)

    // character dummies
    private var dummyCharacterId: Int = 0

    private val dummyCharacterDetails: CharacterDetails
        get() = CharacterDetails("dummy description", listOf()).apply {
            id = (++dummyCharacterId) * 100
            name = "Marvel character #$id"
        }
    private val dummyCharacterDetailsList: List<CharacterDetails>
        get() = listOf(dummyCharacterDetails)

    private val dummyCharacterDetailsDataWrapper: DataWrapper<CharacterDetails>
        get() = DataWrapper(0, 1, 100, dummyCharacterDetailsList)

    private val dummyCharacterDetailsResponse: ResponseBody<CharacterDetails>
        get() = ResponseBody(200, "ok", "copyright", dummyCharacterDetailsDataWrapper)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        this.sut = CharacterDetailsService(mockApi)
        this.dummyCharacterId = 0
    }

    @Test
    fun `given a character id when details requested then should return character details`() = runBlocking {
        val expected = dummyCharacterDetailsResponse

        whenever(sut.loadCharacterDetails(anyInt())) doReturn expected

        // given

        val stubId = 19

        // when

        val result = sut.loadCharacterDetails(stubId)

        // then

        verify(mockApi).getPublicCharacterInfo(19)
        assertThat(expected, equalTo(result))
    }

    @Test(expected = Throwable::class)
    fun `given a character id when details requested then should return error`() = runBlocking {
        val expected = Throwable("dummy exception")

        whenever(sut.loadCharacterDetails(anyInt())) doThrow expected

        // given

        val stubId = 19

        // when

        try {
            sut.loadCharacterDetails(stubId)
        } catch (e: Exception) {
            // then

            verify(mockApi).getPublicCharacterInfo(19)
            assertThat(e.message, equalTo("dummy exception"))

            throw e
        }

        Unit
    }

    @Test
    fun `given a character id when comics requested then should return comics list`() = runBlocking {
        val expected = dummyComicsList

        whenever(sut.loadCharacterComics(19, 0)) doReturn expected

        // given

        val stubId = 19

        // when

        val result = sut.loadCharacterComics(stubId, 0)

        // then

        verify(mockApi).getPublicCharacterComics(19, 10, 0)
        assertThat(expected, equalTo(result))
    }

    @Test(expected = Throwable::class)
    fun `given a character id when comics requested then should return error`() = runBlocking {
        val expected = Throwable("dummy exception")

        whenever(sut.loadCharacterComics(19, 0)) doThrow expected

        // given

        val stubId = 19

        // when

        try {
            sut.loadCharacterComics(stubId, 0)
        } catch (e: Exception) {
            // then

            verify(mockApi).getPublicCharacterComics(19, 10, 0)
            assertThat(e.message, equalTo("dummy exception"))

            throw e
        }

        Unit
    }
}
