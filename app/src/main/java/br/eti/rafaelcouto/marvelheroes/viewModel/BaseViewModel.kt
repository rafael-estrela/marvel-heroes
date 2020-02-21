package br.eti.rafaelcouto.marvelheroes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel: ViewModel() {
    // livedata
    protected val mIsLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = mIsLoading

    protected val mHasError = MutableLiveData<Int>()
    val hasError: LiveData<Int>
        get() = mHasError

    // rx
    protected val disposeBag = CompositeDisposable()

    // pagination properties
    protected var page = 0
    protected abstract val offset: Int
    protected var maxItems = 0

    // lifecycle
    override fun onCleared() {
        disposeBag.dispose()

        super.onCleared()
    }
}