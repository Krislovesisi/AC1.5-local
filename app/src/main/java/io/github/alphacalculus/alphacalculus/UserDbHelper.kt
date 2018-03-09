package io.github.alphacalculus.alphacalculus

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(UserModel.SQL_CREATE_ENTRIES)
        db.execSQL(LearningLogDAO.SQL_CREATE_ENTRIES)
        db.execSQL(QuizLogDAO.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

        db.execSQL(UserModel.SQL_DELETE_ENTRIES)
        db.execSQL(LearningLogDAO.SQL_DELETE_ENTRIES)
        db.execSQL(QuizLogDAO.SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 8
        val DATABASE_NAME = "userlog.db"


        private var database: SQLiteDatabase? = null
        fun getDatabase(context: Context): SQLiteDatabase? {
            if (database == null || !database!!.isOpen()) {
                database = UserDbHelper(context).getWritableDatabase()
            }

            return database
        }
    }
}
