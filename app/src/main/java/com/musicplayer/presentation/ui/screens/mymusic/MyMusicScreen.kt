package com.musicplayer.presentation.ui.screens.mymusic

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.musicplayer.R
import com.musicplayer.domain.model.Song
import com.musicplayer.presentation.ui.components.AlbumArtImage
import com.musicplayer.presentation.ui.components.PlaylistPickerDialog
import com.musicplayer.presentation.ui.components.formatDuration
import com.musicplayer.presentation.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MyMusicScreen(
    viewModel: MusicViewModel = hiltViewModel(),
    onNavigateToPlayDetail: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToPlaylist: (Long, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    // songToAddToPlaylist 为 null 表示未显示对话框；为 0L 表示新建歌单模式（无目标歌曲）；> 0 表示添加指定歌曲 ID
    var songToAddToPlaylist by remember { mutableStateOf<Long?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }
    var playlistToDelete by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var playlistToRename by remember { mutableStateOf<Pair<Long, String>?>(null) }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf(
        stringResource(R.string.local_music),
        stringResource(R.string.favorites),
        stringResource(R.string.recently_played),
        stringResource(R.string.playlists)
    )

    // 无权限页面（没有权限时直接显示无权限说明）
    if (!uiState.hasPermission) {
        NoPermissionContent()
        return
    }

    // 等待歌曲数据加载完成
    if (!uiState.hasLoadedSongs) {
        return
    }

    // Pager状态，在Scaffold外部定义以便在TopAppBar中访问
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    Scaffold(
        topBar = {
            if (isSearchVisible) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("搜索歌曲") },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { keyboardController?.hide() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchVisible = false
                            searchQuery = ""
                            keyboardController?.hide()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                LaunchedEffect(isSearchVisible) {
                    if (isSearchVisible) {
                        searchFocusRequester.requestFocus()
                        keyboardController?.show()
                    }
                }
                // 切换到歌单Tab时隐藏搜索框
                LaunchedEffect(pagerState.currentPage) {
                    if (pagerState.currentPage == 3) {
                        isSearchVisible = false
                        searchQuery = ""
                        keyboardController?.hide()
                    }
                }
            } else {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.my_music),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        if (pagerState.currentPage != 3) {
                            IconButton(onClick = { isSearchVisible = true }) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Row 与 Pager 联动
            val coroutineScope = rememberCoroutineScope()

            // 响应从设置页面跳转来的tab切换
            LaunchedEffect(uiState.selectedMyMusicTab) {
                if (uiState.selectedMyMusicTab != pagerState.currentPage) {
                    pagerState.scrollToPage(uiState.selectedMyMusicTab)
                }
            }

            // 每个Tab独立的列表状态，在MyMusicScreen层保持
            val localMusicListState = rememberLazyListState()
            val favoritesListState = rememberLazyListState()
            val recentlyPlayedListState = rememberLazyListState()

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                                viewModel.setSelectedMyMusicTab(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // 根据搜索结果显示
            val filteredSongs = if (searchQuery.isEmpty()) {
                uiState.songs
            } else {
                uiState.songs.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.artist.contains(searchQuery, ignoreCase = true)
                }
            }

            // 构建与显示顺序一致的播放列表（从全部歌曲而非搜索结果构建，保证播放队列完整）
            val allSongsGrouped = uiState.songs.groupBy { it.artist }
            val pinnedArtists = uiState.pinnedArtists.filter { allSongsGrouped.containsKey(it) }.toSet()
            val unpinnedArtists = allSongsGrouped.keys.filter { !pinnedArtists.contains(it) }.sorted()
            val displayOrderSongs: List<Song> = buildList {
                pinnedArtists.forEach { artist ->
                    allSongsGrouped[artist]?.let { addAll(it) }
                }
                unpinnedArtists.forEach { artist ->
                    allSongsGrouped[artist]?.let { addAll(it) }
                }
            }

            // HorizontalPager，所有页面都保持状态
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> LocalMusicTab(
                        songs = filteredSongs,
                        displayOrderSongs = displayOrderSongs,
                        onSongClick = { song, playlist -> viewModel.playSongs(playlist, song) },
                        onFavoriteClick = { songId -> viewModel.toggleFavorite(songId) },
                        onAddToPlaylistClick = { songId -> songToAddToPlaylist = songId },
                        onRemoveFromMusicClick = { song -> songToDelete = song },
                        onTogglePinArtist = { artist -> viewModel.togglePinArtist(artist) },
                        onInsertNextClick = { song -> viewModel.insertNext(song) },
                        onSetRingtoneClick = { viewModel.onSetRingtoneClick(it) },
                        onShareClick = viewModel::onShareClick,
                        uiState = uiState,
                        listState = localMusicListState
                    )
                    1 -> FavoritesTab(
                        songs = filteredSongs,
                        favoriteIds = uiState.favoriteSongIds,
                        onSongClick = { song ->
                            val favoriteSongs = filteredSongs.filter { uiState.favoriteSongIds.contains(it.id) }
                            viewModel.playSongs(favoriteSongs, song, playlistId = "favorites")
                        },
                        onFavoriteClick = { songId -> viewModel.toggleFavorite(songId) },
                        onAddToPlaylistClick = { songId -> songToAddToPlaylist = songId },
                        onInsertNextClick = { song -> viewModel.insertNext(song) },
                        onSetRingtoneClick = { viewModel.onSetRingtoneClick(it) },
                        onShareClick = viewModel::onShareClick,
                        currentPlayingSongId = uiState.playbackState.currentSong?.id,
                        isCurrentlyPlaying = uiState.playbackState.isPlaying,
                        listState = favoritesListState,
                        hasLoadedSongs = uiState.hasLoadedSongs
                    )
                    2 -> RecentlyPlayedTab(
                        songs = filteredSongs,
                        recentSongIds = uiState.recentSongIds,
                        onSongClick = { song ->
                            val allRecentSongs = viewModel.getRecentSongs()
                            val searchFilteredRecent = uiState.recentSongIds.mapNotNull { id -> allRecentSongs.find { it.id == id } }
                            viewModel.playSongsQuietly(searchFilteredRecent, song)
                        },
                        onFavoriteClick = { songId -> viewModel.toggleFavorite(songId) },
                        onAddToPlaylistClick = { songId -> songToAddToPlaylist = songId },
                        onRemoveFromRecentClick = { songId -> viewModel.removeFromRecentPlays(songId) },
                        onInsertNextClick = { song -> viewModel.insertNext(song) },
                        onSetRingtoneClick = { viewModel.onSetRingtoneClick(it) },
                        onShareClick = viewModel::onShareClick,
                        favoriteIds = uiState.favoriteSongIds,
                        currentPlayingSongId = uiState.playbackState.currentSong?.id,
                        isCurrentlyPlaying = uiState.playbackState.isPlaying,
                        listState = recentlyPlayedListState,
                        hasLoadedSongs = uiState.hasLoadedSongs
                    )
                    3 -> PlaylistsTab(
                        playlists = uiState.playlists,
                        onPlaylistClick = { playlistId, playlistName -> onNavigateToPlaylist(playlistId, playlistName) },
                        onCreatePlaylist = { songToAddToPlaylist = -1L },
                        onDeletePlaylist = { playlistId, playlistName -> playlistToDelete = playlistId to playlistName },
                        onRenamePlaylist = { playlistId, playlistName -> playlistToRename = playlistId to playlistName }
                    )
                }
            }
        }
    }

    // 歌单选择/创建统一对话框
    if (songToAddToPlaylist != null) {
        PlaylistPickerDialog(
            playlists = uiState.playlists,
            onDismiss = { songToAddToPlaylist = null },
            onPlaylistSelected = { playlistId ->
                songToAddToPlaylist?.let { songId ->
                    if (songId > 0) {
                        viewModel.addToPlaylist(playlistId, songId)
                    }
                }
                songToAddToPlaylist = null
            },
            onPlaylistCreated = { name ->
                val newId = viewModel.createPlaylist(name)
                songToAddToPlaylist?.let { songId ->
                    if (songId > 0) {
                        viewModel.addToPlaylist(newId, songId)
                    }
                }
                newId
            },
            title = if (songToAddToPlaylist == 0L) "新建歌单" else "添加到歌单",
            checkDuplicate = { name -> viewModel.hasPlaylistWithName(name) }
        )
    }

    // 删除歌单确认对话框
    if (playlistToDelete != null) {
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("删除歌单") },
            text = { Text("确定要删除歌单\"${playlistToDelete!!.second}\"吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        playlistToDelete?.let { (playlistId, _) ->
                            viewModel.deletePlaylist(playlistId)
                        }
                        playlistToDelete = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 重命名歌单对话框
    if (playlistToRename != null) {
        RenamePlaylistDialog(
            currentName = playlistToRename!!.second,
            onDismiss = { playlistToRename = null },
            onRename = { newName ->
                playlistToRename?.let { (playlistId, _) ->
                    viewModel.renamePlaylist(playlistId, newName)
                }
                playlistToRename = null
            }
        )
    }

    // 删除歌曲确认对话框
    if (songToDelete != null) {
        DeleteSongDialog(
            song = songToDelete!!,
            onDismiss = { songToDelete = null },
            onConfirm = { deleteFile ->
                songToDelete?.let { song ->
                    viewModel.deleteSong(song.id, deleteFile)
                }
                songToDelete = null
            }
        )
    }
}

@Composable
fun NoPermissionContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "无权限访问音乐",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "请授予存储和通知权限以完整使用应用",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LocalMusicTab(
    songs: List<Song>,
    displayOrderSongs: List<Song>,
    onSongClick: (Song, List<Song>) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onAddToPlaylistClick: (Long) -> Unit,
    onRemoveFromMusicClick: (Song) -> Unit,
    onTogglePinArtist: (String) -> Unit,
    onInsertNextClick: (Song) -> Unit,
    onSetRingtoneClick: (Long) -> Unit,
    onShareClick: (Song) -> Unit,
    uiState: com.musicplayer.presentation.viewmodel.MusicUiState,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    if (songs.isEmpty()) {
        // 只有真正加载完成后且歌曲为空才显示空页面
        if (uiState.hasLoadedSongs) {
            EmptyMusicContent()
        }
    } else {
        // 按歌手分组
        val groupedSongs = songs.groupBy { it.artist }
        val pinnedArtists = uiState.pinnedArtists.filter { groupedSongs.containsKey(it) }.toSet()
        val unpinnedArtists = groupedSongs.keys.filter { !pinnedArtists.contains(it) }.sorted()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 先显示置顶的歌手
            pinnedArtists.forEach { artist ->
                item(key = "header_$artist") {
                    ArtistHeader(
                        artistName = artist,
                        isPinned = true,
                        onLongClick = { onTogglePinArtist(artist) }
                    )
                }
                groupedSongs[artist]?.forEach { song ->
                    item(key = "song_${song.id}") {
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song, displayOrderSongs) },
                            onFavoriteClick = { onFavoriteClick(song.id) },
                            onAddToPlaylistClick = { onAddToPlaylistClick(song.id) },
                            onRemoveFromMusicClick = { onRemoveFromMusicClick(song) },
                            onInsertNextClick = { onInsertNextClick(song) },
                            onSetRingtoneClick = { onSetRingtoneClick(song.id) },
                            onShareClick = onShareClick,
                            isFavorite = uiState.favoriteSongIds.contains(song.id),
                            isCurrentSong = uiState.playbackState.currentSong?.id == song.id,
                            isCurrentlyPlaying = uiState.playbackState.isPlaying && uiState.playbackState.currentSong?.id == song.id
                        )
                    }
                }
            }
            // 再显示非置顶的歌手
            unpinnedArtists.forEach { artist ->
                item(key = "header_$artist") {
                    ArtistHeader(
                        artistName = artist,
                        isPinned = false,
                        onLongClick = { onTogglePinArtist(artist) }
                    )
                }
                groupedSongs[artist]?.forEach { song ->
                    item(key = "song_${song.id}") {
                        SongListItem(
                            song = song,
                            onClick = { onSongClick(song, displayOrderSongs) },
                            onFavoriteClick = { onFavoriteClick(song.id) },
                            onAddToPlaylistClick = { onAddToPlaylistClick(song.id) },
                            onRemoveFromMusicClick = { onRemoveFromMusicClick(song) },
                            onInsertNextClick = { onInsertNextClick(song) },
                            onSetRingtoneClick = { onSetRingtoneClick(song.id) },
                            onShareClick = onShareClick,
                            isFavorite = uiState.favoriteSongIds.contains(song.id),
                            isCurrentSong = uiState.playbackState.currentSong?.id == song.id,
                            isCurrentlyPlaying = uiState.playbackState.isPlaying && uiState.playbackState.currentSong?.id == song.id
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistHeader(
    artistName: String,
    isPinned: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isPinned) Icons.Default.PushPin else Icons.Default.Person,
            contentDescription = if (isPinned) "已置顶" else null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = artistName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EmptyMusicContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.MusicOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无本地音乐",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "请将音乐文件放入手机存储",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun FavoritesTab(
    songs: List<Song>,
    favoriteIds: Set<Long>,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onAddToPlaylistClick: (Long) -> Unit,
    onInsertNextClick: (Song) -> Unit,
    onSetRingtoneClick: (Long) -> Unit,
    onShareClick: (Song) -> Unit,
    currentPlayingSongId: Long?,
    isCurrentlyPlaying: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    hasLoadedSongs: Boolean = false
) {
    val favoriteSongs = songs.filter { favoriteIds.contains(it.id) }

    if (favoriteSongs.isEmpty()) {
        // 只有真正加载完成后才显示空页面
        if (hasLoadedSongs) {
            EmptyState(
                icon = Icons.Default.FavoriteBorder,
                message = stringResource(R.string.no_favorites)
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            itemsIndexed(favoriteSongs, key = { _, song -> "fav_${song.id}" }) { _, song ->
                SongListItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song.id) },
                    onAddToPlaylistClick = { onAddToPlaylistClick(song.id) },
                    onInsertNextClick = { onInsertNextClick(song) },
                    onSetRingtoneClick = { onSetRingtoneClick(song.id) },
                    onShareClick = onShareClick,
                    isFavorite = true,
                    isCurrentSong = currentPlayingSongId == song.id,
                    isCurrentlyPlaying = isCurrentlyPlaying && currentPlayingSongId == song.id
                )
            }
        }
    }
}

@Composable
fun RecentlyPlayedTab(
    songs: List<Song>,
    recentSongIds: List<Long>,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onAddToPlaylistClick: (Long) -> Unit,
    onRemoveFromRecentClick: (Long) -> Unit,
    onInsertNextClick: (Song) -> Unit,
    onSetRingtoneClick: (Long) -> Unit,
    onShareClick: (Song) -> Unit,
    favoriteIds: Set<Long> = emptySet(),
    currentPlayingSongId: Long?,
    isCurrentlyPlaying: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    hasLoadedSongs: Boolean = false
) {
    // 按最近播放顺序排序，且只显示最近播放中存在的歌曲
    val recentSongs = recentSongIds.mapNotNull { id -> songs.find { it.id == id } }

    if (recentSongs.isEmpty()) {
        // 只有真正加载完成后才显示空页面
        if (hasLoadedSongs) {
            EmptyState(
                icon = Icons.Default.History,
                message = stringResource(R.string.no_recently_played)
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            itemsIndexed(recentSongs, key = { _, song -> "recent_${song.id}" }) { _, song ->
                SongListItem(
                    song = song,
                    onClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song.id) },
                    onAddToPlaylistClick = { onAddToPlaylistClick(song.id) },
                    onRemoveFromRecentClick = { onRemoveFromRecentClick(song.id) },
                    onInsertNextClick = { onInsertNextClick(song) },
                    onSetRingtoneClick = { onSetRingtoneClick(song.id) },
                    onShareClick = onShareClick,
                    isFavorite = favoriteIds.contains(song.id),
                    isCurrentSong = currentPlayingSongId == song.id,
                    isCurrentlyPlaying = isCurrentlyPlaying && currentPlayingSongId == song.id
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistsTab(
    playlists: List<com.musicplayer.data.local.PlaylistEntity>,
    onPlaylistClick: (Long, String) -> Unit,
    onCreatePlaylist: () -> Unit,
    onDeletePlaylist: (Long, String) -> Unit,
    onRenamePlaylist: (Long, String) -> Unit
) {
    Box {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                // 创建歌单按钮
                ListItem(
                    headlineContent = { Text("新建歌单") },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    modifier = Modifier.clickable { onCreatePlaylist() }
                )
            }

            if (playlists.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无歌单，创建一个吧",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(playlists, key = { it.id }) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist.id, playlist.name) },
                        onRenameClick = { onRenamePlaylist(playlist.id, playlist.name) },
                        onDeleteClick = { onDeletePlaylist(playlist.id, playlist.name) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistItem(
    playlist: com.musicplayer.data.local.PlaylistEntity,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var menuOffsetX by remember { mutableFloatStateOf(0f) }
    var itemHeight by remember { mutableFloatStateOf(0f) }

    Box {
        ListItem(
            headlineContent = { Text(playlist.name) },
            supportingContent = { Text("${playlist.songCount} 首歌曲") },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size -> itemHeight = size.height.toFloat() }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { offset ->
                            menuOffsetX = offset.x
                            showMenu = true
                        }
                    )
                }
        )

        if (showMenu) {
            Popup(
                offset = IntOffset(menuOffsetX.toInt(), (itemHeight / 2).toInt()),
                onDismissRequest = { showMenu = false }
            ) {
                Surface(
                    modifier = Modifier.widthIn(max = 160.dp),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        DropdownMenuItem(
                            text = { Text("重命名") },
                            onClick = {
                                showMenu = false
                                onRenameClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    isFavorite: Boolean,
    isCurrentSong: Boolean = false,
    isCurrentlyPlaying: Boolean = false,
    onRemoveFromRecentClick: (() -> Unit)? = null,
    onRemoveFromMusicClick: (() -> Unit)? = null,
    onInsertNextClick: (() -> Unit)? = null,
    onSetRingtoneClick: (() -> Unit)? = null,
    onShareClick: (Song) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var menuOffsetX by remember { mutableFloatStateOf(0f) }
    var itemHeight by remember { mutableFloatStateOf(0f) }

    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .onSizeChanged { size -> itemHeight = size.height.toFloat() }
                .pointerInput(isCurrentSong, isCurrentlyPlaying) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        },
                        onLongPress = { offset ->
                            menuOffsetX = offset.x
                            showMenu = true
                        }
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtImage(
                albumArtUri = song.albumArtUri,
                contentDescription = null,
                size = 52.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} • ${formatDuration(song.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (showMenu) {
            Popup(
                offset = IntOffset(menuOffsetX.toInt(), (itemHeight / 2).toInt()),
                onDismissRequest = { showMenu = false }
            ) {
                Surface(
                    modifier = Modifier.widthIn(max = 200.dp),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        if (onRemoveFromRecentClick != null) {
                            DropdownMenuItem(
                                text = { Text("从最近播放移除") },
                                onClick = {
                                    showMenu = false
                                    onRemoveFromRecentClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                        }
                        if (onRemoveFromMusicClick != null) {
                            DropdownMenuItem(
                                text = { Text("从本地音乐移除") },
                                onClick = {
                                    showMenu = false
                                    onRemoveFromMusicClick()
                                },
                                leadingIcon = { Icon(Icons.Default.PlaylistRemove, contentDescription = null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("下一首播放") },
                            onClick = {
                                showMenu = false
                                onInsertNextClick?.invoke()
                            },
                            leadingIcon = { Icon(Icons.Default.QueueMusic, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("添加到歌单") },
                            onClick = {
                                showMenu = false
                                onAddToPlaylistClick()
                            },
                            leadingIcon = { Icon(Icons.Default.PlaylistAdd, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (isFavorite) "取消收藏" else "收藏") },
                            onClick = {
                                showMenu = false
                                onFavoriteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("分享") },
                            onClick = {
                                showMenu = false
                                onShareClick(song)
                            },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("设为铃声") },
                            onClick = {
                                showMenu = false
                                onSetRingtoneClick?.invoke()
                            },
                            leadingIcon = { Icon(Icons.Default.RingVolume, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteSongDialog(
    song: Song,
    onDismiss: () -> Unit,
    onConfirm: (deleteFile: Boolean) -> Unit
) {
    var deleteFile by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("移除歌曲") },
        text = {
            Column {
                Text("确定要从本地音乐中移除《${song.title}》吗？")
                val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { deleteFile = !deleteFile },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = deleteFile,
                        onClick = null
                    )
                    Text(
                        text = "同时删除本地文件",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(deleteFile) }
            ) {
                Text("移除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun RenamePlaylistDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(currentName)) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
        // Select all text after focus
        kotlinx.coroutines.delay(50)
        textFieldValue = textFieldValue.copy(selection = TextRange(0, currentName.length))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名歌单") },
        text = {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = { Text("歌单名称") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onRename(textFieldValue.text) }
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(textFieldValue.text) },
                enabled = textFieldValue.text.isNotBlank() && textFieldValue.text != currentName
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

