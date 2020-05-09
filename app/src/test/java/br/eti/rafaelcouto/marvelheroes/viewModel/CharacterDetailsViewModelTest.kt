package br.eti.rafaelcouto.marvelheroes.viewModel

import android.os.Build
import android.os.Bundle
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.SynchronousTestRule
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.model.general.DataWrapper
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.model.general.Thumbnail
import br.eti.rafaelcouto.marvelheroes.network.service.CharacterDetailsService
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
import org.hamcrest.Matchers.notNullValue
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
class CharacterDetailsViewModelTest {
    // rule
    @Rule
    @JvmField
    val testRule = SynchronousTestRule()

    // delayer
    private val delayer = PublishSubject.create<Boolean>()

    // sut
    private lateinit var sut: CharacterDetailsViewModel

    // mocks
    @Mock
    private lateinit var mockService: CharacterDetailsService
    @Mock
    private lateinit var mockExtras: Bundle

    // comics dummies
    private var dummyComicId: Int = 0

    private val dummyComic: Comic
        get() = Comic(
            "Marvel comic, issue #${++dummyComicId}",
            Thumbnail("dummy path", "jpg")
        )

    private val dummyCharacterComicsList: List<Comic>
        get() = listOf(
            dummyComic,
            dummyComic,
            dummyComic,
            dummyComic,
            dummyComic,
            dummyComic,
            dummyComic,
            dummyComic,
            dummyComic,
            dummyComic
        )

    private val dummyCharacterComicsDataWrapper: DataWrapper<Comic>
        get() = DataWrapper(0, 10, 20, dummyCharacterComicsList)

    private val dummyCharacterComics: ResponseBody<Comic>
        get() = ResponseBody(200, "ok", "copyright", dummyCharacterComicsDataWrapper)

    // character dummies
    private var dummyCharacterId: Int = 0

    private val dummyCharacterDetails: CharacterDetails
        get() = CharacterDetails(
            "dummy description",
            dummyCharacterComicsList
        ).apply {
            id = (++dummyCharacterId) * 100
            name = "Marvel character #$id"
        }

    private val dummyCharacterDetailsList: List<CharacterDetails>
        get() = listOf(dummyCharacterDetails)

    private val dummyCharacterDetailsDataWrapper: DataWrapper<CharacterDetails>
        get() = DataWrapper(0, 10, 100, dummyCharacterDetailsList)

    private val dummyCharacterDetailsResponse: ResponseBody<CharacterDetails>
        get() = ResponseBody(200, "ok", "copyright", dummyCharacterDetailsDataWrapper)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        this.sut = CharacterDetailsViewModel(mockService)
        this.dummyCharacterId = 0
        this.dummyComicId = 0

        sut.characterComics.observeForever { }
    }

    @Test
    fun `given no character id when trying to load details then should not request anything`() {
        // given

        val extras: Bundle? = null

        // when

        sut.loadCharacterInfo(extras)

        // then

        verifyNoMoreInteractions(mockService)
    }

    @Test
    fun `given a character id when load details requested and details request ends first then should update character`() {
        val expected = dummyCharacterDetailsResponse

        dummyComicId = 0

        val expectedComic = dummyCharacterComics

        val mDelayer = PublishSubject.create<Boolean>()

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(expected).delaySubscription(delayer)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(expectedComic).delaySubscription(mDelayer)

        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        // given

        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        // when

        sut.loadCharacterInfo(mockExtras)

        // then

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        mDelayer.onComplete()

        val expectedResult = expected.data.results.first()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, equalTo(expectedResult))
        assertThat(sut.characterComics.value, equalTo(expectedComic.data.results))
    }

    @Test
    fun `given a character id when load details requested then copyright must be updated`() {
        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(dummyCharacterDetailsResponse)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics)

        assertThat(sut.copyright.value, nullValue())

        // given

        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        // when

        sut.loadCharacterInfo(mockExtras)

        // then

        assertThat(sut.copyright.value, equalTo("copyright"))
    }

    @Test
    fun `given a character id when load details requested and comics request ends first then should update character`() {
        val expected = dummyCharacterDetailsResponse

        dummyComicId = 0

        val expectedComic = dummyCharacterComics

        val mDelayer = PublishSubject.create<Boolean>()

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(expected).delaySubscription(delayer)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(expectedComic).delaySubscription(mDelayer)

        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        // given

        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        // when

        sut.loadCharacterInfo(mockExtras)

        // then

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        mDelayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        delayer.onComplete()

        val expectedResult = expected.data.results.first()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, equalTo(expectedResult))
        assertThat(sut.characterComics.value, equalTo(expectedComic.data.results))
    }

    @Test
    fun `given a character id when load details request fails then should display error`() {
        val mDelayer = PublishSubject.create<Boolean>()

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.error<ResponseBody<CharacterDetails>>(
            Throwable("dummy exception")
        ).delaySubscription(delayer)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics).delaySubscription(mDelayer)

        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        // given

        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        // when

        sut.loadCharacterInfo(mockExtras)

        // then

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        mDelayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, equalTo(R.string.default_error))
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())
    }

    @Test
    fun `given a character id when load comics request fails then should display error`() {
        val mDelayer = PublishSubject.create<Boolean>()

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(dummyCharacterDetailsResponse).delaySubscription(mDelayer)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.error<ResponseBody<Comic>>(
            Throwable("dummy exception")
        ).delaySubscription(delayer)

        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        // given

        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        // when

        sut.loadCharacterInfo(mockExtras)

        // then

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        mDelayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, equalTo(R.string.default_error))
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())
    }

    @Test
    fun `given an initial comics list when more comics requested then should load more comics`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        // given

        val expected = dummyCharacterDetailsResponse
        val expectedComic = dummyCharacterComics

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(expected)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(expectedComic)

        sut.loadCharacterInfo(mockExtras)

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)

        // when

        val secondExpectedComic = dummyCharacterComics

        whenever(
            mockService.loadCharacterComics(100, 10)
        ) doReturn Single.just(secondExpectedComic).delaySubscription(delayer)

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, notNullValue())
        assertThat(sut.characterComics.value, notNullValue())

        sut.loadCharacterComics()

        // then

        verify(mockService).loadCharacterComics(100, 10)
        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, notNullValue())
        assertThat(sut.characterComics.value, notNullValue())

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, notNullValue())

        assertThat(
            sut.characterComics.value,
            contains(
                *expectedComic.data.results.toTypedArray(),
                *secondExpectedComic.data.results.toTypedArray()
            )
        )
    }

    @Test
    fun `given an initial comics list when more comics requested then should display error`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        // given

        val expected = dummyCharacterDetailsResponse
        val expectedComic = dummyCharacterComics

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(expected)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(expectedComic)

        sut.loadCharacterInfo(mockExtras)

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)

        // when

        whenever(
            mockService.loadCharacterComics(100, 10)
        ) doReturn Single.error<ResponseBody<Comic>>(
            Throwable("dummy error")
        ).delaySubscription(delayer)

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, notNullValue())
        assertThat(sut.characterComics.value, notNullValue())

        sut.loadCharacterComics()

        // then

        verify(mockService).loadCharacterComics(100, 10)
        assertThat(sut.isLoading.value, equalTo(true))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, notNullValue())
        assertThat(sut.characterComics.value, notNullValue())

        delayer.onComplete()

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, equalTo(R.string.default_error))
        assertThat(sut.characterDetails.value, notNullValue())

        assertThat(
            sut.characterComics.value,
            contains(
                *expectedComic.data.results.toTypedArray()
            )
        )
    }

    @Test
    fun `given a failed comics request when retry requested then should reload same page`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        // given

        val expected = dummyCharacterDetailsResponse
        val expectedComic = dummyCharacterComics

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(expected)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(expectedComic)

        sut.loadCharacterInfo(mockExtras)

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)

        whenever(
            mockService.loadCharacterComics(100, 10)
        ) doReturn Single.error<ResponseBody<Comic>>(
            Throwable("dummy error")
        )

        sut.loadCharacterComics()

        verify(mockService).loadCharacterComics(100, 10)
        assertThat(sut.hasError.value, equalTo(R.string.default_error))

        // when

        sut.retry()

        // then

        verify(mockService, times(2)).loadCharacterComics(100, 10)
    }

    @Test
    fun `given a successful pagination scenario when checking pagination then should return true`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(dummyCharacterDetailsResponse)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics)

        sut.loadCharacterInfo(mockExtras)

        // given

        val scrollY = 100
        val oldScrollY = 0
        val scrollViewHeight = 800
        val recyclerViewHeight = 600

        // when

        val actual = sut.shouldPaginate(scrollY, oldScrollY, scrollViewHeight, recyclerViewHeight)

        // then

        assertThat(actual, equalTo(true))
    }

    @Test
    fun `given a loading request when checking pagination then should return false`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(dummyCharacterDetailsResponse)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics).delaySubscription(delayer)

        sut.loadCharacterInfo(mockExtras)

        // given

        assertThat(sut.isLoading.value, equalTo(true))

        val scrollY = 100
        val oldScrollY = 0
        val scrollViewHeight = 800
        val recyclerViewHeight = 600

        // when

        val actual = sut.shouldPaginate(scrollY, oldScrollY, scrollViewHeight, recyclerViewHeight)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given no initial data when checking pagination then should return false`() {
        // given

        assertThat(sut.characterDetails.value, nullValue())

        val scrollY = 100
        val oldScrollY = 0
        val scrollViewHeight = 800
        val recyclerViewHeight = 600

        // when

        val actual = sut.shouldPaginate(scrollY, oldScrollY, scrollViewHeight, recyclerViewHeight)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given last page reached when checking pagination then should return false`() {
        val dummyCharacterComics = ResponseBody(
            200,
            "ok",
            "copyright",
            DataWrapper(
                0,
                10,
                10,
                dummyCharacterComicsList
            )
        )

        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(dummyCharacterDetailsResponse)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics)

        sut.loadCharacterInfo(mockExtras)

        // given

        val scrollY = 100
        val oldScrollY = 0
        val scrollViewHeight = 800
        val recyclerViewHeight = 600

        // when

        val actual = sut.shouldPaginate(scrollY, oldScrollY, scrollViewHeight, recyclerViewHeight)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given scrollview is scrolling up when checking pagination then should return false`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(dummyCharacterDetailsResponse)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics)

        sut.loadCharacterInfo(mockExtras)

        // given

        val scrollY = 0 // if scrollY <= oldScrollY, it's scrolling up
        val oldScrollY = 100
        val scrollViewHeight = 800
        val recyclerViewHeight = 600

        // when

        val actual = sut.shouldPaginate(scrollY, oldScrollY, scrollViewHeight, recyclerViewHeight)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given a scroll movement at the top of the scrollview when checking pagination then should return false`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(dummyCharacterDetailsResponse)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics)

        sut.loadCharacterInfo(mockExtras)

        // given

        val scrollY = 0 // if scrollY < recyclerViewHeight - scrollViewHeight, user hasn't reached recycler yet
        val oldScrollY = 100
        val scrollViewHeight = 0
        val recyclerViewHeight = 10

        // when

        val actual = sut.shouldPaginate(scrollY, oldScrollY, scrollViewHeight, recyclerViewHeight)

        // then

        assertThat(actual, equalTo(false))
    }

    @Test
    fun `given a failed details request when reload requested then should reload details`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics)

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.error<ResponseBody<CharacterDetails>>(Throwable("dummy exception"))

        assertThat(sut.isLoading.value, nullValue())
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterDetails.value, nullValue())
        assertThat(sut.characterComics.value, nullValue())

        // given

        sut.loadCharacterInfo(mockExtras)

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)
        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, equalTo(R.string.default_error))
        assertThat(sut.characterComics.value, nullValue())

        // when

        sut.retry()

        // then

        verify(mockService, times(2)).loadCharacterDetails(100)
        verify(mockService, times(2)).loadCharacterComics(100, 0)
    }

    @Test
    fun `given a failed comics request when reload requested then should reload comics`() {
        whenever(
            mockExtras.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY)
        ) doReturn 100

        whenever(
            mockService.loadCharacterDetails(anyInt())
        ) doReturn Single.just(dummyCharacterDetailsResponse)

        whenever(
            mockService.loadCharacterComics(100, 0)
        ) doReturn Single.just(dummyCharacterComics)

        sut.loadCharacterInfo(mockExtras)

        verify(mockService).loadCharacterDetails(100)
        verify(mockService).loadCharacterComics(100, 0)

        // given

        whenever(
            mockService.loadCharacterComics(100, 10)
        ) doReturn Single.error(Throwable("dummy exception"))

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, nullValue())
        assertThat(sut.characterComics.value, notNullValue())

        sut.loadCharacterComics()

        verify(mockService).loadCharacterComics(100, 10)

        assertThat(sut.isLoading.value, equalTo(false))
        assertThat(sut.hasError.value, equalTo(R.string.default_error))
        assertThat(sut.characterComics.value, notNullValue())

        // when

        sut.retry()

        // then

        verify(mockService, times(2)).loadCharacterComics(100, 10)
    }
}
