package br.eti.rafaelcouto.marvelheroes.viewModel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.network.service.CharacterDetailsService
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class CharacterDetailsViewModel(
    private val service: CharacterDetailsService
): BaseViewModel() {
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

    fun loadCharacterComics() {
        service.loadCharacterComics(characterId, offset)
            .map {
                maxItems = it.data.total
                it.data.results
            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { mIsLoading.value = true }
            .doFinally { mIsLoading.value = false }
            .subscribeBy(onSuccess = { comics ->
                page++

                mCharacterDetails.value?.let {
                    mCharacterDetails.value = CharacterDetails(
                        it.description,
                        it.comics + comics
                    ).apply {
                        name = it.name
                        thumbnail = it.thumbnail
                    }
                }
            }, onError = {
                mHasError.value = R.string.default_error
            }
        ).addTo(disposeBag)
    }

    // api requests
    private fun loadCharacterInfo() {
        Single.zip(
            service.loadCharacterDetails(characterId)
                .map { it.data.results.first() }
                .subscribeOn(Schedulers.newThread()),
            service.loadCharacterComics(characterId, offset)
                .map {
                    maxItems = it.data.total
                    it.data.results
                }.subscribeOn(Schedulers.newThread()),
            BiFunction { character: CharacterDetails, comics: List<Comic> ->
                CharacterDetails(character.description, comics).apply {
                    name = character.name
                    thumbnail = character.thumbnail
                }
            }
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { mIsLoading.value = true }
            .doFinally { mIsLoading.value = false }
            .subscribeBy(onSuccess = {
                page++
                mCharacterDetails.value = it
            }, onError = {
                mHasError.value = R.string.default_error
            }
        ).addTo(disposeBag)
    }
}