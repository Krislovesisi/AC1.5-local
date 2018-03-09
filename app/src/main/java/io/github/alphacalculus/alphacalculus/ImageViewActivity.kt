package io.github.alphacalculus.alphacalculus

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView

/**
 * Show image in a new Activity.
 */
class ImageViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)
        val resid = intent.getIntExtra("io.github.alphacalculus.IMAGE_SHOW_ID", 0)
        val imageView = findViewById(R.id.imageView) as ImageView
        imageView.setImageResource(resid)
    }
}
