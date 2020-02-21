package br.eti.rafaelcouto.marvelheroes.network.service

import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import br.eti.rafaelcouto.marvelheroes.network.config.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.viewModel.CharactersListViewModel
import io.reactivex.Single

class CharactersListService(
    private val api: INetworkAPI
) {
    fun loadCharacters(offset: Int): Single<ResponseBody<Character>> {
        return api.getPublicCharacters(CharactersListViewModel.CHARACTERS_PER_PAGE, offset)
    }
}