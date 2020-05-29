package br.eti.rafaelcouto.marvelheroes.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.data.State
import br.eti.rafaelcouto.marvelheroes.databinding.ActivityCharacterDetailsBinding
import br.eti.rafaelcouto.marvelheroes.view.list.details.character.CharacterDetailsAdapter
import br.eti.rafaelcouto.marvelheroes.view.list.details.comics.ComicsAdapter
import br.eti.rafaelcouto.marvelheroes.viewModel.CharacterDetailsViewModel
import com.google.android.material.snackbar.Snackbar

class CharacterDetailsActivity : AppCompatActivity() {
    companion object {
        const val CHARACTER_ID_KEY = "characterIdExtra"
    }

    private lateinit var mViewModel: CharacterDetailsViewModel
    private lateinit var binding: ActivityCharacterDetailsBinding

    private val numberOfColumns: Int
        get() = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 2
            else -> 3
        }

    private val comicsAdapter = ComicsAdapter()
    private val detailsAdapter = CharacterDetailsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = CharacterDetailsViewModel()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_character_details)

        binding.apply {
            lifecycleOwner = this@CharacterDetailsActivity
            viewModel = mViewModel
        }

        mViewModel.setup(intent.getIntExtra(CHARACTER_ID_KEY, 0))
        setupRecyclerView()
        observe()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        overridePendingTransition(R.anim.slide_in_ltr, R.anim.slide_out_ltr)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.itemId.takeIf { it == android.R.id.home }?.let {
            onBackPressed()

            true
        } ?: super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        val context = this

        binding.aCharacterDetailsRvContent.apply {
            layoutManager = GridLayoutManager(context, numberOfColumns).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (position == 0) numberOfColumns else 1
                    }
                }
            }

            itemAnimator = DefaultItemAnimator()
            adapter = MergeAdapter(detailsAdapter, comicsAdapter)
        }
    }

    private fun observe() {
        mViewModel.characterDetails.observe(this, Observer {
            detailsAdapter.updateData(it)
        })

        mViewModel.characterComics.observe(this, Observer {
            comicsAdapter.submitList(it)
        })

        mViewModel.state.observe(this, Observer {
            if (it == State.LOADING) {
                binding.aCharacterDetailsPbLoader.visibility = View.VISIBLE
            } else {
                binding.aCharacterDetailsPbLoader.visibility = View.GONE

                if (it == State.FAILED) {
                    Snackbar.make(binding.root, getString(R.string.default_error), Snackbar.LENGTH_INDEFINITE).apply {
                        setAction(R.string.default_retry) { mViewModel.retry() }
                        show()
                    }
                }
            }
        })
    }
}
