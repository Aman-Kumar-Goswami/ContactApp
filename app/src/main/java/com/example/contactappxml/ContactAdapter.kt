package com.example.contactappxml

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation

class ContactAdapter(private var contacts: List<Contact>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivContact: ImageView = view.findViewById(R.id.ivContact)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phone ?: "No number"

        // Phone number par click karne se dialer khulega
        holder.tvPhone.setOnClickListener {
            val phone = contact.phone
            if (!phone.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                holder.itemView.context.startActivity(intent)
            }
        }

        // Agar image_url hai toh load karein, varna default person icon dikhayein
        if (!contact.image_url.isNullOrEmpty()) {
            holder.ivContact.load(contact.image_url) {
                crossfade(true)
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
                transformations(CircleCropTransformation())
            }
        } else {
            holder.ivContact.setImageResource(R.drawable.ic_person)
        }
    }

    override fun getItemCount() = contacts.size

    fun updateList(newList: List<Contact>) {
        contacts = newList
        notifyDataSetChanged()
    }
}
