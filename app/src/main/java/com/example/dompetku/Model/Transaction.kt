package com.example.dompetku.Model

import java.io.Serializable

class Transaction (
    var id: Int = 0,
    var jenis: String,
    var nominal: Int,
    var tanggal: String,
    var catatan: String,
    var idKategori: Int
) : Serializable