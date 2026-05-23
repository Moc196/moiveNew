package com.example.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MoviePaginatedResponse(
    val status: String,
    val paginate: PaginateInfo?,
    val items: List<MovieItem> = emptyList(),
    val cat: CategoryInfo? = null
)

@JsonClass(generateAdapter = true)
data class PaginateInfo(
    @Json(name = "current_page") val currentPage: Int,
    @Json(name = "total_page") val totalPage: Int,
    @Json(name = "total_items") val totalItems: Int,
    @Json(name = "items_per_page") val itemsPerPage: Int
)

@JsonClass(generateAdapter = true)
data class CategoryInfo(
    val name: String,
    val title: String,
    val slug: String
)

@JsonClass(generateAdapter = true)
data class MovieItem(
    val name: String,
    val slug: String,
    @Json(name = "original_name") val originalName: String?,
    @Json(name = "thumb_url") val thumbUrl: String?,
    @Json(name = "poster_url") val posterUrl: String?,
    val created: String?,
    val modified: String?,
    val description: String?,
    @Json(name = "total_episodes") val totalEpisodes: Any?, // Can be Int or String sometimes
    @Json(name = "current_episode") val currentEpisode: String?,
    val time: String?,
    val quality: String?,
    val language: String?,
    val director: String?,
    val casts: String?
)

@JsonClass(generateAdapter = true)
data class MovieDetailResponse(
    val status: String,
    val movie: MovieDetail?
) {
    // Computed property to preserve existing references in ViewModels/Views
    val episodes: List<EpisodeServer>
        get() = movie?.episodes ?: emptyList()
}

@JsonClass(generateAdapter = true)
data class MovieDetail(
    val id: String,
    val name: String,
    val slug: String,
    @Json(name = "original_name") val originalName: String?,
    @Json(name = "thumb_url") val thumbUrl: String?,
    @Json(name = "poster_url") val posterUrl: String?,
    val created: String?,
    val modified: String?,
    val description: String?,
    @Json(name = "total_episodes") val totalEpisodes: Any?,
    @Json(name = "current_episode") val currentEpisode: String?,
    val time: String?,
    val quality: String?,
    val language: String?,
    val director: String?,
    val casts: String?,
    val category: Map<String, CategoryGroup>? = null,
    val episodes: List<EpisodeServer> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CategoryGroup(
    val group: CategoryGroupMeta?,
    val list: List<CategoryGroupItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CategoryGroupMeta(
    val id: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class CategoryGroupItem(
    val id: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class EpisodeServer(
    @Json(name = "server_name") val serverName: String,
    val items: List<EpisodeItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class EpisodeItem(
    val name: String,
    val slug: String,
    val embed: String?,
    val m3u8: String?
)
