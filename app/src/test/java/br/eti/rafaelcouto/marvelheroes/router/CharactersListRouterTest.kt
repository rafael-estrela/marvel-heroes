package br.eti.rafaelcouto.marvelheroes.router

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import br.eti.rafaelcouto.marvelheroes.view.CharacterDetailsActivity
import br.eti.rafaelcouto.marvelheroes.viewModel.CharacterDetailsViewModel
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class CharactersListRouterTest {
    // sut
    private lateinit var sut: CharactersListRouter

    // mocks
    private lateinit var activity: AppCompatActivity

    @Before
    fun setUp() {
        this.activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        this.sut = CharactersListRouter(activity)
    }

    @Test
    fun `when go to details requested then should go to details`() {
        val expectedIntent = Intent(activity, CharacterDetailsActivity::class.java)

        sut.proceedToCharacterDetails(100)

        val actualIntent = shadowOf(activity).nextStartedActivity

        assertThat(expectedIntent.filterEquals(actualIntent), equalTo(true))

        assertThat(actualIntent.extras, notNullValue())
        actualIntent.extras?.let {
            assertThat(it.getInt(CharacterDetailsViewModel.CHARACTER_ID_KEY), equalTo(100))
        }
    }
}
