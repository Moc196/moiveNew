package com.example.data.api

import com.example.data.models.MovieDetailResponse
import com.example.data.models.MoviePaginatedResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {

    @GET("films/phim-moi-cap-nhat")
    suspend fun getNewlyUpdatedMovies(
        @Query("page") page: Int
    ): MoviePaginatedResponse

    @GET("film/{slug}")
    suspend fun getMovieDetail(
        @Path("slug") slug: String
    ): MovieDetailResponse

    @GET("films/search")
    suspend fun searchMovies(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1
    ): MoviePaginatedResponse

    @GET("films/the-loai/{genre}")
    suspend fun getMoviesByGenre(
        @Path("genre") genre: String,
        @Query("page") page: Int = 1
    ): MoviePaginatedResponse

    @GET("films/quoc-gia/{country}")
    suspend fun getMoviesByCountry(
        @Path("country") country: String,
        @Query("page") page: Int = 1
    ): MoviePaginatedResponse

    @GET("films/nam-phat-hanh/{year}")
    suspend fun getMoviesByYear(
        @Path("year") year: String,
        @Query("page") page: Int = 1
    ): MoviePaginatedResponse
}

object ApiClient {
    private const val BASE_URL_NGUONC = "https://phim.nguonc.com/api/"
    private const val BASE_URL_KKPHIM = "https://phimapi.com/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // --- NguonC Service ---
    private val nguonCService: MovieApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_NGUONC)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(MovieApiService::class.java)
    }

    // --- KKPhim Service ---
    private val kkPhimService: KKPhimApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_KKPHIM)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(KKPhimApiService::class.java)
    }

    // --- Repositories ---
    val nguonCRepository: IMovieDataSource by lazy {
        NguonCRepositoryImpl(nguonCService)
    }

    val kkPhimRepository: IMovieDataSource by lazy {
        KKPhimRepositoryImpl(kkPhimService)
    }

    // Dùng cái này cho Code cũ chưa chuyển đổi (tạm thời)
    val service: MovieApiService
        get() = nguonCService
}
