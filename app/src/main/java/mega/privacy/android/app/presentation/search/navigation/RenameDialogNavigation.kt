package mega.privacy.android.app.presentation.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import mega.privacy.android.app.presentation.node.dialogs.RenameNodeDialog
import mega.privacy.android.app.presentation.node.dialogs.RenameNodeDialogViewModel

internal fun NavGraphBuilder.renameDialogNavigation(
    navHostController: NavHostController,
    renameNodeDialogViewModel: RenameNodeDialogViewModel,
) {
    dialog(
        "$searchRenameDialog/{${searchRenameDialogArgumentNodeId}}",
        arguments = listOf(navArgument(searchRenameDialogArgumentNodeId) {
            type = NavType.LongType
        }),
    ) {
        it.arguments?.getLong(searchRenameDialogArgumentNodeId)?.let { nodeId ->
            RenameNodeDialog(
                nodeId = nodeId,
                onDismiss = {
                    navHostController.popBackStack()
                },
                viewModel = renameNodeDialogViewModel
            )
        }
    }
}

internal const val searchRenameDialog = "search/rename_dialog"
internal const val searchRenameDialogArgumentNodeId = "nodeId"