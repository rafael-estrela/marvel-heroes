package br.eti.rafaelcouto.marvelheroes.network.service

import android.os.Build
import br.eti.rafaelcouto.marvelheroes.SynchronousTestRule
import br.eti.rafaelcouto.marvelheroes.model.Character
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
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CharactersListServiceTest {
    // rules
    @Rule
    @JvmField
    val testRule = SynchronousTestRule()

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
    fun `when characters list requested then api should return initial characters list`() {
        val expected = dummyResult

        whenever(sut.loadCharacters(0)) doReturn Single.just(expected)

        // when

        val result = sut.loadCharacters(0).test()

        // then

        verify(mockApi).getPublicCharacters(20, 0)
        result.assertNoErrors().assertValue(expected)
    }

    @Test
    fun `when characters list requested then api should fail`() {
        val expected = Throwable("dummy exception")

        whenever(sut.loadCharacters(0)) doReturn Single.error(expected)

        // when

        val result = sut.loadCharacters(0).test()

        // then

        verify(mockApi).getPublicCharacters(20, 0)
        result.assertError(expected)
    }

    @Test
    fun `when filtered list requested then api should return initial characters list`() {
        val expected = dummyResult

        whenever(sut.filterCharacters(0, "dummy")) doReturn Single.just(expected)

        // when

        val result = sut.filterCharacters(0, "dummy").test()

        // then

        verify(mockApi).getPublicCharacters(20, 0, "dummy")
        result.assertNoErrors().assertValue(expected)
    }

    @Test
    fun `when filtered list requested then api should fail`() {
        val expected = Throwable("dummy exception")

        whenever(sut.filterCharacters(0, "dummy")) doReturn Single.error(expected)

        // when

        val result = sut.filterCharacters(0, "dummy").test()

        // then

        verify(mockApi).getPublicCharacters(20, 0, "dummy")
        result.assertError(expected)
    }
}
