package io.github.alphacalculus.alphacalculus

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.*

class QuizLogModel(val quizIdx: Int): Parcelable {


    val score get() = QuizLogDAO.scoreOf(quizIdx)
    val logDate get () = QuizLogDAO.logDate(quizIdx)

    // Parcelling part
    constructor(p: Parcel) : this (
            quizIdx = p.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(quizIdx)
    }

    companion object CREATOR : Parcelable.Creator<QuizLogModel>{
        override fun createFromParcel(`in`: Parcel): QuizLogModel {
            return QuizLogModel(`in`)
        }

        override fun newArray(size: Int): Array<QuizLogModel?> {
            return arrayOfNulls(size)
        }
    }
}

object QuizLogDAO {
    val db  get() = UserDbHelper.getDatabase(context = TheApp.instance!!.applicationContext)
    val uid get() = TheApp.instance!!.uid

    fun finished(quizIdx: Int): Boolean {
        return (scoreOf(quizIdx) >= 60.0)
    }

    val logList: List<QuizLogModel> get() {
        val cursor = db!!.query(
                table_name,
                null,
                "user_id = ?",
                arrayOf(uid.toString()),
                null,
                null,
                "quiz_idx DESC")
        val result = arrayListOf<QuizLogModel>()
        while (cursor.moveToNext()) {
            result.add(QuizLogModel(cursor.getInt(cursor.getColumnIndex("quiz_idx"))))
        }
        return result
    }

    fun cursorOf(quizIdx: Int, column_name: String)
            = db!!.query(table_name, arrayOf(column_name), "user_id = ? AND quiz_idx = ?",
                arrayOf(uid.toString(), quizIdx.toString()),null,null,"score DESC")

    fun scoreOf(quizIdx: Int): Double {
        val cursor = cursorOf(quizIdx, "score")
        if (cursor.moveToFirst()) {
            return cursor.getDouble(cursor.getColumnIndex("score"))
        } else {
            return -1.0
        }
    }

    fun logDate(quizIdx: Int): Date {
        val cursor = cursorOf(quizIdx,"log_date")
        cursor.moveToFirst()
        val date_str = cursor.getString(cursor.getColumnIndex("log_date"))
        return SimpleDateFormat("yyyy-MM-dd").parse(date_str)
    }

    fun updateScore(quizIdx: Int, wrongList: List<Int>) {
        val questionCount = ChapterItemFactory.getQuestionCount(quizIdx);
        val score = 100.00 - (wrongList.size*100.00/questionCount)
        val oldScore = scoreOf(quizIdx)

        val cv = ContentValues()
        cv.put("user_id", uid)
        cv.put("quiz_idx",quizIdx)
        cv.put("score",score)
        if (oldScore==-1.0) {
            db!!.insert(table_name, null, cv)
        } else if (score > oldScore) {
            db!!.update(table_name, cv, "user_id = ? AND quiz_idx = ?", arrayOf(uid.toString(), quizIdx.toString()))
        }
    }

    val table_name = "user_quiz_log"

    val SQL_CREATE_ENTRIES = "CREATE TABLE ${table_name} ("+
                "user_id INTEGER," +
                "quiz_idx INTEGER, "+
                "score REAL DEFAULT 0, "+
                "log_date TEXT DEFAULT (DATE('NOW')), "+
                "PRIMARY KEY(user_id, quiz_idx) )"
    val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${table_name}"
}