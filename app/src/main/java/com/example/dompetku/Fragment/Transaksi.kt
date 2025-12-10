package com.example.dompetku.Fragment

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.cardview.widget.CardView
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
    private lateinit var inputSearch: EditText
    private lateinit var btnDelete: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v = inflater.inflate(R.layout.fragment_transaksi, container, false)

        dbHelper = DatabaseHelper(requireContext())

        recyclerView = v.findViewById(R.id.rvTransaksi)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        inputSearch = v.findViewById(R.id.inputSearch)
        btnDelete = v.findViewById(R.id.btnHapus)

        val btnBack = v.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        loadData()

        // ================= SEARCH =================
        inputSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s.toString())
            }
        })

        // ================= DELETE =================
        btnDelete.setOnClickListener {
            if (adapter.getCheckedCount() == 0) {
                android.widget.Toast.makeText(requireContext(), "Tidak ada item yang dipilih!", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Hapus")
                .setMessage("Yakin ingin menghapus transaksi yang dipilih")
                .setPositiveButton("Hapus") { _, _ ->
                    adapter.deleteChecked()
                    android.widget.Toast.makeText(requireContext(), "Berhasil dihapus!", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        return v
    }

    private fun loadData() {
        val transaksiList = dbHelper.getAllTransaksi().toMutableList()

        adapter = TransactionAdapter(
            requireContext(),
            transaksiList
        ) { transaksi ->
            openEditFragment(transaksi)
        }

        recyclerView.adapter = adapter
    }

    // ================= OPEN EDIT FRAGMENT =================
    private fun openEditFragment(tr: Transaction) {
        val fragment = Edit.newInstance(tr)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment) // pastikan id container benar!
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        loadData() // refresh setelah balik dari Edit
    }
}