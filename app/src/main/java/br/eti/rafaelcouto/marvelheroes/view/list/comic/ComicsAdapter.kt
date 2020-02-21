package br.eti.rafaelcouto.marvelheroes.view.list.comic

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.view.list.BaseAdapter

class ComicsAdapter(
    context: Context,
    items: LiveData<List<Comic>>
) : BaseAdapter<Comic, ComicsViewHolder>(context, items) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicsViewHolder {
        return ComicsViewHolder(
            DataBindingUtil.inflate(inflater, viewType, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ComicsViewHolder, position: Int) {
        holder.binding.item = getItemAtPosition(position)
        holder.displayImage()
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_comic
}
