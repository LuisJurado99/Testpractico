package developer.unam.testpractico.retrofit.movies

import java.io.Serializable

data class Result(
    val id: Int,
    val title: String,
    val poster_path: String
):Serializable