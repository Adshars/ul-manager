package com.example.apiarymanager.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.apiarymanager.data.local.dao.ApiaryDao
import com.example.apiarymanager.data.local.dao.FeedingDao
import com.example.apiarymanager.data.local.dao.HiveDao
import com.example.apiarymanager.data.local.dao.HivePhotoDao
import com.example.apiarymanager.data.local.dao.HoneyHarvestDao
import com.example.apiarymanager.data.local.dao.InspectionDao
import com.example.apiarymanager.data.local.dao.TaskDao
import com.example.apiarymanager.data.local.dao.TreatmentDao
import com.example.apiarymanager.data.local.entity.ApiaryEntity
import com.example.apiarymanager.data.local.entity.FeedingEntity
import com.example.apiarymanager.data.local.entity.HiveEntity
import com.example.apiarymanager.data.local.entity.HivePhotoEntity
import com.example.apiarymanager.data.local.entity.HoneyHarvestEntity
import com.example.apiarymanager.data.local.entity.InspectionEntity
import com.example.apiarymanager.data.local.entity.TaskEntity
import com.example.apiarymanager.data.local.entity.TreatmentEntity

@Database(
    entities = [
        ApiaryEntity::class,
        HiveEntity::class,
        InspectionEntity::class,
        TaskEntity::class,
        HoneyHarvestEntity::class,
        TreatmentEntity::class,
        FeedingEntity::class,
        HivePhotoEntity::class
    ],
    version = 10,  // v10: removed FK constraints from tasks (race condition on startup)
    exportSchema = false
)
abstract class ApiaryManagerDatabase : RoomDatabase() {
    abstract fun apiaryDao(): ApiaryDao
    abstract fun hiveDao(): HiveDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun taskDao(): TaskDao
    abstract fun honeyHarvestDao(): HoneyHarvestDao
    abstract fun treatmentDao(): TreatmentDao
    abstract fun feedingDao(): FeedingDao
    abstract fun hivePhotoDao(): HivePhotoDao

    companion object {
        const val DATABASE_NAME = "apiary_manager.db"

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hives ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE hives ADD COLUMN longitude REAL")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE inspections ADD COLUMN new_queen_year INTEGER")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `hive_photos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `hive_id` INTEGER NOT NULL,
                        `inspection_id` INTEGER,
                        `local_path` TEXT NOT NULL,
                        `created_at` TEXT NOT NULL,
                        FOREIGN KEY(`hive_id`) REFERENCES `hives`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`inspection_id`) REFERENCES `inspections`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_hive_photos_hive_id` ON `hive_photos` (`hive_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_hive_photos_inspection_id` ON `hive_photos` (`inspection_id`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN email_notification_enabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN notification_at TEXT")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hive_photos ADD COLUMN remote_id INTEGER")
                db.execSQL("ALTER TABLE hive_photos ADD COLUMN upload_status TEXT NOT NULL DEFAULT 'PENDING'")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tasks_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `apiary_id` INTEGER,
                        `hive_id` INTEGER,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `due_date` INTEGER,
                        `priority` TEXT NOT NULL,
                        `is_completed` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `email_notification_enabled` INTEGER NOT NULL DEFAULT 0,
                        `notification_at` TEXT
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `tasks_new`
                    SELECT `id`,`apiary_id`,`hive_id`,`title`,`description`,`due_date`,`priority`,`is_completed`,`created_at`,`email_notification_enabled`,`notification_at`
                    FROM `tasks`
                """.trimIndent())
                db.execSQL("DROP TABLE `tasks`")
                db.execSQL("ALTER TABLE `tasks_new` RENAME TO `tasks`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_apiary_id` ON `tasks` (`apiary_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_hive_id` ON `tasks` (`hive_id`)")
            }
        }
    }
}
