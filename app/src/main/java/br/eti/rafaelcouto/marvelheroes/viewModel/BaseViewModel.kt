package br.eti.rafaelcouto.marvelheroes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class BaseViewModel(
    final override val coroutineContext: CoroutineContext
) : ViewModel(), CoroutineScope {

    // coroutine
    protected val viewModelScope = CoroutineScope(coroutineContext)

    protected val jobs = ArrayList<Job>()

    // livedata
    protected val mIsLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = mIsLoading

    protected val mHasError = MutableLiveData<Int>()
    val hasError: LiveData<Int>
        get() = mHasError

    protected val mCopyright = MutableLiveData<String>()
    val copyright: LiveData<String>
        get() = mCopyright

    // pagination properties
    protected var page = 0
    protected abstract val offset: Int
    protected var maxItems = 0

    // lifecycle
    override fun onCleared() {
        super.onCleared()

        jobs.forEach {
            if (!it.isCancelled) it.cancel()
        }
    }
}
