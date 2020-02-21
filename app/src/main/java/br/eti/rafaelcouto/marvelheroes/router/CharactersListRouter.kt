package br.eti.rafaelcouto.marvelheroes.router

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.view.CharacterDetailsActivity
import br.eti.rafaelcouto.marvelheroes.viewModel.CharacterDetailsViewModel

class CharactersListRouter(private val activity: AppCompatActivity) {
    fun proceedToCharacterDetails(characterId: Int) {
        activity.startActivity(
            Intent(activity, CharacterDetailsActivity::class.java).apply {
                putExtra(CharacterDetailsViewModel.CHARACTER_ID_KEY, characterId)
            }
        )

        activity.overridePendingTransition(R.anim.slide_in_rtl, R.anim.slide_out_rtl)
    }
}