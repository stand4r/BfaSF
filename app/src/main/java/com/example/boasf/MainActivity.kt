package com.example.boasf

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.math.roundToLong
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue
import kotlin.time.toJavaDuration

private const val URLSEARCH = "https://avidreaders.ru/genre/"


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
            try {
                checkCatalog()
                checkHistoryDownloads()
                startActivity(Intent(this, StartActivity::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show()
                finish()
        }
    }

    private fun checkHistoryDownloads() {
        thread {
            val map = Gson().fromJson<Map<String, String>>(
                getSharedPreferences(
                    "HistoryDownloads",
                    MODE_PRIVATE
                )?.getString("history", mutableMapOf<String, String>().toString()).toString(),
                object : TypeToken<MutableMap<String?, String?>?>() {}.type
            ).toMutableMap()
            for ((i, j) in map) {
                if (!isFileExists(
                        File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            j
                        )
                    )
                ) {
                    map.remove(i)
                }
            }
            val share = getSharedPreferences("HistoryDownloads", MODE_PRIVATE)
            val editor = share.edit()
            editor.putString("history", Gson().toJson(map))
            editor.apply()
        }
    }


    private fun checkCatalog() {
        thread {
            val share = getSharedPreferences("Catalog", MODE_PRIVATE)
            val editor = share.edit()
            val map = mutableMapOf<String, String>()
            val res: Connection.Response = Jsoup
                .connect(URLSEARCH)
                .method(Connection.Method.GET)
                .execute()
            val doc = res.parse()
            val divsRow = doc.select("div.row")
            if (divsRow.size != 0) {
                for (f in divsRow) {
                    val divsColumn = f.select("div.item")
                    for (i in divsColumn) {
                        if (i.select("ul.cat").size != 0) {
                            for (x in i.select("ul.cat").select("a")) {
                                val nameGenre = x.text().toString()
                                val urlGenre = x.attr("href").toString()
                                map[nameGenre] = urlGenre
                            }
                        } else {
                            val nameGenre = i.select("a").text().toString()
                            val urlGenre = i.select("a").attr("href").toString()
                            map[nameGenre] = urlGenre
                        }
                    }
                }
            }
            editor.putString("list", Gson().toJson(map).toString())
            editor.apply()
        }
    }

    private fun isFileExists(file: File): Boolean {
        return file.exists() && !file.isDirectory
    }
}