package com.example.dompetku.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.Model.Transaction
import com.example.dompetku.R

class TransactionAdapterDashboard(
    private val context: Context,
    private var listData: MutableList<Transaction>
) : RecyclerView.Adapter<TransactionAdapterDashboard.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val iconContainer: LinearLayout = v.findViewById(R.id.iconContainer)
        val iconArrow: ImageView = v.findViewById(R.id.iconArrow)
        val textJudul: TextView = v.findViewById(R.id.textJudul)
        val textCatatan: TextView = v.findViewById(R.id.textCatatan)
        val textNominal: TextView = v.findViewById(R.id.textNominal)
        val tanggal: TextView = v.findViewById(R.id.tanggal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_terbaru, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val t = listData[position]

        // teks
        holder.textJudul.text = t.jenis.replaceFirstChar { it.uppercase() }
        holder.textCatatan.text = t.catatan
        holder.tanggal.text = t.tanggal

        holder.textNominal.text = "Rp. " + String.format("%,d", t.nominal).replace(",", ".")

        // LOGIKA WARNA + ICON
        if (t.jenis.lowercase() == "pemasukkan" || t.jenis.lowercase() == "pemasukan") {
            // background
            holder.iconContainer.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.bgMasuk)

            // icon masuk
            holder.iconArrow.setImageResource(R.drawable.ic_masuk)

            // warna text
            holder.textNominal.setTextColor(
                ContextCompat.getColor(context, R.color.masuk)
            )

        } else {
            // background
            holder.iconContainer.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.bgKeluar)

            // icon keluar
            holder.iconArrow.setImageResource(R.drawable.ic_keluar)

            // warna text
            holder.textNominal.setTextColor(
                ContextCompat.getColor(context, R.color.keluar)
            )
        }
    }

    // untuk update data ketika pilih tanggal
    fun updateData(list: List<Transaction>) {
        listData.clear()
        listData.addAll(list)
        notifyDataSetChanged()
    }
}