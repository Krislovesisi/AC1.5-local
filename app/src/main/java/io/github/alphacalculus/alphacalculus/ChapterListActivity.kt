package io.github.alphacalculus.alphacalculus

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ListView
import java.util.*


class ChapterListActivity : AppCompatActivity() {

    private var chapterList: List<ChapterItem> = ArrayList<ChapterItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chapter_list)
        when (intent.extras!!.getInt("part")) {
            1 -> initChapters(1)
            2 -> initChapters(2)
            R.id.nav_daoshu -> initChapters(1)
            R.id.nav_jifen -> initChapters(2)
        }
        val adapter = ChapterListAdapter(this@ChapterListActivity, R.layout.chapter_list_item, chapterList)
        val listView = findViewById(R.id.chapter_list) as ListView
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            System.err.println(position)
            val chapter = chapterList.get(position)
            System.err.println(chapter.content)
            // Open a new ItemActivity to show the clicked chapter.
            val intent = Intent(this@ChapterListActivity, ItemActivity::class.java)
            intent.putExtra(ItemActivity.CHAPTER_ITEM, chapter)
            startActivity(intent)
        }
    }

    private fun initChapters(part_index: Int) {
        chapterList = ChapterItemFactory.getChapterList(part_index)
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
