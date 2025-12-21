package com.example.dompetku.Fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.dompetku.R
import com.example.dompetku.Utils.ProfilePref
import com.example.dompetku.Utils.ThemePref
import com.example.dompetku.Utils.DatabaseHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate

class Profil : Fragment() {

    private lateinit var profilePref: ProfilePref
    private lateinit var themePref: ThemePref

    private lateinit var imgProfile: ImageView
    private lateinit var profileName: TextView
    private lateinit var switchDark: Switch

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult

                // Simpan URI secara persist
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                imgProfile.setImageURI(uri)
                profilePref.savePhoto(uri.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi ThemePref (tanpa setDefaultNightMode di sini)
        themePref = ThemePref(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profil, container, false)

        profilePref = ProfilePref(requireContext())
        themePref = ThemePref(requireContext())

        imgProfile = view.findViewById(R.id.imgProfile)
        imgProfile.setOnClickListener{
            showImagePreview()
        }

        profileName = view.findViewById(R.id.profileName)
        switchDark = view.findViewById(R.id.switchDark)
        val textDarkMode = view.findViewById<TextView>(R.id.darkMood)

        // Set label awal "Mode Gelap" / "Mode Terang"
        textDarkMode.text = if (themePref.isDarkMode()) "Mode Terang" else "Mode Gelap"

        // Load nama & foto
        profileName.text = profilePref.getName()
        profilePref.getPhoto()?.let {
            try {
                imgProfile.setImageURI(Uri.parse(it))
            } catch (_: Exception) {
                imgProfile.setImageResource(R.drawable.account)
            }
        }

        // Sinkron switch dengan dark mode yang aktif
        switchDark.isChecked = themePref.isDarkMode()

        // Klik Switch → Ubah Mode
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            // Simpan ke SharedPreferences
            themePref.saveDarkMode(isChecked)

            // Ubah teks label
            textDarkMode.text = if (isChecked) "Mode Terang" else "Mode Gelap"

            // Terapkan mode gelap/terang ke seluruh app
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Tombol back
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Edit foto
        view.findViewById<ImageView>(R.id.btnEditPhoto).setOnClickListener {
            openGallery()
        }

        // Ubah nama
        view.findViewById<LinearLayout>(R.id.ubahNama).setOnClickListener {
            showEditNameBottomSheet()
        }

        // Hapus semua data
        view.findViewById<LinearLayout>(R.id.hapus).setOnClickListener {
            showDeleteConfirmation()
        }

        return view
    }

    private fun showImagePreview() {
        val dialog = android.app.Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.photo_zoom, null)
        dialog.setContentView(view)

        val imgPreview =
            view.findViewById<com.github.chrisbanes.photoview.PhotoView>(R.id.imgPreview)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)

        imgPreview.setImageDrawable(imgProfile.drawable)

        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        pickImage.launch(intent)
    }

    private fun showEditNameBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.edit_nama, null)
        dialog.setContentView(view)

        val input = view.findViewById<EditText>(R.id.inputNamaBaru)
        val simpan = view.findViewById<Button>(R.id.btnSimpanNama)

        input.setText(profilePref.getName())

        simpan.setOnClickListener {
            val nama = input.text.toString().trim()
            if (nama.isEmpty()) {
                input.error = "Nama tidak boleh kosong!"
                return@setOnClickListener
            }
            profilePref.saveName(nama)
            profileName.text = nama
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset Semua Data?")
            .setMessage("Semua data akan dihapus.")
            .setPositiveButton("Ya") { _, _ -> resetAllData() }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun resetAllData() {
        val db = DatabaseHelper(requireContext())
        db.clearAllData()

        profilePref.clear()
        themePref.clear()

        profileName.text = "Nama Pengguna"
        imgProfile.setImageResource(R.drawable.account)

        activity?.recreate()
    }
}