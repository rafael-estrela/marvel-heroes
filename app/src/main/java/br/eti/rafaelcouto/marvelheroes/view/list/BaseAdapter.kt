package br.eti.rafaelcouto.marvelheroes.view.list

import android.content.Context
import android.view.LayoutInflater
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<Item, VH : RecyclerView.ViewHolder>(
    context: Context,
    private val items: LiveData<List<Item>>
) : RecyclerView.Adapter<VH>() {
    protected val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = items.value.orEmpty().size

    protected fun getItemAtPosition(position: Int): Item {
        return items.value?.let {
            it[position]
        } ?: throw IllegalStateException()
    }

    abstract override fun getItemViewType(position: Int): Int
}
