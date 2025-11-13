package com.example.dompetku.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper
import com.example.dompetku.Model.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class Statistik : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistik, container, false)

        pieChart = view.findViewById(R.id.pieChartKategori)
        dbHelper = DatabaseHelper(requireContext())

        tampilkanPieChart()

        return view
    }

    private fun tampilkanPieChart() {
        val transaksiList: List<Transaction> = dbHelper.getAllTransaksi()

        var totalMakan = 0
        var totalBelanja = 0
        var totalTrans = 0
        var totalHiburan = 0
        var totalTagihan = 0
        var totalLainnya = 0

        for (t in transaksiList) {
            if (t.jenis == "Pengeluaran") {
                val nominal = t.nominal ?: 0
                when (t.kategori) {
                    "Makanan" -> totalMakan += nominal
                    "Belanja" -> totalBelanja += nominal
                    "Transportasi" -> totalTrans += nominal
                    "Hiburan" -> totalHiburan += nominal
                    "Tagihan" -> totalTagihan += nominal
                    "Lainnya" -> totalLainnya += nominal
                }
            }
        }

        val totalSemua = totalMakan + totalBelanja + totalTrans + totalHiburan + totalTagihan + totalLainnya

        val entries = ArrayList<PieEntry>()
        if (totalMakan > 0) entries.add(PieEntry(totalMakan.toFloat(), "Makanan"))
        if (totalBelanja > 0) entries.add(PieEntry(totalBelanja.toFloat(), "Belanja"))
        if (totalTrans > 0) entries.add(PieEntry(totalTrans.toFloat(), "Transportasi"))
        if (totalHiburan > 0) entries.add(PieEntry(totalHiburan.toFloat(), "Hiburan"))
        if (totalTagihan > 0) entries.add(PieEntry(totalTagihan.toFloat(), "Tagihan"))
        if (totalLainnya > 0) entries.add(PieEntry(totalLainnya.toFloat(), "Lainnya"))

        // 🎨 Warna custom dari colors.xml
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.pink),
            ContextCompat.getColor(requireContext(), R.color.orange),
            ContextCompat.getColor(requireContext(), R.color.hijau),
            ContextCompat.getColor(requireContext(), R.color.babyBlue),
            ContextCompat.getColor(requireContext(), R.color.ungu),
            ContextCompat.getColor(requireContext(), R.color.yellow)
        )

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 13f
        dataSet.sliceSpace = 3f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        // ✅ Tambah formatter persen
        val data = PieData(dataSet)
        pieChart.setUsePercentValues(true)
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.data = data

        // ⚙️ Konfigurasi tampilan chart
        pieChart.description.isEnabled = false
        pieChart.centerText = "Pengeluaran"
        pieChart.setCenterTextSize(16f)
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.animateY(1400)
        pieChart.legend.textSize = 13f
        pieChart.legend.isWordWrapEnabled = true
        pieChart.invalidate()
    }
}