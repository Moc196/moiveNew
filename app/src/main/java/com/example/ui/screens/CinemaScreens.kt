package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FavoriteMovie
import com.example.data.local.WatchHistory
import com.example.data.models.EpisodeItem
import com.example.data.models.MovieItem
import com.example.ui.components.EmbedVideoPlayer
import com.example.ui.viewmodel.MovieViewModel
import com.example.ui.viewmodel.PrimaryFilter
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- REUSABLE MOVIE CARD ---
@Composable
fun MovieCard(
    name: String,
    posterUrl: String?,
    quality: String?,
    episodeText: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .width(135.dp)
            .height(235.dp)
            .clickable(onClick = onClick)
            .testTag("movie_card_${name.replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Poster Image
            AsyncImage(
                model = posterUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            // Dynamic quality badge
            if (!quality.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = quality,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Episode text badge
            if (!episodeText.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = episodeText,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Text section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- EXPLORE / HOME SCREEN ---
@Composable
fun HomeScreenContent(
    viewModel: MovieViewModel,
    onMovieClick: (String) -> Unit
) {
    val newlyUpdated by viewModel.newlyUpdatedMovies.collectAsState()
    val featuredMovies by viewModel.featuredMovies.collectAsState()
    val actionMovies by viewModel.actionMovies.collectAsState()
    val cartoons by viewModel.cartoons.collectAsState()
    val romanceMovies by viewModel.romanceMovies.collectAsState()
    val homeState by viewModel.homeState.collectAsState()
    val isMoreLoading by viewModel.isMoreHomeLoading.collectAsState()

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Setup infinite scroll loading
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= lazyListState.layoutInfo.totalItemsCount - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadMoreHomeMovies()
        }
    }

    when (val state = homeState) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadHomeData() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Tải lại", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
        is UiState.Success -> {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Feature Banner
                if (featuredMovies.isNotEmpty()) {
                    item {
                        FeaturedCarousel(featuredMovies, onMovieClick)
                    }
                }

                // Category row: Phim Hành động
                if (actionMovies.isNotEmpty()) {
                    item {
                        MovieCategoryRow(
                            title = "Hành Động Kịch Tính 🔥",
                            movies = actionMovies,
                            onMovieClick = onMovieClick
                        )
                    }
                }

                // Category row: Phim Hoạt hình
                if (cartoons.isNotEmpty()) {
                    item {
                        MovieCategoryRow(
                            title = "Hoạt Hình & Anime 🌸",
                            movies = cartoons,
                            onMovieClick = onMovieClick
                        )
                    }
                }

                // Category row: Phim Tình Cảm
                if (romanceMovies.isNotEmpty()) {
                    item {
                        MovieCategoryRow(
                            title = "Tình Cảm Lãng Mạn 💕",
                            movies = romanceMovies,
                            onMovieClick = onMovieClick
                        )
                    }
                }

                // Grid Head of Updated Movies
                item {
                    Text(
                        text = "Mới Cập Nhật ✨",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 12.dp)
                    )
                }

                // Newly updated grid items in linear format
                val chunkedItems = newlyUpdated.chunked(2)
                items(chunkedItems) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (item in rowItems) {
                            MovieCard(
                                name = item.name,
                                posterUrl = item.posterUrl ?: item.thumbUrl,
                                quality = item.quality,
                                episodeText = item.currentEpisode,
                                modifier = Modifier.weight(1f),
                                onClick = { onMovieClick(item.slug) }
                            )
                        }
                        // Fill spacer if odd row
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (isMoreLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun FeaturedCarousel(
    movies: List<MovieItem>,
    onMovieClick: (String) -> Unit
) {
    var activeIdx by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = movies) {
        while (true) {
            delay(5000)
            activeIdx = (activeIdx + 1) % movies.size
        }
    }

    val activeMovie = movies.getOrNull(activeIdx) ?: return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .clickable { onMovieClick(activeMovie.slug) }
    ) {
        // Banner Image
        AsyncImage(
            model = activeMovie.posterUrl ?: activeMovie.thumbUrl,
            contentDescription = activeMovie.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Mask for cinematic layout overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 150f
                    )
                )
        )

        // Contents
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "NỔI BẬT",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = activeMovie.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!activeMovie.originalName.isNullOrEmpty()) {
                Text(
                    text = activeMovie.originalName,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onMovieClick(activeMovie.slug) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("featured_watch_now")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Xem Ngay", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            movies.forEachIndexed { idx, _ ->
                Box(
                    modifier = Modifier
                        .size(if (idx == activeIdx) 10.dp else 6.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (idx == activeIdx) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
fun MovieCategoryRow(
    title: String,
    movies: List<MovieItem>,
    onMovieClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies) { item ->
                MovieCard(
                    name = item.name,
                    posterUrl = item.posterUrl ?: item.thumbUrl,
                    quality = item.quality,
                    episodeText = item.currentEpisode,
                    onClick = { onMovieClick(item.slug) }
                )
            }
        }
    }
}

// --- SEARCH SCREEN ---
@Composable
fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFFF2B3C) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.LightGray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// --- SEARCH SCREEN ---
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SearchScreenContent(
    viewModel: MovieViewModel,
    onMovieClick: (String) -> Unit
) {
    val primaryFilter by viewModel.primaryFilter.collectAsState()
    val searchQuery = (primaryFilter as? PrimaryFilter.Search)?.query ?: ""
    val searchState by viewModel.searchState.collectAsState()

    val selectedQuality by viewModel.selectedQuality.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val genres = listOf(
        "hanh-dong" to "Hành động",
        "vien-tuong" to "Viễn tưởng",
        "kinh-di" to "Kinh dị",
        "tinh-cam" to "Tình cảm",
        "hoat-hinh" to "Hoạt hình",
        "hai-huoc" to "Hài hước",
        "phieu-luu" to "Phiêu lưu",
        "hinh-su" to "Hình sự",
        "co-trang" to "Cổ trang",
        "kiem-hiep" to "Kiếm hiệp",
        "tam-ly" to "Tâm lý",
        "than-thoai" to "Thần thoại"
    )

    val countries = listOf(
        "han-quoc" to "Hàn Quốc",
        "trung-quoc" to "Trung Quốc",
        "viet-nam" to "Việt Nam",
        "au-my" to "Âu Mỹ",
        "thai-lan" to "Thái Lan",
        "nhat-ban" to "Nhật Bản",
        "an-do" to "Ấn Độ"
    )

    val years = listOf("2024", "2023", "2022", "2021", "2020", "2019", "2018")

    val qualities = listOf("Tất cả", "FullHD", "HD", "SD", "CAM")
    val languages = listOf("Tất cả", "Vietsub", "Thuyết Minh", "Lồng Tiếng", "Phụ Đề")

    // Filter Bottom Sheet
    if (showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Tiêu chí lọc chính (Chọn 1)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Text(text = "Thể loại", color = Color.LightGray, fontSize = 12.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChipItem(
                            text = "Tất cả",
                            isSelected = primaryFilter is PrimaryFilter.None || primaryFilter is PrimaryFilter.Search,
                            onClick = { viewModel.setPrimaryFilter(PrimaryFilter.None) }
                        )
                        genres.forEach { (slug, name) ->
                            val isSel = (primaryFilter as? PrimaryFilter.Genre)?.genreSlug == slug
                            FilterChipItem(
                                text = name,
                                isSelected = isSel,
                                onClick = { viewModel.setPrimaryFilter(PrimaryFilter.Genre(slug)) }
                            )
                        }
                    }
                }

                item {
                    Text(text = "Quốc gia", color = Color.LightGray, fontSize = 12.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        countries.forEach { (slug, name) ->
                            val isSel = (primaryFilter as? PrimaryFilter.Country)?.countrySlug == slug
                            FilterChipItem(
                                text = name,
                                isSelected = isSel,
                                onClick = { viewModel.setPrimaryFilter(PrimaryFilter.Country(slug)) }
                            )
                        }
                    }
                }

                item {
                    Text(text = "Năm phát hành", color = Color.LightGray, fontSize = 12.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        years.forEach { year ->
                            val isSel = (primaryFilter as? PrimaryFilter.Year)?.year == year
                            FilterChipItem(
                                text = year,
                                isSelected = isSel,
                                onClick = { viewModel.setPrimaryFilter(PrimaryFilter.Year(year)) }
                            )
                        }
                    }
                }

                item {
                    Divider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lọc phụ (Tùy chọn)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Text(text = "Định dạng", color = Color.LightGray, fontSize = 12.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        qualities.forEach { q ->
                            val isSel = (q == "Tất cả" && selectedQuality == null) || (q == selectedQuality)
                            FilterChipItem(
                                text = q,
                                isSelected = isSel,
                                onClick = { viewModel.selectQuality(q) }
                            )
                        }
                    }
                }

                item {
                    Text(text = "Ngôn ngữ", color = Color.LightGray, fontSize = 12.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        languages.forEach { l ->
                            val isSel = (l == "Tất cả" && selectedLanguage == null) || (l == selectedLanguage)
                            FilterChipItem(
                                text = l,
                                isSelected = isSel,
                                onClick = { viewModel.selectLanguage(l) }
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp)
    ) {
        // Search bar & Filter Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { 
                    viewModel.setPrimaryFilter(PrimaryFilter.Search(it))
                },
                placeholder = { Text("Tìm kiếm tên phim...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.setPrimaryFilter(PrimaryFilter.None)
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa", tint = Color.Gray)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input_field")
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Filter Button
            IconButton(
                onClick = { showFilterSheet = true },
                modifier = Modifier
                    .size(56.dp) // Match TextField height
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Bộ lọc",
                    tint = if (primaryFilter !is PrimaryFilter.None && primaryFilter !is PrimaryFilter.Search) MaterialTheme.colorScheme.primary else Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val state = searchState) {
            is UiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tìm phim theo tên hoặc nhấn nút Bộ lọc",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF2B3C))
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.White, fontSize = 14.sp)
                }
            }
            is UiState.Success -> {
                val results = state.data
                if (results.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.DarkGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Không tìm thấy bộ phim nào phù hợp.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(results) { item ->
                            MovieCard(
                                name = item.name,
            }
        }
    }
}

// --- BOOKMARKS & HISTORY SCREEN ---
@Composable
fun BookmarkScreenContent(
    viewModel: MovieViewModel,
    onMovieClick: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Bookmarks, 1: History
    val bookmarks by viewModel.favoriteMovies.collectAsState()
    val historyList by viewModel.watchHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab selector row
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Phim Yêu Thích (${bookmarks.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Lịch Sử Xem", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == 0) {
            // Bookmarks view
            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Danh sách yêu thích trống.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(bookmarks) { item ->
                        MovieCard(
                            name = item.name,
                            posterUrl = item.posterUrl ?: item.thumbUrl,
                            quality = item.quality,
                            episodeText = item.currentEpisode,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onMovieClick(item.slug) }
                        )
                    }
                }
            }
        } else {
            // History view
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Chưa có lịch sử xem phim nào.",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Xóa lịch sử", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(historyList) { item ->
                            HistoryRowItem(item = item, onMovieClick = onMovieClick, onDeleteClick = {
                                viewModel.deleteWatchHistoryItem(item.slug)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRowItem(
    item: WatchHistory,
    onMovieClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onMovieClick(item.slug) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.posterUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 60.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Đang xem: ${item.episodeName}",
                    color = Color(0xFFFF851B),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Nguồn: ${item.serverName}",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}

// --- DETAIL & INTEGRATED PLAYER SCREEN ---
@Composable
fun DetailPlayerScreenContent(
    slug: String,
    viewModel: MovieViewModel,
    onBackClick: () -> Unit
) {
    val detailState by viewModel.movieDetailState.collectAsState()
    val selectedServer by viewModel.selectedServer.collectAsState()
    val selectedEpisode by viewModel.selectedEpisode.collectAsState()
    val favoriteList by viewModel.favoriteMovies.collectAsState()

    var isFav by remember { mutableStateOf(false) }
    var useDirectM3u8 by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = slug) {
        viewModel.loadMovieDetail(slug)
    }

    LaunchedEffect(key1 = favoriteList, key2 = slug) {
        isFav = favoriteList.any { it.slug == slug }
    }

    LaunchedEffect(key1 = selectedEpisode) {
        if (selectedEpisode != null) {
            if (selectedEpisode?.embed.isNullOrEmpty() && !selectedEpisode?.m3u8.isNullOrEmpty()) {
                useDirectM3u8 = true
            } else {
                useDirectM3u8 = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = detailState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.message, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Quay lại")
                        }
                    }
                }
            }
            is UiState.Success -> {
                val detailResponse = state.data
                val movie = detailResponse.movie ?: return

                // 1. Integrated WebView Player Header
                val activeEmbedUrl = selectedEpisode?.embed
                val activeM3u8Url = selectedEpisode?.m3u8
                val pipMode = com.example.LocalPipMode.current

                // Prefer selected player format if both available, otherwise fallback
                val streamUrl = if (useDirectM3u8 && !activeM3u8Url.isNullOrEmpty()) {
                    activeM3u8Url
                } else if (!activeEmbedUrl.isNullOrEmpty()) {
                    activeEmbedUrl
                } else {
                    activeM3u8Url ?: ""
                }

                if (streamUrl.isNotEmpty()) {
                    val playerModifier = if (pipMode) {
                        Modifier.fillMaxSize().background(Color.Black)
                    } else {
                        Modifier.fillMaxWidth().aspectRatio(1.77f).background(Color.Black)
                    }
                    
                    Box(
                        modifier = playerModifier
                    ) {
                        EmbedVideoPlayer(
                            url = streamUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    if (!pipMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa có nguồn phát cho tập phim này", color = Color.White)
                        }
                    }
                }

                if (!pipMode) {
                    // Scrollable movie meta and episodes below the video deck
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Back and title header block
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = movie.name,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!movie.originalName.isNullOrEmpty()) {
                                    Text(
                                        text = movie.originalName,
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Favorite toggle button
                            IconButton(
                                onClick = {
                                    val movieItem = MovieItem(
                                        name = movie.name,
                                        slug = movie.slug,
                                        originalName = movie.originalName,
                                        thumbUrl = movie.thumbUrl,
                                        posterUrl = movie.posterUrl,
                                        created = movie.created,
                                        modified = movie.modified,
                                        description = movie.description,
                                        totalEpisodes = movie.totalEpisodes,
                                        currentEpisode = movie.currentEpisode,
                                        time = movie.time,
                                        quality = movie.quality,
                                        language = movie.language,
                                        director = movie.director,
                                        casts = movie.casts
                                    )
                                    viewModel.toggleFavorite(movieItem)
                                },
                                modifier = Modifier.testTag("bookmark_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (isFav) MaterialTheme.colorScheme.primary else Color.White
                                )
                            }
                        }
                    }

                    // Metadata details
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!movie.quality.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(movie.quality, color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!movie.language.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(movie.language, color = Color.White, fontSize = 11.sp)
                                }
                            }
                            if (!movie.time.isNullOrEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(movie.time, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Format Switcher Row
                    if (!activeEmbedUrl.isNullOrEmpty() && !activeM3u8Url.isNullOrEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Chế độ phát video",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Hãy đổi chế độ nếu video lỗi, không chạy",
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val isEmbedSelected = !useDirectM3u8
                                        val isM3u8Selected = useDirectM3u8

                                        // Embed Iframe selector
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isEmbedSelected) MaterialTheme.colorScheme.primary else Color.DarkGray)
                                                .clickable { useDirectM3u8 = false }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Nhúng (Iframe)",
                                                color = if (isEmbedSelected) Color.Black else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Direct HLS selector
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isM3u8Selected) MaterialTheme.colorScheme.primary else Color.DarkGray)
                                                .clickable { useDirectM3u8 = true }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "HLS (m3u8)",
                                                color = if (isM3u8Selected) Color.Black else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Movie Description Box
                    item {
                        if (!movie.description.isNullOrEmpty()) {
                            var isExpanded by remember { mutableStateOf(false) }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = movie.description,
                                    color = Color(0xFFC0C0C8),
                                    fontSize = 13.sp,
                                    maxLines = if (isExpanded) 100 else 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (isExpanded) "Thu gọn ▲" else "Xem thêm ▼",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { isExpanded = !isExpanded }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Servers/Sources selector
                    if (detailResponse.episodes.size > 1) {
                        item {
                            Text(
                                text = "Nguồn Phát (Server)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(detailResponse.episodes) { server ->
                                    val isSelected = selectedServer?.serverName == server.serverName
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectServer(server) },
                                        label = { Text(server.serverName) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            labelColor = Color.LightGray
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Grid of episodes
                    item {
                        Text(
                            text = "Danh Sách Tập Phim",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 18.dp, bottom = 12.dp)
                        )
                    }

                    val epList = selectedServer?.items ?: emptyList()
                    val chunkedEpisodes = epList.chunked(4)

                    items(chunkedEpisodes) { epChunk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (ep in epChunk) {
                                val isSelected = selectedEpisode?.slug == ep.slug
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(45.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                        )
                                        .clickable { viewModel.selectEpisode(ep) }
                                        .testTag("episode_btn_${ep.slug}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ep.name,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.LightGray,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                            // Filler boxes for incomplete row chunks
                            val emptySlots = 4 - epChunk.size
                            for (i in 0 until emptySlots) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Cast and Director meta details
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp, bottom = 80.dp)
                        ) {
                            Divider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            if (!movie.casts.isNullOrEmpty()) {
                                Text(
                                    text = "Diễn viên: ${movie.casts}",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            if (!movie.director.isNullOrEmpty()) {
                                Text(
                                    text = "Đạo diễn: ${movie.director}",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
