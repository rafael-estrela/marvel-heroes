package br.eti.rafaelcouto.marvelheroes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.service.CharactersListService
import br.eti.rafaelcouto.marvelheroes.router.CharactersListRouter
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class CharactersListViewModel(
    private val router: CharactersListRouter,
    private val service: CharactersListService,
    context: CoroutineContext = IO
) : BaseViewModel(context) {
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

        mIsLoading.value = true

        viewModelScope.launch {
            try {
                treatResult(service.loadCharacters(offset))
                page++
            } catch (e: Exception) {
                mHasError.postValue(R.string.default_error)
            } finally {
                mIsLoading.postValue(false)
            }
        }
    }

    fun filterCharacters(name: String) {
        isFiltering = true
        lastFilterTerm = name

        mIsLoading.value = true

        jobs.add(launch {
            try {
                treatResult(service.filterCharacters(offset, name))
                page++
            } catch (e: Exception) {
                mHasError.postValue(R.string.default_error)
            } finally {
                mIsLoading.postValue(false)
            }
        })
    }

    private fun treatResult(result: ResponseBody<Character>) {
        result.apply {
            maxItems = data.total
            mCopyright.postValue(attributionText)

            mCharacters.value?.let {
                mCharacters.postValue(it + data.results)
            } ?: run {
                mCharacters.postValue(data.results)
            }
        }
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
