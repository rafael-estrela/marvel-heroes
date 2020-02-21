package br.eti.rafaelcouto.marvelheroes.model.general

data class ResponseBody<Data>(
    val code: Int,
    val status: String,
    val data: DataWrapper<Data>
)