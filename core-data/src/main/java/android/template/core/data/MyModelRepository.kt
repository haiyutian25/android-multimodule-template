package android.template.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import android.template.core.database.MyModelDao
import android.template.core.database.MyModelEntity
import android.template.core.database.toModel
import javax.inject.Inject

interface MyModelRepository {
    val myModels: Flow<List<String>>

    suspend fun add(name: String)
}

class DefaultMyModelRepository @Inject constructor(
    private val myModelDao: MyModelDao
) : MyModelRepository {

    override val myModels: Flow<List<String>> =
        myModelDao.getMyModels().map { items -> items.map { it.toModel().name } }

    override suspend fun add(name: String) {
        myModelDao.insertMyModel(MyModelEntity(name = name))
    }
}
