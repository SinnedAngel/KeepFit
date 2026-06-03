package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        DailySteps::class,
        BmiRecord::class,
        CompletedExercise::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KeepFitDatabase : RoomDatabase() {
    abstract fun keepFitDao(): KeepFitDao

    companion object {
        @Volatile
        private var INSTANCE: KeepFitDatabase? = null

        fun getDatabase(context: Context): KeepFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeepFitDatabase::class.java,
                    "keep_fit_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
