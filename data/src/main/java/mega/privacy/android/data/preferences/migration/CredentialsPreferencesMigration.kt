package mega.privacy.android.data.preferences.migration

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import mega.privacy.android.data.database.DatabaseHandler
import mega.privacy.android.data.preferences.CredentialsPreferencesDataStore.Companion.migrate
import javax.inject.Inject

internal class CredentialsPreferencesMigration @Inject constructor(
    private val databaseHandler: DatabaseHandler,
) : DataMigration<Preferences> {

    override suspend fun cleanUp() {
        // No-op
    }

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData.asMap().keys.isEmpty()

    override suspend fun migrate(currentData: Preferences): Preferences {
        // it only run if shouldMigrate as true so ephemeral not null
        val credentials = databaseHandler.credentials
        checkNotNull(credentials)
        return currentData.toMutablePreferences().apply {
            migrate(this, credentials)
        }
    }
}