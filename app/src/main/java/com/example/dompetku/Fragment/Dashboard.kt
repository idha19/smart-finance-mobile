package com.example.dompetku.Fragment

import android.app.DatePickerDialog
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.Adapter.TransactionAdapterDashboard
import com.example.dompetku.R
import com.example.dompetku.Utils.DatabaseHelper
import com.example.dompetku.Utils.ProfilePref
import java.util.*

class Dashboard : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var profilePref: ProfilePref

    private lateinit var tvProfileName: TextView
    private lateinit var imgProfile: ImageView

    private lateinit var tvTotalSaldo: TextView
    private lateinit var tvPemasukkan: TextView
    private lateinit var tvPengeluaran: TextView

    private lateinit var rvTransaksi: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapterDashboard

    private lateinit var iconCalendar: ImageView

    private lateinit var notifCard: CardView
    private lateinit var txtNotif: TextView
    private lateinit var iconNotif: ImageView
    private lateinit var tvEmptyTransaksi: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        profilePref = ProfilePref(requireContext())

        initView(view)

        transactionAdapter = TransactionAdapterDashboard(requireContext(), mutableListOf())
        rvTransaksi.adapter = transactionAdapter
        rvTransaksi.layoutManager = LinearLayoutManager(requireContext())

        iconCalendar.setOnClickListener { showDatePicker() }

        refreshUI()
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        refreshProfile()
        loadRingkasanSaldo()
        loadLatestTransaksi()
    }

    private fun initView(view: View) {
        tvProfileName = view.findViewById(R.id.profileName)
        imgProfile = view.findViewById(R.id.imgProfile)
        tvTotalSaldo = view.findViewById(R.id.nominalSisa)
        tvPemasukkan = view.findViewById(R.id.nominalPemasukan)
        tvPengeluaran = view.findViewById(R.id.nominalPengeluaran)
        rvTransaksi = view.findViewById(R.id.rvTransaksi)
        iconCalendar = view.findViewById(R.id.iconCalendar)

        notifCard = view.findViewById(R.id.notif)
        txtNotif = view.findViewById(R.id.txtNotif)
        iconNotif = view.findViewById(R.id.iconNotif)
        tvEmptyTransaksi = view.findViewById(R.id.tvEmptyTransaksi)
    }

    private fun refreshProfile() {
        tvProfileName.text = profilePref.getName()
        profilePref.getPhoto()?.let { uri ->
            try { imgProfile.setImageURI(Uri.parse(uri)) }
            catch (_: Exception) { imgProfile.setImageResource(R.drawable.account) }
        }
    }

    private fun loadRingkasanSaldo() {
        val allTrans = dbHelper.getAllTransaksi()

        var pemasukan = 0L
        var pengeluaran = 0L

        allTrans.forEach {
            if (it.jenis.equals("Pemasukkan", true)) pemasukan += it.nominal.toLong()
            if (it.jenis.equals("Pengeluaran", true)) pengeluaran += it.nominal.toLong()
        }

        tvTotalSaldo.text = formatRupiah(pemasukan - pengeluaran)
        tvPemasukkan.text = formatRupiah(pemasukan)
        tvPengeluaran.text = formatRupiah(pengeluaran)

        updateNotif(pemasukan, pengeluaran)
    }

    private fun updateNotif(pemasukan: Long, pengeluaran: Long) {

        if (pemasukan == 0.toLong() && pengeluaran == 0.toLong()) {
            notifCard.visibility = View.GONE
            return
        }

        notifCard.visibility = View.VISIBLE

        val background = GradientDrawable()
        background.cornerRadius = 8.dpToPx().toFloat() // sudut melengkung

        if (pemasukan == pengeluaran) {
            background.setColor(resources.getColor(R.color.notifKuning))
            background.setStroke(1.dpToPx(), resources.getColor(R.color.iconKuning))

            notifCard.background = background
            iconNotif.setColorFilter(resources.getColor(R.color.iconKuning))
            txtNotif.setTextColor(resources.getColor(R.color.iconKuning))
            txtNotif.text = "Pemasukan dan pengeluaran bulan ini seimbang."
            return
        }

        val besar = maxOf(pemasukan, pengeluaran)
        val kecil = minOf(pemasukan, pengeluaran)
        var persen = if (kecil > 0) ((besar - kecil) / kecil.toFloat() * 100).toInt() else 100
        persen = persen.coerceAtMost(100) // maksimal 100%

        if (pengeluaran > pemasukan) {
            background.setColor(resources.getColor(R.color.notifMerah))
            background.setStroke(1.dpToPx(), resources.getColor(R.color.iconMerah))

            notifCard.background = background
            iconNotif.setColorFilter(resources.getColor(R.color.iconMerah))
            txtNotif.setTextColor(resources.getColor(R.color.iconMerah))
            txtNotif.text = "Pengeluaran bulan ini $persen% lebih tinggi dari pemasukan."
        } else {
            background.setColor(resources.getColor(R.color.notifHijau))
            background.setStroke(1.dpToPx(), resources.getColor(R.color.iconHijau))

            notifCard.background = background
            iconNotif.setColorFilter(resources.getColor(R.color.iconHijau))
            txtNotif.setTextColor(resources.getColor(R.color.iconHijau))
            txtNotif.text = "Pemasukan bulan ini $persen% lebih tinggi dari pengeluaran."
        }
    }

    private fun loadLatestTransaksi(selectedDate: String? = null) {
        val allTrans = dbHelper.getAllTransaksi()

        val filtered = if (selectedDate != null) {
            allTrans.filter { it.tanggal == selectedDate }
        } else {
            allTrans
        }

        val latest = filtered.sortedByDescending { it.id }.take(4)

        if (latest.isEmpty()) {
            rvTransaksi.visibility = View.GONE
            tvEmptyTransaksi.visibility = View.VISIBLE
        } else {
            rvTransaksi.visibility = View.VISIBLE
            tvEmptyTransaksi.visibility = View.GONE
            transactionAdapter.updateData(latest)
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = convertTanggal(day, month, year)
                loadLatestTransaksi(selected)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun convertTanggal(day: Int, monthZero: Int, year: Int): String {
        val bulan = arrayOf("Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agu","Sep","Okt","Nov","Des")
        return "%02d %s %d".format(day, bulan[monthZero], year)
    }

    private fun formatRupiah(amount: Long): String {
        return "Rp. " + String.format("%,d", amount).replace(",", ".")
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}