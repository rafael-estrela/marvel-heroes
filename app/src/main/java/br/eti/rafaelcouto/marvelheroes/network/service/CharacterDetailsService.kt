package br.eti.rafaelcouto.marvelheroes.network.service

import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.config.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.viewModel.CharacterDetailsViewModel
import io.reactivex.Single

class CharacterDetailsService(
    private val api: INetworkAPI
) {
    fun loadCharacterDetails(characterId: Int): Single<ResponseBody<CharacterDetails>> {
        return api.getPublicCharacterInfo(characterId)
    }

    fun loadCharacterComics(characterId: Int, offset: Int): Single<ResponseBody<Comic>> {
        return api.getPublicCharacterComics(
            characterId,
            CharacterDetailsViewModel.COMICS_PER_PAGE,
            offset
        )
    }
}