package br.eti.rafaelcouto.marvelheroes.model

data class CharacterDetails(
    val description: String,
    @Transient val comics: List<Comic>
) : Character()
