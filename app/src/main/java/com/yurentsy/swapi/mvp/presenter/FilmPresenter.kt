package com.yurentsy.swapi.mvp.presenter

import com.yurentsy.swapi.mvp.model.entity.Film
import com.yurentsy.swapi.mvp.model.repo.Repo
import com.yurentsy.swapi.mvp.view.IListView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FilmPresenter(val view: IListView<Film>, val repo: Repo) : ListPresenter<Film> {
    lateinit var data: MutableList<Film>

    override fun getDataIsFavorite(search: String?) {
        var tempData = data
        search?.let {
            tempData = tempData.filter { d -> d.title.toLowerCase().contains(search.toLowerCase()) }
                .toMutableList()
        }
        view.showData(tempData.filter { d -> d.isFavorite }
            .toMutableList())
    }

    override fun getDataByTitleSearch(search: String) {
        view.showData(data.filter { d ->
            d.title.toLowerCase()
                .contains(search.toLowerCase())
        }.toMutableList())
    }

    override fun updateFlagIsFavorite(list: MutableList<Film>, position: Int, b: Boolean) {
        data[position].isFavorite = b
        data.find { d -> d.episodeId == list[position].episodeId }
            ?.let { f ->
                f.isFavorite = b
                repo.setFilm(f)
            }
    }

    override fun load(search: String?) {
        repo.getFilmsBySearch(search)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                view.showProgressBar()
            }
            .subscribe({ res ->
                data = res.results
                view.showData(res.results)
                view.hideProgressBar()
                view.hideSwipe()
            }, {
                view.showToast("Error load")
            })
    }

    override fun loadFromCache(search: String?) {
        repo.getFilmsBySearchFromCache(search)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext {
                view.showProgressBar()
            }
            .subscribe({ res ->
                view.showData(res.results)
                view.hideProgressBar()
                view.hideSwipe()
            }, {
                view.showToast("Error load")
            })
    }

    override fun saveDataWasChanged(data: MutableList<Film>) {
        repo.setFilmsHasWasChanged(data)
    }
}