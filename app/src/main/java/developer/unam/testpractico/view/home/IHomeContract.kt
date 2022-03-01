package developer.unam.testpractico.view.home

import developer.unam.testpractico.retrofit.movies.Movies
import developer.unam.testpractico.retrofit.movies.Result

interface IHomeContract {
    interface View {
        fun showLoader()
        fun hideLoader()
        fun showListMovies(listMoviesShow: List<Result>, statusCode: Int, totalPages: Int)
        fun errorListMovies(statusCode: Int)
    }

    interface Presenter {
        fun changePath(path:String,page:Int)
    }

    interface CallbackNecesary {
        fun onResponse(listMoviesShow: List<Result>, statusCode: Int,totalPages:Int=1 )
        fun onError(statusCode: Int)
    }
}