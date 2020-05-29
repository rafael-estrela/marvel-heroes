package br.eti.rafaelcouto.marvelheroes.view.list.character

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.data.datasource.list.CharacterDiffUtil
import br.eti.rafaelcouto.marvelheroes.model.Character

class CharactersAdapter : PagedListAdapter<Character, CharactersViewHolder>(CharacterDiffUtil()) {
    var onItemClick: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewType,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int) {
        holder.binding.item = getItem(position)
        holder.setOnItemClickListener(onItemClick)
        holder.displayImage()
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_character

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClick = listener
    }
}
