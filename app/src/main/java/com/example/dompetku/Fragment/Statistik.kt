package com.example.dompetku.Fragment

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.color.MaterialColors
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Statistik : Fragment() {

    // ===================== Variabel UI =====================
    private lateinit var pieChart: PieChart
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var tvTanggal: TextView
    private lateinit var spinnerPeriode: Spinner
    private lateinit var spinnerUrutan: Spinner
    private lateinit var layoutKategori: LinearLayout // akan gunakan cardKategoriBesar sebagai container (keep header index 0)

    // ===================== Variabel Filter =====================
    private var selectedDate: Calendar = Calendar.getInstance()
    private var filterMode = "HARI"    // default filter = HARI
    private var urutan = "Terbanyak"   // default urutan = Terbanyak

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        val view = inflater.inflate(R.layout.fragment_statistik, container, false)

        // ===================== Binding UI =====================
        pieChart = view.findViewById(R.id.pieChart)
        dbHelper = DatabaseHelper(requireContext())
        tvTanggal = view.findViewById(R.id.tvTanggal)
        spinnerPeriode = view.findViewById(R.id.spinnerPeriode)
        spinnerUrutan = view.findViewById(R.id.spinnerUrutan)

        // CardKategoriBesar di XML kita gunakan sebagai container. Header berada di child index 0.
        layoutKategori = view.findViewById(R.id.cardKategoriBesar)


        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Set tanggal default hari ini (format singkat bahasa Indonesia: "Sab, 22 Nov 2025")
        val sdfShort = SimpleDateFormat("EEE, dd MMM yyyy", Locale("id", "ID"))
        tvTanggal.text = sdfShort.format(selectedDate.time)

        // ===================== DatePicker =====================
        tvTanggal.setOnClickListener {
            val dp = DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    // Update selectedDate ketika user memilih tanggal
                    selectedDate.set(y, m, d)
                    tvTanggal.text = sdfShort.format(selectedDate.time)

                    // Refresh PieChart & Kategori
                    tampilkanPieChart()
                    tampilkanKategori()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            dp.show()
        }

        // ===================== Spinner Periode =====================
        val periodeOptions = arrayOf("Hari", "Minggu", "Bulan", "Tahun")
        val adapterPeriode = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            periodeOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerPeriode.adapter = adapterPeriode
        spinnerPeriode.setSelection(0)

        val textColorOnPrimary = MaterialColors.getColor(
            spinnerPeriode,
            com.google.android.material.R.attr.colorOnPrimary,
            0
        )
        (spinnerPeriode.selectedView as? TextView)?.setTextColor(textColorOnPrimary)

        spinnerPeriode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(textColorOnPrimary)

                filterMode = when (periodeOptions[position].uppercase(Locale.getDefault())) {
                    "HARI" -> "HARI"
                    "MINGGU" -> "MINGGU"
                    "BULAN" -> "BULAN"
                    "TAHUN" -> "TAHUN"
                    else -> "HARI"
                }
                tampilkanPieChart()
                tampilkanKategori()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        // ===================== Spinner Urutan =====================
        val urutanOptions = arrayOf("Terbanyak", "Tersedikit")
        val adapterUrutan = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            urutanOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinnerUrutan.adapter = adapterUrutan
        spinnerUrutan.setSelection(0)

        (spinnerUrutan.selectedView as? TextView)?.setTextColor(textColorOnPrimary)

        spinnerUrutan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(textColorOnPrimary)

                urutan = urutanOptions[position]
                tampilkanPieChart()
                tampilkanKategori()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Konfigurasi awal PieChart minimal
        configurePieChart()

        // Tampilkan data pertama kali
        tampilkanPieChart()
        tampilkanKategori()

        return view
    }

    // ===================== Configure PieChart (basic settings) =====================
    private fun configurePieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setCenterTextSize(16f)
        pieChart.legend.isWordWrapEnabled = true

        val legendTextColor = MaterialColors.getColor(
            pieChart,
            com.google.android.material.R.attr.colorOnPrimary,
            0
        )
        pieChart.legend.textColor = legendTextColor
        pieChart.setEntryLabelColor(legendTextColor)
    }

    // ===================== Helper: parsing tanggal toleran =====================
    private fun parseDateLenient(dateStr: String?): Date? {
        if (dateStr == null) return null
        val formats = listOf(
            "yyyy-MM-dd",
            "dd MMMM yyyy",
            "dd MMM yyyy",
            "dd-MM-yyyy",
            "yyyy/MM/dd",
            "dd/MM/yyyy"
        )
        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale("id", "ID"))
                sdf.isLenient = false
                return sdf.parse(dateStr)
            } catch (e: Exception) {
                // coba format lain
            }
        }
        return null
    }

    // ===================== Filter Transaksi Berdasarkan Tanggal =====================
    private fun filterByDate(list: List<Transaction>): List<Transaction> {
        return list.filter { trans ->
            val parsed = parseDateLenient(trans.tanggal)
            if (parsed == null) {
                // kalau tidak bisa parse, abaikan transaksi tersebut (atau ubah sesuai kebutuhan)
                false
            } else {
                val cal = Calendar.getInstance()
                cal.time = parsed

                when (filterMode) {
                    "HARI" -> cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)

                    "MINGGU" -> {
                        val startWeek = selectedDate.clone() as Calendar
                        // pastikan startWeek ke awal minggu (mengikuti default locale)
                        startWeek.set(Calendar.DAY_OF_WEEK, startWeek.firstDayOfWeek)
                        val endWeek = startWeek.clone() as Calendar
                        endWeek.add(Calendar.DAY_OF_WEEK, 6)
                        !cal.before(startWeek) && !cal.after(endWeek)
                    }

                    "BULAN" -> cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                            cal.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)

                    "TAHUN" -> cal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)

                    else -> true
                }
            }
        }
    }

    // ===================== Tampilkan PieChart =====================
    private fun tampilkanPieChart() {
        val allTrans = dbHelper.getAllTransaksi()
        val transaksiList = filterByDate(allTrans)

        val kategoriMap = dbHelper.getAllKategori().associateBy { it.idKategori }

        var totalPemasukan = 0
        var totalPengeluaran = 0
        var totalSemua = 0

        // Gabungkan income + expense per kategori
        val mapKategori = mutableMapOf<String, Int>()
        transaksiList.forEach { t ->
            val namaKategori = kategoriMap[t.idKategori]?.nama ?: "Lainnya"
            mapKategori[namaKategori] = (mapKategori[namaKategori] ?: 0) + t.nominal
            totalSemua += t.nominal

            if (t.jenis.equals("Pemasukkan", true)) {
                totalPemasukan += t.nominal
            } else {
                totalPengeluaran += t.nominal
            }
        }

        // ==== Jika tidak ada data ====
        if (mapKategori.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = "Tidak ada data"
            pieChart.invalidate()
            return
        }

        val sorted = if (urutan == "Terbanyak")
            mapKategori.entries.sortedByDescending { it.value }
        else mapKategori.entries.sortedBy { it.value }

        val entries = sorted.map { PieEntry(it.value.toFloat(), it.key) }

        val colors = mutableListOf<Int>().apply {
            addAll(ColorTemplate.MATERIAL_COLORS.toList())
            addAll(ColorTemplate.COLORFUL_COLORS.toList())
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.valueTextSize = 13f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))

        pieChart.centerText = filterMode
        pieChart.data = data
        pieChart.animateY(900)
        pieChart.invalidate()
    }


    // ===================== Tampilkan Daftar Kategori di Bawah PieChart =====================
    private fun tampilkanKategori() {
        // Kita menggunakan cardKategoriBesar (layoutKategori) sebagai parent.
        // Child index 0: header (Kategori + dropdown). Kita hapus semua child setelah index 0, lalu tambahkan dynamic items.
        try {
            while (layoutKategori.childCount > 1) {
                // Remove everything after header
                layoutKategori.removeViewAt(1)
            }
        } catch (e: Exception) {
            // ignore
        }

        val allTrans = dbHelper.getAllTransaksi()
        val transaksiList = filterByDate(allTrans)
        val kategoriMap = dbHelper.getAllKategori().associateBy { it.idKategori }

        // Hitung total per kategori dan total nominal
        val mapKategori = mutableMapOf<String, Int>()
        var totalNominal = 0
        transaksiList.forEach { t ->
            val namaKategori = kategoriMap[t.idKategori]?.nama ?: "Lainnya"
            mapKategori[namaKategori] = (mapKategori[namaKategori] ?: 0) + t.nominal
            totalNominal += t.nominal
        }

        // Jika kosong, tampilkan pesan singkat
        if (mapKategori.isEmpty()) {
            val tvKosong = TextView(requireContext()).apply {
                text = "Tidak ada data untuk periode ini."
                val textColor = MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnPrimary,
                    0
                )
                setTextColor(textColor)

                textSize = 14f
                setPadding(8, 18, 8, 18)
            }
            layoutKategori.addView(tvKosong)
            return
        }

        // Sort kategori
        val sortedKategori = if (urutan.equals("Terbanyak", ignoreCase = true)) {
            mapKategori.entries.sortedByDescending { it.value }
        } else {
            mapKategori.entries.sortedBy { it.value }
        }

        // ===================== Generate View Dinamis Sesuai Template =====================
        for ((nama, jumlah) in sortedKategori) {
            val persen = if (totalNominal > 0) (jumlah * 100 / totalNominal) else 0

            // LinearLayout utama horizontal
            val itemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(12) }
                setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6))
            }

//            // Card kecil untuk persen (kotak warna)
//            val card = LinearLayout(requireContext()).apply {
//                orientation = LinearLayout.VERTICAL
//                layoutParams = LinearLayout.LayoutParams(dpToPx(40), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
//                    rightMargin = dpToPx(10)
//                }
//                setBackgroundColor(Color.parseColor("#442B7E"))
//                setPadding(4, 4, 4, 4)
//                gravity = Gravity.CENTER
//            }
//            val tvPersen = TextView(requireContext()).apply {
//                text = "$persen%"
//                setTextColor(Color.WHITE)
//                textSize = 12f
//                gravity = Gravity.CENTER
//            }
//            card.addView(tvPersen)

            // LinearLayout vertikal untuk judul + progress bar
            val verticalLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvJudul = TextView(requireContext()).apply {
                text = nama
                val textColor = MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnPrimary,
                    0
                )
                setTextColor(textColor)
                setTypeface(null, Typeface.BOLD)
                textSize = 14f
            }

            val progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = persen
                progressTintList = ColorStateList.valueOf(Color.parseColor("#FFD700")) // atau #BBE8C1 sesuai selera
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EEEEEE"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(8)
                ).apply { topMargin = dpToPx(6) }
            }
            verticalLayout.addView(tvJudul)
            verticalLayout.addView(progressBar)

            // TextView nominal (kanan)
            val tvHarga = TextView(requireContext()).apply {
                text = "Rp ${formatRupiah(jumlah)}"
                val textColor = MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnPrimary,
                    0
                )
                setTextColor(textColor)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { leftMargin = dpToPx(10) }
                gravity = Gravity.CENTER_VERTICAL
            }

            // Gabungkan
//            itemLayout.addView(card)
            itemLayout.addView(verticalLayout)
            itemLayout.addView(tvHarga)

            // Tambah ke container (setelah header)
            layoutKategori.addView(itemLayout)
        }
    }

    // ===================== Helper Format Rupiah =====================
    private fun formatRupiah(nominal: Int): String {
        return String.format("%,d", nominal).replace(",", ".")
    }

    // ===================== Helper: dp to px =====================
    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}