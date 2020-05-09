package br.eti.rafaelcouto.marvelheroes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.service.CharactersListService
import br.eti.rafaelcouto.marvelheroes.router.CharactersListRouter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class CharactersListViewModel(
    private val router: CharactersListRouter,
    private val service: CharactersListService
) : BaseViewModel() {
    // static
    companion object {
        const val CHARACTERS_PER_PAGE = 20
    }

    // liveData
    private val mCharacters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>>
        get() = mCharacters

    private var isFiltering = false
    private var lastFilterTerm = ""

    override val offset: Int
        get() = page * CHARACTERS_PER_PAGE

    // api requests
    fun loadCharacters() {
        isFiltering = false
        lastFilterTerm = ""

        makeRequest(service.loadCharacters(offset))
    }

    fun filterCharacters(name: String) {
        isFiltering = true
        lastFilterTerm = name

        makeRequest(service.filterCharacters(offset, name))
    }

    private fun makeRequest(request: Single<ResponseBody<Character>>) {
        request.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { mIsLoading.value = true }
            .doFinally { mIsLoading.value = false }
            .map {
                maxItems = it.data.total
                mCopyright.value = it.attributionText
                it.data.results
            }.subscribeBy(onSuccess = { characters ->
                page++

                mCharacters.value?.let {
                    mCharacters.value = it + characters
                } ?: run {
                    mCharacters.value = characters
                }
            }, onError = {
                mHasError.value = R.string.default_error
            }
        ).addTo(disposeBag)
    }

    fun clearList() {
        mCharacters.value = emptyList()
        page = 0
    }

    // pagination verification
    fun shouldPaginate(
        visibleItems: Int,
        totalItems: Int,
        firstVisibleItemPosition: Int,
        dy: Int
    ): Boolean {
        return takeIf {
            dy > 0
        }?.takeIf {
            isLoading.value?.let { !it } ?: true
        }?.takeIf {
            visibleItems + firstVisibleItemPosition >= totalItems
        }?.takeIf {
            firstVisibleItemPosition >= 0
        }?.takeIf {
            characters.value?.size?.let {
                it < maxItems
            } ?: false
        } != null
    }

    fun reload() {
        if (isFiltering) {
            filterCharacters(lastFilterTerm)
        } else {
            loadCharacters()
        }
    }

    // click events
    fun onCharacterSelected(position: Int) {
        mCharacters.value?.let {
            router.proceedToCharacterDetails(it[position].id)
        }
    }
}
