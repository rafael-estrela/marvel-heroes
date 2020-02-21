package br.eti.rafaelcouto.marvelheroes.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.databinding.ActivityCharactersListBinding
import br.eti.rafaelcouto.marvelheroes.network.config.INetworkAPI
import br.eti.rafaelcouto.marvelheroes.network.service.CharactersListService
import br.eti.rafaelcouto.marvelheroes.router.CharactersListRouter
import br.eti.rafaelcouto.marvelheroes.view.list.character.CharactersAdapter
import br.eti.rafaelcouto.marvelheroes.view.list.character.OnItemClickListener
import br.eti.rafaelcouto.marvelheroes.viewModel.CharactersListViewModel
import com.google.android.material.snackbar.Snackbar

class CharactersListActivity : AppCompatActivity() {
    private lateinit var mViewModel: CharactersListViewModel
    private lateinit var binding: ActivityCharactersListBinding

    private val numberOfColumns: Int
        get() = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 3
            else -> 5
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_characters_list)
        mViewModel = CharactersListViewModel(
            CharactersListRouter(this),
            CharactersListService(INetworkAPI.baseApi)
        )

        binding.apply {
            lifecycleOwner = this@CharactersListActivity
            viewModel = mViewModel

            loaderVisibility = Transformations.map(mViewModel.isLoading) {
                if (it) View.VISIBLE else View.GONE
            }
        }

        setupRecyclerView()
        observe()

        mViewModel.loadCharacters()
    }

    private fun setupRecyclerView() {
        val context = this

        binding.aCharactersListRvCharacters.apply {
            layoutManager = GridLayoutManager(context, numberOfColumns)
            itemAnimator = DefaultItemAnimator()
            adapter = CharactersAdapter(
                context,
                mViewModel.characters
            ).apply {
                setOnItemClickListener(object :
                    OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        mViewModel.onCharacterSelected(position)
                    }
                })
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val mngr = recyclerView.layoutManager as? GridLayoutManager ?: return

                    mViewModel.shouldPaginate(
                        mngr.childCount,
                        mngr.itemCount,
                        mngr.findFirstVisibleItemPosition(),
                        dy
                    ).takeIf { it }?.run {
                        mViewModel.loadCharacters()
                    }
                }
            })
        }
    }

    private fun observe() {
        mViewModel.characters.observe(this, Observer {
            binding.apply {
                binding.aCharactersListRvCharacters.adapter?.notifyDataSetChanged()
            }
        })

        mViewModel.hasError.observe(this, Observer {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).apply {
                setAction(R.string.default_retry) { mViewModel.loadCharacters() }
                show()
            }
        })
    }
}
