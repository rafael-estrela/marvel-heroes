package br.eti.rafaelcouto.marvelheroes.data.datasource.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import br.eti.rafaelcouto.marvelheroes.data.api.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.model.Character
import kotlinx.coroutines.CoroutineScope

class CharactersListDataSourceFactory(
    private val filterValue: LiveData<String?>,
    private val scope: CoroutineScope,
    private val api: INetworkAPI = INetworkAPI.baseApi
) : DataSource.Factory<Int, Character>() {
    private val mDataSource = MutableLiveData<CharactersListDataSource>()
    val dataSource: LiveData<CharactersListDataSource>
        get() = mDataSource

    override fun create(): DataSource<Int, Character> {
        val dataSource = CharactersListDataSource(filterValue, scope, api)
        mDataSource.postValue(dataSource)
        return dataSource
    }
}