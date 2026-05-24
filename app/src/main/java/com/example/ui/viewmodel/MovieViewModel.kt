package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiClient
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteMovie
import com.example.data.local.WatchHistory
import com.example.data.models.EpisodeItem
import com.example.data.models.EpisodeServer
import com.example.data.models.MovieDetailResponse
import com.example.data.models.MovieItem
import com.example.data.repository.MovieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed class PrimaryFilter {
    data class Search(val query: String) : PrimaryFilter()
    data class Genre(val genreSlug: String) : PrimaryFilter()
    data class Country(val countrySlug: String) : PrimaryFilter()
    data class Year(val year: String) : PrimaryFilter()
    object None : PrimaryFilter()
}

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MovieRepository(ApiClient.service, database.movieDao())

    // --- Home Screen States ---
    private val _newlyUpdatedMovies = MutableStateFlow<List<MovieItem>>(emptyList())
    val newlyUpdatedMovies: StateFlow<List<MovieItem>> = _newlyUpdatedMovies.asStateFlow()

    private val _featuredMovies = MutableStateFlow<List<MovieItem>>(emptyList())
    val featuredMovies: StateFlow<List<MovieItem>> = _featuredMovies.asStateFlow()

    private val _actionMovies = MutableStateFlow<List<MovieItem>>(emptyList())
    val actionMovies: StateFlow<List<MovieItem>> = _actionMovies.asStateFlow()

    private val _cartoons = MutableStateFlow<List<MovieItem>>(emptyList())
    val cartoons: StateFlow<List<MovieItem>> = _cartoons.asStateFlow()

    private val _romanceMovies = MutableStateFlow<List<MovieItem>>(emptyList())
    val romanceMovies: StateFlow<List<MovieItem>> = _romanceMovies.asStateFlow()

    private val _homeState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val homeState: StateFlow<UiState<Unit>> = _homeState.asStateFlow()

    private var currentHomePage = 1
    val isMoreHomeLoading = MutableStateFlow(false)

    // --- Search States ---
    private val _primaryFilter = MutableStateFlow<PrimaryFilter>(PrimaryFilter.None)
    val primaryFilter: StateFlow<PrimaryFilter> = _primaryFilter.asStateFlow()

    private val _selectedQuality = MutableStateFlow<String?>(null)
    val selectedQuality: StateFlow<String?> = _selectedQuality.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<String?>(null)
    val selectedLanguage: StateFlow<String?> = _selectedLanguage.asStateFlow()

    private var rawSearchResults = emptyList<MovieItem>()
    private var currentSearchPage = 1
    val isMoreSearchLoading = MutableStateFlow(false)

    private val _searchState = MutableStateFlow<UiState<List<MovieItem>>>(UiState.Idle)
    val searchState: StateFlow<UiState<List<MovieItem>>> = _searchState.asStateFlow()

    // --- Detail States ---
    private val _movieDetailState = MutableStateFlow<UiState<MovieDetailResponse>>(UiState.Loading)
    val movieDetailState: StateFlow<UiState<MovieDetailResponse>> = _movieDetailState.asStateFlow()

    // --- Player Session States ---
    private val _selectedServer = MutableStateFlow<EpisodeServer?>(null)
    val selectedServer: StateFlow<EpisodeServer?> = _selectedServer.asStateFlow()

    private val _selectedEpisode = MutableStateFlow<EpisodeItem?>(null)
    val selectedEpisode: StateFlow<EpisodeItem?> = _selectedEpisode.asStateFlow()

    // --- Local Database Observation ---
    val favoriteMovies: StateFlow<List<FavoriteMovie>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistory>> = repository.getAllWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _homeState.value = UiState.Loading
            try {
                // Fetch newly updated
                val updateRes = repository.getNewlyUpdatedMovies(1)
                updateRes.onSuccess { response ->
                    _newlyUpdatedMovies.value = response.items
                    // Grab top 5 for featured carousel
                    _featuredMovies.value = response.items.take(5)
                }.onFailure {
                    _homeState.value = UiState.Error("Không thể tải danh sách phim mới: ${it.localizedMessage}")
                    return@launch
                }

                // Fetch Action genre list
                repository.getMoviesByGenre("hanh-dong", 1).onSuccess {
                    _actionMovies.value = it.items
                }

                // Fetch Anime/Cartoon genre list
                repository.getMoviesByGenre("hoat-hinh", 1).onSuccess {
                    _cartoons.value = it.items
                }

                // Fetch Romance genre list
                repository.getMoviesByGenre("tinh-cam", 1).onSuccess {
                    _romanceMovies.value = it.items
                }

                _homeState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _homeState.value = UiState.Error("Có lỗi xảy ra: ${e.localizedMessage}")
            }
        }
    }

    fun loadMoreHomeMovies() {
        if (isMoreHomeLoading.value) return
        viewModelScope.launch {
            isMoreHomeLoading.value = true
            currentHomePage++
            try {
                val response = repository.getNewlyUpdatedMovies(currentHomePage)
                response.onSuccess {
                    if (it.items.isNotEmpty()) {
                        _newlyUpdatedMovies.value = _newlyUpdatedMovies.value + it.items
                    }
                }
            } catch (e: Exception) {
                // Fail silently for load more
                currentHomePage--
            } finally {
                isMoreHomeLoading.value = false
            }
        }
    }

    fun setPrimaryFilter(filter: PrimaryFilter) {
        _primaryFilter.value = filter
        currentSearchPage = 1
        
        // Reset secondary filters when primary filter changes to avoid unexpected empty results
        _selectedQuality.value = null
        _selectedLanguage.value = null
        
        if (filter is PrimaryFilter.None || (filter is PrimaryFilter.Search && filter.query.trim().isEmpty())) {
            rawSearchResults = emptyList()
            _searchState.value = UiState.Idle
            return
        }

        viewModelScope.launch {
            _searchState.value = UiState.Loading
            try {
                val response = when (filter) {
                    is PrimaryFilter.Search -> repository.searchMovies(filter.query, 1)
                    is PrimaryFilter.Genre -> repository.getMoviesByGenre(filter.genreSlug, 1)
                    is PrimaryFilter.Country -> repository.getMoviesByCountry(filter.countrySlug, 1)
                    is PrimaryFilter.Year -> repository.getMoviesByYear(filter.year, 1)
                    else -> null
                }
                
                response?.onSuccess { res ->
                    rawSearchResults = res.items
                    applyClientFilters()
                }?.onFailure {
                    _searchState.value = UiState.Error("Không tìm thấy phim hoặc có lỗi xảy ra: ${it.localizedMessage}")
                }
            } catch (e: Exception) {
                _searchState.value = UiState.Error("Có lỗi xảy ra: ${e.localizedMessage}")
            }
        }
    }

    fun loadMoreSearchMovies() {
        if (isMoreSearchLoading.value) return
        val currentPrimary = _primaryFilter.value
        if (currentPrimary is PrimaryFilter.None || (currentPrimary is PrimaryFilter.Search && currentPrimary.query.trim().isEmpty())) {
            return
        }

        viewModelScope.launch {
            isMoreSearchLoading.value = true
            currentSearchPage++
            try {
                val response = when (currentPrimary) {
                    is PrimaryFilter.Search -> repository.searchMovies(currentPrimary.query, currentSearchPage)
                    is PrimaryFilter.Genre -> repository.getMoviesByGenre(currentPrimary.genreSlug, currentSearchPage)
                    is PrimaryFilter.Country -> repository.getMoviesByCountry(currentPrimary.countrySlug, currentSearchPage)
                    is PrimaryFilter.Year -> repository.getMoviesByYear(currentPrimary.year, currentSearchPage)
                    else -> null
                }
                
                response?.onSuccess { res ->
                    if (res.items.isNotEmpty()) {
                        rawSearchResults = rawSearchResults + res.items
                        applyClientFilters()
                    } else {
                        currentSearchPage--
                    }
                }?.onFailure {
                    currentSearchPage--
                }
            } catch (e: Exception) {
                currentSearchPage--
            } finally {
                isMoreSearchLoading.value = false
            }
        }
    }

    fun selectQuality(quality: String?) {
        _selectedQuality.value = if (quality == "Tất cả") null else quality
        applyClientFilters()
    }

    fun selectLanguage(language: String?) {
        _selectedLanguage.value = if (language == "Tất cả") null else language
        applyClientFilters()
    }

    private fun applyClientFilters() {
        val currentPrimary = _primaryFilter.value
        if (currentPrimary is PrimaryFilter.None || (currentPrimary is PrimaryFilter.Search && currentPrimary.query.trim().isEmpty())) {
            _searchState.value = UiState.Idle
            return
        }

        val qFilter = _selectedQuality.value
        val lFilter = _selectedLanguage.value
        val filtered = rawSearchResults.filter { item ->
            matchesQuality(item, qFilter) && matchesLanguage(item, lFilter)
        }
        _searchState.value = UiState.Success(filtered)
    }

    private fun matchesQuality(item: MovieItem, filter: String?): Boolean {
        if (filter == null || filter == "Tất cả") return true
        val q = item.quality?.uppercase() ?: return false
        val f = filter.uppercase()
        return when (f) {
            "FULLHD" -> q.contains("FULLHD") || q.contains("1080") || q.contains("FHD")
            "HD" -> q.contains("720") || q.contains("HD") && !q.contains("FULLHD") && !q.contains("FHD")
            "SD" -> q.contains("SD") || q.contains("480") || q.contains("360")
            "CAM" -> q.contains("CAM")
            else -> q.contains(f)
        }
    }

    private fun matchesLanguage(item: MovieItem, filter: String?): Boolean {
        if (filter == null || filter == "Tất cả") return true
        val l = item.language?.lowercase() ?: return false
        val f = filter.lowercase()
        return when (f) {
            "vietsub" -> l.contains("sub") || l.contains("việt")
            "thuyết minh" -> l.contains("thuyết") || l.contains("minh") || l.contains("tm")
            "lồng tiếng" -> l.contains("lồng") || l.contains("lt")
            "phụ đề" -> l.contains("phụ") || l.contains("sub")
            else -> l.contains(f)
        }
    }

    fun loadMovieDetail(slug: String) {
        viewModelScope.launch {
            _movieDetailState.value = UiState.Loading
            _selectedEpisode.value = null
            _selectedServer.value = null
            repository.getMovieDetail(slug).onSuccess { response ->
                _movieDetailState.value = UiState.Success(response)
                // Initialize episode/server list if available
                if (response.episodes.isNotEmpty()) {
                    val defaultServer = response.episodes.first()
                    _selectedServer.value = defaultServer
                    if (defaultServer.items.isNotEmpty()) {
                        // Find if we have history for this slug
                        val history = repository.getWatchHistoryBySlug(slug)
                        val lastEpisode = defaultServer.items.find { it.slug == history?.episodeSlug }
                        val epToPlay = lastEpisode ?: defaultServer.items.first()
                        _selectedEpisode.value = epToPlay
                        saveEpisodeToHistory(epToPlay)
                    }
                }
            }.onFailure {
                _movieDetailState.value = UiState.Error("Không thể tải chi tiết phim: ${it.localizedMessage}")
            }
        }
    }

    fun selectServer(server: EpisodeServer) {
        _selectedServer.value = server
        // Keep episode selected or find corresponding
        val currentEp = _selectedEpisode.value
        val matches = server.items.find { it.name == currentEp?.name }
        val epToPlay = matches ?: server.items.firstOrNull()
        _selectedEpisode.value = epToPlay
        if (epToPlay != null) {
            saveEpisodeToHistory(epToPlay)
        }
    }

    fun selectEpisode(episode: EpisodeItem) {
        _selectedEpisode.value = episode
        saveEpisodeToHistory(episode)
    }

    fun selectNextEpisode() {
        val server = _selectedServer.value ?: return
        val currentEp = _selectedEpisode.value ?: return
        val currentIndex = server.items.indexOfFirst { it.slug == currentEp.slug }
        if (currentIndex != -1 && currentIndex + 1 < server.items.size) {
            val nextEp = server.items[currentIndex + 1]
            _selectedEpisode.value = nextEp
            saveEpisodeToHistory(nextEp)
        }
    }

    fun selectPreviousEpisode() {
        val server = _selectedServer.value ?: return
        val currentEp = _selectedEpisode.value ?: return
        val currentIndex = server.items.indexOfFirst { it.slug == currentEp.slug }
        if (currentIndex > 0) {
            val prevEp = server.items[currentIndex - 1]
            _selectedEpisode.value = prevEp
            saveEpisodeToHistory(prevEp)
        }
    }

    fun isFavorite(slug: String): Flow<Boolean> = repository.isFavoriteFlow(slug)

    fun toggleFavorite(movieItem: MovieItem) {
        viewModelScope.launch {
            val isFav = repository.isFavoriteDirect(movieItem.slug)
            if (isFav) {
                repository.removeFavorite(movieItem.slug)
            } else {
                repository.addFavorite(
                    FavoriteMovie(
                        slug = movieItem.slug,
                        name = movieItem.name,
                        originalName = movieItem.originalName,
                        thumbUrl = movieItem.thumbUrl,
                        posterUrl = movieItem.posterUrl,
                        quality = movieItem.quality,
                        language = movieItem.language,
                        currentEpisode = movieItem.currentEpisode
                    )
                )
            }
        }
    }

    private fun saveEpisodeToHistory(episode: EpisodeItem) {
        val state = _movieDetailState.value
        if (state is UiState.Success) {
            val detail = state.data.movie ?: return
            viewModelScope.launch {
                repository.saveWatchHistory(
                    WatchHistory(
                        slug = detail.slug,
                        name = detail.name,
                        posterUrl = detail.posterUrl ?: detail.thumbUrl,
                        episodeName = episode.name,
                        episodeSlug = episode.slug,
                        serverName = _selectedServer.value?.serverName ?: "Nguồn C Stream",
                        m3u8Url = episode.m3u8,
                        embedUrl = episode.embed,
                        watchedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteWatchHistoryItem(slug: String) {
        viewModelScope.launch {
            repository.deleteWatchHistory(slug)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearWatchHistory()
        }
    }
}
