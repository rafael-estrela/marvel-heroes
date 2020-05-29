package br.eti.rafaelcouto.marvelheroes.data.datasource.list

import androidx.lifecycle.LiveData
import br.eti.rafaelcouto.marvelheroes.data.State
import br.eti.rafaelcouto.marvelheroes.data.api.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.data.datasource.BaseDataSource
import br.eti.rafaelcouto.marvelheroes.model.Character
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CharactersListDataSource(
    private val filterValue: LiveData<String?>,
    private val scope: CoroutineScope,
    private val api: INetworkAPI
) :BaseDataSource<Character>() {
    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Character>
    ) {
        mState.postValue(State.LOADING)

        scope.launch {
            try {
                val result = api.getPublicCharacters(params.requestedLoadSize, 0, filterValue.value)

                if (result.code == 200) {
                    mCopyright.postValue(result.attributionText)

                    val data = result.data

                    callback.onResult(data.results, data.offset, data.total, null, params.requestedLoadSize)

                    mState.postValue(State.LOADED)
                } else throw Exception()
            } catch (e: Exception) {
                retry = { loadInitial(params, callback) }

                mState.postValue(State.FAILED)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Character>) {
        mState.postValue(State.LOADING)

        scope.launch {
            try {
                val result = api.getPublicCharacters(params.requestedLoadSize, params.key, filterValue.value)

                if (result.code == 200) {
                    callback.onResult(result.data.results, params.key + params.requestedLoadSize)

                    mState.postValue(State.LOADED)
                } else throw Exception()
            } catch (e: Exception) {
                retry = { loadBefore(params, callback) }

                mState.postValue(State.FAILED)
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Character>) {
        mState.postValue(State.LOADING)

        scope.launch {
            try {
                val result = api.getPublicCharacters(params.requestedLoadSize, params.key, filterValue.value)

                if (result.code == 200) {
                    callback.onResult(result.data.results, params.key + params.requestedLoadSize)

                    mState.postValue(State.LOADED)
                } else throw Exception()
            } catch (e: Exception) {
                retry = { loadAfter(params, callback) }

                mState.postValue(State.FAILED)
            }
        }
    }
}