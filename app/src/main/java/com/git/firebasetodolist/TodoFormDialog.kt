package com.git.firebasetodolist.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.git.firebasetodolist.Book
import com.git.firebasetodolist.databinding.DialogTodoFormBinding
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*

class TodoFormDialog(
    private val context: Context,
    private val todoRef: DatabaseReference,
    private val key: String? = null,
    private val todo: Book? = null
) {

    fun showForm() {
        val binding = DialogTodoFormBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        // prefill if editing
        if (todo != null) {
            binding.edtTitle.setText(todo.title)
            binding.edtDesc.setText(todo.description)
            binding.edtDate.setText(todo.date ?: todo.release)
        }

        binding.edtDate.setOnClickListener {
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH)
            val d = calendar.get(Calendar.DAY_OF_MONTH)

            val dp = DatePickerDialog(context, { _, yy, mm, dd ->
                calendar.set(yy, mm, dd)
                binding.edtDate.setText(dateFormatter.format(calendar.time))
            }, y, m, d)

            dp.datePicker.minDate = System.currentTimeMillis()
            dp.show()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.edtTitle.text.toString().trim()
            val desc = binding.edtDesc.text.toString().trim()
            val date = binding.edtDate.text.toString().trim()

            if (title.isEmpty() || date.isEmpty()) {
                Toast.makeText(context, "Judul dan tanggal wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (todo == null) {
                // add new
                val newKey = todoRef.push().key ?: UUID.randomUUID().toString()
                val newBook = Book(
                    id = newKey,
                    title = title,
                    release = date,
                    description = desc,
                    done = false,
                    date = date,
                    timestamp = System.currentTimeMillis()
                )
                todoRef.child(newKey).setValue(newBook)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Tugas ditambahkan", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Gagal menambah tugas", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // update existing
                val updated = Book(
                    id = todo.id,
                    title = title,
                    release = date,
                    description = desc,
                    done = todo.done,
                    date = date,
                    timestamp = todo.timestamp
                )
                todoRef.child(todo.id!!).setValue(updated)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Tugas diperbarui", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Gagal memperbarui tugas", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
