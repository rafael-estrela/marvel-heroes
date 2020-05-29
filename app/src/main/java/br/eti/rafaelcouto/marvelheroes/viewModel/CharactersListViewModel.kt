package br.eti.rafaelcouto.marvelheroes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import br.eti.rafaelcouto.marvelheroes.data.State
import br.eti.rafaelcouto.marvelheroes.data.datasource.list.CharactersListDataSourceFactory
import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.router.CharactersListRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlin.coroutines.CoroutineContext

class CharactersListViewModel(
    private val router: CharactersListRouter,
    override val coroutineContext: CoroutineContext = IO
) : ViewModel(), CoroutineScope {
    private val charactersPerPage = 20

    private lateinit var factory: CharactersListDataSourceFactory
    lateinit var characters: LiveData<PagedList<Character>>

    val state: LiveData<State> by lazy {
        Transformations.switchMap(factory.dataSource) { it.state }
    }

    val copyright: LiveData<String> by lazy {
        Transformations.switchMap(factory.dataSource) { it.copyright }
    }

    private val mFilterValue = MutableLiveData<String?>()

    fun setup(factory: CharactersListDataSourceFactory = CharactersListDataSourceFactory(mFilterValue, this)) {
        this.factory = factory

        val config = PagedList.Config.Builder()
            .setPageSize(charactersPerPage)
            .setEnablePlaceholders(false)
            .build()

        characters = LivePagedListBuilder(factory, config).build()
    }

    fun updateFilterState(term: String?) {
        mFilterValue.value = term
        factory.dataSource.value?.invalidate()
    }

    fun retry() = factory.dataSource.value?.retry()

    fun onCharacterSelected(position: Int) {
        characters.value?.let {
            router.proceedToCharacterDetails(it[position]?.id ?: 0)
        }
    }
}
