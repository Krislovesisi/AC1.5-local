package io.github.alphacalculus.alphacalculus

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ListView

class QuizLogActivity : AppCompatActivity() {
    companion object {
        val IS_LEARNING_LOG_VIEW = "io.github.alphacalculus.alphacalculus.IS_LEARNING_LOG_VIEW"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_log)

        if (TheApp.instance!!.isAuthed) {
            val adapter =
                if (intent.getBooleanExtra(IS_LEARNING_LOG_VIEW, false))
                    LearningLogAdapter(this, R.layout.activity_quiz_log_item,
                            LearningLogDAO(applicationContext).list
                    )
                else
                    QuizLogAdapter(this, R.layout.activity_quiz_log_item, QuizLogDAO.logList)
            val listView = findViewById(R.id.quiz_log_list) as ListView
            listView.adapter = adapter
        }
    }
}
