package android.template.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import android.template.core.model.Greeting

@Entity(tableName = "greeting")
data class GreetingEntity(
    val message: String,
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}

fun GreetingEntity.toModel(): Greeting = Greeting(message = message, uid = uid)

@Dao
interface GreetingDao {
    @Query("SELECT * FROM greeting ORDER BY uid DESC LIMIT 10")
    fun getGreetings(): Flow<List<GreetingEntity>>

    @Insert
    suspend fun insertGreeting(item: GreetingEntity)
}
