package io.github.alphacalculus.alphacalculus


import android.content.Context
import android.net.Uri
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

object ChapterItemFactory {

    private var parts: NodeList? = null
    private var part_number: Int = 0
    private var chapter_number: IntArray? = null
    private val ci = arrayOfNulls<ArrayList<ChapterItem>>(3)
    private var data: Node? = null;

    init {
        val context = TheApp.instance!!.applicationContext
        readXML(context)
    }

    fun readXML(context: Context) {
        val __inputSrc = InputSource(context.resources.openRawResource(R.raw.itemdata))
        val xpath = XPathFactory.newInstance().newXPath()
        try {
            data = xpath.evaluate("//Data", __inputSrc, XPathConstants.NODE) as Node
            parts = xpath.evaluate("//Data/Part", data, XPathConstants.NODESET) as NodeList
            part_number = parts!!.length
            if (chapter_number == null) {
                chapter_number = IntArray(part_number)
            }
        } catch (e: XPathExpressionException) {
            e.printStackTrace()
        }

    }

    fun getChapterCount(part_idx: Int): Int {
        if (chapter_number != null && chapter_number!![part_idx] > 0) {
            return chapter_number!![part_idx]
        }
        val xpath = XPathFactory.newInstance().newXPath()
        try {
            chapter_number!![part_idx] = (xpath.evaluate("count(.//Chapter)", parts!!.item(part_idx), XPathConstants.NUMBER) as Double).toInt()
            return chapter_number!![part_idx]
        } catch (e: XPathExpressionException) {
            e.printStackTrace()
            return 0
        }

    }

    /** Read one chapter content from xml.
     * @param part_idx 0: homepage; 1: Derivatives; 2: Integrals.
     * @param chapter_idx starting with 0.
     */
    fun getChapter(part_idx: Int, chapter_idx: Int): ChapterItem? {
        var chapter_idx = chapter_idx
        var ch: ChapterItem? = null
        val context = TheApp.instance!!.applicationContext
        val xpath = XPathFactory.newInstance().newXPath()
        chapter_idx += 1
        try {
            val node = xpath.evaluate("(.//Chapter)[$chapter_idx]", parts!!.item(part_idx), XPathConstants.NODE) as Node
            var chidx = "ch"
            try {
                Integer.parseInt(xpath.evaluate("./@index", node, XPathConstants.STRING) as String)
                chidx += (if (chapter_idx >= 10) "" else "0") + chapter_idx
                System.err.println("编号：${chidx}")
            } catch (throwable: Throwable) {
                chidx = "ch" + xpath.evaluate("./@index", node) as String
            }

            var content = xpath.evaluate("./text()", node)
            val contentLines = content.split("[\r\n]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            content = ""
            for (s in contentLines) {
                content += s.trim { it <= ' ' } + "\n"
            }
            var video = Uri.EMPTY
            try {
                val vid = xpath.evaluate("./@video", node, XPathConstants.STRING) as String
                if (vid != "") {
                    video = Uri.parse("android.resource://${TheApp.instance!!.packageName}/raw/v" + vid.replace('.', '_').replace('-', '_').replace("_mp4", ""))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            var lockedBy = -1
            try {
                lockedBy = Integer.parseInt(xpath.evaluate("./@lockedby", node, XPathConstants.STRING) as String)
            } catch (e: Exception) {
            }

            ch = ChapterItem(xpath.evaluate("./@title", node, XPathConstants.STRING) as String,
                    content, video, part_idx, chapter_idx - 1,
                    context.resources.getIdentifier(chidx,
                            "drawable", context.packageName), lockedBy)
        } catch (e: XPathExpressionException) {
            e.printStackTrace()
        }

        return ch
    }

    /**
     * Get all chapters in the part of part_idx.
     * @param part_idx 0: homepage; 1: Derivatives; 2: Integrals.
     * @return An array of chapters.
     */
    fun getChapters(part_idx: Int): Array<ChapterItem?> {
        val chapterN = getChapterCount(part_idx)
        val l = arrayOfNulls<ChapterItem>(chapterN)
        for (i in 0 until chapterN) {
            l[i] = getChapter(part_idx, i)
        }
        return l
    }

    /**
     * ArrayList version of getChapters
     */
    fun getChapterList(part_idx: Int): ArrayList<ChapterItem> {
        if (ci[part_idx] != null) {
            return ci[part_idx]!!
        }
        val chapterN = getChapterCount(part_idx)
        val l = ArrayList<ChapterItem>()
        for (i in 0 until chapterN) {
            val ch = getChapter(part_idx, i)
            if (ch!=null) {
                l.add(ch)
            }
        }
        ci[part_idx] = l
        return l
    }

    /**
     * Cached version of getChapter
     */
    fun getChapterCached(part_idx: Int, chapter_idx: Int): ChapterItem? {
        return if (ci[part_idx] != null) {
            ci[part_idx]!![chapter_idx]
        } else {
            getChapter(part_idx, chapter_idx)
        }
    }

    private var allquiz: NodeList? = null

    fun quizNumber() = allquiz!!.length

    fun getQuestionCount(quiz_idx: Int):Int {
        val xpath = XPathFactory.newInstance().newXPath()
        val quiz =xpath.evaluate("//Data/Quiz", data, XPathConstants.NODESET) as NodeList
        return (xpath.evaluate("count(./Question)", quiz.item(quiz_idx), XPathConstants.NUMBER) as Double).toInt()
    }

    fun getQuiz(quiz_idx: Int): ArrayList<QuestionItem> {
        val xpath = XPathFactory.newInstance().newXPath()
        val quiz =xpath.evaluate("//Data/Quiz", data, XPathConstants.NODESET) as NodeList
        val questions =xpath.evaluate("./Question", quiz.item(quiz_idx), XPathConstants.NODESET) as NodeList

        val result = arrayListOf<QuestionItem>()
        for (i in 0 until questions.length) {
            val question = questions.item(i)
            val questionText = xpath.evaluate("./Content", question, XPathConstants.STRING) as String
            val answers = xpath.evaluate("./Answer", question, XPathConstants.NODESET) as NodeList
            var correctAnswerIdx = -1;
            val answerTexts = (0 until answers.length).map { j ->
                if (answers.item(j).attributes.getNamedItem("is_true")!=null) {
                    correctAnswerIdx=j
                }
                answers.item(j).textContent
            }
            result.add(QuestionItem(questionText, correctAnswerIdx, answerTexts))
        }
        return result
    }

}
