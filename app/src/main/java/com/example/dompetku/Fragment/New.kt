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

class New : Fragment() {

    interface NewFragmentListener {
        fun onTransactionAdded(transaction: Transaction)
    }

    private var listener: NewFragmentListener? = null

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
    private lateinit var listView: ListView

    private lateinit var db: DatabaseHelper
    private var kategoriList = mutableListOf<Kategori>()
    private var selectedKategoriId: Int? = null
    private var jenisTransaksi = "Pemasukkan"
    private var popupWindow: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_new, container, false)

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

        db = DatabaseHelper(requireContext())
        kategoriList = db.getAllKategori().toMutableList()

        // Default tanggal
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        inputTanggal.setText(dateFormat.format(calendar.time))

        val textColorTanggal = MaterialColors.getColor(
            inputTanggal,
            com.google.android.material.R.attr.colorOnPrimary,
            0
        )

        val textColorOnPrimary = MaterialColors.getColor(
            inputKategori,
            com.google.android.material.R.attr.colorOnPrimary,
            0
        )

        inputKategori.setTextColor(textColorOnPrimary)
        inputJumlah.setTextColor(textColorOnPrimary)
        inputCatatan.setTextColor(textColorOnPrimary)

        inputTanggal.setTextColor(textColorTanggal)

        inputTanggal.setOnClickListener {
            val dp = DatePickerDialog(requireContext(), { _, y, m, d ->
                calendar.set(y, m, d)
                inputTanggal.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dp.show()
        }

        // Format rupiah
        var current = ""
        inputJumlah.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    inputJumlah.removeTextChangedListener(this)
                    val clean = s.toString().replace("[^0-9]".toRegex(), "")
                    if (clean.isNotEmpty()) {
                        current = "Rp " + String.format("%,d", clean.toLong()).replace(",", ".")
                        inputJumlah.setText(current)
                        inputJumlah.setSelection(current.length)
                    }
                    inputJumlah.addTextChangedListener(this)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // === Dropdown kategori ===
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

        // Switch tombol
        cardMasuk.setOnClickListener {
            jenisTransaksi = "Pemasukkan"
            setCardActive(cardMasuk, cardKeluar, txtMasuk, txtKeluar)
        }
        cardKeluar.setOnClickListener {
            jenisTransaksi = "Pengeluaran"
            setCardActive(cardKeluar, cardMasuk, txtKeluar, txtMasuk)
        }

        btnSimpan.setOnClickListener { simpanTransaksi() }

        // --- Di onCreateView ---
        initKategoriDropdown() // Panggil ini SATU KALI

// Input kategori klik → dropdown muncul
        inputKategori.setOnClickListener { showKategoriDropdown() }
        btnKategoriDropdown.setOnClickListener { showKategoriDropdown() }

// Filter saat mengetik
        inputKategori.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Pastikan popupWindow sudah diinisialisasi
                if (popupWindow != null && !popupWindow!!.isShowing) {
                    popupWindow!!.showAsDropDown(inputKategori)
                }
                updateKategoriDropdown(s.toString())
                selectedKategoriId = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })


        return v
    }


    private fun initKategoriDropdown() {
        listView = ListView(requireContext())
        popupWindow = PopupWindow(listView, inputKategori.width, ViewGroup.LayoutParams.WRAP_CONTENT, false)
        popupWindow!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow!!.isOutsideTouchable = true
        popupWindow!!.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

        listView.setOnItemClickListener { _, _, pos, _ ->
            val item = listView.adapter.getItem(pos).toString()
            inputKategori.setText(item)
            selectedKategoriId = kategoriList.first { it.nama == item }.idKategori
            popupWindow?.dismiss()
        }

        // TextWatcher untuk filter
        inputKategori.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateKategoriDropdown(s.toString())
                selectedKategoriId = null
                if (!(popupWindow?.isShowing ?: false)) {
                    popupWindow!!.width = inputKategori.width
                    popupWindow!!.showAsDropDown(inputKategori)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        // Klik EditText atau tombol dropdown → tampilkan popup
        inputKategori.setOnClickListener { showKategoriDropdown() }
        btnKategoriDropdown.setOnClickListener { showKategoriDropdown() }
    }

    private fun showKategoriDropdown() {
        if (popupWindow != null && !popupWindow!!.isShowing) {
            popupWindow!!.width = inputKategori.width
            popupWindow!!.showAsDropDown(inputKategori)
        }
    }

    private fun updateKategoriDropdown(filter: String) {
        val filtered = kategoriList.filter { it.nama.contains(filter, ignoreCase = true) }.map { it.nama }
        if (filtered.isEmpty()) {
            popupWindow?.dismiss()
            return
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, filtered)
        (popupWindow?.contentView as? ListView)?.adapter = adapter
    }

    private fun simpanTransaksi() {
        val kategoriText = inputKategori.text.toString().trim()
        if (kategoriText.isEmpty()) {
            toast("Kategori tidak boleh kosong!")
            return
        }

        val nominal = inputJumlah.text.toString().replace("[Rp .]".toRegex(), "").toLongOrNull()
        if (nominal == null || nominal <= 0) {
            toast("Nominal tidak valid!")
            return
        }

        var kategoriId = selectedKategoriId
        if (kategoriId == null) {
            val existingKategori = kategoriList.firstOrNull() {
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

        val tr = Transaction(
            jenis = jenisTransaksi,
            nominal = nominal,
            tanggal = inputTanggal.text.toString(),
            catatan = inputCatatan.text.toString(),
            idKategori = kategoriId
        )

//        db.insertTransaksi(tr)
//        toast("Transaksi berhasil disimpan!")
//        resetForm()
//        listener?.onTransactionAdded(tr)


        db.insertTransaksi(tr)
        toast("Transaksi berhasil disimpan!")

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, Transaksi())
            .commit()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun resetForm() {
        inputKategori.setText("")
        inputJumlah.setText("")
        inputCatatan.setText("")
        selectedKategoriId = null
        popupWindow?.dismiss()
    }

    private fun setCardActive(a: CardView, b: CardView, ta: TextView, tb: TextView) {
        a.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.utamaUt))
        b.setCardBackgroundColor(MaterialColors.getColor(b, com.google.android.material.R.attr.colorSecondary))
        ta.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        tb.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }
}