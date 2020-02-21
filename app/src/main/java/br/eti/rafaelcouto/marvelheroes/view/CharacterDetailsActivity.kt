package br.eti.rafaelcouto.marvelheroes.view

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.databinding.ActivityCharacterDetailsBinding
import br.eti.rafaelcouto.marvelheroes.network.config.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.network.service.CharacterDetailsService
import br.eti.rafaelcouto.marvelheroes.view.list.comic.ComicsAdapter
import br.eti.rafaelcouto.marvelheroes.viewModel.CharacterDetailsViewModel
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

class CharacterDetailsActivity : AppCompatActivity() {
    private lateinit var mViewModel: CharacterDetailsViewModel
    private lateinit var binding: ActivityCharacterDetailsBinding

    private val numberOfColumns: Int
        get() = when(resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 2
            else -> 3
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_character_details)
        mViewModel = CharacterDetailsViewModel(CharacterDetailsService(INetworkAPI.baseApi))

        binding.apply {
            lifecycleOwner = this@CharacterDetailsActivity
            viewModel = mViewModel

            loaderVisibility = Transformations.map(mViewModel.isLoading) {
                if (it) View.VISIBLE else View.GONE
            }
        }

        setupRecyclerView()
        observe()

        mViewModel.loadCharacterInfo(intent.extras)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        overridePendingTransition(R.anim.slide_in_ltr, R.anim.slide_out_ltr)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return item?.itemId.takeIf { it == android.R.id.home }?.let {
            onBackPressed()

            true
        } ?: super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        val context = this

        binding.apply {
            aCharacterDetailsRvComics.apply {
                layoutManager = GridLayoutManager(context, numberOfColumns)
                itemAnimator = DefaultItemAnimator()
                adapter = ComicsAdapter(context, mViewModel.characterComics)
            }

            aCharacterDetailsNsvContent.apply {
                setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                    v.getChildAt(v.childCount - 1)?.let { recyclerView ->
                        mViewModel.shouldPaginate(
                            scrollY,
                            oldScrollY,
                            measuredHeight,
                            recyclerView.measuredHeight
                        ).takeIf { it }?.run {
                            mViewModel.loadCharacterComics()
                        }
                    }
                }
            }
        }
    }

    private fun observe() {
        mViewModel.characterDetails.observe(this, Observer {
            Picasso.with(this)
                .load(it.thumbnail?.standardLarge)
                .into(binding.aCharacterDetailsIvThumb)
        })

        mViewModel.characterComics.observe(this, Observer {
            binding.aCharacterDetailsRvComics.adapter?.notifyDataSetChanged()
        })

        mViewModel.hasError.observe(this, Observer {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).apply {
                setAction(R.string.default_retry) { mViewModel.retry() }
                show()
            }
        })
    }
}
