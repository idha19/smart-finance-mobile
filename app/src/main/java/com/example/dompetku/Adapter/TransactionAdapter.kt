package com.example.dompetku.Adapter

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.Model.Transaction
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionAdapter(val context: Context, private var transactions: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private val dbHelper = DatabaseHelper(context)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJenis: TextView = view.findViewById(R.id.textJenis)
        val tvNominal: TextView = view.findViewById(R.id.textNominal)
        val tvTanggal: TextView = view.findViewById(R.id.textTanggal)
        val tvCatatan: TextView = view.findViewById(R.id.textCatatan)
        val iconKategori: ImageView = view.findViewById(R.id.iconKategori)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaksi_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val t = transactions[position]
        holder.tvJenis.text = t.jenis
        holder.tvNominal.text = "Rp ${formatRupiah(t.nominal)}"
        holder.tvTanggal.text = t.tanggal
        holder.tvCatatan.text = t.catatan

        val iconRes = when (t.kategori) {
            "Makanan" -> R.drawable.ic_makanan
            "Transportasi" -> R.drawable.ic_transportasi
            "Belanja" -> R.drawable.ic_belanja
            "Hiburan" -> R.drawable.ic_hiburan
            "Tagihan" -> R.drawable.ic_tagihan
            "Gaji" -> R.drawable.ic_gaji
            "Lainnya" -> R.drawable.ic_lainnya
            else -> R.drawable.ic_home // fallback jika kategori tidak cocok
        }
        holder.iconKategori.setImageResource(iconRes)
    }


    override fun getItemCount(): Int = transactions.size

    private fun formatRupiah(nominal: Int): String {
        return String.format("%,d", nominal).replace(",", ".")
    }

    // Fungsi untuk menambahkan transaksi baru
    fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction) // tambah di awal list
        notifyItemInserted(0)
    }

    // Fungsi untuk update seluruh list (opsional)
    fun updateList(newList: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newList)
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        val transaksi = transactions[position]
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Hapus Transaksi")
        builder.setMessage("Yakin ingin menghapus transaksi ini?")
        builder.setPositiveButton("Ya") { _, _ ->
            dbHelper.deleteTransaksi(transaksi.id)
            transactions.removeAt(position)
            notifyItemRemoved(position)
        }
        builder.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
            notifyItemChanged(position)
        }
        builder.show()
    }

    fun editItem(position: Int) {
        val transaksi = transactions[position]
        val dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_edit, null)

        val etNominal = dialogView.findViewById<EditText>(R.id.etNominal)
        val checkMasuk = dialogView.findViewById<CheckBox>(R.id.CheckBox1)
        val checkKeluar = dialogView.findViewById<CheckBox>(R.id.CheckBox2)
        val spinnerKategori = dialogView.findViewById<Spinner>(R.id.spinnerKategori)
        val etCatatan = dialogView.findViewById<EditText>(R.id.etCatatan)
        val etTanggal = dialogView.findViewById<EditText>(R.id.etTanggal)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)

        // set nilai awal
        etNominal.setText(transaksi.nominal.toString())
        etCatatan.setText(transaksi.catatan)
        etTanggal.setText(transaksi.tanggal)
        checkMasuk.isChecked = transaksi.jenis == "Pemasukkan"
        checkKeluar.isChecked = transaksi.jenis == "Pengeluaran"

        // setup Spinner
        val kategoriList = listOf("Makanan", "Transportasi", "Belanja", "Hiburan", "Tagihan", "Gaji", "Lainnya")
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, kategoriList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategori.adapter = adapter
        val selectedIndex = kategoriList.indexOf(transaksi.kategori).takeIf { it >= 0 } ?: 0
        spinnerKategori.setSelection(selectedIndex)

        // TextWatcher untuk nominal
        var current = ""
        etNominal.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s.toString() != current) {
                    etNominal.removeTextChangedListener(this)
                    val clean = s.toString().replace(".", "")
                    if (clean.isNotEmpty()) {
                        try {
                            val value = clean.toLong()
                            val formatted = String.format("%,d", value).replace(",", ".")
                            current = formatted
                            etNominal.setText(formatted)
                            etNominal.setSelection(formatted.length)
                        } catch (_: Exception) {}
                    }
                    etNominal.addTextChangedListener(this)
                }
            }
        })

        // Checkbox eksklusif
        checkMasuk.setOnCheckedChangeListener { _, isChecked -> if (isChecked) checkKeluar.isChecked = false }
        checkKeluar.setOnCheckedChangeListener { _, isChecked -> if (isChecked) checkMasuk.isChecked = false }

        // DatePicker
        etTanggal.isFocusable = false
        etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            try { calendar.time = dateFormat.parse(etTanggal.text.toString()) ?: calendar.time } catch (_: Exception) {}
            DatePickerDialog(context,
                { _, y, m, d -> calendar.set(y, m, d); etTanggal.setText(dateFormat.format(calendar.time)) },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(context).setView(dialogView).create()

        btnSimpan.setOnClickListener {
            val nominalStr = etNominal.text.toString().replace(".", "").trim()
            val catatan = etCatatan.text.toString().trim()
            val kategori = spinnerKategori.selectedItem.toString()
            val jenis = when {
                checkMasuk.isChecked -> "Pemasukkan"
                checkKeluar.isChecked -> "Pengeluaran"
                else -> { Toast.makeText(context, "Pilih jenis transaksi!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            }

            if (nominalStr.isEmpty()) { Toast.makeText(context, "Masukkan nominal!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (jenis == "Pemasukkan" && kategori !in listOf("Gaji", "Lainnya")) { Toast.makeText(context, "Kategori tidak valid untuk pemasukkan!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (jenis == "Pengeluaran" && kategori == "Gaji") { Toast.makeText(context, "Kategori tidak valid untuk pengeluaran!", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            transaksi.nominal = nominalStr.toInt()
            transaksi.jenis = jenis
            transaksi.kategori = kategori
            transaksi.catatan = catatan
            transaksi.tanggal = etTanggal.text.toString()

            dbHelper.updateTransaksi(transaksi)
            transactions[position] = transaksi
            notifyItemChanged(position)
            dialog.dismiss()
        }

        dialog.show()
    }
}