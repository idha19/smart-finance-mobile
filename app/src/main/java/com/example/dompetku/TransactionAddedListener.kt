package com.example.dompetku

import com.example.dompetku.Model.Transaction

interface TransactionAddedListener {
 fun onTransactionAddedListener(transaction: Transaction)
}