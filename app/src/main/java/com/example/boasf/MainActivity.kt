package com.example.boasf

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.Typeface.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import android.view.Gravity.*
import android.view.View
import android.view.View.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputEditText
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.util.*
import kotlin.concurrent.thread


private const val URLSEARCH = "https://avidreaders.ru/s/"
private const val REQUEST_CODE_SPEECH = 100

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "BoaSF"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val uri: Uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun search(@Suppress("UNUSED_PARAMETER")view: View) {
        val scrollView = findViewById<ScrollView>(R.id.scrollView2)
        val scrollLayout = findViewById<LinearLayout>(R.id.Lay1)
        val switchView = findViewById<Switch>(R.id.switch1)
        val btnSearch = findViewById<Button>(R.id.buttonSearch)
        val nameInput = findViewById<TextInputEditText>(R.id.bookInput)
        btnSearch.isEnabled = false
        val name = nameInput.text.toString()
        if (name != "") {
            try {
                scrollLayout.removeAllViews()
            } catch (_: java.lang.Exception) {
            }
            if (switchView.isChecked) {
                thread { getAuthors(name) }
            } else {
                thread { getBooks(name) }
            }
            scrollView.visibility = VISIBLE
        } else {
            try {
                scrollLayout.removeAllViews()
            } catch (_: java.lang.Exception) {
            }
            addInputEmpty()
        }
        btnSearch.isEnabled = true
    }

    // -------------------------------------------------Microphone----------------------------------
    fun speak(@Suppress("UNUSED_PARAMETER")view: View) {
        val mIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите название книги или автора")
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        try {
            startActivityForResult(mIntent, REQUEST_CODE_SPEECH)
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SPEECH -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val inp = findViewById<TextInputEditText>(R.id.bookInput)
                    inp.setText(result?.get(0).toString().capitalize(Locale.ROOT))
                }
            }
        }
    }


    // --------------------------------------------------GET BOOKS----------------------------------
    private fun getBooks(name: String) {
        val countTv = findViewById<TextView>(R.id.countTv)
        if (name != "") {
            val res: Connection.Response = Jsoup
                .connect(URLSEARCH + name)
                .cookie("list_view_full_books", "1")
                .method(Connection.Method.GET)
                .execute()
            val doc: Document = res.parse()
            val divs = doc.select("div.card_info")
            if (divs.size != 0) {
                ("Найдено: " + divs.size.toString() + " книг").also { countTv.text = it }
                for (i in 0 until divs.size) {
                    runOnUiThread {
                        val nameBook = divs[i].select("div.book_name").select("a").text().toString()
                        val urlBook = divs[i].select("a.btn").attr("href").toString()
                        val genreBook = if (divs[i].select("a.genre").size > 0) {
                            divs[i].select("a.genre").text().toString()
                        } else {
                            divs[i].select("span").text().toString()
                        }
                        addCard(
                            nameBook,
                            urlBook,
                            genreBook
                        )
                    }
                }
            } else {
                runOnUiThread { addCardNull() }
            }
        }
    }

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
            MATCH_PARENT,
            WRAP_CONTENT,
        )
        params.setMargins(35, 20, 40, 15)
        params.gravity = CENTER
        card.layoutParams = params
        card.radius = 20.0F
        card.useCompatPadding = true
        card.elevation = 25.0F


        val params3 = LinearLayout.LayoutParams(
            MATCH_PARENT,
            WRAP_CONTENT
        )
        linearParent.layoutParams = params3
        linearParent.orientation = LinearLayout.VERTICAL
        linearParent.setBackgroundColor(parseColor("#5777CA"))

        val params2 = LinearLayout.LayoutParams(
            MATCH_PARENT,
            100
        )
        params2.setMargins(15, 0, 0, 10)
        linear.layoutParams = params2
        linear.orientation = LinearLayout.HORIZONTAL
        linear.gravity = TOP
        linear.setBackgroundColor(parseColor("#5777CA"))


        txt.textSize = 15.0F
        txt.setTextColor(Color.WHITE)
        txt.text = name
        txt.setTypeface(null, BOLD)
        txt.maxLines = 2
        txt.textAlignment = TEXT_ALIGNMENT_TEXT_START
        txt.width = 400
        txt.setPadding(0, 0, 0, 5)
        txt.gravity = CENTER_HORIZONTAL and CENTER_VERTICAL


        btn.width = 250
        btn.height = WRAP_CONTENT
        btn.text = "Скачать"
        btn.setPadding(0, 5, 5, 0)
        btn.setBackgroundColor(parseColor("#34699B"))
        btn.setTextColor(Color.WHITE)
        btn.isAllCaps = false
        btn.textSize = 14.0F
        btn.setOnClickListener {
            downloadBook(url, name)
        }

        txtGenre.textSize = 14.0F
        txtGenre.setTextColor(parseColor("#ECECEC"))
        txtGenre.text = "       $author"
        txtGenre.maxLines = 1
        txtGenre.textAlignment = TEXT_ALIGNMENT_TEXT_START
        txtGenre.setTypeface(null, ITALIC)
        txtGenre.height = 40
        txtGenre.width = 400
        txtGenre.gravity = CENTER_VERTICAL and CENTER_HORIZONTAL


        linear.addView(txt)
        linear.addView(btn)
        linearParent.addView(linear)
        linearParent.addView(txtGenre)
        card.addView(linearParent)
        parentLayout.addView(card)
    }

    private fun downloadBook(url: String, name: String) {
        thread {
            try {
                val res: Connection.Response = Jsoup
                    .connect(url)
                    .cookie("list_view_full_books", "1")
                    .method(Connection.Method.GET)
                    .execute()
                val doc: Document = res.parse()
                val str = doc.select("a.btn")
                val urlDownload = str.attr("href").toString().split("?")[0] + "?f=fb2"
                val res2 = Jsoup
                    .connect(urlDownload)
                    .cookie("list_view_full_books", "1")
                    .method(Connection.Method.GET)
                    .execute()
                val doc2: Document = res2.parse()
                val urlFile = doc2.select("div.dnld-info").select("a").attr("href").toString()
                val res3 = Jsoup
                    .connect(urlFile)
                    .ignoreContentType(true)
                    .referrer(urlDownload)
                    .method(Connection.Method.GET)
                    .execute()
                val bytes = res3.bodyAsBytes()
                val nameFile = res3.headers()["Content-Disposition"].toString()
                    .split(";")[1].split("=")[1].drop(1)
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    nameFile
                ).writeBytes(bytes)
                runOnUiThread {
                    Toast.makeText(this, "Книга '$name' скачана", Toast.LENGTH_LONG).show()
                }
            } catch (e: java.lang.Exception) {
                Log.i("error", e.message.toString())
                runOnUiThread {
                    Toast.makeText(this, "Книга '$name' не скачана\nОшибка...", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


            // --------------------------------------------------GET AUTHORS--------------------------------
            @SuppressLint("SetTextI18n")
            private fun getAuthors(name: String) {
                val countTv = findViewById<TextView>(R.id.countTv)
                if (name != "") {
                    val res: Connection.Response = Jsoup
                        .connect(URLSEARCH + name)
                        .cookie("list_view_full_books", "1")
                        .method(Connection.Method.GET)
                        .execute()
                    val doc: Document = res.parse()
                    val divs = doc.select("div.slider").select("a")
                    if (divs.size != 0) {
                        countTv.text = "Найдено: " + divs.size.toString() + " авторов"
                        for (i in 0 until divs.size) {
                            runOnUiThread {
                                val urlGenre = divs[i].attr("href").toString()
                                val nameGenre = divs[i].select("div.popular_name").text().toString()
                                addCardGenre(
                                    nameGenre,
                                    urlGenre
                                )
                            }
                        }
                    } else {
                        runOnUiThread { addCardNull() }
                    }
                }
            }

            private fun addCardGenre(nameGenre: String, urlGenre: String) {
                val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
                val card = CardView(this)
                val linear = LinearLayout(this)
                val txt = TextView(this)
                val btn = Button(this)


                val params = LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    WRAP_CONTENT,
                )
                params.setMargins(40, 20, 40, 20)
                params.gravity = CENTER
                card.layoutParams = params
                card.radius = 40.0F
                card.useCompatPadding = true
                card.elevation = 25.0F


                val params2 = LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    120
                )
                params2.setMargins(0, 0, 0, 0)
                linear.layoutParams = params2
                linear.orientation = LinearLayout.HORIZONTAL
                linear.setBackgroundColor(parseColor("#5777CA"))
                linear.updatePadding(20, 0, 0, 0)


                txt.textSize = 15.0F
                txt.updatePadding(0, 0, 10, 0)
                txt.setTextColor(Color.WHITE)
                txt.text = nameGenre
                txt.maxLines = 2
                txt.setTypeface(null, BOLD_ITALIC)
                txt.textAlignment = TEXT_ALIGNMENT_TEXT_START
                txt.width = 400
                txt.gravity = CENTER_VERTICAL and CENTER_HORIZONTAL


                btn.width = 230
                btn.height = WRAP_CONTENT
                btn.text = "Перейти"
                btn.setBackgroundColor(parseColor("#34699B"))
                btn.setTextColor(Color.WHITE)
                btn.textSize = 14.0F
                btn.isAllCaps = false
                btn.setOnClickListener {
                    selectGenre(urlGenre)
                }


                linear.addView(txt)
                linear.addView(btn)
                card.addView(linear)
                parentLayout.addView(card)
            }

            private fun selectGenre(urlGenre: String) {
                val scrollView = findViewById<ScrollView>(R.id.scrollView2)
                val scrollLayout = findViewById<LinearLayout>(R.id.Lay1)
                val btnSearch = findViewById<Button>(R.id.buttonSearch)
                btnSearch.isEnabled = false
                scrollLayout.removeAllViews()
                thread {
                    val res: Connection.Response = Jsoup
                        .connect(urlGenre)
                        .cookie("list_view_full_books", "1")
                        .method(Connection.Method.GET)
                        .execute()
                    val doc: Document = res.parse()
                    val divs = doc.select("div.card_info")
                    ("Найдено: " + divs.size.toString() + " книг").also {
                        val countTv = findViewById<TextView>(R.id.countTv)
                        countTv.text = it
                    }
                    if (divs.size != 0) {
                        for (i in 0 until divs.size) {
                            runOnUiThread {
                                val nameBook =
                                    divs[i].select("div.book_name").select("a").text().toString()
                                val urlBook = divs[i].select("a.btn").attr("href").toString()
                                val genreBook = if (divs[i].select("a.genre").size > 0) {
                                    divs[i].select("a.genre").text().toString()
                                } else {
                                    divs[i].select("span").text().toString()
                                }
                                addCard(
                                    nameBook,
                                    urlBook,
                                    genreBook
                                )
                            }
                        }
                    } else {
                        runOnUiThread { addCardNull() }
                    }
                }
                scrollView.visibility = VISIBLE
                btnSearch.isEnabled = true
            }


            // --------------------------------------------------BAD RESULT---------------------------------
            private fun addCardNull() {
                val countTv = findViewById<TextView>(R.id.countTv)
                val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
                val card = CardView(this)
                val txt = TextView(this)
                countTv.text = ""


                val params = LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    WRAP_CONTENT,
                )
                params.setMargins(40, 20, 40, 20)
                params.gravity = CENTER_VERTICAL
                card.layoutParams = params
                card.radius = 20.0F
                card.useCompatPadding = true
                card.elevation = 25.0F


                txt.textSize = 16.0F
                txt.setTextColor(Color.WHITE)
                txt.setBackgroundColor(parseColor("#5777CA"))
                txt.text = "Ничего не найдено"
                txt.height = 100
                txt.maxLines = 1
                txt.setTypeface(null, ITALIC)
                txt.textAlignment = TEXT_ALIGNMENT_CENTER
                txt.gravity = CENTER_VERTICAL


                card.addView(txt)
                parentLayout.addView(card)
            }

            private fun addInputEmpty() {
                val countTv = findViewById<TextView>(R.id.countTv)
                val parentLayout = findViewById<LinearLayout>(R.id.Lay1)
                val scroll = findViewById<ScrollView>(R.id.scrollView2)
                val card = CardView(this)
                val txt = TextView(this)
                countTv.text = ""


                val params = LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    WRAP_CONTENT,
                )
                params.setMargins(40, 20, 40, 20)
                params.gravity = CENTER_VERTICAL
                card.layoutParams = params
                card.radius = 20.0F
                card.useCompatPadding = true
                card.elevation = 25.0F


                txt.textSize = 16.0F
                txt.setTextColor(Color.WHITE)
                txt.setBackgroundColor(parseColor("#5777CA"))
                txt.text = "Введите название книги или автора"
                txt.height = 100
                txt.maxLines = 1
                txt.setTypeface(null, ITALIC)
                txt.textAlignment = TEXT_ALIGNMENT_CENTER
                txt.gravity = CENTER_VERTICAL


                card.addView(txt)
                parentLayout.addView(card)
                scroll.visibility = VISIBLE
            }
        }