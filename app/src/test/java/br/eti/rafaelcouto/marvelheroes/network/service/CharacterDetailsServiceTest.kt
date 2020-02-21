package br.eti.rafaelcouto.marvelheroes.network.service

import br.eti.rafaelcouto.marvelheroes.SynchronousTestRule
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.model.general.DataWrapper
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.config.INetworkAPI
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CharacterDetailsServiceTest {
    // rules
    @Rule
    @JvmField
    val testRule = SynchronousTestRule()

    // sut
    private lateinit var sut: CharacterDetailsService

    // mocks
    @Mock
    private lateinit var mockApi: INetworkAPI

    // comics dummies
    private val dummyComicsListDataWrapper: DataWrapper<Comic>
        get() = DataWrapper(0, 10, 100, listOf())

    private val dummyComicsList: ResponseBody<Comic>
        get() = ResponseBody(200, "ok", dummyComicsListDataWrapper)

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
        get() = ResponseBody(200, "ok", dummyCharacterDetailsDataWrapper)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        this.sut = CharacterDetailsService(mockApi)
        this.dummyCharacterId = 0
    }

    @Test
    fun `given a character id when details requested then should return character details`() {
        val expected = dummyCharacterDetailsResponse

        whenever(sut.loadCharacterDetails(anyInt())) doReturn Single.just(expected)

        // given

        val stubId = 19

        // when

        val result = sut.loadCharacterDetails(stubId).test()

        // then

        verify(mockApi).getPublicCharacterInfo(19)
        result.assertNoErrors().assertValue(expected)
    }

    @Test
    fun `given a character id when details requested then should return error`() {
        val expected = Throwable("dummy exception")

        whenever(sut.loadCharacterDetails(anyInt())) doReturn Single.error(expected)

        // given

        val stubId = 19

        // when

        val result = sut.loadCharacterDetails(stubId).test()

        // then

        verify(mockApi).getPublicCharacterInfo(19)
        result.assertError(expected)
    }

    @Test
    fun `given a character id when comics requested then should return comics list`() {
        val expected = dummyComicsList

        whenever(sut.loadCharacterComics(19, 0)) doReturn Single.just(expected)

        // given

        val stubId = 19

        // when

        val result = sut.loadCharacterComics(stubId, 0).test()

        // then

        verify(mockApi).getPublicCharacterComics(19, 10, 0)
        result.assertNoErrors().assertValue(expected)
    }

    @Test
    fun `given a character id when comics requested then should return error`() {
        val expected = Throwable("dummy exception")

        whenever(sut.loadCharacterComics(19, 0)) doReturn Single.error(expected)

        // given

        val stubId = 19

        // when

        val result = sut.loadCharacterComics(stubId, 0).test()

        // then

        verify(mockApi).getPublicCharacterComics(19, 10, 0)
        result.assertError(expected)
    }
}