package com.example.dompetku

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.dompetku.Fragment.Dashboard
import com.example.dompetku.Fragment.New
import com.example.dompetku.Fragment.Statistik
import com.example.dompetku.Fragment.Transaksi
import com.example.dompetku.Model.Transaction

class MainActivity : AppCompatActivity(), New.NewFragmentListener {

    private lateinit var btnHome: LinearLayout
    private lateinit var btnAdd: LinearLayout
    private lateinit var btnCatatan: LinearLayout
    private lateinit var btnStatistik: LinearLayout

    private lateinit var imgHome: ImageView
    private lateinit var imgAdd: ImageView
    private lateinit var imgCatatan: ImageView
    private lateinit var imgStatistik: ImageView

    private lateinit var txtHome: TextView
    private lateinit var txtAdd: TextView
    private lateinit var txtCatatan: TextView
    private lateinit var txtStatistik: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi tombol bottom menu
        btnHome = findViewById(R.id.btn_home)
        btnAdd = findViewById(R.id.btn_add)
        btnCatatan = findViewById(R.id.btn_catatan)
        btnStatistik = findViewById(R.id.btn_statistik)

        // Inisialisasi icon dan text
        imgHome = findViewById(R.id.imageViewHome)
        imgAdd = findViewById(R.id.imageViewAdd)
        imgCatatan = findViewById(R.id.imageViewCatatan)
        imgStatistik = findViewById(R.id.imageViewStatistik)

        txtHome = findViewById(R.id.textViewHome)
        txtAdd = findViewById(R.id.textViewAdd)
        txtCatatan = findViewById(R.id.textViewCatatan)
        txtStatistik = findViewById(R.id.textViewStatistik)

        // Load default fragment (Dashboard)
        replaceFragment(Dashboard())
        setActiveButton(btnHome)

        // Set click listener bottom menu
        btnHome.setOnClickListener {
            replaceFragment(Dashboard())
            setActiveButton(btnHome)
        }

        btnStatistik.setOnClickListener {
            replaceFragment(Statistik())
            setActiveButton(btnStatistik)
        }

        btnAdd.setOnClickListener {
            replaceFragment(New())
            setActiveButton(btnAdd)
        }

        btnCatatan.setOnClickListener {
            replaceFragment(Transaksi())
            setActiveButton(btnCatatan)
        }
    }

    // Fungsi untuk mengganti fragment
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    // Fungsi untuk highlight tombol aktif
    private fun setActiveButton(activeButton: LinearLayout) {
        val buttons = listOf(btnHome, btnStatistik, btnAdd, btnCatatan)
        buttons.forEach { btn ->
            val img = when (btn) {
                btnHome -> imgHome
                btnStatistik -> imgStatistik
                btnAdd -> imgAdd
                else -> imgCatatan
            }
            val txt = when (btn) {
                btnHome -> txtHome
                btnStatistik -> txtStatistik
                btnAdd -> txtAdd
                else -> txtCatatan
            }

            if (btn == activeButton) {
                img.setColorFilter(Color.parseColor("#FF4081")) // pink highlight
                txt.setTextColor(Color.parseColor("#FF4081"))
            } else {
                img.setColorFilter(Color.parseColor("#7B1FA2")) // ungu default
                txt.setTextColor(Color.parseColor("#7B1FA2"))
            }
        }
    }

    // Callback dari NewFragment setelah simpan transaksi
    override fun onTransactionAdded(transaksi: Transaction) {
        // Pindah ke Transaksi fragment dan bisa kirim data jika mau
        val fragment = Transaksi()
        replaceFragment(fragment)
        setActiveButton(btnCatatan)
    }
}
