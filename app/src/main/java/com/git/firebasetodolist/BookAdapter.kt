package com.git.firebasetodolist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.git.firebasetodolist.databinding.ItemBookBinding

class BookAdapter(
    private var list: MutableList<Book>,
    private val onEdit: (Book, Int) -> Unit,
    private val onDelete: (Book, Int) -> Unit,
    private val onToggleDone: (Book, Int, Boolean) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book, position: Int) {

            binding.tvTitle.text = book.title
            binding.tvDesc.text = book.description ?: ""
            binding.tvRelease.text = book.release ?: ""

            val isDone = book.done

            // visual
            binding.root.alpha = if (isDone) 0.5f else 1f
            binding.tvTitle.paint.isStrikeThruText = isDone

            val gray = Color.parseColor("#9E9E9E")
            val normalTitle = Color.parseColor("#1A1A1A")
            val normalDesc = Color.parseColor("#666666")
            val normalDate = Color.parseColor("#4169E1")

            if (isDone) {
                binding.tvTitle.setTextColor(gray)
                binding.tvDesc.setTextColor(gray)
                binding.tvRelease.setTextColor(gray)
                binding.tvRelease.alpha = 0.5f
            } else {
                binding.tvTitle.setTextColor(normalTitle)
                binding.tvDesc.setTextColor(normalDesc)
                binding.tvRelease.setTextColor(normalDate)
                binding.tvRelease.alpha = 1f
            }

            // checkbox sync - avoid callback loop
            binding.checkDone.setOnCheckedChangeListener(null)
            binding.checkDone.isChecked = isDone
            binding.checkDone.setOnCheckedChangeListener { _, checked ->
                book.done = checked
                onToggleDone(book, position, checked)
                notifyItemChanged(position)
            }

            // delete
            binding.btnDelete.setOnClickListener { onDelete(book, position) }

            // edit on root click
            binding.root.setOnClickListener { onEdit(book, position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(
            ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Book>) {
        list = newList.toMutableList()
        notifyDataSetChanged()
    }
}
