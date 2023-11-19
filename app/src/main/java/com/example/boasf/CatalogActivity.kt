@file:Suppress("DEPRECATION")

package com.example.boasf

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Thread.sleep
import java.util.*
import kotlin.concurrent.thread


private const val REQUEST_CODE_SPEECH = 100
private const val ELEM1 = "#15161E"

class CatalogActivity : AppCompatActivity() {
    private var flag = true

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val uri: Uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
        }
        findViewById<LinearLayout>(R.id.searchLayout).startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.shorttextanim
            )
        )
        thread { getGenres() }

        val editText = findViewById<View>(R.id.bookInput) as EditText
        editText.setOnKeyListener { v, keyCode, event ->
            var consumed = false
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                this.currentFocus?.let { view ->
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(view.windowToken, 0)
                }
                getGenresSearch(editText.text.toString())
                consumed = true
            }
            consumed
        }
    }

    //----------------------------------- ANY FUNCTION ---------------------------------------------

    private fun toPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (flag) {
            if (findViewById<TextInputEditText>(R.id.bookInput).text.toString() != "") {
                findViewById<TextInputEditText>(R.id.bookInput).text = null
                thread { getGenres() }
            } else {
                finish()
                overridePendingTransition(R.anim.slide_out_bottom_rev, R.anim.slide_in_bottom_rev)
            }

        } else {
            if (findViewById<TextInputEditText>(R.id.bookInput).text.toString() != "") {
                thread { getGenres() }
            } else {
                thread { getGenres() }
            }
        }
        overridePendingTransition(R.anim.slide_out_bottom_rev, R.anim.slide_in_bottom_rev)
    }

    fun search(view: View) {
        thread {
            val ed = findViewById<EditText>(R.id.bookInput)
            getGenresSearch(ed.text.toString())
        }
    }

    private fun optionBook(url: String) {
        val intent = Intent(this, DownloadActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    fun speak(@Suppress("UNUSED_PARAMETER") view: View) {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите название книги или автора")
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        try {
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SPEECH -> {
                if (resultCode == RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val inp = findViewById<TextInputEditText>(R.id.bookInput)
                    inp.setText(result?.get(0).toString().capitalize(Locale.ROOT))
                }
            }
        }
    }


    //-------------------------------------------CATALOG PARSING-------------------------------------

    private fun getGenres() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
        val lisGenres = Gson().fromJson<Map<String, String>>(
            getSharedPreferences(
                "Catalog",
                MODE_PRIVATE
            )?.getString("list", mutableMapOf<String, String>().toString()).toString(),
            object : TypeToken<MutableMap<String?, String?>?>() {}.type
        )
        runOnUiThread {
            val searchlayout = findViewById<LinearLayout>(R.id.searchLayout)
            searchlayout.visibility = VISIBLE
            val scrollLayout = findViewById<LinearLayout>(R.id.Lay1)
            scrollLayout.removeAllViews()
        }
        val ed = findViewById<EditText>(R.id.bookInput).text.toString()
        flag = true
        for ((f, i) in lisGenres) {
                runOnUiThread {
                    addCardGenre(f, i)
                }
            }
        }

    private fun getGenresSearch(filter: String) {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
        if (filter != "") {
        val lisGenres = Gson().fromJson<Map<String, String>>(
            getSharedPreferences(
                "Catalog",
                MODE_PRIVATE
            )?.getString("list", mutableMapOf<String, String>().toString()).toString(),
            object : TypeToken<MutableMap<String?, String?>?>() {}.type
        )
        runOnUiThread {
            val searchlayout = findViewById<LinearLayout>(R.id.searchLayout)
            searchlayout.visibility = VISIBLE
            val scrollLayout = findViewById<LinearLayout>(R.id.Lay1)
            scrollLayout.removeAllViews()
            val genreTv = findViewById<TextView>(R.id.GenreTv)
            genreTv.visibility = INVISIBLE
        }
        val ed = findViewById<EditText>(R.id.bookInput).text.toString()
        flag = true
        for ((f, i) in lisGenres) {
                if (filter in f) {
                    sleep(5)
                    runOnUiThread {
                        addCardGenre(f, i)
                    }
                }
            }
        }
    }


    //----------------------------------------GRAPHICS----------------------------------------------
    @SuppressLint("SetTextI18n")
    private fun addCard(name: String, url: String, author: String) {
        val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
        val card = CardView(this)
        val linear = LinearLayout(this)
        val txt = TextView(this)
        val btn = Button(this)
        val txtGenre = TextView(this)
        val linearParent = LinearLayout(this)


        val params = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER
        params.setMargins(0, 0, 0, toPx(20))
        card.layoutParams = params
        card.radius = 20.0F
        card.useCompatPadding = true
        card.elevation = 25.0F


        val params3 = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT
        )
        linearParent.layoutParams = params3
        linearParent.orientation = LinearLayout.VERTICAL
        linearParent.setBackgroundColor(Color.parseColor(ELEM1))

        val params2 = LinearLayout.LayoutParams(
            MATCH_PARENT,
            WRAP_CONTENT
        )
        params2.setMargins(15, 0, 0, 10)
        linear.layoutParams = params2
        linear.orientation = LinearLayout.HORIZONTAL
        linear.gravity = Gravity.TOP
        linear.setBackgroundColor(Color.parseColor(ELEM1))


        txt.textSize = 15.0F
        txt.setTextColor(Color.WHITE)
        txt.text = name
        txt.setTypeface(null, Typeface.BOLD)
        txt.minLines = 1
        txt.maxLines = 2
        txt.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        txt.width = toPx(255)
        txt.height = toPx(55)
        txt.setPadding(toPx(5), 0, toPx(10), 5)
        txt.gravity = Gravity.CENTER_HORIZONTAL and Gravity.CENTER_VERTICAL

        btn.width = toPx(30)
        btn.text = "Подробнее"
        btn.setPadding(0, 0, toPx(10), 0)
        btn.setBackgroundColor(Color.parseColor(ELEM1))
        btn.setTextColor(Color.WHITE)
        btn.isAllCaps = false
        btn.textSize = 14.0F
        btn.setOnClickListener {
            optionBook(url)
        }

        txtGenre.textSize = 14.0F
        txtGenre.setTextColor(Color.parseColor("#ECECEC"))
        txtGenre.text = "       $author"
        txtGenre.maxLines = 1
        txtGenre.isSingleLine = true
        txtGenre.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        txtGenre.setTypeface(null, Typeface.ITALIC)
        txtGenre.height = toPx(33)
        txtGenre.width = toPx(330)
        txtGenre.setPadding(0, toPx(7), toPx(20), 0)
        txtGenre.gravity = Gravity.CENTER_VERTICAL and Gravity.CENTER_HORIZONTAL


        linear.addView(txt)
        linear.addView(btn)
        linearParent.addView(linear)
        linearParent.addView(txtGenre)
        card.addView(linearParent)
        parentLayout.addView(card)
    }

    private fun addCardGenre(nameGenre: String, urlGenre: String) {
        val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
        val card = CardView(this)
        val linear = LinearLayout(this)
        val txt = TextView(this)

        val params = LinearLayout.LayoutParams(
            MATCH_PARENT,
            WRAP_CONTENT,
        )
        params.setMargins(10, 20, 10, 20)
        params.gravity = Gravity.CENTER
        card.layoutParams = params
        card.radius = 20.0F
        card.useCompatPadding = true
        card.elevation = 25.0F
        card.setOnClickListener {
            selectGenre(nameGenre, urlGenre)
        }


        val params2 = LinearLayout.LayoutParams(
            MATCH_PARENT,
            WRAP_CONTENT
        )
        params2.setMargins(0, 0, 0, 0)
        linear.layoutParams = params2
        linear.orientation = LinearLayout.HORIZONTAL
        linear.setBackgroundResource(R.drawable.gradientcatalog)
        linear.updatePadding(20, 0, 0, 0)

        txt.textSize = 16.0F
        txt.updatePadding(toPx(10), toPx(3), toPx(10), 0)
        txt.setTextColor(Color.parseColor("#ECECEC"))
        txt.text = nameGenre
        txt.isSingleLine = true
        txt.maxLines = 2
        txt.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        txt.width = toPx(310)
        txt.height = toPx(48)
        txt.gravity = Gravity.CENTER_VERTICAL

        linear.addView(txt)
        card.addView(linear)
        parentLayout.addView(card)
    }

    private fun selectGenre(name: String, urlGenre: String) {
        val scrollView = findViewById<ScrollView>(R.id.scrollView2)
        val scrollLayout = findViewById<LinearLayout>(R.id.Lay1)
        val searchlayout = findViewById<LinearLayout>(R.id.searchLayout)
        val genre = findViewById<TextView>(R.id.GenreTv)
        genre.text = name
        genre.visibility = VISIBLE
        searchlayout.visibility = INVISIBLE
        flag = false
        scrollLayout.removeAllViews()
        thread {
            try {
                val res: Connection.Response = Jsoup
                    .connect(urlGenre)
                    .cookie("list_view_full_books", "1")
                    .method(Connection.Method.GET)
                    .execute()
                val doc: Document = res.parse()
                val divs = doc.select("div.card_info")
                if (divs.size != 0) {
                    for (i in 0 until divs.size) {
                        sleep(40)
                        runOnUiThread {
                            val nameBook =
                                divs[i].select("div.book_name").select("a").text().toString()
                            val urlBook = divs[i].select("a.btn").attr("href").toString()
                            val genreBook = if (divs[i].select("a.genre").size > 0) {
                                divs[i].select("a.genre").text().toString()
                            } else {
                                divs[i].select("span").text().toString()
                            }
                            if ("Черт из табакерки" !in nameBook) {
                                addCard(
                                    nameBook,
                                    urlBook,
                                    genreBook
                                )
                            }
                        }
                    }
                } else {
                    runOnUiThread { addInputEmpty() }
                }
            } catch (e: java.lang.Exception) {
                addBadNetwork()
            }
        }
        scrollView.visibility = VISIBLE
    }


    // --------------------------------------------------BAD RESULT---------------------------------
    private fun addInputEmpty() {
        val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
        val scroll = findViewById<ScrollView>(R.id.scrollView2)
        val card = CardView(this)
        val txt = TextView(this)
        val params = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
        )
        params.setMargins(40, 20, 40, 20)
        params.gravity = Gravity.CENTER
        card.layoutParams = params
        card.radius = 30.0F
        card.useCompatPadding = true
        card.elevation = 25.0F
        txt.textSize = 16.0F
        txt.setTextColor(Color.WHITE)
        txt.setBackgroundColor(Color.parseColor(ELEM1))
        txt.text = "Книги не найдены"
        txt.height = toPx(50)
        txt.width = toPx(350)
        txt.maxLines = 1
        txt.setTypeface(null, Typeface.ITALIC)
        txt.textAlignment = View.TEXT_ALIGNMENT_CENTER
        txt.gravity = Gravity.CENTER_VERTICAL
        card.addView(txt)
        parentLayout.addView(card)
        scroll.visibility = VISIBLE
    }


    private fun addBadNetwork() {
        val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
        val scroll = findViewById<ScrollView>(R.id.scrollView2)
        val card = CardView(this)
        val txt = TextView(this)
        val params = LinearLayout.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
        )
        params.setMargins(40, 20, 40, 20)
        params.gravity = Gravity.CENTER
        card.layoutParams = params
        card.radius = 30.0F
        card.useCompatPadding = true
        card.elevation = 25.0F
        txt.textSize = 16.0F
        txt.setTextColor(Color.WHITE)
        txt.setBackgroundColor(Color.parseColor(ELEM1))
        txt.text = "Проблемы с сетью"
        txt.height = toPx(50)
        txt.width = toPx(350)
        txt.maxLines = 1
        txt.setTypeface(null, Typeface.ITALIC)
        txt.textAlignment = View.TEXT_ALIGNMENT_CENTER
        txt.gravity = Gravity.CENTER_VERTICAL
        card.addView(txt)
        parentLayout.addView(card)
        scroll.visibility = VISIBLE
    }
}
