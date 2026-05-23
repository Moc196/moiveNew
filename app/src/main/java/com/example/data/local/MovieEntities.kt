package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteMovie(
    @PrimaryKey val slug: String,
    val name: String,
    val originalName: String?,
    val thumbUrl: String?,
    val posterUrl: String?,
    val quality: String?,
    val language: String?,
    val currentEpisode: String?,
    val bookmarkedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey val slug: String,
    val name: String,
    val posterUrl: String?,
    val episodeName: String, // e.g. "Tập 1"
    val episodeSlug: String, // e.g. "tap-1"
    val serverName: String,  // e.g. "Vietsub #1"
    val m3u8Url: String?,    // Cached stream URL
    val embedUrl: String?,   // Cached embed URL
    val progressMs: Long = 0,
    val durationMs: Long = 0,
    val watchedAt: Long = System.currentTimeMillis()
)
