package io.github.alphacalculus.alphacalculus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat



class QuizLogAdapter(context: Context, private val resourceId: Int, objects: List<QuizLogModel>) : ArrayAdapter<QuizLogModel>(context, resourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val quiz_log = getItem(position)
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(resourceId, null)
            viewHolder = ViewHolder()
            viewHolder.nameView = view.findViewById(R.id.quiz_log_text) as TextView
            view.tag = viewHolder // 将ViewHolder存储在View中
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder // 重新获取ViewHolder
        }
        viewHolder.nameView!!.text = "测试 ${quiz_log.quizIdx+1} 于 ${SimpleDateFormat("yyyy-MM-dd").format(quiz_log.logDate)} 完成，您共获得 ${quiz_log.score} 分"
        return view
    }

    internal inner class ViewHolder {

        var nameView: TextView? = null

    }

}