package br.eti.rafaelcouto.marvelheroes.network.service

import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.config.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.viewModel.CharactersListViewModel

class CharactersListService(
    private val api: INetworkAPI
) {
    suspend fun loadCharacters(offset: Int): ResponseBody<Character> {
        return api.getPublicCharacters(CharactersListViewModel.CHARACTERS_PER_PAGE, offset)
    }

    suspend fun filterCharacters(offset: Int, name: String): ResponseBody<Character> {
        return api.getPublicCharacters(CharactersListViewModel.CHARACTERS_PER_PAGE, offset, name)
    }
}
