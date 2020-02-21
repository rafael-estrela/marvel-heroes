package br.eti.rafaelcouto.marvelheroes.model.general

data class DataWrapper<Data>(
    val offset: Int,
    val limit: Int,
    val total: Int,
    val results: List<Data>
)