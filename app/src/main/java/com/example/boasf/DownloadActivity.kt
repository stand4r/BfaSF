package com.example.boasf

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import kotlin.concurrent.thread


@Suppress("DEPRECATION")
class DownloadActivity : AppCompatActivity() {
    private var name = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        val url = intent.extras!!.get("url").toString()
        setInfo(url)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_out_bottom_rev, R.anim.slide_in_bottom_rev)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
    private fun setInfo(url: String) {
        thread {
            val btn = arrayListOf(
                findViewById(R.id.button4),
                findViewById(R.id.button5),
                findViewById<Button>(R.id.button6)
            )
            val res: Connection.Response = Jsoup
                .connect(url)
                .method(Connection.Method.GET)
                .execute()
            val doc: Document = res.parse()
            runOnUiThread {
                findViewById<ImageView>(R.id.imageView).visibility = VISIBLE
                findViewById<TextView>(R.id.textView2).visibility = VISIBLE
                name = doc.select("h1").text().toString()
                findViewById<TextView>(R.id.tvName).text = name
                val author =
                    doc.select("div.author_info").select("a").select("span").text().toString()
                findViewById<TextView>(R.id.tvAuthor).text = author
                val option = doc.select("div.wrap_description").select("p")
                for (i in option) {
                    findViewById<TextView>(R.id.option).text =
                        findViewById<TextView>(R.id.option).text.toString() + "\n  " + i.text()
                }
                val title = doc.select("h2.margin-btm").text().toString()
                findViewById<TextView>(R.id.title).text = title
                val formats = doc.select("div.format_download")
                for (i in 0 until formats.size - 1) {
                    btn[i].visibility = VISIBLE
                    btn[i].text = formats[i].select("strong").text().toString()
                    btn[i].setOnClickListener {
                        downloadBook(formats[i].select("a.btn").attr("href").toString())
                        btn[i].isEnabled = false
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun downloadBook(url: String) {
        val progressbar = findViewById<ProgressBar>(R.id.progressBar2)
        thread {
            try {
                runOnUiThread { progressbar.visibility = VISIBLE }
                val res2 = Jsoup
                    .connect(url)
                    .method(Connection.Method.GET)
                    .execute()
                val doc2: Document = res2.parse()
                val urlFile = doc2.select("div.dnld-info").select("a").attr("href").toString()
                val res3 = Jsoup
                    .connect(urlFile)
                    .ignoreContentType(true)
                    .referrer(url)
                    .method(Connection.Method.GET)
                    .execute()
                val bytes = res3.bodyAsBytes()
                val fileName = res3.headers()["Content-Disposition"].toString()
                    .split(";")[1].split("=")[1].replace("avidreaders.ru__", "")
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                ).writeBytes(bytes)
                val hist = Gson().fromJson<Map<String, String>>(
                    getSharedPreferences(
                        "HistoryDownloads",
                        MODE_PRIVATE
                    )?.getString(
                        "history",
                        mutableMapOf<String, String>().toString()
                    ).toString(), object : TypeToken<MutableMap<String?, String?>?>() {}.type
                )
                    .toMutableMap()
                hist[name] = fileName
                val share = getSharedPreferences("HistoryDownloads", MODE_PRIVATE).edit()
                share.putString("history", Gson().toJson(hist))
                share.apply()
                runOnUiThread { progressbar.visibility = INVISIBLE }
                runOnUiThread {
                    shareNotif(name)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Ошибка при скачивании", Toast.LENGTH_LONG).show()
                }
                Log.e("Error download", e.message.toString())
                runOnUiThread { progressbar.visibility = INVISIBLE }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission", "LaunchActivityFromNotification")
    private fun shareNotif(name: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        val intentHistory = PendingIntent.getActivity(
            applicationContext,
            1,
            Intent(this, HistoryActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(applicationContext, "101")
            .setSmallIcon(R.drawable.ic_action_name)
            .setContentTitle("Книга '$name' скачана")
            .addAction(R.drawable.baseline_history_24, "Открыть историю скачиваний", intentHistory)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(intentHistory)
            .setAutoCancel(true)


        notifCnannelIfNeeded(notificationManager)
        notificationManager.notify(1, builder.build())
    }

    private fun notifCnannelIfNeeded(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel("101", "101", IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
        }
    }

}