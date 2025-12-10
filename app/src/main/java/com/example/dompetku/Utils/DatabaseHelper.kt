package com.example.dompetku.Utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dompetku.Model.Kategori
import com.example.dompetku.Model.Transaction

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "dompetKu_db"
        private const val DATABASE_VERSION = 1

        // ----------------- TABLE KATEGORI -----------------
        private const val TABLE_KATEGORI = "kategori"
        private const val ID_KATEGORI = "idKategori"
        private const val NAMA = "nama"

        private const val CREATE_TABLE_KATEGORI = """
            CREATE TABLE $TABLE_KATEGORI (
                $ID_KATEGORI INTEGER PRIMARY KEY AUTOINCREMENT,
                $NAMA TEXT
            )
        """

        // ----------------- TABLE TRANSAKSI -----------------
        private const val TABLE_TRANSAKSI = "transaksi"
        private const val ID = "id"
        private const val JENIS = "jenis"
        private const val NOMINAL = "nominal"
        private const val TANGGAL = "tanggal"
        private const val CATATAN = "catatan"
        private const val FK_KATEGORI = "idKategori"

        private const val CREATE_TABLE_TRANSAKSI = """
            CREATE TABLE $TABLE_TRANSAKSI (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $JENIS TEXT,
                $NOMINAL INTEGER,
                $TANGGAL TEXT,
                $CATATAN TEXT,
                $FK_KATEGORI INTEGER,
                FOREIGN KEY ($FK_KATEGORI) REFERENCES $TABLE_KATEGORI($ID_KATEGORI)
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(CREATE_TABLE_KATEGORI)
        db.execSQL(CREATE_TABLE_TRANSAKSI)

        // seed default kategori (hanya saat DB pertama kali dibuat)
        val defaults = listOf(
            "Makanan", "Minuman", "Belanja", "Transportasi", "Hiburan",
            "Tagihan", "Gaji", "Bonus", "Investasi", "Lainnya"
        )
        for (nama in defaults) {
            val cv = ContentValues().apply { put(NAMA, nama) }
            db.insert(TABLE_KATEGORI, null, cv)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSAKSI")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_KATEGORI")
        onCreate(db)
    }

    // ----------------- CRUD KATEGORI -----------------

    fun insertKategori(kategori: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(NAMA, kategori)
        }
        val id = db.insert(TABLE_KATEGORI, null, values)
        db.close()
        return id
    }

    fun getAllKategori(): List<Kategori> {
        val list = mutableListOf<Kategori>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_KATEGORI", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Kategori(
                        idKategori = cursor.getInt(cursor.getColumnIndexOrThrow(ID_KATEGORI)),
                        nama = cursor.getString(cursor.getColumnIndexOrThrow(NAMA)),
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun deleteKategori(idKategori: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_KATEGORI, "$ID_KATEGORI = ?", arrayOf(idKategori.toString()))
        db.close()
        return result
    }

    // ----------------- CRUD TRANSAKSI -----------------

    fun insertTransaksi(transaksi: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(JENIS, transaksi.jenis)
            put(NOMINAL, transaksi.nominal)
            put(TANGGAL, transaksi.tanggal)
            put(CATATAN, transaksi.catatan)
            put(FK_KATEGORI, transaksi.idKategori)
        }
        val id = db.insert(TABLE_TRANSAKSI, null, values)
        db.close()
        return id
    }

    fun getAllTransaksi(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSAKSI ORDER BY $ID DESC", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Transaction(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(ID)),
                        jenis = cursor.getString(cursor.getColumnIndexOrThrow(JENIS)),
                        nominal = cursor.getInt(cursor.getColumnIndexOrThrow(NOMINAL)),
                        tanggal = cursor.getString(cursor.getColumnIndexOrThrow(TANGGAL)),
                        catatan = cursor.getString(cursor.getColumnIndexOrThrow(CATATAN)),
                        idKategori = cursor.getInt(cursor.getColumnIndexOrThrow(FK_KATEGORI))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun updateTransaksi(transaksi: Transaction): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(JENIS, transaksi.jenis)
            put(NOMINAL, transaksi.nominal)
            put(TANGGAL, transaksi.tanggal)
            put(CATATAN, transaksi.catatan)
            put(FK_KATEGORI, transaksi.idKategori)
        }
        val result = db.update(TABLE_TRANSAKSI, values, "$ID = ?", arrayOf(transaksi.id.toString()))
        db.close()
        return result
    }

    fun deleteTransaksi(id: Int): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_TRANSAKSI, "$ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun clearAllData() {
        val db = writableDatabase

        // 1. Hapus tabel
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSAKSI")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_KATEGORI")

        // 2. Buat ulang tabel
        db.execSQL(CREATE_TABLE_KATEGORI)
        db.execSQL(CREATE_TABLE_TRANSAKSI)

        // 3. Insert kategori default lagi
        val defaults = listOf(
            "Makanan", "Minuman", "Belanja", "Transportasi", "Hiburan",
            "Tagihan", "Gaji", "Bonus", "Investasi", "Lainnya"
        )

        for (nama in defaults) {
            val cv = ContentValues().apply { put(NAMA, nama) }
            db.insert(TABLE_KATEGORI, null, cv)
        }

        db.close()
    }
}