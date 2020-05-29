package br.eti.rafaelcouto.marvelheroes.data.datasource.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import br.eti.rafaelcouto.marvelheroes.data.api.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.model.Comic
import kotlinx.coroutines.CoroutineScope

class CharacterDetailsDataSourceFactory(
    private val characterId: Int,
    private val scope: CoroutineScope,
    private val api: INetworkAPI = INetworkAPI.baseApi
) : DataSource.Factory<Int, Comic>() {
    private val mDataSource = MutableLiveData<CharacterDetailsDataSource>()
    val dataSource: LiveData<CharacterDetailsDataSource>
        get() = mDataSource

    override fun create(): DataSource<Int, Comic> {
        val dataSource = CharacterDetailsDataSource(characterId, scope, api)
        mDataSource.postValue(dataSource)
        return dataSource
    }
}