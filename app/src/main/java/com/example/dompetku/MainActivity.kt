package com.example.dompetku

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dompetku.Fragment.*
import com.example.dompetku.Model.Transaction
import com.example.dompetku.Utils.ThemePref
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), New.NewFragmentListener {

    private lateinit var btnHome: LinearLayout
    private lateinit var btnCatatan: LinearLayout
    private lateinit var btnStatistik: LinearLayout
    private lateinit var btnProfil: LinearLayout

    private lateinit var imgHome: ImageView
    private lateinit var imgCatatan: ImageView
    private lateinit var imgStatistik: ImageView
    private lateinit var imgProfil: ImageView

    private lateinit var txtHome: TextView
    private lateinit var txtCatatan: TextView
    private lateinit var txtStatistik: TextView
    private lateinit var txtProfil: TextView

    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {

        val themePref = ThemePref(this)
        AppCompatDelegate.setDefaultNightMode(
            if (themePref.isDarkMode()) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: androidx.fragment.app.FragmentManager, fragment: Fragment) {
                    when (fragment) {
                        is Dashboard -> setActiveButton(btnHome)
                        is Statistik -> setActiveButton(btnStatistik)
                        is Transaksi -> setActiveButton(btnCatatan)
                        is Profil -> setActiveButton(btnProfil)
                    }
                }
            }, true
        )

        // INIT BOTTOM MENU
        btnHome = findViewById(R.id.btn_home)
        btnCatatan = findViewById(R.id.btn_notes)
        btnStatistik = findViewById(R.id.btn_statistik)
        btnProfil = findViewById(R.id.btn_profil)

        // INIT ICON
        imgHome = findViewById(R.id.imageViewHome)
        imgCatatan = findViewById(R.id.imageViewNotes)
        imgStatistik = findViewById(R.id.imageViewStatistik)
        imgProfil = findViewById(R.id.imageViewProfil)

        // INIT TEXT
        txtHome = findViewById(R.id.textViewHome)
        txtCatatan = findViewById(R.id.textViewNotes)
        txtStatistik = findViewById(R.id.textViewStatistik)
        txtProfil = findViewById(R.id.textViewProfil)

        // INIT FAB
        fab = findViewById(R.id.fab)

        // DEFAULT: DASHBOARD
        replaceFragment(Dashboard())
        setActiveButton(btnHome)

        // MENU CLICK LISTENER
        btnHome.setOnClickListener {
            replaceFragment(Dashboard(), false)
            setActiveButton(btnHome)
        }

        btnStatistik.setOnClickListener {
            replaceFragment(Statistik(), true)
            setActiveButton(btnStatistik)
        }

        btnCatatan.setOnClickListener {
            replaceFragment(Transaksi(), true)
            setActiveButton(btnCatatan)
        }

        btnProfil.setOnClickListener {
            replaceFragment(Profil(), true)
            setActiveButton(btnProfil)
        }

        // FAB → Halaman tambah transaksi
        fab.setOnClickListener {
            replaceFragment(New(), true)
            fab.hide() // highlight menu Laporan
        }
    }

    // -----------------------------------------
    // REPLACE FRAGMENT
    // -----------------------------------------
    private fun replaceFragment(fragment: Fragment, addToBackstack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)

        if(addToBackstack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    // -----------------------------------------
    // SET ACTIVE MENU & FAB VISIBILITY
    // -----------------------------------------
    private fun setActiveButton(activeButton: LinearLayout) {

        val buttons = listOf(btnHome, btnStatistik, btnCatatan, btnProfil)
        val defaultColor = ContextCompat.getColor(this, R.color.textMenu)
        val activeColor = ContextCompat.getColor(this, R.color.utamaUt)

        // TAMPILKAN FAB HANYA DI HOME
        if (activeButton == btnHome) {
            fab.show()
        } else {
            fab.hide()
        }

        buttons.forEach { btn ->

            val img = when (btn) {
                btnHome -> imgHome
                btnStatistik -> imgStatistik
                btnCatatan -> imgCatatan
                else -> imgProfil
            }

            val txt = when (btn) {
                btnHome -> txtHome
                btnStatistik -> txtStatistik
                btnCatatan -> txtCatatan
                else -> txtProfil
            }

            if (btn == activeButton) {
                img.setColorFilter(activeColor)
                txt.setTextColor(activeColor)
            } else {
                img.setColorFilter(defaultColor)
                txt.setTextColor(defaultColor)
            }
        }
    }

    // -----------------------------------------
    // CALLBACK SETELAH ADD TRANSAKSI
    // -----------------------------------------
    override fun onTransactionAdded(transaksi: Transaction) {
        replaceFragment(Transaksi())
        setActiveButton(btnCatatan)
    }
}