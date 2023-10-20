package com.example.boasf

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    public fun search(view: View) {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }

    public fun catalog(view: View) {
        val intent = Intent(this, CatalogActivity::class.java)
        startActivity(intent)
    }

    public fun favorite(view: View) {
        val intent = Intent(this, FavoriteActivity::class.java)
        startActivity(intent)
    }
}