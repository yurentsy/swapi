package com.yurentsy.swapi.mvp.model.cache

import android.arch.persistence.room.Room
import com.yurentsy.swapi.App
import com.yurentsy.swapi.mvp.model.cache.room.AppDatabase
import com.yurentsy.swapi.mvp.model.entity.Film
import com.yurentsy.swapi.mvp.model.entity.People
import com.yurentsy.swapi.mvp.model.entity.Result
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

class StarWarsCache : Cache {

    private val db = Room.databaseBuilder(App.getInstance(), AppDatabase::class.java, "starwars")
        .allowMainThreadQueries()
        .build()

    override fun getAllFilms(): Observable<Result<Film>> {
        val films = db.filmDao().getAll()
        return Observable.just(Result(films.size, films))
    }

    override fun getAllFilmsSearch(title: String?): Observable<Result<Film>> {
        title?.let {
            title.takeIf { t -> t.isNotEmpty() }?.let { s ->
                val films = db.filmDao().getSearch("%$s%")
                return Observable.just(Result(films.size, films))
            }
        }
        return getAllFilms()
    }

    override fun getAllFilmsFavorite(): Observable<Result<Film>> {
        val films = db.filmDao().getAllFavorite(true)
        return Observable.just(Result(films.size, films))
    }

    override fun putAllFilms(films: Observable<Result<Film>>) {
        films.subscribeOn(Schedulers.io()).subscribe({ res ->
            res.results.forEach { film ->
                db.filmDao().insert(film)
            }
        }, {
            //skip throwable
        })
    }

    override fun putFilm(film: Observable<Film>) {
        film.subscribe({ f ->
            db.filmDao().update(f)
        }, {
            //skip
        })
    }

    override fun updateAllFilms(films: Observable<Result<Film>>) {
        films.subscribe({ res ->
            res.results.forEach { film ->
                db.filmDao().update(film)
            }
        }, {
            //skip throwable
        })
    }

    override fun getAllPeople(): Observable<Result<People>> {
        val people = db.peopleDao().getAll()
        return Observable.just(Result(people.size, people))
    }

    override fun getAllPeopleSearch(name: String?): Observable<Result<People>> {
        name?.let {
            name.takeIf { t -> t.isNotEmpty() }?.let { s ->
                val people = db.peopleDao().getSearch("%$s%")
                return Observable.just(Result(people.size, people))
            }
        }
        return getAllPeople()
    }

    override fun getAllPeopleFavorite(): Observable<Result<People>> {
        val people = db.peopleDao().getAllFavorite(true)
        return Observable.just(Result(people.size, people))
    }

    override fun putAllPeople(people: Observable<Result<People>>) {
        people.subscribeOn(Schedulers.io()).subscribe({ res ->
            res.results.forEach { person ->
                db.peopleDao().insert(person)
            }
        }, {
            //skip throwable
        })
    }

    override fun putPerson(person: Observable<People>) {
        person.subscribe({ p ->
            db.peopleDao().update(p)
        }, {
            //skip
        })
    }

    override fun updateAllPeople(people: Observable<Result<People>>) {
        people.subscribe({ res ->
            res.results.forEach { person ->
                db.peopleDao().update(person)
            }
        }, {
            //skip throwable
        })
    }
}
