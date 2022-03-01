package developer.unam.testpractico.view.home.presenter

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import developer.unam.testpractico.retrofit.movies.Result
import developer.unam.testpractico.view.home.IHomeContract
import developer.unam.testpractico.view.home.data.HomeInteractor

class HomePresenter(private val view:IHomeContract.View, private val apiKey:String,private val context: Context):IHomeContract.Presenter {
    private val homeInterector = HomeInteractor(apiKey)

    override fun changePath(path: String) {
        view.showLoader()
        homeInterector.retrieveFavoriteChangePath(object : IHomeContract.CallbackNecesary {
            override fun onResponse(listMoviesShow: List<Result>, statusCode: Int) {
                view.hideLoader()
                Log.e("listShow","list $statusCode ${Gson().toJson(listMoviesShow)}")
                view.showListMovies(listMoviesShow, statusCode)
            }

            override fun onError(statusCode: Int) {
                view.hideLoader()
                view.errorListMovies(statusCode)
            }

        },path,context)
    }


}