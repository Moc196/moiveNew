package com.example.data.api

import com.example.data.models.MovieDetailResponse
import com.example.data.models.MoviePaginatedResponse
import com.example.data.models.toDomain

class KKPhimRepositoryImpl(private val apiService: KKPhimApiService) : IMovieDataSource {
    override suspend fun getNewlyUpdatedMovies(page: Int): MoviePaginatedResponse {
        return apiService.getNewlyUpdatedMovies(page).toDomain()
    }

    override suspend fun searchMovies(keyword: String, page: Int): MoviePaginatedResponse {
        return apiService.searchMovies(keyword, page).toDomain()
    }

    override suspend fun getMoviesByGenre(genre: String, page: Int): MoviePaginatedResponse {
        return apiService.getMoviesByGenre(genre, page).toDomain()
    }

    override suspend fun getMoviesByCountry(country: String, page: Int): MoviePaginatedResponse {
        return apiService.getMoviesByCountry(country, page).toDomain()
    }

    override suspend fun getMoviesByYear(year: String, page: Int): MoviePaginatedResponse {
        return apiService.getMoviesByYear(year, page).toDomain()
    }

    override suspend fun getMovieDetail(slug: String): MovieDetailResponse {
        return apiService.getMovieDetail(slug).toDomain()
    }
}
