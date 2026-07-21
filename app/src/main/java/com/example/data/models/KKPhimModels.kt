package com.example.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KKPhimPaginatedResponse(
    val status: Boolean?,
    val items: List<KKPhimItem> = emptyList(),
    val pagination: KKPhimPagination?
)

@JsonClass(generateAdapter = true)
data class KKPhimPagination(
    val totalItems: Int,
    val totalItemsPerPage: Int,
    val currentPage: Int,
    val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class KKPhimItem(
    val name: String,
    val slug: String,
    @Json(name = "origin_name") val originName: String?,
    @Json(name = "thumb_url") val thumbUrl: String?,
    @Json(name = "poster_url") val posterUrl: String?,
    val year: Int?
)

@JsonClass(generateAdapter = true)
data class KKPhimDetailResponse(
    val status: Boolean?,
    val msg: String?,
    val movie: KKPhimDetail?,
    val episodes: List<KKPhimEpisodeServer> = emptyList()
)

@JsonClass(generateAdapter = true)
data class KKPhimDetail(
    @Json(name = "_id") val id: String?,
    val name: String,
    val slug: String,
    @Json(name = "origin_name") val originName: String?,
    val content: String?,
    val type: String?,
    val status: String?,
    @Json(name = "thumb_url") val thumbUrl: String?,
    @Json(name = "poster_url") val posterUrl: String?,
    val time: String?,
    @Json(name = "episode_current") val episodeCurrent: String?,
    @Json(name = "episode_total") val episodeTotal: Any?,
    val quality: String?,
    val lang: String?,
    val year: Int?,
    val actor: List<String>?,
    val director: List<String>?,
    val category: List<KKPhimCategory>?,
    val country: List<KKPhimCategory>?
)

@JsonClass(generateAdapter = true)
data class KKPhimCategory(
    val id: String?,
    val name: String,
    val slug: String
)

@JsonClass(generateAdapter = true)
data class KKPhimEpisodeServer(
    @Json(name = "server_name") val serverName: String,
    @Json(name = "server_data") val serverData: List<KKPhimEpisodeItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class KKPhimEpisodeItem(
    val name: String,
    val slug: String,
    val filename: String?,
    @Json(name = "link_embed") val linkEmbed: String?,
    @Json(name = "link_m3u8") val linkM3u8: String?
)

// Helper methods to map KKPhim to Domain models (NguonC models)
fun KKPhimPaginatedResponse.toDomain(): MoviePaginatedResponse {
    return MoviePaginatedResponse(
        status = if (status == true) "success" else "error",
        paginate = pagination?.let {
            PaginateInfo(
                currentPage = it.currentPage,
                totalPage = it.totalPages,
                totalItems = it.totalItems,
                itemsPerPage = it.totalItemsPerPage
            )
        },
        items = items.map { it.toDomain() },
        cat = null
    )
}

fun KKPhimItem.toDomain(): MovieItem {
    return MovieItem(
        name = this.name,
        slug = this.slug,
        originalName = this.originName,
        thumbUrl = this.thumbUrl,
        posterUrl = this.posterUrl,
        created = null,
        modified = null,
        description = null,
        totalEpisodes = null,
        currentEpisode = null,
        time = null,
        quality = null,
        language = null,
        director = null,
        casts = null
    )
}

fun KKPhimDetailResponse.toDomain(): MovieDetailResponse {
    val domainEpisodes = episodes.map { server ->
        EpisodeServer(
            serverName = server.serverName,
            items = server.serverData.map { item ->
                EpisodeItem(
                    name = item.name,
                    slug = item.slug,
                    embed = item.linkEmbed,
                    m3u8 = item.linkM3u8
                )
            }
        )
    }

    val domainMovie = movie?.let { kMovie ->
        MovieDetail(
            id = kMovie.id ?: "",
            name = kMovie.name,
            slug = kMovie.slug,
            originalName = kMovie.originName,
            thumbUrl = kMovie.thumbUrl,
            posterUrl = kMovie.posterUrl,
            created = null,
            modified = null,
            description = kMovie.content,
            totalEpisodes = kMovie.episodeTotal,
            currentEpisode = kMovie.episodeCurrent,
            time = kMovie.time,
            quality = kMovie.quality,
            language = kMovie.lang,
            director = kMovie.director?.joinToString(", "),
            casts = kMovie.actor?.joinToString(", "),
            category = null,
            episodes = domainEpisodes
        )
    }

    return MovieDetailResponse(
        status = if (status == true) "success" else "error",
        movie = domainMovie
    )
}
