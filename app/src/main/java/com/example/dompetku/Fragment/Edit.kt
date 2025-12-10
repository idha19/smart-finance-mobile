package com.example.dompetku.Fragment

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dompetku.Model.Kategori
import com.example.dompetku.Model.Transaction
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.*

class Edit : Fragment() {

    companion object {
        private const val ARG_TRANSACTION = "transaction"

        fun newInstance(tr: Transaction): Edit {
            val f = Edit()
            val b = Bundle()
            b.putSerializable(ARG_TRANSACTION, tr)
            f.arguments = b
            return f
        }
    }

    private lateinit var transaction: Transaction
    private lateinit var db: DatabaseHelper

    private lateinit var cardMasuk: CardView
    private lateinit var cardKeluar: CardView
    private lateinit var txtMasuk: TextView
    private lateinit var txtKeluar: TextView

    private lateinit var inputKategori: EditText
    private lateinit var btnKategoriDropdown: ImageView
    private lateinit var inputJumlah: EditText
    private lateinit var inputCatatan: EditText
    private lateinit var inputTanggal: EditText
    private lateinit var btnSimpan: CardView

    private var kategoriList = mutableListOf<Kategori>()
    private var selectedKategoriId: Int? = null
    private var jenisTransaksi = "Pemasukkan"
    private var popupWindow: PopupWindow? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transaction = arguments?.getSerializable(ARG_TRANSACTION) as Transaction
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val v = inflater.inflate(R.layout.fragment_edit, container, false)

        db = DatabaseHelper(requireContext())
        kategoriList = db.getAllKategori().toMutableList()

        cardMasuk = v.findViewById(R.id.cardMasuk)
        cardKeluar = v.findViewById(R.id.cardKeluar)
        txtMasuk = v.findViewById(R.id.btnMasukText)
        txtKeluar = v.findViewById(R.id.btnKeluarText)

        inputKategori = v.findViewById(R.id.inputKategori)
        btnKategoriDropdown = v.findViewById(R.id.btnKategoriDropdown)
        inputJumlah = v.findViewById(R.id.inputJumlah)
        inputCatatan = v.findViewById(R.id.inputCatatan)
        inputTanggal = v.findViewById(R.id.inputTanggal)
        btnSimpan = v.findViewById(R.id.btnSimpan)

        val btnBack = v.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        jenisTransaksi = transaction.jenis
        selectedKategoriId = transaction.idKategori

        inputCatatan.setText(transaction.catatan)
        inputTanggal.setText(transaction.tanggal)
        inputJumlah.setText(formatRupiah(transaction.nominal))

        val textColorOnPrimary = MaterialColors.getColor(
            inputKategori,
            com.google.android.material.R.attr.colorOnPrimary,
            0
        )

        inputKategori.setTextColor(textColorOnPrimary)
        inputJumlah.setTextColor(textColorOnPrimary)
        inputCatatan.setTextColor(textColorOnPrimary)


        inputKategori.setText(
            kategoriList.find { it.idKategori == selectedKategoriId }?.nama ?: ""
        )

        updateJenisUI()

        cardMasuk.setOnClickListener {
            jenisTransaksi = "Pemasukkan"
            updateJenisUI()
        }

        cardKeluar.setOnClickListener {
            jenisTransaksi = "Pengeluaran"
            updateJenisUI()
        }

        inputTanggal.setOnClickListener {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
            val calendar = Calendar.getInstance()

            try {
                calendar.time = sdf.parse(inputTanggal.text.toString()) ?: Date()
            } catch (_: Exception) {}

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    calendar.set(y, m, d)
                    inputTanggal.setText(sdf.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        val textColorTanggal = MaterialColors.getColor(
            inputTanggal,
            com.google.android.material.R.attr.colorOnPrimary,
            0
        )

        inputTanggal.setTextColor(textColorTanggal)

        var current = ""
        inputJumlah.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    inputJumlah.removeTextChangedListener(this)

                    val clean = s.toString().replace("[^\\d]".toRegex(), "")
                    if (clean.isNotEmpty()) {
                        val angka = clean.toLong()
                        val formatted = String.format("%,d", angka).replace(",", ".")
                        current = "Rp $formatted"
                        inputJumlah.setText(current)
                        inputJumlah.setSelection(current.length)
                    } else {
                        current = ""
                        inputJumlah.setText("")
                    }

                    inputJumlah.addTextChangedListener(this)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        inputKategori.setOnClickListener { showKategoriDropdown() }
        btnKategoriDropdown.setOnClickListener { showKategoriDropdown() }

        inputKategori.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateKategoriDropdown(s.toString())
                selectedKategoriId = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSimpan.setOnClickListener { updateTransaksi() }

        return v
    }

    private fun showKategoriDropdown() {
        if (popupWindow != null && popupWindow!!.isShowing) return

        val listView = ListView(requireContext())
        popupWindow?.dismiss()
        popupWindow = PopupWindow(
            listView,
            inputKategori.width,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )

        popupWindow!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow!!.isOutsideTouchable = true
        popupWindow!!.isFocusable = false

        popupWindow!!.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow!!.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

        updateKategoriDropdown(inputKategori.text.toString())

        listView.setOnItemClickListener { _, _, pos, _ ->
            val item = listView.adapter.getItem(pos).toString()
            inputKategori.setText(item)
            selectedKategoriId = kategoriList.first { it.nama == item }.idKategori
            popupWindow?.dismiss()
        }

        popupWindow!!.showAsDropDown(inputKategori)
    }

    private fun updateKategoriDropdown(filter: String) {
        val listView = (popupWindow?.contentView as? ListView) ?: return

        val filtered = kategoriList.filter {
            it.nama.lowercase().contains(filter.lowercase())
        }.map { it.nama }

        if (filtered.isEmpty()) {
            popupWindow?.dismiss()
            return
        }

        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, filtered)
        listView.adapter = adapter

        if (!(popupWindow?.isShowing ?: false)) {
            popupWindow?.showAsDropDown(inputKategori)
        }
    }

    private fun updateTransaksi() {

        val kategoriText = inputKategori.text.toString().trim()
        if (kategoriText.isEmpty()) {
            Toast.makeText(requireContext(), "Kategori tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val nominal = inputJumlah.text.toString().replace("[Rp .]".toRegex(), "").toIntOrNull()
        if (nominal == null || nominal <= 0) {
            Toast.makeText(requireContext(), "Nominal tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        var kategoriId = selectedKategoriId
        if (kategoriId == null) {
            val existingKategori = kategoriList.firstOrNull {
                it.nama.equals(kategoriText, ignoreCase = true)
            }

            if (existingKategori != null) {
                kategoriId = existingKategori.idKategori
            } else {
                val newId = db.insertKategori(kategoriText)
                kategoriId = newId.toInt()
                kategoriList = db.getAllKategori().toMutableList()
            }
        }

        transaction.jenis = jenisTransaksi
        transaction.nominal = nominal
        transaction.tanggal = inputTanggal.text.toString()
        transaction.catatan = inputCatatan.text.toString()
        transaction.idKategori = kategoriId!!

        db.updateTransaksi(transaction)

        Toast.makeText(requireContext(), "Transaksi berhasil diupdate!", Toast.LENGTH_SHORT).show()

        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun setCardActive(a: CardView, b: CardView, ta: TextView, tb: TextView) {
        a.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.utamaUt))
        b.setCardBackgroundColor(
            MaterialColors.getColor(
                b,
                com.google.android.material.R.attr.colorSecondary,
                0
            )
        )
        ta.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        tb.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun updateJenisUI() {
        if (jenisTransaksi == "Pemasukkan") {
            setCardActive(cardMasuk, cardKeluar, txtMasuk, txtKeluar)
        } else {
            setCardActive(cardKeluar, cardMasuk, txtKeluar, txtMasuk)
        }
    }

    private fun formatRupiah(n: Int): String {
        val formatted = String.format("%,d", n).replace(",", ".")
        return "Rp $formatted"
    }
}