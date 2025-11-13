package com.example.dompetku.Utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.dompetku.Model.Transaction

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, NAME, null, VERSION) {

    companion object {
        private const val VERSION = 1
        private const val NAME = "dompetKu"
        private const val TABLE_TRANSAKSI = "transaksi"

        private const val ID = "id"
        private const val JENIS = "jenis"
        private const val NOMINAL = "nominal"
        private const val TANGGAL = "tanggal"
        private const val CATATAN = "catatan"
        private const val KATEGORI = "kategori"

        private const val CREATE_TABLE_TRANSAKSI = """
            CREATE TABLE $TABLE_TRANSAKSI (
                $ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $JENIS TEXT,
                $NOMINAL INTEGER,
                $TANGGAL TEXT,
                $CATATAN TEXT,
                $KATEGORI TEXT
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(CREATE_TABLE_TRANSAKSI)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSAKSI")
        onCreate(db)
    }

    fun insertTransaksi(transaksi: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(JENIS, transaksi.jenis)
        values.put(NOMINAL, transaksi.nominal)
        values.put(TANGGAL, transaksi.tanggal)
        values.put(CATATAN, transaksi.catatan)
        values.put(KATEGORI, transaksi.kategori)

        // insert data dan return ID-nya
        val id = db.insert(TABLE_TRANSAKSI, null, values)
        db.close()
        return id
    }

    fun getAllTransaksi(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_TRANSAKSI ORDER BY $ID DESC"
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val transaksi = Transaction(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(ID)),
                    jenis = cursor.getString(cursor.getColumnIndexOrThrow(JENIS)),
                    nominal = cursor.getInt(cursor.getColumnIndexOrThrow(NOMINAL)),
                    tanggal = cursor.getString(cursor.getColumnIndexOrThrow(TANGGAL)),
                    catatan = cursor.getString(cursor.getColumnIndexOrThrow(CATATAN)),
                    kategori = cursor.getString(cursor.getColumnIndexOrThrow(KATEGORI))
                )
                list.add(transaksi)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}
