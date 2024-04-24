package mega.privacy.android.core.ui.controls.lists

import mega.privacy.android.icon.pack.R as IconPackR
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import mega.privacy.android.core.ui.controls.controlssliders.MegaRadioButton
import mega.privacy.android.core.ui.controls.dividers.DividerType
import mega.privacy.android.core.ui.controls.dividers.MegaDivider
import mega.privacy.android.core.ui.controls.images.GridThumbnailView
import mega.privacy.android.core.ui.controls.text.LongTextBehaviour
import mega.privacy.android.core.ui.controls.text.MegaText
import mega.privacy.android.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.core.ui.theme.AndroidTheme
import mega.privacy.android.core.ui.theme.MegaTheme
import mega.privacy.android.core.ui.theme.extensions.color_button_brand
import mega.privacy.android.core.ui.theme.tokens.TextColor
import mega.privacy.android.icon.pack.R

/**
 * Node grid view item
 *
 * @param isSelected if the item is selected
 * @param name the name of the item
 * @param thumbnailData the thumbnail data of the item
 * @param duration the duration of the item
 * @param isTakenDown if the item is taken down
 * @param modifier Modifier
 * @param onClick action when the item is clicked
 * @param onLongClick action when the item is long clicked
 * @param onMenuClick action when the item menu is clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NodeGridViewItem(
    isSelected: Boolean,
    name: String,
    @DrawableRes iconRes: Int,
    thumbnailData: Any?,
    isTakenDown: Boolean,
    modifier: Modifier = Modifier,
    duration: String? = null,
    isFolderNode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isSelected) MegaTheme.colors.border.subtleSelected
                else MegaTheme.colors.border.subtle,
                shape = RoundedCornerShape(5.dp),
            )
            .background(MegaTheme.colors.background.pageBackground)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() },
            )
    ) {
        if (isFolderNode.not()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                GridThumbnailView(
                    data = thumbnailData,
                    defaultImage = iconRes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .testTag(THUMBNAIL_FILE_TEST_TAG),
                    contentDescription = name,
                    contentScale = ContentScale.Crop
                )

                // Video/Audio duration
                if (duration != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(MegaTheme.colors.background.blur)
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp)
                                .testTag(VIDEO_PLAY_ICON_TEST_TAG),
                            painter = painterResource(id = R.drawable.ic_play_medium_regular_solid),
                            tint = MegaTheme.colors.icon.onColor,
                            contentDescription = "Taken Down",
                        )
                        MegaText(
                            text = duration,
                            style = MaterialTheme.typography.body2,
                            textColor = TextColor.OnColor,
                            modifier = Modifier.testTag(VIDEO_DURATION_TEST_TAG),
                        )
                    }

                }
            }
            MegaDivider(dividerType = DividerType.FullSize)
        }
        Footer(
            isFolderNode = isFolderNode,
            iconRes = iconRes,
            name = name,
            isTakenDown = isTakenDown,
            onMenuClick = onMenuClick,
            isSelected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
private fun Footer(
    isFolderNode: Boolean,
    iconRes: Int,
    name: String,
    isTakenDown: Boolean,
    onMenuClick: (() -> Unit)?,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (isFolderNode) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "Folder",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(24.dp)
                    .testTag(FOLDER_VIEW_ICON_TEST_TAG),
            )
        }
        MegaText(
            text = name,
            textColor = if (isTakenDown) TextColor.Error else TextColor.Primary,
            style = MaterialTheme.typography.body2,
            overflow = LongTextBehaviour.MiddleEllipsis,
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
                .testTag(NODE_TITLE_TEXT_TEST_TAG),
        )
        // Taken down
        if (isTakenDown) {
            Image(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp)
                    .testTag(GRID_VIEW_TAKEN_TEST_TAG),
                painter = painterResource(id = R.drawable.ic_alert_triangle_medium_regular_outline),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.color_button_brand),
                contentDescription = "Taken Down",
            )
        }
        if (onMenuClick != null) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(id = IconPackR.drawable.ic_more_vertical_medium_regular_outline),
                    tint = MegaTheme.colors.icon.secondary,
                    contentDescription = "More",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onMenuClick() }
                        .testTag(GRID_VIEW_MORE_ICON_TEST_TAG)
                )
            }
        } else {
            MegaRadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.testTag(GRID_VIEW_RADIO_SELECTION_TEST_TAG),
            )
        }
    }
}


@CombinedThemePreviews
@Composable
private fun NodeGridViewItemPreview(
    @PreviewParameter(NodeGridViewItemDataProvider::class) data: NodeGridViewItemData,
) {
    AndroidTheme(isDark = isSystemInDarkTheme()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {

            listOf(false, true).forEach { selectionMode ->
                item {
                    var isSelected by remember {
                        mutableStateOf(false)
                    }
                    NodeGridViewItem(
                        isSelected = isSelected,
                        name = data.name,
                        iconRes = data.iconRes,
                        thumbnailData = data.thumbnailData,
                        isTakenDown = data.isTakenDown,
                        onClick = {
                            isSelected = isSelected.not()
                        },
                        onMenuClick = { }.takeIf { selectionMode.not() },
                        isFolderNode = data.isFolderNode,
                        duration = data.duration
                    )
                }
            }
        }
    }
}

private data class NodeGridViewItemData(
    val name: String,
    val thumbnailData: Any?,
    val duration: String?,
    val isTakenDown: Boolean,
    val iconRes: Int,
    val isFolderNode: Boolean,
)

private class NodeGridViewItemDataProvider : PreviewParameterProvider<NodeGridViewItemData> {
    override val values = sequenceOf(
        NodeGridViewItemData(
            name = "NodeGridViewItem",
            thumbnailData = null,
            duration = null,
            isTakenDown = false,
            iconRes = IconPackR.drawable.ic_folder_backup_medium_solid,
            isFolderNode = true
        ),
        NodeGridViewItemData(
            name = "NodeGridViewItem1",
            thumbnailData = null,
            duration = null,
            isTakenDown = false,
            iconRes = IconPackR.drawable.ic_photoshop_medium_solid,
            isFolderNode = false
        ),
        NodeGridViewItemData(
            name = "NodeGridViewItem2",
            thumbnailData = "https://mega.io/wp-content/themes/megapages/megalib/images/megaicon.svg",
            duration = null,
            isTakenDown = false,
            iconRes = IconPackR.drawable.ic_generic_medium_solid,
            isFolderNode = false
        ),
        NodeGridViewItemData(
            name = "NodeGridViewItem3",
            thumbnailData = null,
            duration = "12:3",
            isTakenDown = false,
            iconRes = IconPackR.drawable.ic_video_medium_solid,
            isFolderNode = false
        ),
        NodeGridViewItemData(
            name = "NodeGridViewItem4",
            thumbnailData = "https://mega.io/wp-content/themes/megapages/megalib/images/megaicon.svg",
            duration = "12:3",
            isTakenDown = true,
            iconRes = IconPackR.drawable.ic_audio_medium_solid,
            isFolderNode = false
        )
    )
}


/**
 * Test tag for node title
 */
const val NODE_TITLE_TEXT_TEST_TAG = "node_grid_view_item:node_title"

/**
 * Text tag for selected item
 */
const val GRID_VIEW_RADIO_SELECTION_TEST_TAG = "node_grid_view_item:node_selected"


/**
 * Test tag for file item
 */
const val THUMBNAIL_FILE_TEST_TAG = "node_grid_view_item:thumbnail_file"


/**
 * Test tag for taken item for folder
 */
const val GRID_VIEW_TAKEN_TEST_TAG = "node_grid_view_item:grid_view_icon_taken"

/**
 * Test tag for more icon
 */
const val GRID_VIEW_MORE_ICON_TEST_TAG = "node_grid_view_item:grid_view_more_icon"


/**
 * Test tag for video play icon
 */
const val VIDEO_PLAY_ICON_TEST_TAG = "node_grid_view_item:video_play_icon"

/**
 * Test tag for video duration
 */
const val VIDEO_DURATION_TEST_TAG = "node_grid_view_item:video_duration"

/**
 * Folder view thumbnail icon test tag
 */
const val FOLDER_VIEW_ICON_TEST_TAG = "node_grid_view_item:folder_view_icon"

