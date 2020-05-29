package br.eti.rafaelcouto.marvelheroes.data.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import br.eti.rafaelcouto.marvelheroes.data.State

abstract class BaseDataSource<T> : PageKeyedDataSource<Int, T>() {
    protected var mState = MutableLiveData<State>()
    val state: LiveData<State>
        get() = mState

    protected var mCopyright = MutableLiveData<String>()
    val copyright: LiveData<String>
        get() = mCopyright

    protected var retry: (() -> Unit)? = null

    fun retry() = retry?.invoke()
}