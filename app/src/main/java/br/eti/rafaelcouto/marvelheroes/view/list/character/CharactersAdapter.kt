package br.eti.rafaelcouto.marvelheroes.view.list.character

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.view.list.BaseAdapter

class CharactersAdapter(
    context: Context,
    items: LiveData<List<Character>>
): BaseAdapter<Character, CharactersViewHolder>(context, items) {
    var onItemClick: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharactersViewHolder {
        return CharactersViewHolder(
            DataBindingUtil.inflate(inflater, viewType, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CharactersViewHolder, position: Int) {
        holder.binding.item = getItemAtPosition(position)
        holder.setOnItemClickListener(onItemClick)
        holder.displayImage()
    }

    override fun getItemViewType(position: Int): Int = R.layout.item_character

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClick = listener
    }
}