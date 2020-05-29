package br.eti.rafaelcouto.marvelheroes.view.list.details.comics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.data.datasource.details.ComicsDiffUtil
import br.eti.rafaelcouto.marvelheroes.model.Comic

class ComicsAdapter : PagedListAdapter<Comic, ComicsViewHolder>(ComicsDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComicsViewHolder {
        return ComicsViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewType,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ComicsViewHolder, position: Int) {
        holder.binding.item = getItem(position)
        holder.displayImage()
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_comic
}
