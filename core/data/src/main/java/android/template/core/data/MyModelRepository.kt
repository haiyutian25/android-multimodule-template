package android.template.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import android.template.core.common.AppDispatchers.IO
import android.template.core.common.Dispatcher
import android.template.core.database.MyModelDao
import android.template.core.database.MyModelEntity
import android.template.core.database.toModel
import android.template.core.network.MyModelNetworkDataSource
import javax.inject.Inject

interface MyModelRepository : Syncable {
    val myModels: Flow<List<String>>

    suspend fun add(name: String)
}

class DefaultMyModelRepository @Inject constructor(
    private val myModelDao: MyModelDao,
    private val networkDataSource: MyModelNetworkDataSource,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : MyModelRepository {

    override val myModels: Flow<List<String>> =
        myModelDao.getMyModels().map { items -> items.map { it.toModel().name } }

    override suspend fun add(name: String) {
        myModelDao.insertMyModel(MyModelEntity(name = name))
    }

    /**
     * Synchronizes the local database with the remote source.
     *
     * TODO: Replace the placeholder logic with a real sync strategy, e.g. fetch the changes
     *  since the last sync from [networkDataSource] and upsert them into the local database.
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = suspendRunCatching {
        withContext(ioDispatcher) {
            if (myModelDao.getMyModels().first().isEmpty()) {
                networkDataSource.fetchMyModels().forEach { model ->
                    myModelDao.insertMyModel(MyModelEntity(name = model.name))
                }
            }
        }
    }.isSuccess
}
