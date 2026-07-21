package com.example.data.api

import com.example.data.models.MovieDetailResponse
import com.example.data.models.MoviePaginatedResponse

class NguonCRepositoryImpl(private val apiService: MovieApiService) : IMovieDataSource {
    override suspend fun getNewlyUpdatedMovies(page: Int): MoviePaginatedResponse {
        return apiService.getNewlyUpdatedMovies(page)
    }

    override suspend fun searchMovies(keyword: String, page: Int): MoviePaginatedResponse {
        return apiService.searchMovies(keyword, page)
    }

    override suspend fun getMoviesByGenre(genre: String, page: Int): MoviePaginatedResponse {
        return apiService.getMoviesByGenre(genre, page)
    }

    override suspend fun getMoviesByCountry(country: String, page: Int): MoviePaginatedResponse {
        return apiService.getMoviesByCountry(country, page)
    }

    override suspend fun getMoviesByYear(year: String, page: Int): MoviePaginatedResponse {
        return apiService.getMoviesByYear(year, page)
    }

    override suspend fun getMovieDetail(slug: String): MovieDetailResponse {
        return apiService.getMovieDetail(slug)
    }
}
