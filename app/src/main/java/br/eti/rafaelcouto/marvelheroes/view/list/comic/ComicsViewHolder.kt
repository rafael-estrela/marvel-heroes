package br.eti.rafaelcouto.marvelheroes.view.list.comic

import androidx.recyclerview.widget.RecyclerView
import br.eti.rafaelcouto.marvelheroes.databinding.ItemComicBinding
import com.squareup.picasso.Picasso

class ComicsViewHolder(
    val binding: ItemComicBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun displayImage() {
        Picasso.with(binding.root.context)
            .load(binding.item?.thumbnail?.portraitRegular)
            .into(binding.iComicIvThumb)
    }
}
