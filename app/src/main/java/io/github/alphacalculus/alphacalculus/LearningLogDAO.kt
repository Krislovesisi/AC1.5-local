
package io.github.alphacalculus.alphacalculus

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Parcel
import android.os.Parcelable

class LearningLogDAO(context: Context) {


    private val db: SQLiteDatabase

    init {
        db = UserDbHelper.getDatabase(context)!!
    }

    private val uid get() = TheApp.instance!!.uid

    private fun getCursor(partIdx: Int, chapterIdx: Int, columnName: String)
            = db.query(table_name,
                arrayOf(columnName),
                "user_id = ? AND part_idx = ? AND chapter_idx = ?",
                arrayOf(uid.toString(),
                    partIdx.toString(), chapterIdx.toString()),
                null,
                null,
                null
            )

    fun setLogPoint(chapterItem: ChapterItem,msec:Int) {
        val oldmsec = getLogPoint(chapterItem)
        if (oldmsec<msec) {
            val cv = ContentValues().apply {
                put("user_id", uid)
                put("part_idx", chapterItem.partIdx)
                put("chapter_idx", chapterItem.chapterIdx)
                put("watched", msec)
            }
            insertOrUpdate(chapterItem.partIdx, chapterItem.chapterIdx, cv)
        }
    }

    fun isLearned(partIdx: Int, chapterIdx: Int): Boolean {
        val cursor = getCursor(partIdx, chapterIdx,"learned")
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex("learned"))==1
        } else {
            return false
        }
    }

    fun setLearned(chapterItem: ChapterItem)
            = setLearned(chapterItem.partIdx, chapterItem.chapterIdx)
    fun setLearned(partIdx: Int, chapterIdx: Int) {
        val cv = ContentValues().apply{
            put("user_id", uid)
            put("part_idx", partIdx)
            put("chapter_idx", chapterIdx)
            put("learned", 1)
        }
        insertOrUpdate(partIdx, chapterIdx,cv)
    }

    private fun insertOrUpdate(partIdx: Int, chapterIdx: Int, cv: ContentValues) {
        val whereClause = "user_id = ? AND part_idx = ? AND chapter_idx = ?"
        val whereArgs = arrayOf(uid.toString(), partIdx.toString(), chapterIdx.toString())
        if (db.update(table_name, cv, whereClause, whereArgs)==0) {
            db.insert(table_name, null, cv)
        }
    }

    fun isLearned(chi:ChapterItem): Boolean = isLearned(chi.partIdx, chi.chapterIdx)

    fun getLogPoint(chapterItem: ChapterItem): Int
            = getLogPoint(chapterItem.partIdx, chapterItem.chapterIdx)
    fun getLogPoint(partIdx: Int, chapterIdx: Int): Int {
        val cursor = getCursor(partIdx, chapterIdx,"watched")
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex("watched"))
        } else {
            return 0
        }
    }

    fun getLogDate(partIdx: Int, chapterIdx: Int): String {
        val cursor = getCursor(partIdx, chapterIdx,"log_date").apply {
            moveToFirst()
        }
        return cursor.getString(cursor.getColumnIndex("log_date"))
    }

    val list:List<LearningLogModel> get() {
        val cursor = db.query(table_name,
                arrayOf("part_idx", "chapter_idx"),
                "user_id = ? AND part_idx <> 0",
                arrayOf(uid.toString()),
                null,
                null,
                null)
        var result = arrayListOf<LearningLogModel>()
        while (cursor.moveToNext()) {
            val partIdx = cursor.getInt(cursor.getColumnIndex("part_idx"))
            val chapterIdx = cursor.getInt(cursor.getColumnIndex("chapter_idx"))
            result.add(LearningLogModel(partIdx, chapterIdx))
        }
        return result
    }

    companion object {
        val table_name = "user_learning_log"

        val SQL_CREATE_ENTRIES = "CREATE TABLE ${table_name} ("+
                "user_id INTEGER," +
                "part_idx INTEGER, "+
                "chapter_idx INTEGER, "+
                "learned INTEGER DEFAULT 0, "+
                "watched INTEGER DEFAULT 0," +
                "log_date TEXT DEFAULT (DATE('NOW')), "+
                "PRIMARY KEY(user_id, part_idx, chapter_idx) )"

        val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${table_name}"
    }
}



class LearningLogModel(val partIdx: Int, val chapterIdx: Int): Parcelable {

    private val lldao = LearningLogDAO(TheApp.instance!!.applicationContext)
    val title get() = ChapterItemFactory.getChapterCached(partIdx, chapterIdx)!!.name
    val learningLength get()
        = lldao.getLogPoint(partIdx, chapterIdx)
    val isLearned get() = lldao.isLearned(partIdx, chapterIdx)
    val logDate get() = lldao.getLogDate(partIdx, chapterIdx)


    // Parcelling part
    constructor(p: Parcel) : this (
            partIdx = p.readInt(),
            chapterIdx = p.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(partIdx)
        dest.writeInt(chapterIdx)
    }

    companion object CREATOR : Parcelable.Creator<LearningLogModel>{
        override fun createFromParcel(`in`: Parcel): LearningLogModel {
            return LearningLogModel(`in`)
        }

        override fun newArray(size: Int): Array<LearningLogModel?> {
            return arrayOfNulls(size)
        }
    }
}