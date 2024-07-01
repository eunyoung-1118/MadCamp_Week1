package com.example.madcamp_week1

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.madcamp_week1.data.db.PhoneBook

class phoneBookAdapter(private var contacts: List<PhoneBook>) : BaseAdapter() {

    override fun getCount(): Int = contacts.size

    override fun getItem(position: Int): PhoneBook = contacts[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(parent?.context).inflate(R.layout.phone_item, parent, false)
        val contact = contacts[position]

        val nameTextView = view.findViewById<TextView>(R.id.nameItem)
        val numTextView = view.findViewById<TextView>(R.id.numItem)
        val contactImageView = view.findViewById<ImageView>(R.id.contact_image)

        nameTextView.text = contact.name
        numTextView.text = contact.num
        contact.image?.let {
            if (it.isNotEmpty()) {
                contactImageView.setImageURI(Uri.parse(it))
            } else {
                contactImageView.setImageResource(R.drawable.person) // Default image resource
            }
        } ?: run {
            contactImageView.setImageResource(R.drawable.person) // Default image resource
        }

        return view
    }

    // 데이터 업데이트 메서드
    fun updateData(newContacts: List<PhoneBook>) {
        contacts = newContacts
        notifyDataSetChanged() // 데이터가 변경되었음을 어댑터에 알림
    }
}
