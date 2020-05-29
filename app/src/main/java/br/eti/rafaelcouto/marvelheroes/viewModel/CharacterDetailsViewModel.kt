package br.eti.rafaelcouto.marvelheroes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import br.eti.rafaelcouto.marvelheroes.data.State
import br.eti.rafaelcouto.marvelheroes.data.datasource.details.CharacterDetailsDataSourceFactory
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers.IO

class CharacterDetailsViewModel(
    override val coroutineContext: CoroutineContext = IO
) : ViewModel(), CoroutineScope {
    private val comicsPerPage = 10

    private lateinit var factory: CharacterDetailsDataSourceFactory
    lateinit var characterComics: LiveData<PagedList<Comic>>

    val state: LiveData<State> by lazy {
        Transformations.switchMap(factory.dataSource) {
            it.state
        }
    }

    val copyright: LiveData<String> by lazy {
        Transformations.switchMap(factory.dataSource) { it.copyright }
    }

    val characterDetails: LiveData<CharacterDetails> by lazy {
        Transformations.switchMap(factory.dataSource) {
            it.characterDetails
        }
    }

    fun retry() = factory.dataSource.value?.retry()

    fun setup(
        characterId: Int,
        factory: CharacterDetailsDataSourceFactory = CharacterDetailsDataSourceFactory(characterId, this)
    ) {
        this.factory = factory

        val config = PagedList.Config.Builder()
            .setPageSize(comicsPerPage)
            .setEnablePlaceholders(false)
            .build()

        characterComics = LivePagedListBuilder(factory, config).build()
    }
}
