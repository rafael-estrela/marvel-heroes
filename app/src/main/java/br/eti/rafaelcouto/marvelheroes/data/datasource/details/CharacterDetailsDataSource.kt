package br.eti.rafaelcouto.marvelheroes.data.datasource.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.eti.rafaelcouto.marvelheroes.data.State
import br.eti.rafaelcouto.marvelheroes.data.api.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.data.datasource.BaseDataSource
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception

class CharacterDetailsDataSource(
    private val characterId: Int,
    private val scope: CoroutineScope,
    private val api: INetworkAPI = INetworkAPI.baseApi
) : BaseDataSource<Comic>() {
    private val mCharacterDetails = MutableLiveData<CharacterDetails>()
    val characterDetails: LiveData<CharacterDetails>
        get() = mCharacterDetails

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Comic>
    ) {
        mState.postValue(State.LOADING)

        scope.launch {
            try {
                if (mCharacterDetails.value == null) {
                    val details = api.getPublicCharacterInfo(characterId)

                    if (details.code == 200) {
                        mCharacterDetails.postValue(details.data.results.first())
                        mCopyright.postValue(details.attributionText)
                    } else throw Exception()
                }

                val comics = api.getPublicCharacterComics(characterId, params.requestedLoadSize, 0)

                if (comics.code == 200) {
                    val data = comics.data

                    callback.onResult(data.results, data.offset, data.total, null, params.requestedLoadSize)

                    mState.postValue(State.LOADED)
                } else throw Exception()
            } catch (e: Exception) {
                retry = { loadInitial(params, callback) }

                mState.postValue(State.FAILED)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Comic>) {
        mState.postValue(State.LOADING)

        scope.launch {
            try {
                val result = api.getPublicCharacterComics(characterId, params.requestedLoadSize, params.key)

                if (result.code == 200) {
                    val comics = result.data.results

                    callback.onResult(comics, params.key + params.requestedLoadSize)

                    mState.postValue(State.LOADED)
                } else throw Exception()
            } catch (e: Exception) {
                retry = { loadBefore(params, callback) }

                mState.postValue(State.FAILED)
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Comic>) {
        mState.postValue(State.LOADING)

        scope.launch {
            try {
                val result = api.getPublicCharacterComics(characterId, params.requestedLoadSize, params.key)

                if (result.code == 200) {
                    val comics = result.data.results

                    callback.onResult(comics, params.key + params.requestedLoadSize)

                    mState.postValue(State.LOADED)
                } else throw Exception()
            } catch (e: Exception) {
                retry = { loadAfter(params, callback) }

                mState.postValue(State.FAILED)
            }
        }
    }
}