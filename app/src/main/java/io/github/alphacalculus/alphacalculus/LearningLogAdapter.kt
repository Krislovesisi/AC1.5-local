package io.github.alphacalculus.alphacalculus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class LearningLogAdapter(context: Context, private val resourceId: Int, objects: List<LearningLogModel>) : ArrayAdapter<LearningLogModel>(context, resourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val learningLog = getItem(position)
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
        viewHolder.nameView!!.text =
                "${learningLog.logDate} "+
                (if (learningLog.isLearned) "完成" else "")+
                "学习 ${learningLog.partIdx}.${learningLog.chapterIdx+1} ${learningLog.title}"+
                "时长 ${formatTime(learningLog.learningLength/1000)}"
        return view
    }

    fun formatTime(sec:Int) = getHour(sec)+getMinute(sec)+getSecond(sec)

    fun getHour(sec: Int) = if (sec>=3600) "${(sec/3600)} 小时" else ""
    fun getMinute(sec: Int) = if (sec>=60) "${(sec/60%60)} 分钟" else ""
    fun getSecond(sec: Int) = if (sec>=1) "${(sec%60%60)} 秒" else ""

    internal inner class ViewHolder {

        var nameView: TextView? = null

    }

}