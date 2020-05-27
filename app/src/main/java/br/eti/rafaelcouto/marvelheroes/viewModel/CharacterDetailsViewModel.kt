package br.eti.rafaelcouto.marvelheroes.viewModel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.service.CharacterDetailsService
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(
    private val service: CharacterDetailsService,
    context: CoroutineContext = IO
) : BaseViewModel(context) {
    companion object {
        const val CHARACTER_ID_KEY = "characterIdExtra"
        const val COMICS_PER_PAGE = 10
    }

    // lifecycle
    private val mCharacterDetails = MutableLiveData<CharacterDetails>()
    val characterDetails: LiveData<CharacterDetails>
        get() = mCharacterDetails

    val characterComics = Transformations.map(characterDetails) { it.comics }

    // pagination
    override val offset: Int
        get() = page * COMICS_PER_PAGE

    // character id
    private var characterId: Int = 0

    // capturing id
    fun loadCharacterInfo(extras: Bundle?) {
        extras?.let {
            characterId = it.getInt(CHARACTER_ID_KEY)

            loadCharacterInfo()
        }
    }

    // retry
    fun retry() {
        mCharacterDetails.value?.let {
            loadCharacterComics()
        } ?: run {
            loadCharacterInfo()
        }
    }

    // pagination verification
    fun shouldPaginate(
        scrollY: Int,
        oldScrollY: Int,
        scrollViewHeight: Int,
        recyclerViewHeight: Int
    ): Boolean {
        return takeIf {
            scrollY >= recyclerViewHeight - scrollViewHeight
        }?.takeIf {
            scrollY > oldScrollY
        }?.takeIf {
            isLoading.value?.let { !it } ?: true
        }?.takeIf {
            characterComics.value?.size?.let {
                it < maxItems
            } ?: false
        } != null
    }

    // api requests
    fun loadCharacterComics() {
        mIsLoading.value = true

        viewModelScope.launch {
            try {
                val result = service.loadCharacterComics(characterId, offset)

                treatComics(result)
                updateComics(result.data.results)

                page++
            } catch (e: Exception) {
                mHasError.postValue(R.string.default_error)
            } finally {
                mIsLoading.postValue(false)
            }
        }
    }

    private fun loadCharacterInfo() {
        mIsLoading.value = true

        viewModelScope.launch {
            try {
                val detailsFlow = flowOf(async { service.loadCharacterDetails(characterId) })
                val comicsFlow = flowOf(async { service.loadCharacterComics(characterId, offset) })

                detailsFlow.zip(comicsFlow) { deferredDetails, deferredComics ->
                    treatDetails(deferredDetails.await(), deferredComics.await())
                }.collect {
                    mCharacterDetails.postValue(it)
                }

                page++
            } catch (e: Exception) {
                mHasError.postValue(R.string.default_error)
            } finally {
                mIsLoading.postValue(false)
            }
        }
    }

    private fun treatDetails(
        detailsResult: ResponseBody<CharacterDetails>,
        comicsResult: ResponseBody<Comic>
    ): CharacterDetails {
        val details = detailsResult.data.results.first()
        val comics = comicsResult.data.results

        treatComics(comicsResult)

        return CharacterDetails(details.description, comics).apply {
            name = details.name
            thumbnail = details.thumbnail
        }
    }

    private fun treatComics(result: ResponseBody<Comic>) {
        result.apply {
            maxItems = data.total
            mCopyright.postValue(attributionText)
        }
    }

    private fun updateComics(comics: List<Comic>) {
        mCharacterDetails.value?.let {
            val details = CharacterDetails(
                it.description,
                it.comics + comics
            ).apply {
                name = it.name
                thumbnail = it.thumbnail
            }

            mCharacterDetails.postValue(details)
        }
    }
}
