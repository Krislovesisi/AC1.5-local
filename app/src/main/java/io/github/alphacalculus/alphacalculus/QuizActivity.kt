package io.github.alphacalculus.alphacalculus

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_quiz.*
import org.json.JSONArray
import java.util.concurrent.ThreadLocalRandom

class QuizActivity : AppCompatActivity() {
    companion object {

        val QUIZ_ID = "io.github.alphacalculus.QUIZ_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        val quiz_idx = intent.getIntExtra(QUIZ_ID, 0)
        if (quiz_idx<0) {
            return
        }
        val quiz = ChapterItemFactory.getQuiz(quiz_idx)
        val ulInner = (0 until quiz.size).map { i ->
            val questionItem = quiz[i]
            val sequence = (0 until questionItem.answers.size).toList().toIntArray()
            shuffleArray(sequence)
            "<li id=qid_${i}><p>${questionItem.textContent}</p><p>"+sequence.map {
                "<span>" + (if (questionItem.correctAnswerIdx==it) {
                    "<input type=radio id=qid_${i}_aid_${it} name=qid_${i} data-is-correct=true>"
                } else {
                    "<input type=radio id=qid_${i}_aid_${it} name=qid_${i} >"
                })+"${questionItem.answers[it]}</span>"
            }.joinToString("")+"</p></li>"
        }.joinToString("")
        val html = """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
            <script type="text/javascript">
                function submitQuiz () {
                    var questions = document.querySelectorAll("#quiz li");
                    var wrongIds = [];
                    [].forEach.call(questions, function(liQ){
                        var radios = liQ.querySelectorAll('input[type=radio]');
                        var checked = [].filter.call(radios, function(r){return !!r.checked;});
                        if (checked.length!=1 || checked[0].getAttribute('data-is-correct')!='true') {
                            wrongIds.push(parseInt(liQ.id.replace('qid_','')));
                        }
                    });
                    alert(JSON.stringify(wrongIds));
                }
            </script>

            <script type="text/javascript"
   src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.2/MathJax.js?config=TeX-AMS-MML_HTMLorMML">
</script>
</head>
<body>
            <h1>测试${quiz_idx}</h1>
            <form id="quiz">
                <ol>
                    ${ulInner}
                </ol>
                <input type="button" value="提交" onclick="submitQuiz();">
            </form>
            </body></html>
        """.trimIndent()
        webView.settings.javaScriptEnabled = true
        webView.loadData(html, "text/html; charset=UTF-8", "UTF-8")
        webView.setWebChromeClient(Wcc(quiz_idx))
    }

    fun shuffleArray(ar: IntArray) {
        val rnd = ThreadLocalRandom.current()
        for (i in ar.size - 1 downTo 1) {
            val index = rnd.nextInt(i + 1)
            // Simple swap
            val a = ar[index]
            ar[index] = ar[i]
            ar[i] = a
        }
    }

    class Wcc(val quiz_idx:Int): WebChromeClient() {
        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            val ja = JSONArray(message)
            val wrongList = (0 until ja.length()).map {
                ja.getInt(it)
            }
            QuizLogDAO.updateScore(quiz_idx, wrongList)
            Toast.makeText(TheApp.instance!!.applicationContext, "您共获得${QuizLogDAO.scoreOf(quiz_idx)}分", Toast.LENGTH_SHORT).show()
            //return super.onJsAlert(view, url, message, result)
            return true
        }
    }
}
