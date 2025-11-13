package com.example.dompetku.Fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.dompetku.Model.Transaction
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class New : Fragment() {

    interface NewFragmentListener {
        fun onTransactionAdded(transaction: Transaction)
    }

    private var listener: NewFragmentListener? = null
    private lateinit var inputNominal: EditText
    private lateinit var checkMasuk: CheckBox
    private lateinit var checkKeluar: CheckBox
    private lateinit var spinnerKategori: Spinner
    private lateinit var inputCatatan: EditText
    private lateinit var textTanggal: TextView
    private lateinit var btnSimpan: Button
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new, container, false)

        // inisialisasi view
        inputNominal = view.findViewById(R.id.inputNominal)
        checkMasuk = view.findViewById(R.id.CheckBox1)
        checkKeluar = view.findViewById(R.id.CheckBox2)
        spinnerKategori = view.findViewById(R.id.spinnerKategori)
        inputCatatan = view.findViewById(R.id.task)
        textTanggal = view.findViewById(R.id.textTanggal)
        btnSimpan = view.findViewById(R.id.btnSimpan)

        dbHelper = DatabaseHelper(requireContext())

        // set tanggal otomatis ke hari ini
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        textTanggal.text = dateFormat.format(calendar.time)

        // set OnClickListener untuk tanggal (kalau mau bisa diubah)
        textTanggal.setOnClickListener {
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    textTanggal.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // membuat checkbox saling eksklusif
        checkMasuk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkKeluar.isChecked = false
        }
        checkKeluar.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) checkMasuk.isChecked = false
        }

        // tombol simpan
        btnSimpan.setOnClickListener {
            val nominalStr = inputNominal.text.toString().trim()
            val catatan = inputCatatan.text.toString().trim()
            val kategori = spinnerKategori.selectedItem.toString()

            if (nominalStr.isEmpty()) {
                Toast.makeText(requireContext(), "Masukkan nominal!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jenis = when {
                checkMasuk.isChecked -> "Pemasukkan"
                checkKeluar.isChecked -> "Pengeluaran"
                else -> {
                    Toast.makeText(requireContext(), "Pilih jenis transaksi!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val nominal = nominalStr.toInt()
            val tanggal = textTanggal.text.toString()

            // buat objek Transaksi
            val transaksi = Transaction(
                id = 0, // id otomatis di SQLite
                jenis = jenis,
                nominal = nominal,
                tanggal = tanggal,
                catatan = catatan,
                kategori = kategori
            )

            val id = dbHelper.insertTransaksi(transaksi)
            if (id > 0) {
                Toast.makeText(requireContext(), "Berhasil menyimpan transaksi", Toast.LENGTH_SHORT).show()

                listener?.onTransactionAdded(transaksi)
                // reset form
                inputNominal.text.clear()
                inputCatatan.text.clear()
                checkMasuk.isChecked = false
                checkKeluar.isChecked = false
                spinnerKategori.setSelection(0)
                textTanggal.text = dateFormat.format(Calendar.getInstance().time)
            } else {
                Toast.makeText(requireContext(), "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
