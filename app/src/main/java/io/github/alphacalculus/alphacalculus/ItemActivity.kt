package io.github.alphacalculus.alphacalculus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import java.util.*

class ItemActivity : AppCompatActivity() {

    private var chapterItem: ChapterItem? = null
    private var isLearned = false
    private var startupDate = Date(System.currentTimeMillis())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        val intent = intent
        chapterItem = intent.extras!!.getParcelable<ChapterItem>(CHAPTER_ITEM)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val btn_back = findViewById(R.id.btn_back) as Button
        btn_back.setOnClickListener(fun (_) {
            val self = this@ItemActivity
            self.chapterItem = self.chapterItem!!.previousChapter
            self.initChapter()
        })
        startupDate = Date(System.currentTimeMillis())
        val btn_next = findViewById(R.id.btn_next) as Button
        btn_next.setOnClickListener(fun (_) {
            if (TheApp.instance!!.isAuthed) {
                val learningLogDAO = LearningLogDAO(applicationContext)
                val currentDate = Date(System.currentTimeMillis())
                learningLogDAO.setLogPoint(chapterItem!!, (currentDate.time - startupDate.time).toInt())
                learningLogDAO.setLearned(chapterItem!!)
                if (chapterItem!!.nextChapter!!.isReadable) {
                    chapterItem = chapterItem!!.nextChapter
                    initChapter()
                } else {
                    val intent = Intent(this@ItemActivity, QuizActivity::class.java)
                    intent.putExtra(QuizActivity.QUIZ_ID, chapterItem!!.nextChapter!!.lockedBy)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(applicationContext, "您没有登录，无法记录学习时间", Toast.LENGTH_SHORT).show()
                chapterItem = chapterItem!!.nextChapter
                initChapter()
            }
        })
        initChapter()
    }

    override fun onStart() {
        super.onStart()
        deal_with_btn_learn()
    }

    private fun initChapter() {
        val itemName = chapterItem!!.name
        val itemImageId = chapterItem!!.imageId
        val itemImageView = findViewById(R.id.fruit_image_view) as ImageView
        val itemContentText = findViewById(R.id.chapter_content_text) as TextViewWithImages
        val collapsingToolbar = findViewById(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        collapsingToolbar.setTitle(itemName)
        Glide.with(this).load(itemImageId).into(itemImageView)
        itemContentText.setText(chapterItem!!.content, TextView.BufferType.NORMAL)
        if (TheApp.instance!!.isAuthed) {
            val log = LearningLogDAO(context = applicationContext)
            isLearned = log.isLearned(chapterItem!!)
        } else {
            Toast.makeText(applicationContext, "您没有登录，无法记录学习时间", Toast.LENGTH_SHORT).show()
            isLearned = false;
        }
        deal_with_btn_learn()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val itemContentText = findViewById(R.id.chapter_content_text) as TextViewWithImages
        itemContentText.setText(chapterItem!!.content, TextView.BufferType.NORMAL)
    }

    private fun deal_with_btn_learn() {
        val btn_learned = findViewById(R.id.btn_learned) as Button
        btn_learned.visibility = View.VISIBLE
        if (chapterItem!!.partIdx == 0) {
            if (chapterItem!!.chapterIdx <= 1) {
                btn_learned.text = "开始学习"
                btn_learned.setOnClickListener {
                    val intent = Intent(this@ItemActivity, ItemActivity::class.java)
                    intent.putExtra(ItemActivity.CHAPTER_ITEM,
                            ChapterItemFactory
                                    .getChapterCached(chapterItem!!.chapterIdx + 1, 0))
                    startActivity(intent)
                }
            } else {
                btn_learned.visibility = View.INVISIBLE
            }
        } else if (chapterItem!!.video != Uri.EMPTY) {
            val btn_next = findViewById(R.id.btn_next) as Button
            btn_next.isEnabled = false
            btn_learned.text = "暂停"
            btn_learned.isEnabled = true
            val videoView: VideoView = findViewById(R.id.videoView) as VideoView
            val itemImageView = findViewById(R.id.fruit_image_view) as ImageView
            itemImageView.visibility = ImageView.INVISIBLE
            videoView.setVideoURI(chapterItem!!.video)
            videoView.visibility = VideoView.VISIBLE
            videoView.start()
            btn_learned.setOnClickListener {
                if (videoView.isPlaying) {
                    videoView.pause()
                    btn_learned.text = "播放"
                } else {
                    videoView.start()
                    btn_learned.text = "暂停"
                }
            }
            videoView.setOnCompletionListener {
                btn_next.isEnabled = true
                btn_learned.text = "重看"
                btn_learned.setOnClickListener({
                    videoView.seekTo(0)
                    videoView.start()
                    btn_learned.setOnClickListener({
                        if (videoView.isPlaying) {
                            videoView.pause()
                            btn_learned.text = "播放"
                        } else {
                            videoView.start()
                            btn_learned.text = "暂停"
                        }
                    })
                })
            }
        } else {
            btn_learned.visibility = View.INVISIBLE
            btn_learned.text = "学习完毕"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
        if (TheApp.instance!!.isAuthed) {

            val learningLogDAO = LearningLogDAO(applicationContext)
            val currentDate = Date(System.currentTimeMillis())
            learningLogDAO.setLogPoint(chapterItem!!, (currentDate.time - startupDate.time).toInt())
        }
        super.onBackPressed()
    }

    companion object {

        val ITEM_NAME = "item_name"

        val ITEM_IMAGE_ID = "item_image_id"

        val CHAPTER_ITEM = "chapter_item"
    }
}
