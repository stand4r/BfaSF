package com.example.boasf

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Color.TRANSPARENT
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        findViewById<ScrollView>(R.id.scrollView).startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.shorttextanim
            )
        )
        setBooks()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_out_bottom_rev, R.anim.slide_in_bottom_rev)
    }

    @SuppressLint("SetTextI18n")
    private fun setBooks() {
        val books = Gson().fromJson<Map<String, String>>(
            getSharedPreferences(
                "HistoryDownloads",
                MODE_PRIVATE
            )?.getString("history", mutableMapOf<String, String>().toString()).toString(),
            object : TypeToken<MutableMap<String?, String?>?>() {}.type
        ).toMutableMap()
        for ((i, j) in books) {
            createCard(i, j)
        }
        findViewById<TextView>(R.id.countBooks).text = "Скачано книг: " + books.size.toString()
    }

    @SuppressLint("ResourceType")
    private fun createCard(name: String, file: String) {
        val parentLayout = findViewById<LinearLayout>(R.id.lay2)
        val card = CardView(this)
        val linear = LinearLayout(this)
        val txt = TextView(this)
        val imgShare = ImageView(this)
        val imgOpen = ImageView(this)
        val imgDelete = ImageView(this)
        val fileBook = File("/storage/emulated/0/Download/$file")
        val scale = 1.5F

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        card.layoutParams = params
        card.useCompatPadding = true
        card.elevation = 0.0F
        card.setCardBackgroundColor(TRANSPARENT)
        val params2 = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        linear.layoutParams = params2
        linear.orientation = LinearLayout.HORIZONTAL
        linear.setBackgroundResource(TRANSPARENT)
        linear.gravity = Gravity.CENTER_VERTICAL

        val paramsTxt = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val paramsImg = LinearLayout.LayoutParams(toPx(80), toPx(80))
        paramsImg.setMargins(toPx(10), 0, toPx(14), 0)

        paramsTxt.weight = 2F
        paramsImg.weight = 1F

        txt.textSize = 17.0F
        txt.typeface = Typeface.create("monospace", Typeface.ITALIC)
        txt.setTextColor(Color.WHITE)
        txt.text = name
        txt.maxLines = 2
        txt.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        txt.gravity = Gravity.CENTER_VERTICAL
        txt.layoutParams = paramsTxt
        txt.maxWidth = toPx(50)
        txt.gravity = Gravity.CENTER_VERTICAL
        txt.updatePadding(0, 0, toPx(10), 0)



        imgOpen.setImageResource(R.drawable.baseline_file_open_24)
        imgOpen.layoutParams = paramsImg
        imgOpen.scaleX = scale
        imgOpen.scaleY = scale
        imgOpen.setOnClickListener {
            val myIntent = Intent(Intent.ACTION_VIEW)
            myIntent.data = FileProvider.getUriForFile(
                applicationContext,
                applicationContext.packageName + ".provider", fileBook
            )
            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val j = Intent.createChooser(myIntent, "Открыть с помощью:")
            startActivity(j)
        }


        imgShare.setImageResource(R.drawable.baseline_share_24)
        imgShare.layoutParams = paramsImg
        imgShare.scaleX = scale
        imgShare.scaleY = scale
        imgShare.setOnClickListener {
            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "application/*"
            myIntent.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    applicationContext,
                    applicationContext.packageName + ".provider", fileBook
                )
            )
            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val j = Intent.createChooser(myIntent, null)
            startActivity(j)
        }

        imgDelete.setImageResource(R.drawable.baseline_delete_24)
        imgDelete.layoutParams = paramsImg
        imgDelete.scaleX = scale
        imgDelete.scaleY = scale
        imgDelete.setOnClickListener {
            fileBook.delete()
            val books = Gson().fromJson<Map<String, String>>(
                getSharedPreferences(
                    "HistoryDownloads",
                    MODE_PRIVATE
                )?.getString("history", mutableMapOf<String, String>().toString()).toString(),
                object : TypeToken<MutableMap<String?, String?>?>() {}.type
            ).toMutableMap()
            books.remove(name)
            findViewById<TextView>(R.id.countBooks).text = "Скачано книг: " + books.size.toString()
            val share = getSharedPreferences("HistoryDownloads", MODE_PRIVATE)
            val editor = share.edit()
            editor.putString("history", Gson().toJson(books))
            editor.apply()
            parentLayout.removeView(card)
        }

        linear.addView(txt)
        linear.addView(imgShare)
        linear.addView(imgOpen)
        linear.addView(imgDelete)
        card.addView(linear)
        parentLayout.addView(card)
    }

    private fun toPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}