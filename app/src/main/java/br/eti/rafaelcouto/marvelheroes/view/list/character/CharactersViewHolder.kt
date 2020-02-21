package br.eti.rafaelcouto.marvelheroes.view.list.character

import androidx.recyclerview.widget.RecyclerView
import br.eti.rafaelcouto.marvelheroes.databinding.ItemCharacterBinding
import com.squareup.picasso.Picasso

class CharactersViewHolder(
    val binding: ItemCharacterBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        binding.root.setOnClickListener { listener?.onItemClick(adapterPosition) }
    }

    fun displayImage() {
        Picasso.with(binding.root.context)
            .load(binding.item?.thumbnail?.standardRegular)
            .into(binding.iCharacterIvThumb)
    }
}
