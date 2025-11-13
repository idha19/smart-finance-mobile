package com.example.dompetku.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.Adapter.TransactionAdapter
import com.example.dompetku.Model.Transaction
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper

class Transaksi : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaksi, container, false)

        recyclerView = view.findViewById(R.id.rvTransaksi) // pastikan di fragment_transaksi.xml ada RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        dbHelper = DatabaseHelper(requireContext())
        val transaksiList: MutableList<Transaction> = dbHelper.getAllTransaksi().toMutableList()

        adapter = TransactionAdapter(transaksiList)
        recyclerView.adapter = adapter

        // Jika ada transaksi baru lewat bundle
        arguments?.let { bundle ->
            val id = bundle.getInt("id", 0)
            val jenis = bundle.getString("jenis", "")
            val nominal = bundle.getInt("nominal", 0)
            val tanggal = bundle.getString("tanggal", "")
            val catatan = bundle.getString("catatan", "")
            val kategori = bundle.getString("kategori", "")

            if (jenis.isNotEmpty()) {
                val newTransaction = Transaction(id, jenis, nominal, tanggal, catatan, kategori)
                adapter.addTransaction(newTransaction)
                recyclerView.scrollToPosition(0)
            }
        }
        return view
    }
}
