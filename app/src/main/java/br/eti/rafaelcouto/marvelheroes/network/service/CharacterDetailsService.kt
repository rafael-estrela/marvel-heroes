package br.eti.rafaelcouto.marvelheroes.network.service

import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.config.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.viewModel.CharacterDetailsViewModel

class CharacterDetailsService(
    private val api: INetworkAPI
) {
    suspend fun loadCharacterDetails(characterId: Int): ResponseBody<CharacterDetails> {
        return api.getPublicCharacterInfo(characterId)
    }

    suspend fun loadCharacterComics(characterId: Int, offset: Int): ResponseBody<Comic> {
        return api.getPublicCharacterComics(
            characterId,
            CharacterDetailsViewModel.COMICS_PER_PAGE,
            offset
        )
    }
}
