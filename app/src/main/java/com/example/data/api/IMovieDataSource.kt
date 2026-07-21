package com.example.data.api

import com.example.data.models.MovieDetailResponse
import com.example.data.models.MoviePaginatedResponse

enum class SourceProvider {
    NGUONC,
    KKPHIM
}

interface IMovieDataSource {
    suspend fun getNewlyUpdatedMovies(page: Int): MoviePaginatedResponse
    suspend fun searchMovies(keyword: String, page: Int = 1): MoviePaginatedResponse
    suspend fun getMoviesByGenre(genre: String, page: Int = 1): MoviePaginatedResponse
    suspend fun getMoviesByCountry(country: String, page: Int = 1): MoviePaginatedResponse
    suspend fun getMoviesByYear(year: String, page: Int = 1): MoviePaginatedResponse
    suspend fun getMovieDetail(slug: String): MovieDetailResponse
}
