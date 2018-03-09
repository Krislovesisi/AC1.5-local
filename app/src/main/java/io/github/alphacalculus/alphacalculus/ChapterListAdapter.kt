package io.github.alphacalculus.alphacalculus

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView


class ChapterListAdapter(context: Context, private val resourceId: Int, objects: List<ChapterItem>) : ArrayAdapter<ChapterItem>(context, resourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val chapter = getItem(position)
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(resourceId, null)
            viewHolder = ViewHolder()
            viewHolder.imageView = view.findViewById(R.id.chapter_image) as ImageView
            viewHolder.nameView = view.findViewById(R.id.chapter_name) as TextView
            view.tag = viewHolder // 将ViewHolder存储在View中
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder // 重新获取ViewHolder
        }
        viewHolder.imageView!!.setImageResource(chapter!!.imageId)
        viewHolder.nameView!!.text = chapter.name
        if (TheApp.instance!!.isAuthed) {
            val learningLogDAO = LearningLogDAO(context)
            if (chapter.isReadable) {
                viewHolder.nameView!!.setTextColor(
                    if (learningLogDAO.isLearned(chapter))
                        Color.rgb(0, 200, 0)
                    else
                        Color.rgb(200, 0, 0))
            } else {
                viewHolder.imageView!!.isEnabled = false
                viewHolder.nameView!!.isEnabled = false
                viewHolder.nameView!!.setTextColor(Color.rgb(100,100,100))
            }
        }
        return view
    }

    internal inner class ViewHolder {

        var imageView: ImageView? = null

        var nameView: TextView? = null

    }

}
