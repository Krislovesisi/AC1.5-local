package io.github.alphacalculus.alphacalculus

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView

class VideoPlayActivity : AppCompatActivity() {
    val CHAPTER_ITEM = "chapter_item"
    var chapterItem: ChapterItem? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)
        val videoView: VideoView = findViewById(R.id.videoView) as VideoView
        chapterItem = intent.extras!!.getParcelable<Parcelable>(CHAPTER_ITEM) as ChapterItem

        val textView: TextViewWithImages = findViewById(R.id.textView) as TextViewWithImages
        val btnNext: Button = findViewById(R.id.btnNext) as Button
        val btnReplay: Button = findViewById(R.id.btnReplay) as Button
        btnNext.setOnClickListener {
            goToNextChapter()
        }
        btnReplay.setOnClickListener {
            btnReplay.visibility = Button.INVISIBLE
            videoView.seekTo(0)
            videoView.start()
        }
        videoView.setVideoURI(chapterItem!!.video)
        if (!TheApp.instance!!.isAuthed) {
            Toast.makeText(applicationContext, "您没有登录，无法记录学习时间", Toast.LENGTH_LONG).show()
        } else {
            val log = LearningLogDAO(context = applicationContext)
            var isLearned = log.isLearned(chapterItem!!)
            if (!isLearned) {
                val recentWatch = log.getLogPoint(chapterItem!!)
                if (recentWatch > videoView.currentPosition && recentWatch < videoView.duration) {
                    videoView.seekTo(recentWatch)
                }
            }
            videoView.setOnCompletionListener {
                if (!isLearned) {
                    log.setLearned(chapterItem!!)
                    isLearned = true
                    btnReplay.visibility = Button.VISIBLE
                }
            }
        }
        videoView.start()
    }

    private fun goToNextChapter() {
        val intent = Intent(this@VideoPlayActivity, ItemActivity::class.java)
        intent.putExtra(ItemActivity.CHAPTER_ITEM, chapterItem!!.nextChapter)
        startActivity(intent)
    }

    private fun logPoint() {
        if (!TheApp.instance!!.isAuthed) {
            Toast.makeText(applicationContext, "您没有登录，无法记录学习时间", Toast.LENGTH_LONG).show()
        } else {
            val log = LearningLogDAO(context = applicationContext)
            val videoView: VideoView = findViewById(R.id.videoView) as VideoView
            log.setLogPoint(chapterItem!!, videoView.currentPosition)
        }
    }

    override fun onBackPressed() {
        logPoint()
        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        logPoint()
        super.onSaveInstanceState(outState)
    }
}
