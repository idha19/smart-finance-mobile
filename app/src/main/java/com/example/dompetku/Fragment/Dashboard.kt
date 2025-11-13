package com.example.dompetku.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper
import com.example.dompetku.Model.Transaction

class Dashboard : Fragment() {

    private lateinit var tvTotalSaldo: TextView
    private lateinit var tvPemasukkan: TextView
    private lateinit var tvPengeluaran: TextView
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var progressMakan: ProgressBar
    private lateinit var progressBelanja: ProgressBar
    private lateinit var progressTrans: ProgressBar
    private lateinit var progressHiburan: ProgressBar
    private lateinit var progressTagihan: ProgressBar
    private lateinit var progressGaji: ProgressBar
    private lateinit var progressLainnya: ProgressBar

    private lateinit var textMakan: TextView
    private lateinit var textBelanja: TextView
    private lateinit var textTrans: TextView
    private lateinit var textHiburan: TextView
    private lateinit var textTagihan: TextView
    private lateinit var textGaji: TextView
    private lateinit var textLainnya: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // 🔹 Inisialisasi TextView utama
        tvTotalSaldo = view.findViewById(R.id.hargaTotal)
        tvPemasukkan = view.findViewById(R.id.masuk)
        tvPengeluaran = view.findViewById(R.id.keluar)

        // 🔹 Text kategori
        textGaji = view.findViewById(R.id.textGaji)
        textMakan = view.findViewById(R.id.textMakan)
        textBelanja = view.findViewById(R.id.textBelanja)
        textTrans = view.findViewById(R.id.textTrans)
        textHiburan = view.findViewById(R.id.textHiburan)
        textTagihan = view.findViewById(R.id.textTagihan)
        textLainnya = view.findViewById(R.id.textLainnya)

        // 🔹 Progress bar kategori
        progressGaji = view.findViewById(R.id.progressGaji)
        progressMakan = view.findViewById(R.id.progressMakan)
        progressBelanja = view.findViewById(R.id.progressBelanja)
        progressTrans = view.findViewById(R.id.progressTrans)
        progressHiburan = view.findViewById(R.id.progressHiburan)
        progressTagihan = view.findViewById(R.id.progressTagihan)
        progressLainnya = view.findViewById(R.id.progressLainnya)

        dbHelper = DatabaseHelper(requireContext())

        updateDashboard()

        return view
    }

    private fun updateDashboard() {
        val transaksiList: List<Transaction> = dbHelper.getAllTransaksi()

        var totalMasuk = 0
        var totalKeluar = 0

        var totalGaji = 0
        var totalMakan = 0
        var totalBelanja = 0
        var totalTrans = 0
        var totalHiburan = 0
        var totalTagihan = 0
        var totalLainnya = 0

        for (t in transaksiList) {
            when (t.jenis) {
                "Pemasukkan" -> totalMasuk += t.nominal
                "Pengeluaran" -> {
                    totalKeluar += t.nominal
                    when (t.kategori) {
                        "Makanan" -> totalMakan += t.nominal
                        "Belanja" -> totalBelanja += t.nominal
                        "Transportasi" -> totalTrans += t.nominal
                        "Hiburan" -> totalHiburan += t.nominal
                        "Tagihan" -> totalTagihan += t.nominal
                        "Lainnya" -> totalLainnya += t.nominal
                    }
                }
            }

            if (t.kategori == "Gaji" && t.jenis == "Pemasukkan") {
                totalGaji += t.nominal
            }
        }

        val saldo = totalMasuk - totalKeluar

        // 🔹 Tampilkan ke TextView
        tvPemasukkan.text = "Rp. ${formatRupiah(totalMasuk)}"
        tvPengeluaran.text = "Rp. ${formatRupiah(totalKeluar)}"
        tvTotalSaldo.text = "Rp. ${formatRupiah(saldo)}"

        // 🔹 Hitung persentase aman biar gak crash
        val totalAll = if (totalKeluar == 0) 1 else totalKeluar

        progressMakan.progress = (totalMakan * 100 / totalAll)
        progressBelanja.progress = (totalBelanja * 100 / totalAll)
        progressTrans.progress = (totalTrans * 100 / totalAll)
        progressHiburan.progress = (totalHiburan * 100 / totalAll)
        progressTagihan.progress = (totalTagihan * 100 / totalAll)
        progressLainnya.progress = (totalLainnya * 100 / totalAll)
        progressGaji.progress = (totalGaji * 100 / (if (totalMasuk == 0) 1 else totalMasuk))
    }

    private fun formatRupiah(nominal: Int): String {
        return String.format("%,d", nominal).replace(",", ".")
    }
}
