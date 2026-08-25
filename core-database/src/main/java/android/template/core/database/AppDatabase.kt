package android.template.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MyModelEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun myModelDao(): MyModelDao
}
