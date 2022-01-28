package developer.unam.testpractico.retrofit.movies

import java.io.Serializable

data class Movies(
    val page: Int,
    val results: List<Result>,
    val total_pages: Int,
    val total_results: Int
) : Serializable