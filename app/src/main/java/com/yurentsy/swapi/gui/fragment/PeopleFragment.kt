package com.yurentsy.swapi.gui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.yurentsy.swapi.App
import com.yurentsy.swapi.R
import com.yurentsy.swapi.gui.Listener
import com.yurentsy.swapi.gui.adapter.people.PeopleAdapter
import com.yurentsy.swapi.mvp.model.entity.People
import com.yurentsy.swapi.mvp.model.repo.Repo
import com.yurentsy.swapi.mvp.presenter.PeoplePresenter
import com.yurentsy.swapi.mvp.view.IListView
import kotlinx.android.synthetic.main.fragment_list.*
import javax.inject.Inject

class PeopleFragment : Fragment(),
    Listener,
    IListView<People>,
    SwipeRefreshLayout.OnRefreshListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        @JvmStatic
        fun newInstance() = PeopleFragment()

        private const val NAVIGATION_HOME = "navigation_home"
        private const val NAVIGATION_BOOKMARKS = "navigation_bookmarks"
    }

    @Inject
    lateinit var listAdapter: PeopleAdapter

    @Inject
    lateinit var repo: Repo

    private val presenter by lazy { PeoplePresenter(this, repo) }
    private var recyclerViewState: Parcelable? = null
    private var activeNav = NAVIGATION_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getInstance().initPeopleComponent(this).inject(this)

        presenter.load()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        (recycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = listAdapter
        recycler.setHasFixedSize(true)
        swipe.setOnRefreshListener(this)
        bottom_navigation.setOnNavigationItemSelectedListener(this)

        toolbar.setTitle(R.string.fragment_people_toolbar_title)
        toolbar.inflateMenu(R.menu.menu_toolbar)

        val searchViewItem = toolbar.menu.findItem(R.id.action_search)
        val searchView = searchViewItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                newText.takeIf { s -> s.isEmpty() }
                    ?.let {
                        when (activeNav) {
                            NAVIGATION_HOME -> presenter.loadFromCache()
                            NAVIGATION_BOOKMARKS -> presenter.getDataIsFavorite()
                        }
                    }
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                when (activeNav) {
                    NAVIGATION_HOME -> presenter.getDataByTitleSearch(query)
                    NAVIGATION_BOOKMARKS -> presenter.getDataIsFavorite(query)
                }
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        recyclerViewState?.let { state ->
            recycler.layoutManager.onRestoreInstanceState(state)
        }
    }

    override fun onPause() {
        super.onPause()
        recyclerViewState = recycler.layoutManager.onSaveInstanceState()
    }

    override fun onDestroy() {
        presenter.saveDataWasChanged(listAdapter.result)
        super.onDestroy()
    }

    override fun onViewHolderClick(position: Int) {
        val model = listAdapter.result[position]
        val expanded = model.isExpanded
        model.isExpanded = !expanded
        listAdapter.notifyItemChanged(position)
    }

    override fun onViewChickenBoxClick(position: Int, b: Boolean) {
        presenter.updateFlagIsFavorite(listAdapter.result, position, b)
    }

    //по хорошему тут надо создавать новые фрагменты
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                activeNav = NAVIGATION_HOME
                presenter.loadFromCache()
            }
            R.id.navigation_bookmarks -> {
                activeNav = NAVIGATION_BOOKMARKS
                presenter.getDataIsFavorite()
            }
        }
        return true
    }

    override fun onRefresh() {
        when (activeNav) {
            NAVIGATION_HOME -> presenter.loadFromCache()
            NAVIGATION_BOOKMARKS -> presenter.getDataIsFavorite()
        }
    }

    override fun showProgressBar() {
        progress.visibility = ProgressBar.VISIBLE
    }

    override fun hideProgressBar() {
        progress.visibility = ProgressBar.INVISIBLE
    }

    override fun showData(data: MutableList<People>) {
        listAdapter.result = data
        hideSwipe()
    }

    //this is bad
    override fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun hideSwipe() {
        swipe.isRefreshing = false
    }
}