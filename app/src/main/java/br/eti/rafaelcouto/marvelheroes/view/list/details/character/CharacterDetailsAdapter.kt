package br.eti.rafaelcouto.marvelheroes.view.list.details.character

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails

class CharacterDetailsAdapter : RecyclerView.Adapter<CharacterDetailsViewHolder>() {
    private val items = mutableListOf<CharacterDetails>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterDetailsViewHolder {
        return CharacterDetailsViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewType,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CharacterDetailsViewHolder, position: Int) {
        holder.binding.characterDetails = getItem(position)
        holder.displayImage()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = R.layout.item_character_details

    fun updateData(element: CharacterDetails) {
        items.clear()
        items += element
        notifyDataSetChanged()
    }

    private fun getItem(position: Int) = items[position]
}