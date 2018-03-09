package io.github.alphacalculus.alphacalculus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class UserModel(context: Context) {


    private val db: SQLiteDatabase

    init {
        db = UserDbHelper.getDatabase(context)!!
    }

    fun auth(username: String, password: String): Boolean {
        val queryArgs = arrayOf(username, sha256(password))
        val user = db.query("user", null,
                "username = ? AND password = ?",
                queryArgs, null, null, null)

        return (user.getCount() > 0);
    }

    fun getUid(username: String): Int {
        val columns = arrayOf("id", "username")
        val queryArgs = arrayOf(username)
        val user = db.query("user",
                columns,
                "username = ?",
                queryArgs, null, null, null)

        if (user.getCount() > 0) {
            user.moveToNext()
            return user.getInt(0)
        } else {
            return 0
        }
    }

    @Throws(Exception::class)
    fun insert(username: String, password: String) {
        val cv = ContentValues()
        cv.put("username", username)
        cv.put("password", password)
        val id = db.insert("user", "", cv)
        if (id <= 0) {
            throw Exception("注册失败")
        }
    }

    @Throws(Exception::class)
    fun register(username: String, password: String) {
        insert(username, sha256(password))
    }

    fun sha256(password: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(password.toByteArray())
            return encodeBytes(md.digest())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return "error"
        }

    }

    /** 二进制转字符串
     * @param b bytes
     * @return
     */
    fun encodeBytes(b: ByteArray): String {
        var hexStr = ""
        for (n in b.indices) {
            val tmp = java.lang.Integer.toHexString(b[n].toInt() and 0xff)
            if (tmp.length === 1)
                hexStr = hexStr + "0" + tmp
            else
                hexStr = hexStr + tmp
            if (n < b.size - 1) hexStr = hexStr + ":"
        }
        return hexStr.toLowerCase()
    }

    companion object {
        val table_name = "user"

        val SQL_CREATE_ENTRIES = "CREATE TABLE ${table_name} (" +
                "ID INTEGER PRIMARY KEY," +
                "USERNAME TEXT UNIQUE," +
                "PASSWORD TEXT)"
        /*
            "CREATE TABLE " + DbContract.UserLogEntry.TABLE_NAME + " (" +
                    DbContract.UserLogEntry._ID + " INTEGER PRIMARY KEY," +
                    DbContract.UserLogEntry.COLUMN_NAME_USERNAME + " TEXT," +
                    DbContract.UserLogEntry.COLUMN_NAME_PARTIDX + " INT," +
                    DbContract.UserLogEntry.COLUMN_NAME_CHAPTERIDX + " INT," +
                    DbContract.UserLogEntry.COLUMN_NAME_LEARNING_TIME + " REAL)"

    };

*/

        val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${table_name}"
    }
}