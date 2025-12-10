package com.example.dompetku

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.CommonDataKinds.Im
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dompetku.Utils.ThemePref

class splashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val themePref = ThemePref(this)
        AppCompatDelegate.setDefaultNightMode(
            if (themePref.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.ut)
        val text = findViewById<TextView>(R.id.textUt)

        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo)
        val textAnim = AnimationUtils.loadAnimation(this, R.anim.text)

        logo.startAnimation(logoAnim)
        text.startAnimation(textAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500)
    }
}