package br.eti.rafaelcouto.marvelheroes.viewModel

import android.os.Build
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.SynchronousTestRule
import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.model.general.DataWrapper
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.service.CharactersListService
import br.eti.rafaelcouto.marvelheroes.router.CharactersListRouter
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CharactersListViewModelTest {
    // rule
    @Rule
    @JvmField
    val testRule = SynchronousTestRule()

    // delayer
    private val delayer = PublishSubject.create<Boolean>()

    // sut
    private lateinit var sut: CharactersListViewModel

    // mocks
    @Mock
    private lateinit var mockRouter: CharactersListRouter
    @Mock
    private lateinit var mockService: CharactersListService

    // dummies
    private var dummyId: Int = 0

    private val dummyCharacter: Character
        get() = Character().apply {
            id = (++dummyId) * 100
            name = "Marvel character #$id"
        }

    private val dummyResult: ResponseBody<Character>
        get() = ResponseBody(
            200,
            "ok",
            "copyright",
            DataWrapper(
                dummyId, 20, 100, listOf(
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
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
                )
            )
        )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        this.sut = CharactersListViewModel(mockRouter, mockService)
        this.dummyId = 0
    }

    @Test
    fun `when initial character list requested then should update list`() {
        val expected = dummyResult

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(expected).delaySubscription(delayer)

        assertThat(sut.characters.value, nullValue())
        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())

        // when

        sut.loadCharacters()

        // then

        verify(mockService).loadCharacters(0)
        assertThat(sut.isLoading.value, equalTo(true))

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())

        assertThat(sut.characters.value, equalTo(expected.data.results))
    }

    @Test
    fun `when list requested then copyright must be updated`() {
        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(dummyResult)

        assertThat(sut.copyright.value, nullValue())

        // when

        sut.loadCharacters()

        // then

        assertThat(sut.copyright.value, equalTo("copyright"))
    }

    @Test
    fun `when initial character list requested then should display error`() {
        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.error<ResponseBody<Character>>(
            Throwable("dummy exception")
        ).delaySubscription(delayer)

        assertThat(sut.characters.value, nullValue())
        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())

        // when

        sut.loadCharacters()

        // then

        verify(mockService).loadCharacters(0)
        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, equalTo(R.string.default_error))

        assertThat(sut.characters.value, nullValue())
    }

    @Test
    fun `given initial character list when clear requested then should clear list and reset pagination`() {
        val result = dummyResult

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(result)

        assertThat(sut.characters.value, nullValue())
        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())

        // given

        sut.loadCharacters()
        verify(mockService).loadCharacters(0)
        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())

        assertThat(sut.characters.value, equalTo(result.data.results))

        // when

        sut.clearList()

        // then

        assertThat(sut.characters.value, equalTo(emptyList()))

        sut.loadCharacters()
        verify(mockService, times(2)).loadCharacters(0)
    }

    @Test
    fun `given initial character list when new page requested then should request new page`() {
        val firstResult = dummyResult
        val secondResult = dummyResult

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(firstResult)

        whenever(
            mockService.loadCharacters(20)
        ) doReturn Single.just(secondResult).delaySubscription(delayer)

        assertThat(sut.characters.value, nullValue())
        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())

        // given

        sut.loadCharacters()
        verify(mockService).loadCharacters(0)
        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())

        assertThat(sut.characters.value, equalTo(firstResult.data.results))

        // when

        sut.reload()

        // then

        verify(mockService).loadCharacters(20)

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())

        assertThat(
            sut.characters.value,
            contains(
                *firstResult.data.results.toTypedArray(),
                *secondResult.data.results.toTypedArray()
            )
        )
    }

    @Test
    fun `given initial list when filter requested then only filtered list should be available`() {
        val expected = dummyResult

        whenever(
            mockService.filterCharacters(0, "dummy")
        ) doReturn Single.just(expected).delaySubscription(delayer)

        assertThat(sut.characters.value, nullValue())
        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())

        // when

        sut.filterCharacters("dummy")

        // then

        verify(mockService).filterCharacters(0, "dummy")
        assertThat(sut.isLoading.value, equalTo(true))

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())

        assertThat(sut.characters.value, equalTo(expected.data.results))
    }

    @Test
    fun `given filtered list when paginate requested then filtered list should be paginated`() {
        val firstResult = dummyResult
        val secondResult = dummyResult

        whenever(
            mockService.filterCharacters(0, "dummy")
        ) doReturn Single.just(firstResult)

        whenever(
            mockService.filterCharacters(20, "dummy")
        ) doReturn Single.just(secondResult).delaySubscription(delayer)

        assertThat(sut.characters.value, nullValue())
        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())

        // given

        sut.filterCharacters("dummy")
        verify(mockService).filterCharacters(0, "dummy")
        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())

        assertThat(sut.characters.value, equalTo(firstResult.data.results))

        // when

        sut.reload()

        // then

        verify(mockService).filterCharacters(20, "dummy")

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())

        assertThat(
            sut.characters.value,
            contains(
                *firstResult.data.results.toTypedArray(),
                *secondResult.data.results.toTypedArray()
            )
        )
    }

    @Test
    fun `given a failed list request when requested again then should request same page`() {
        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.error(Throwable("dummy error"))

        assertThat(sut.characters.value, nullValue())
        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())

        // given

        sut.loadCharacters()
        verify(mockService).loadCharacters(0)
        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, equalTo(R.string.default_error))

        assertThat(sut.characters.value, nullValue())

        // when

        sut.reload()

        // then

        verify(mockService, times(2)).loadCharacters(0)
        verify(mockService, times(0)).loadCharacters(20)
    }

    @Test
    fun `given a positive pagination scenario when checking if should paginate then it should be true`() {
        // given

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(dummyResult)

        sut.loadCharacters()

        val visibleItems = 9
        val totalItems = 20
        val firstVisiblePosition = 12
        val dy = 10

        // when

        val actual = sut.shouldPaginate(visibleItems, totalItems, firstVisiblePosition, dy)

        // then

        assertThat(actual, equalTo(true))
    }

    @Test
    fun `given an initial list and a full scroll to bottom when checking if should paginate then it should be false`() {
        // given

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(dummyResult)

        sut.loadCharacters()

        val visibleItems = 9
        val totalItems = 20
        val firstVisiblePosition = 12
        val dy = 0 // bottom of page

        // when

        val actual = sut.shouldPaginate(visibleItems, totalItems, firstVisiblePosition, dy)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given no initial list when checking if should paginate then it should be false`() {
        // given

        val visibleItems = 9
        val totalItems = 20
        val firstVisiblePosition = 12
        val dy = 10

        // when

        val actual = sut.shouldPaginate(visibleItems, totalItems, firstVisiblePosition, dy)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given last page requested when checking if should paginate then it should be false`() {
        // given

        val dummyResult = ResponseBody(
            200,
            "ok",
            "copyright",
            DataWrapper(
                0, 20, 20, listOf(
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
                    dummyCharacter,
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
                )
            )
        )

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(dummyResult)

        val visibleItems = 9
        val totalItems = 20
        val firstVisiblePosition = 12
        val dy = 10

        // when

        val actual = sut.shouldPaginate(visibleItems, totalItems, firstVisiblePosition, dy)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given loading in progress when checking if should paginate then it should be false`() {
        // given

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(dummyResult).delaySubscription(delayer)

        sut.loadCharacters()

        assertThat(sut.isLoading.value, equalTo(true))

        val visibleItems = 9
        val totalItems = 20
        val firstVisiblePosition = 12
        val dy = 10

        // when

        val actual = sut.shouldPaginate(visibleItems, totalItems, firstVisiblePosition, dy)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given a scroll on initial items when checking if should paginate then it should be false`() {
        // given

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(dummyResult)

        sut.loadCharacters()

        val visibleItems = 9
        val totalItems = 20
        val firstVisiblePosition = 6 // is not at bottom of screen
        val dy = 10

        // when

        val actual = sut.shouldPaginate(visibleItems, totalItems, firstVisiblePosition, dy)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given initial data changed scroll when checking if should paginate then it should be false`() {
        // given

        whenever(
            mockService.loadCharacters(0)
        ) doReturn Single.just(dummyResult)

        sut.loadCharacters()

        val visibleItems = 9
        val totalItems = 20
        val firstVisiblePosition = 0 // dataSetChanged triggers scroll on fill
        val dy = 10

        // when

        val actual = sut.shouldPaginate(visibleItems, totalItems, firstVisiblePosition, dy)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given an initial list when details requested then should go to details`() {
        whenever(
            mockService.loadCharacters(anyInt())
        ) doReturn Single.just(dummyResult)

        // given

        sut.loadCharacters()

        // when

        sut.onCharacterSelected(4)

        // then

        verify(mockRouter).proceedToCharacterDetails(500)
    }

    @Test
    fun `given no initial list when details requested then should not go to details`() {
        whenever(
            mockService.loadCharacters(anyInt())
        ) doReturn Single.error(Throwable("dummy error"))

        // given

        sut.loadCharacters()

        // when

        sut.onCharacterSelected(4)

        // then

        verifyNoMoreInteractions(mockRouter)
    }
}
