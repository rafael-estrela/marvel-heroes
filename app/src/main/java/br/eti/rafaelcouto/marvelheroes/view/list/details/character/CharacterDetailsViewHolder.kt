package br.eti.rafaelcouto.marvelheroes.view.list.details.character

import androidx.recyclerview.widget.RecyclerView
import br.eti.rafaelcouto.marvelheroes.databinding.ItemCharacterDetailsBinding
import com.squareup.picasso.Picasso

class CharacterDetailsViewHolder(
    val binding: ItemCharacterDetailsBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun displayImage() {
        Picasso.with(binding.root.context)
            .load(binding.characterDetails?.thumbnail?.standardLarge)
            .into(binding.iCharacterDetailsIvThumb)
    }
}