package com.example.dompetku.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper

class Dashboard : Fragment() {

    private lateinit var tvTotalSaldo: TextView
    private lateinit var tvPemasukkan: TextView
    private lateinit var tvPengeluaran: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        tvTotalSaldo = view.findViewById(R.id.hargaTotal)
        tvPemasukkan = view.findViewById(R.id.masuk)
        tvPengeluaran = view.findViewById(R.id.keluar)

        dbHelper = DatabaseHelper(requireContext())

        updateDashboard()

        return view
    }

    fun updateDashboard(){
        val transaksiList = dbHelper.getAllTransaksi()
        var totalMasuk = 0
        var totalKeluar = 0

        for(t in transaksiList){
            when(t.jenis){
                "Pemasukkan" -> totalMasuk += t.nominal
                "Pengeluaran" -> totalKeluar += t.nominal
            }
        }

        val saldo = totalMasuk - totalKeluar

        tvPemasukkan.text = "Rp. ${formatRupiah(totalMasuk)}"
        tvPengeluaran.text = "Rp. ${formatRupiah(totalKeluar)}"
        tvTotalSaldo.text = "Rp. ${formatRupiah(saldo)}"
    }

    private fun formatRupiah(nominal: Int): String{
        return String.format("%,d", nominal).replace(",", ".")
    }
}
