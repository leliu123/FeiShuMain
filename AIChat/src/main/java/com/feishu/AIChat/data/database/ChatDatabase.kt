package com.feishu.aichat.data.database
import android.content.Context
import androidx.room.Database
import com.feishu.aichat.data.database.entity.ChatMessageEntity
import androidx.room.Room
import androidx.room.RoomDatabase
import com.feishu.aichat.data.database.Dao.ChatMessageDao

@Database(
    entities = [ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase(){
    abstract fun chatMessageDao(): ChatMessageDao
    companion object{
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDataBase(context: Context): ChatDatabase{
            return INSTANCE?:synchronized(this){
                val instancce= Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_database"

                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE=instancce
                instancce
            }
        }

    }
}

