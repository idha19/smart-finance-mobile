package com.example.dompetku.Fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dompetku.Model.Transaction
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.*

class Statistik : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var btnHari: Button
    private lateinit var btnMinggu: Button
    private lateinit var btnBulan: Button
    private lateinit var btnTahun: Button
    private lateinit var etTanggalStatistik: EditText

    private var filterMode = "HARI"
    private var selectedDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistik, container, false)

        pieChart = view.findViewById(R.id.pieChartKategori)
        dbHelper = DatabaseHelper(requireContext())

        etTanggalStatistik = view.findViewById(R.id.etTanggalStatistik)
        btnHari = view.findViewById(R.id.btnHari)
        btnMinggu = view.findViewById(R.id.btnMinggu)
        btnBulan = view.findViewById(R.id.btnBulan)
        btnTahun = view.findViewById(R.id.btnTahun)

        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        etTanggalStatistik.setText(sdf.format(selectedDate.time))

        // klik EditText → DatePicker
        etTanggalStatistik.setOnClickListener {
            val dp = DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    selectedDate.set(y, m, d)
                    etTanggalStatistik.setText(sdf.format(selectedDate.time))
                    tampilkanPieChart()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            dp.show()
        }

        btnHari.setOnClickListener { ubahFilter("HARI") }
        btnMinggu.setOnClickListener { ubahFilter("MINGGU") }
        btnBulan.setOnClickListener { ubahFilter("BULAN") }
        btnTahun.setOnClickListener { ubahFilter("TAHUN") }

        updateButtonUI()
        tampilkanPieChart()

        return view
    }

    private fun ubahFilter(mode: String) {
        filterMode = mode
        updateButtonUI()
        tampilkanPieChart()
    }

    private fun updateButtonUI() {
        val active = ContextCompat.getColor(requireContext(), R.color.pink)
        val inactive = ContextCompat.getColor(requireContext(), R.color.whitePink)

        btnHari.setBackgroundColor(if (filterMode == "HARI") active else inactive)
        btnMinggu.setBackgroundColor(if (filterMode == "MINGGU") active else inactive)
        btnBulan.setBackgroundColor(if (filterMode == "BULAN") active else inactive)
        btnTahun.setBackgroundColor(if (filterMode == "TAHUN") active else inactive)
    }

    private fun filterByDate(list: List<Transaction>): List<Transaction> {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

        return list.filter { trans ->
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(trans.tanggal) ?: Calendar.getInstance().time

            when (filterMode) {
                "HARI" -> cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)

//                "MINGGU" -> {
//                    val diffDays = ((selectedDate.timeInMillis - cal.timeInMillis) / (1000 * 60 * 60 * 24))
//                    diffDays in 0..6
//                }
                "MINGGU" -> {
                    // hitung awal minggu (Senin)
                    val startOfWeek = selectedDate.clone() as Calendar
                    startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)

                    // hitung akhir minggu (Minggu)
                    val endOfWeek = startOfWeek.clone() as Calendar
                    endOfWeek.add(Calendar.DAY_OF_WEEK, 6)

                    !cal.before(startOfWeek) && !cal.after(endOfWeek)
                }

                "BULAN" -> cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)

                "TAHUN" -> cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)

                else -> true
            }
        }
    }

    private fun tampilkanPieChart() {
        val allTrans = dbHelper.getAllTransaksi()
        val transaksiList = filterByDate(allTrans)

        val mapKategori = mutableMapOf(
            "Makanan" to 0,
            "Belanja" to 0,
            "Transportasi" to 0,
            "Hiburan" to 0,
            "Tagihan" to 0,
            "Lainnya" to 0
        )

        transaksiList.filter { it.jenis == "Pengeluaran" }.forEach { t ->
            mapKategori[t.kategori] = (mapKategori[t.kategori] ?: 0) + t.nominal
        }

        val entries = mapKategori.filter { it.value > 0 }.map { PieEntry(it.value.toFloat(), it.key) }

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

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.setUsePercentValues(true)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = filterMode
        pieChart.setCenterTextSize(16f)
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.animateY(1100)
        pieChart.legend.textSize = 13f
        pieChart.legend.isWordWrapEnabled = true
        pieChart.invalidate()
    }
}