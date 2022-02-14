package developer.unam.testpractico.view.home.presenter

import developer.unam.testpractico.retrofit.movies.Result
import developer.unam.testpractico.view.home.IHomeContract
import developer.unam.testpractico.view.home.data.HomeInteractor

class HomePresenter(private val view:IHomeContract.View,private val path:String,private val apiKey:String):IHomeContract.Presenter {
    private val homeInterector = HomeInteractor(apiKey)
    override fun initPresenterActions() {
        view.showLoader()
        homeInterector.retrieveFavoriteInternet(object : IHomeContract.CallbackNecesary {
            override fun onResponse(listMoviesShow: List<Result>, statusCode: Int) {
                view.hideLoader()
                view.showListMovies(listMoviesShow)
            }

            override fun onError(statusCode: Int) {
                view.hideLoader()
                view.errorListMovies(statusCode)
            }

        },path)
    }

    override fun changePath(path: String) {
        view.showLoader()
        homeInterector.retrieveFavoriteChangePath(object : IHomeContract.CallbackNecesary {
            override fun onResponse(listMoviesShow: List<Result>, statusCode: Int) {
                view.hideLoader()
                view.showListMovies(listMoviesShow)
            }

            override fun onError(statusCode: Int) {
                view.hideLoader()
                view.errorListMovies(statusCode)
            }

        },path)
    }


}