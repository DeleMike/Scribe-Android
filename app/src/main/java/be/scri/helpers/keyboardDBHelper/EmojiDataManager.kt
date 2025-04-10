// SPDX-License-Identifier: GPL-3.0-or-later

/**
 * A helper class to manage emoji keywords by querying an SQLite database based on the specified language.
 */

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class EmojiDataManager(
    private val context: Context,
) {
    // Track max keyword length.
    var maxKeywordLength = 0

    fun getEmojiKeywords(language: String): HashMap<String, MutableList<String>> {
        val dbFile = context.getDatabasePath("${language}LanguageData.sqlite")
        return processEmojiKeywords(dbFile.path)
    }

    private fun processEmojiKeywords(dbPath: String): HashMap<String, MutableList<String>> {
        val hashMap = HashMap<String, MutableList<String>>()

        SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            // Get max keyword length.
            db.rawQuery("SELECT MAX(LENGTH(word)) FROM emoji_keywords", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    maxKeywordLength = cursor.getInt(0)
                }
            }

            // Keyword processing.
            db.rawQuery("SELECT * FROM emoji_keywords", null).use { cursor ->
                processEmojiCursor(cursor, hashMap)
            }
        }
        return hashMap
    }

    private fun processEmojiCursor(
        cursor: Cursor,
        hashMap: HashMap<String, MutableList<String>>,
    ) {
        if (!cursor.moveToFirst()) return

        do {
            val key = cursor.getString(0)
            hashMap[key] = getEmojiKeyMaps(cursor)
        } while (cursor.moveToNext())
    }

    private fun getEmojiKeyMaps(cursor: Cursor): MutableList<String> =
        MutableList(cursor.columnCount - 1) { index ->
            cursor.getString(index + 1)
        }
}
