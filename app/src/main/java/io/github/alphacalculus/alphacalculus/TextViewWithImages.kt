package io.github.alphacalculus.alphacalculus

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatTextView
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import java.util.regex.Pattern

class TextViewWithImages : AppCompatTextView {

    private val activity: Activity?
        get() {
            var context = context
            if (context is Activity) {
                return context
            }
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
            return null
        }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    override fun setText(text: CharSequence, type: TextView.BufferType) {
        val s = getTextWithImages(this, text)
        super.setMovementMethod(LinkMovementMethod.getInstance())
        super.setText(s, TextView.BufferType.SPANNABLE)
    }

    companion object {

        private val spannableFactory = Spannable.Factory.getInstance()
        fun px2dip(context: Context, pxValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }

        @SuppressLint("NewApi")
        private fun addImages(self: TextViewWithImages, spannable: Spannable): Boolean {
            val context = self.context
            val parent = self.activity
            val refImg = Pattern.compile("\\Q[img\\E\\s+?src=([a-zA-Z0-9_]+?)/\\Q]\\E")
            var hasChanges = false

            System.err.println(spannable)
            val matcher = refImg.matcher(spannable)
            while (matcher.find()) {
                var set = true
                for (span in spannable.getSpans(matcher.start(), matcher.end(), ImageSpan::class.java)) {
                    if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end()) {
                        spannable.removeSpan(span)
                    } else {
                        set = false
                        break
                    }
                }
                val resname = spannable.subSequence(matcher.start(1), matcher.end(1)).toString().trim { it <= ' ' }
                val id = context.resources.getIdentifier(resname, "drawable", context.packageName)
                if (id == 0) {
                    continue
                }
                if (set) {
                    hasChanges = true
                    var d: Drawable = context.getDrawable(id)
                    System.err.println(self.width)
                    spannable.setSpan(ResizeImageSpan(d, self.measuredWidth),
                            matcher.start(),
                            matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(object : ClickableSpan() {
                        override fun onClick(view: View) {
                            val intent = Intent(parent, ImageViewActivity::class.java)
                            intent.putExtra("io.github.alphacalculus.IMAGE_SHOW_ID", id)
                            parent!!.startActivity(intent)
                        }
                    }, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            return hasChanges
        }

        private fun getTextWithImages(t: TextViewWithImages, text: CharSequence): Spannable {
            val spannable = spannableFactory.newSpannable(text)
            System.err.println(addImages(t, spannable))
            return spannable
        }
    }
}