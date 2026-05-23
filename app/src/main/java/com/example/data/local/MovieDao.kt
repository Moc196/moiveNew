package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    // --- Bookmarks (Favorites) ---
    @Query("SELECT * FROM favorites ORDER BY bookmarkedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteMovie>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE slug = :slug)")
    fun isFavoriteFlow(slug: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE slug = :slug)")
    suspend fun isFavoriteDirect(slug: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(movie: FavoriteMovie)

    @Query("DELETE FROM favorites WHERE slug = :slug")
    suspend fun removeFavorite(slug: String)


    // --- Watch History ---
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC")
    fun getAllWatchHistory(): Flow<List<WatchHistory>>

    @Query("SELECT * FROM watch_history WHERE slug = :slug LIMIT 1")
    suspend fun getWatchHistoryBySlug(slug: String): WatchHistory?

    @Query("SELECT * FROM watch_history WHERE slug = :slug LIMIT 1")
    fun getWatchHistoryBySlugFlow(slug: String): Flow<WatchHistory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchHistory(history: WatchHistory)

    @Query("DELETE FROM watch_history WHERE slug = :slug")
    suspend fun deleteWatchHistory(slug: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()
}
