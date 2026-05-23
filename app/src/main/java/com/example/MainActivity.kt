package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.BookmarkScreenContent
import com.example.ui.screens.DetailPlayerScreenContent
import com.example.ui.screens.HomeScreenContent
import com.example.ui.screens.SearchScreenContent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MovieViewModel

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.updater.UpdateManager
import com.example.updater.UpdateInfo
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState

val LocalPipMode = compositionLocalOf { false }

class MainActivity : ComponentActivity() {
    private val isPipMode = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val pipMode by isPipMode.collectAsState()
            CompositionLocalProvider(LocalPipMode provides pipMode) {
                MyApplicationTheme {
                    MainScreen()
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val params = android.app.PictureInPictureParams.Builder().build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isPipMode.value = isInPictureInPictureMode
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: MovieViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val pipMode = LocalPipMode.current

    // Immersive display logic: hide bottom navigation bar on player/detail screen
    val showBottomBar = currentDestination?.route?.startsWith("detail") != true && !pipMode

    // --- Auto Update Logic ---
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val info = UpdateManager.checkUpdate()
        if (info != null) {
            updateInfo = info
        }
    }

    if (updateInfo != null) {
        AlertDialog(
            onDismissRequest = { 
                if (!isDownloading) updateInfo = null 
            },
            title = { Text("Đã có bản cập nhật mới!") },
            text = {
                if (isDownloading) {
                    Column {
                        Text("Đang tải xuống bản cập nhật...", color = Color.Gray)
                        LinearProgressIndicator(
                            progress = downloadProgress / 100f,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text("$downloadProgress%", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    }
                } else {
                    Column {
                        Text("Phiên bản ${updateInfo?.versionName} đã sẵn sàng để tải xuống.", fontWeight = FontWeight.Bold)
                        if (!updateInfo?.releaseNotes.isNullOrEmpty()) {
                            Text("\nCó gì mới:\n${updateInfo?.releaseNotes}", fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (!isDownloading) {
                    TextButton(onClick = {
                        isDownloading = true
                        val coroutineScope = kotlinx.coroutines.GlobalScope
                        coroutineScope.launch {
                            val apkFile = UpdateManager.downloadApk(context, updateInfo!!.apkUrl) { progress ->
                                downloadProgress = progress
                            }
                            if (apkFile != null && apkFile.exists()) {
                                UpdateManager.installApk(context, apkFile)
                            }
                            isDownloading = false
                            updateInfo = null
                        }
                    }) {
                        Text("Cập nhật ngay")
                    }
                }
            },
            dismissButton = {
                if (!isDownloading) {
                    TextButton(onClick = { updateInfo = null }) {
                        Text("Để sau", color = Color.Gray)
                    }
                }
            }
        )
    }
    // --- End Auto Update Logic ---


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("app_navigation_bar")
                ) {
                    val items = listOf(
                        Triple("home", "Khám Phá", Icons.Default.Home),
                        Triple("search", "Tìm Kiếm", Icons.Default.Search),
                        Triple("favorite", "Lưu Trữ", Icons.Default.Favorite)
                    )

                    items.forEach { (route, label, icon) ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { 
                                Text(
                                    text = label, 
                                    fontSize = 11.sp, 
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color(0xFF49454F), // Sleek active background
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_$route")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("home") {
                HomeScreenContent(
                    viewModel = viewModel,
                    onMovieClick = { slug -> navController.navigate("detail/$slug") }
                )
            }
            composable("search") {
                SearchScreenContent(
                    viewModel = viewModel,
                    onMovieClick = { slug -> navController.navigate("detail/$slug") }
                )
            }
            composable("favorite") {
                BookmarkScreenContent(
                    viewModel = viewModel,
                    onMovieClick = { slug -> navController.navigate("detail/$slug") }
                )
            }
            composable(
                route = "detail/{slug}",
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                DetailPlayerScreenContent(
                    slug = slug,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
