package com.example.data.api

import com.example.data.models.KKPhimDetailResponse
import com.example.data.models.KKPhimPaginatedResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KKPhimApiService {

    @GET("danh-sach/phim-moi-cap-nhat")
    suspend fun getNewlyUpdatedMovies(
        @Query("page") page: Int
    ): KKPhimPaginatedResponse

    @GET("v1/api/tim-kiem")
    suspend fun searchMovies(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1
    ): KKPhimPaginatedResponse

    @GET("v1/api/the-loai/{genre}")
    suspend fun getMoviesByGenre(
        @Path("genre") genre: String,
        @Query("page") page: Int = 1
    ): KKPhimPaginatedResponse

    @GET("v1/api/quoc-gia/{country}")
    suspend fun getMoviesByCountry(
        @Path("country") country: String,
        @Query("page") page: Int = 1
    ): KKPhimPaginatedResponse

    @GET("v1/api/danh-sach/phim-le")
    suspend fun getMoviesByYear(
        // Note: KKPhim API might not have a direct year endpoint, so we fallback to a general list for testing
        @Query("year") year: String,
        @Query("page") page: Int = 1
    ): KKPhimPaginatedResponse

    @GET("phim/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): KKPhimDetailResponse
}
