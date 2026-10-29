package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MeshMessageEntity::class,
        SeenMessageEntity::class,
        EmergencyContactEntity::class,
        MeshNodeStatusEntity::class,
        EmergencyCenterEntity::class,
        FamilyMemberEntity::class,
        SupplyExchangeEntity::class,
        MedicalProfileEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun meshNodeDao(): MeshNodeDao
    abstract fun emergencyCenterDao(): EmergencyCenterDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun supplyExchangeDao(): SupplyExchangeDao
    abstract fun medicalProfileDao(): MedicalProfileDao

    companion object {
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mesh_messages ADD COLUMN senderPublicKey TEXT")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sos_mesh.db"
                ).addMigrations(MIGRATION_6_7).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
