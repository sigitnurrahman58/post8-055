package com.git.firebasetodolist

import android.app.AlertDialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.git.firebasetodolist.databinding.ActivityMainBinding
import com.git.firebasetodolist.ui.TodoFormDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseReference
    private lateinit var adapter: BookAdapter

    // store ordered keys to map adapter positions -> firebase node keys
    private var keyList = mutableListOf<String>()
    private var bookList = mutableListOf<Book>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseDatabase.getInstance().getReference("books") // gunakan "books" agar sama contoh

        adapter = BookAdapter(
            bookList,
            onEdit = { book, pos -> editBook(book, pos) },
            onDelete = { book, pos -> deleteBook(pos) },
            onToggleDone = { book, pos, checked -> updateDoneStatus(pos, checked) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            // show form dialog for add
            TodoFormDialog(this, db).showForm()
        }

        fetchData()
    }

    private fun fetchData() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = mutableListOf<Pair<String, Book>>()
                for (child in snapshot.children) {
                    val item = child.getValue(Book::class.java)
                    val key = child.key ?: continue
                    if (item != null) {
                        temp.add(key to item)
                    }
                }

                // sort: not-done first (false < true), then by timestamp (older first)
                val sorted = temp.sortedWith(
                    compareBy<Pair<String, Book>> { it.second.done }
                        .thenBy { it.second.timestamp }
                )

                // update lists & keys preserving same ordering
                keyList.clear()
                bookList.clear()
                for ((k, b) in sorted) {
                    keyList.add(k)
                    bookList.add(b)
                }

                adapter.updateList(bookList)
                binding.recyclerView.visibility = if (bookList.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateDoneStatus(pos: Int, checked: Boolean) {
        val key = keyList.getOrNull(pos) ?: return
        db.child(key).child("done").setValue(checked)
    }

    private fun editBook(book: Book, pos: Int) {
        val key = keyList.getOrNull(pos)
        if (key != null) {
            TodoFormDialog(this, db, key, book).showForm()
        }
    }

    private fun deleteBook(pos: Int) {
        val key = keyList.getOrNull(pos) ?: return

        AlertDialog.Builder(this)
            .setTitle("Hapus Tugas")
            .setMessage("Yakin ingin menghapus tugas ini?")
            .setPositiveButton("Ya") { _, _ ->
                db.child(key).removeValue()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}
