package br.eti.rafaelcouto.marvelheroes.data.datasource.list

import androidx.recyclerview.widget.DiffUtil
import br.eti.rafaelcouto.marvelheroes.model.Character

class CharacterDiffUtil : DiffUtil.ItemCallback<Character>() {
    override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean = oldItem.id == newItem.id
}