package com.example.data.repository

import com.example.data.api.IMovieDataSource
import com.example.data.local.FavoriteMovie
import com.example.data.local.MovieDao
import com.example.data.local.WatchHistory
import com.example.data.models.MovieDetailResponse
import com.example.data.models.MoviePaginatedResponse
import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private var dataSource: IMovieDataSource,
    private val movieDao: MovieDao
) {

    fun setDataSource(newDataSource: IMovieDataSource) {
        this.dataSource = newDataSource
    }

    // --- Network API calls ---
    suspend fun getNewlyUpdatedMovies(page: Int): Result<MoviePaginatedResponse> {
        return runCatching {
            dataSource.getNewlyUpdatedMovies(page)
        }
    }

    suspend fun getMovieDetail(slug: String): Result<MovieDetailResponse> {
        return runCatching {
            dataSource.getMovieDetail(slug)
        }
    }

    suspend fun searchMovies(keyword: String, page: Int = 1): Result<MoviePaginatedResponse> {
        return runCatching {
            dataSource.searchMovies(keyword, page)
        }
    }

    suspend fun getMoviesByGenre(genre: String, page: Int = 1): Result<MoviePaginatedResponse> {
        return runCatching {
            dataSource.getMoviesByGenre(genre, page)
        }
    }

    suspend fun getMoviesByCountry(country: String, page: Int = 1): Result<MoviePaginatedResponse> {
        return runCatching {
            dataSource.getMoviesByCountry(country, page)
        }
    }

    suspend fun getMoviesByYear(year: String, page: Int = 1): Result<MoviePaginatedResponse> {
        return runCatching {
            dataSource.getMoviesByYear(year, page)
        }
    }


    // --- Local Favorites DB actions ---
    fun getAllFavorites(): Flow<List<FavoriteMovie>> = movieDao.getAllFavorites()

    fun isFavoriteFlow(slug: String): Flow<Boolean> = movieDao.isFavoriteFlow(slug)

    suspend fun isFavoriteDirect(slug: String): Boolean = movieDao.isFavoriteDirect(slug)

    suspend fun addFavorite(movie: FavoriteMovie) {
        movieDao.insertFavorite(movie)
    }

    suspend fun removeFavorite(slug: String) {
        movieDao.removeFavorite(slug)
    }


    // --- Local Watch History DB actions ---
    fun getAllWatchHistory(): Flow<List<WatchHistory>> = movieDao.getAllWatchHistory()

    suspend fun getWatchHistoryBySlug(slug: String): WatchHistory? {
        return movieDao.getWatchHistoryBySlug(slug)
    }

    fun getWatchHistoryBySlugFlow(slug: String): Flow<WatchHistory?> {
        return movieDao.getWatchHistoryBySlugFlow(slug)
    }

    suspend fun saveWatchHistory(history: WatchHistory) {
        movieDao.saveWatchHistory(history)
    }

    suspend fun deleteWatchHistory(slug: String) {
        movieDao.deleteWatchHistory(slug)
    }

    suspend fun clearWatchHistory() {
        movieDao.clearWatchHistory()
    }
}
