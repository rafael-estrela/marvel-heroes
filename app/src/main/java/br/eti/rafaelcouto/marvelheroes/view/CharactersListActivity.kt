package br.eti.rafaelcouto.marvelheroes.view

import android.app.SearchManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import br.eti.rafaelcouto.marvelheroes.R
import br.eti.rafaelcouto.marvelheroes.data.State
import br.eti.rafaelcouto.marvelheroes.databinding.ActivityCharactersListBinding
import br.eti.rafaelcouto.marvelheroes.router.CharactersListRouter
import br.eti.rafaelcouto.marvelheroes.view.list.character.CharactersAdapter
import br.eti.rafaelcouto.marvelheroes.view.list.character.OnItemClickListener
import br.eti.rafaelcouto.marvelheroes.viewModel.CharactersListViewModel
import com.google.android.material.snackbar.Snackbar

class CharactersListActivity : AppCompatActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private lateinit var mViewModel: CharactersListViewModel
    private lateinit var binding: ActivityCharactersListBinding

    private val numberOfColumns: Int
        get() = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 3
            else -> 5
        }

    private val mAdapter = CharactersAdapter().apply {
        setOnItemClickListener(object :
            OnItemClickListener {
            override fun onItemClick(position: Int) {
                mViewModel.onCharacterSelected(position)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewModel = CharactersListViewModel(CharactersListRouter(this))

        binding = DataBindingUtil.setContentView(this, R.layout.activity_characters_list)

        binding.apply {
            lifecycleOwner = this@CharactersListActivity
            viewModel = mViewModel
        }

        mViewModel.setup()
        setupRecyclerView()
        observe()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_characters, menu)

        val searchItem = menu?.findItem(R.id.aCharactersList_mFilter)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = searchItem?.actionView as? SearchView

        searchItem?.setOnActionExpandListener(this)

        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setOnQueryTextListener(this@CharactersListActivity)

        return super.onCreateOptionsMenu(menu)
    }

    private fun setupRecyclerView() {
        val context = this

        binding.aCharactersListRvCharacters.apply {
            layoutManager = GridLayoutManager(context, numberOfColumns)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }
    }

    private fun observe() {
        mViewModel.characters.observe(this, Observer { mAdapter.submitList(it) })

        mViewModel.state.observe(this, Observer { state ->
            if (state == State.LOADING) {
                binding.aCharactersListPbLoader.visibility = View.VISIBLE
            } else {
                binding.aCharactersListPbLoader.visibility = View.GONE

                if (state == State.FAILED) {
                    Snackbar.make(binding.root, getString(R.string.default_error), Snackbar.LENGTH_INDEFINITE).apply {
                        setAction(R.string.default_retry) { mViewModel.retry() }
                        show()
                    }
                }
            }
        })
    }

    override fun onQueryTextChange(newText: String?): Boolean = false
    override fun onMenuItemActionExpand(item: MenuItem?): Boolean = true

    override fun onQueryTextSubmit(query: String?): Boolean {
        mViewModel.updateFilterState(query)

        return false
    }

    override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
        mViewModel.updateFilterState(null)

        return true
    }
}
