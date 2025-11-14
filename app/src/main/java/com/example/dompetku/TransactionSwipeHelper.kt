package com.example.dompetku

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.Adapter.TransactionAdapter
import com.example.dompetku.R

class TransactionSwipeHelper(private val adapter: TransactionAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition

        if (direction == ItemTouchHelper.LEFT) {
            // langsung panggil fungsi hapus (dialog sudah di adapter)
            adapter.deleteItem(position)
        } else if (direction == ItemTouchHelper.RIGHT) {
            // langsung panggil fungsi edit
            adapter.editItem(position)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20
        val icon: Drawable?
        val background: ColorDrawable

        if (dX > 0) {
            // geser kanan = edit
            icon = ContextCompat.getDrawable(adapter.context, R.drawable.ic_edit)
            background = ColorDrawable(ContextCompat.getColor(adapter.context, R.color.blue))
        } else {
            // geser kiri = hapus
            icon = ContextCompat.getDrawable(adapter.context, R.drawable.ic_delete)
            background = ColorDrawable(Color.RED)
        }

        icon?.let {
            val iconMargin = (itemView.height - it.intrinsicHeight) / 2
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + it.intrinsicHeight

            if (dX > 0) {
                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + it.intrinsicWidth
                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setBounds(
                    itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
                )
            } else if (dX < 0) {
                val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                background.setBounds(
                    itemView.right + dX.toInt() - backgroundCornerOffset, itemView.top,
                    itemView.right, itemView.bottom
                )
            } else {
                background.setBounds(0, 0, 0, 0)
            }

            background.draw(c)
            it.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}