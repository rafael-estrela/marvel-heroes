package br.eti.rafaelcouto.marvelheroes.model

import br.eti.rafaelcouto.marvelheroes.model.general.Thumbnail

open class Character {
    var id: Int = 0
    var name: String = ""
    var thumbnail: Thumbnail? = null

    override fun hashCode(): Int = id
    override fun equals(other: Any?): Boolean = (other as? Character)?.let { it.id == id } ?: false
    override fun toString(): String = "Character(id = $id, name = $name)"
}
