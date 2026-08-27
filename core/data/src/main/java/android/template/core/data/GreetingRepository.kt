package android.template.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import android.template.core.common.AppDispatchers.IO
import android.template.core.common.Dispatcher
import android.template.core.database.GreetingDao
import android.template.core.database.GreetingEntity
import android.template.core.database.toModel
import android.template.core.model.Greeting
import android.template.core.network.GreetingNetworkDataSource
import javax.inject.Inject

interface GreetingRepository : Syncable {
    val greetings: Flow<List<Greeting>>
}

class DefaultGreetingRepository @Inject constructor(
    private val greetingDao: GreetingDao,
    private val networkDataSource: GreetingNetworkDataSource,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : GreetingRepository {

    override val greetings: Flow<List<Greeting>> =
        greetingDao.getGreetings().map { items -> items.map { it.toModel() } }

    /**
     * Synchronizes the local database with the remote source.
     *
     * TODO: Replace the placeholder logic with a real sync strategy, e.g. fetch the changes
     *  since the last sync from [networkDataSource] and upsert them into the local database.
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = suspendRunCatching {
        withContext(ioDispatcher) {
            if (greetingDao.getGreetings().first().isEmpty()) {
                networkDataSource.fetchGreetings().forEach { model ->
                    greetingDao.insertGreeting(GreetingEntity(message = model.message))
                }
            }
        }
    }.isSuccess
}
