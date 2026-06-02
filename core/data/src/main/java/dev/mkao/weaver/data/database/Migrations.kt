package dev.mkao.weaver.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Explicit Room migrations; destructive fallback is never used on upgrade
 * paths.
 *
 * v1 → v2: flattens the legacy Gson-serialized `source` JSON column into
 * `sourceId` / `sourceName` / `sourceUrl` and drops the type converter.
 * Bookmarks are preserved by copying rows into the new table.
 */
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create the new flattened table.
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `Articles_new` (
                    `url` TEXT NOT NULL PRIMARY KEY,
                    `sourceId` TEXT,
                    `sourceName` TEXT,
                    `sourceUrl` TEXT,
                    `author` TEXT,
                    `title` TEXT NOT NULL,
                    `content` TEXT,
                    `description` TEXT,
                    `isBookedMarked` INTEGER NOT NULL DEFAULT 0,
                    `image` TEXT,
                    `publishedAt` TEXT NOT NULL
                )
                """.trimIndent(),
            )

            // Best-effort backfill: the legacy `source` column held a JSON
            // blob that cannot be parsed reliably in SQL, so the bookmark flag
            // and core columns are preserved and the source is nulled
            // (re-fetched on the next refresh).
            db.execSQL(
                """
                INSERT OR REPLACE INTO `Articles_new`
                    (`url`, `sourceId`, `sourceName`, `sourceUrl`, `author`,
                     `title`, `content`, `description`, `isBookedMarked`, `image`, `publishedAt`)
                SELECT `url`, NULL, NULL, NULL, `author`,
                       `title`, `content`, `description`, `isBookedMarked`, `image`, `publishedAt`
                FROM `Articles`
                """.trimIndent(),
            )

            db.execSQL("DROP TABLE `Articles`")
            db.execSQL("ALTER TABLE `Articles_new` RENAME TO `Articles`")
        }
    }

val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `Articles` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'general'")
        }
    }

/**
 * v3 -> v4: adds the recent-search history table (`recent_searches`) and the
 * data-freshness ledger (`table_updates`).
 */
val MIGRATION_3_4 =
    object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `recent_searches` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `query` TEXT NOT NULL,
                    `searchTime` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `table_updates` (
                    `table_name` TEXT NOT NULL PRIMARY KEY,
                    `last_updated` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

/**
 * v4 -> v5: adds the `Videos` table for caching the video feed.
 */
val MIGRATION_4_5 =
    object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `Videos` (
                    `videoId` TEXT NOT NULL PRIMARY KEY,
                    `title` TEXT NOT NULL,
                    `channelName` TEXT NOT NULL,
                    `thumbnailUrl` TEXT NOT NULL,
                    `watchUrl` TEXT NOT NULL,
                    `publishedAt` TEXT NOT NULL,
                    `isPlayable` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }
    }
