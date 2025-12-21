package com.example.dompetku.Adapter

import android.content.Context
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.Model.Kategori
import com.example.dompetku.Model.Transaction
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val context: Context,
    private var originalList: MutableList<Transaction>,
    private val onEditClick: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val db = DatabaseHelper(context)

    private var groupedList: MutableList<Any> = mutableListOf()
    private val checkedPositions = mutableSetOf<Int>()

    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    init {
        rebuildList()
    }

    fun getCheckedCount(): Int = checkedPositions.size

    override fun getItemViewType(position: Int): Int {
        return if (groupedList[position] is String) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.header_tanggal, parent, false)
            HeaderHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.transaksi_item, parent, false)
            ItemHolder(v)
        }
    }

    override fun getItemCount(): Int = groupedList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = groupedList[position]

        if (holder is HeaderHolder) holder.bind(item as String)
        else if (holder is ItemHolder) holder.bind(item as Transaction, position)
    }

    // ========================= HEADER HOLDER =========================
    inner class HeaderHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvHeader: TextView = v.findViewById(R.id.textTanggalHeader)
        fun bind(text: String) { tvHeader.text = text }
    }

    // ========================= ITEM HOLDER =========================
    inner class ItemHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val judul: TextView = v.findViewById(R.id.textJudul)
        private val kategori: TextView = v.findViewById(R.id.textKategori)
        private val nominal: TextView = v.findViewById(R.id.textNominal)
        private val jenis: TextView = v.findViewById(R.id.textJenis)
        private val check: CheckBox = v.findViewById(R.id.checkBoxKategori)
        private val edit: ImageView = v.findViewById(R.id.btnEditKategori)

        fun bind(t: Transaction, realPos: Int) {

            judul.text = t.catatan
            jenis.text = t.jenis

            // ======== Atur warna & prefix nominal berdasarkan jenis ========
            val prefix = if (t.jenis.equals("Pemasukkan", ignoreCase = true)) {
                nominal.setTextColor(context.getColor(R.color.masuk))
                "+Rp "
            } else {
                nominal.setTextColor(context.getColor(R.color.keluar))
                "-Rp "
            }

            fun formatRupiah(nominal: Long): String {
                return String.format("%,d", nominal).replace(",", ".")
            }

            nominal.text = "$prefix${formatRupiah(t.nominal)}"

            // ===============================================================

            kategori.text =
                db.getAllKategori().find { it.idKategori == t.idKategori }?.nama ?: "-"

            check.setOnCheckedChangeListener(null)
            check.isChecked = checkedPositions.contains(realPos)

            check.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) checkedPositions.add(realPos)
                else checkedPositions.remove(realPos)
            }

            edit.setOnClickListener { onEditClick(t) }
        }

    }

    // ========================= GROUPING DATE =========================
    private fun rebuildList() {
        groupedList.clear()

        val df = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val today = Calendar.getInstance()

        val map = originalList.groupBy { it.tanggal }
        val sortedDates = map.keys.sortedByDescending { df.parse(it) }

        for (tgl in sortedDates) {
            groupedList.add(getHeaderLabel(today, df.parse(tgl)!!))
            groupedList.addAll(map[tgl]!!)
        }
    }

    private fun getHeaderLabel(today: Calendar, date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }

        val df = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val dayName = SimpleDateFormat("EEEE", Locale("id", "ID")).format(date)

        return when {
            isSameDay(today, cal) -> "Hari ini, ${df.format(date)}"
            isYesterday(today, cal) -> "Kemarin, ${df.format(date)}"
            else -> "$dayName, ${df.format(date)}"
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean =
        c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

    private fun isYesterday(today: Calendar, other: Calendar): Boolean {
        val cal = today.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(cal, other)
    }

    private fun formatRupiah(n: Int) =
        "Rp " + String.format("%,d", n).replace(",", ".")

    // ========================= DELETE CHECKED FIXED =========================
    fun deleteChecked() {
        val itemsToRemove = checkedPositions.map { groupedList[it] }
            .filterIsInstance<Transaction>()

        itemsToRemove.forEach { trx ->
            db.deleteTransaksi(trx.id)
            originalList.remove(trx)
        }

        checkedPositions.clear()
        rebuildList()
        notifyDataSetChanged()
    }

    // ========================= SEARCH =========================
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(s: CharSequence?): FilterResults {
                val filtered = if (s.isNullOrBlank()) originalList
                else originalList.filter {
                    it.catatan.lowercase().contains(s.toString().lowercase()) ||
                            it.jenis.lowercase().contains(s.toString().lowercase()) ||
                            it.tanggal.lowercase().contains(s.toString().lowercase())
                }

                return FilterResults().apply { values = filtered }
            }

            override fun publishResults(s: CharSequence?, results: FilterResults?) {
                originalList = (results?.values as List<Transaction>).toMutableList()
                rebuildList()
                notifyDataSetChanged()
            }
        }
    }
}