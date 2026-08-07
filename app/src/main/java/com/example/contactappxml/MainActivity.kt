package com.example.contactappxml

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactAdapter
    private var contactList = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 2. RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ContactAdapter(contactList)
        recyclerView.adapter = adapter

        // 3. ye code + icon ke baad ka hai
        val addButton = findViewById<FloatingActionButton>(R.id.fabAdd)
        addButton.setOnClickListener {
            val intent = Intent(this, AddEditContactActivity::class.java)
            startActivity(intent)
        }

        // 4. Supabase se data fetch karein
        fetchContacts()
    }

    override fun onResume() {
        super.onResume()
        // Naya contact add hone ke baad list update karne ke liye
        fetchContacts()
    }

    private fun fetchContacts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Supabase se data lein
                val contacts = SupabaseManager.client.from("contacts")
                    .select().decodeList<Contact>()

                withContext(Dispatchers.Main) {
                    contactList.clear()
                    contactList.addAll(contacts)
                    adapter.updateList(contactList)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Fetch Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.queryHint = "Search Contact..."

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })

        return true
    }

    private fun filterList(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            contactList
        } else {
            contactList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        adapter.updateList(filteredList)
    }
}
