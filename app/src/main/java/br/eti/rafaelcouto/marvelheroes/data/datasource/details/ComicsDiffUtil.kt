package br.eti.rafaelcouto.marvelheroes.data.datasource.details

import androidx.recyclerview.widget.DiffUtil
import br.eti.rafaelcouto.marvelheroes.model.Comic

class ComicsDiffUtil : DiffUtil.ItemCallback<Comic>() {
    override fun areItemsTheSame(oldItem: Comic, newItem: Comic): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Comic, newItem: Comic): Boolean = oldItem.id == newItem.id
}