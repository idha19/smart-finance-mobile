package com.example.dompetku.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.Model.Transaction
import com.example.dompetku.R

class TransactionAdapter(private var transactions: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

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
}