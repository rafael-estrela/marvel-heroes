package br.eti.rafaelcouto.marvelheroes.model.general

data class Thumbnail(val path: String, val extension: String) {
    val standardRegular: String
        get() = "$path/standard_medium.$extension"

    val standardLarge: String
        get() = "$path/standard_xlarge.$extension"

    val portraitRegular: String
        get() = "$path/portrait_medium.$extension"
}
