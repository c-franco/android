@file:OptIn(ExperimentalFoundationApi::class)

package mega.privacy.android.app.presentation.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.yield
import mega.privacy.android.app.R
import mega.privacy.android.app.imageviewer.ImageViewerViewModel
import mega.privacy.android.app.presentation.extensions.isDarkMode
import mega.privacy.android.app.presentation.slideshow.view.PhotoBox
import mega.privacy.android.app.presentation.slideshow.view.PhotoState
import mega.privacy.android.app.presentation.slideshow.view.rememberPhotoState
import mega.privacy.android.core.ui.theme.AndroidTheme
import mega.privacy.android.core.ui.theme.black
import mega.privacy.android.core.ui.theme.grey_alpha_070
import mega.privacy.android.core.ui.theme.white
import mega.privacy.android.core.ui.theme.white_alpha_070
import mega.privacy.android.domain.entity.ThemeMode
import mega.privacy.android.domain.entity.photos.Photo
import mega.privacy.android.domain.entity.slideshow.SlideshowOrder
import mega.privacy.android.domain.entity.slideshow.SlideshowSpeed
import mega.privacy.android.domain.usecase.GetThemeMode
import timber.log.Timber
import javax.inject.Inject

/**
 * Slideshow fragment
 */
@AndroidEntryPoint
class SlideshowFragment : Fragment() {
    private val slideshowViewModel: SlideshowViewModel by viewModels()
    private val imageViewerViewModel: ImageViewerViewModel by activityViewModels()

    @Inject
    lateinit var getThemeMode: GetThemeMode

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val mode by getThemeMode()
                    .collectAsStateWithLifecycle(initialValue = ThemeMode.System)
                AndroidTheme(isDark = mode.isDarkMode()) {
                    SlideshowBody()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        setupMenu()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.fragment_image_slideshow, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_options -> {
                        getNavController()?.navigate(
                            SlideshowFragmentDirections.actionNewSlideshowToSlideshowSettings()
                        )
                        true
                    }
                    else -> true
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun getNavController(): NavController? =
        activity?.let {
            it.supportFragmentManager
                .findFragmentById(R.id.images_nav_host_fragment) as NavHostFragment
        }?.navController

    private fun setupObserver() {
        imageViewerViewModel.images.observe(viewLifecycleOwner) { items ->
            items?.let {
                if (it.isNotEmpty()) {
                    slideshowViewModel.setData(it)
                }
            }
        }
    }

    @Composable
    private fun SlideshowBody() {
        val slideshowViewState by slideshowViewModel.state.collectAsStateWithLifecycle()
        val scrollState = rememberScaffoldState()
        val pagerState = rememberPagerState(
            initialPage = 0
        )
        val items = slideshowViewState.items
        val order = slideshowViewState.order ?: SlideshowOrder.Shuffle
        val speed = slideshowViewState.speed ?: SlideshowSpeed.Normal
        val repeat = slideshowViewState.repeat
        var isPlaying by remember {
            mutableStateOf(true)
        }

        var showBottomPanel by remember {
            mutableStateOf(true)
        }

        val playItems = remember(items, order) {
            when (order) {
                SlideshowOrder.Shuffle -> items.shuffled()
                SlideshowOrder.Newest -> items.sortedByDescending { it.modificationTime }
                SlideshowOrder.Oldest -> items.sortedBy { it.modificationTime }
            }
        }

        val photoState = rememberPhotoState()
        LaunchedEffect(repeat, isPlaying) {
            if (isPlaying) {
                showBottomPanel = false
                imageViewerViewModel.showToolbar(showBottomPanel)
                while (true) {
                    yield()
                    delay(speed.duration * 1000L)
                    tween<Float>(600)
                    if (isPlaying) {
                        pagerState.animateScrollToPage(
                            page = if (pagerState.canScrollForward) {
                                pagerState.currentPage + 1
                            } else {
                                0
                            },
                        )
                    }
                }
            } else {
                showBottomPanel = true
                imageViewerViewModel.showToolbar(showBottomPanel)
            }
        }

        LaunchedEffect(Unit) {
            // When order change, restart slideshow
            snapshotFlow { order }.distinctUntilChanged().collect {
                pagerState.animateScrollToPage(0)
            }
        }

        LaunchedEffect(pagerState.canScrollForward) {
            // Not repeat and the last one.
            isPlaying = !(repeat.not() && pagerState.canScrollForward.not())
        }

        LaunchedEffect(pagerState.currentPage) {
            // When move to next, reset scale
            photoState.resetScale()
        }

        LaunchedEffect(photoState.isScaled) {
            // Observe if scaling, then pause slideshow
            if (photoState.isScaled) {
                isPlaying = false
            }
        }

        SlideshowCompose(
            scrollState = scrollState,
            playItems = playItems,
            pagerState = pagerState,
            photoState = photoState,
            isPlaying = isPlaying,
            showBottomPanel = showBottomPanel,
            onPlayIconClick = {
                isPlaying = !isPlaying
            },
            onImageTap = {
                isPlaying = false
            }
        )
    }

    @Composable
    private fun SlideshowCompose(
        scrollState: ScaffoldState = rememberScaffoldState(),
        playItems: List<Photo>,
        pagerState: PagerState,
        photoState: PhotoState,
        isPlaying: Boolean,
        showBottomPanel: Boolean,
        onPlayIconClick: () -> Unit,
        onImageTap: ((Offset) -> Unit),
    ) {
        Scaffold(
            scaffoldState = scrollState,
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxSize(),
                    pageCount = playItems.size,
                    state = pagerState,
                    beyondBoundsPageCount = 5,
                    key = { playItems[it].id }
                ) { index ->

                    val photo = playItems[index]
                    val imageState =
                        produceState<String?>(initialValue = null) {
                            runCatching {
                                slideshowViewModel.downloadFullSizeImage(
                                    nodeHandle = photo.id
                                ).collectLatest { imageResult ->
                                    value = imageResult.getHighestResolutionAvailableUri()
                                }
                            }.onFailure { exception ->
                                Timber.e(exception)
                            }
                        }

                    PhotoBox(
                        modifier = Modifier.fillMaxSize(),
                        state = photoState,
                        onTap = onImageTap
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageState.value)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (showBottomPanel) {
                    Row(
                        modifier = Modifier
                            .height(72.dp)
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                if (MaterialTheme.colors.isLight)
                                    white_alpha_070
                                else
                                    grey_alpha_070
                            ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPlayIconClick) {
                            Icon(
                                painter = if (isPlaying)
                                    painterResource(id = R.drawable.ic_pause)
                                else
                                    painterResource(id = R.drawable.ic_play),
                                contentDescription = null,
                                modifier = Modifier,
                                tint = if (MaterialTheme.colors.isLight)
                                    black
                                else
                                    white,
                            )
                        }
                    }
                }
            }
        }
    }
}

