package com.example.jptalusan.imagecomicslister

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

class MyDatabaseOpenHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "ComicBookDatabase", null, 1) {
    companion object {
        private var instance: MyDatabaseOpenHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): MyDatabaseOpenHelper {
            if (instance == null) {
                instance = MyDatabaseOpenHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(comicBooks, ifNotExists = true,
                columns = *arrayOf(
                        "comicBook" to TEXT,
                        "issue" to TEXT,
                        "imageUrl" to TEXT,
                        "releaseDate" to TEXT)
        )

        db.createTable(comicBookTitles, ifNotExists = true,
                columns = *arrayOf(
                        "title" to TEXT,
                        "link" to TEXT
                ))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Here you can upgrade tables, as usual
        db.dropTable(comicBooks, true)
    }
}

// Access property for Context
val Context.database: MyDatabaseOpenHelper
    get() = MyDatabaseOpenHelper.getInstance(applicationContext)

val comicBooks: String
    get() = "AllComicBooks"

val comicBookTitles: String
    get() = "AllTitles"