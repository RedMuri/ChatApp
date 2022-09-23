package com.example.chatapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Picasso

class MessagesAdapter: Adapter<MessagesAdapter.MessageViewHolder>() {
    private var messages: ArrayList<Message> = arrayListOf()
    class MessageViewHolder(itemView: View) : ViewHolder(itemView){
        val author: TextView = itemView.findViewById(R.id.textViewAuthor)
        val message: TextView = itemView.findViewById(R.id.textViewMessage)
        val image:ImageView = itemView.findViewById(R.id.imageOfMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val message = LayoutInflater.from(parent.context).inflate(R.layout.item_message,parent,false)
        return MessageViewHolder(message)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.author.text = message.author
        if (!message.message.isNullOrEmpty()) {
            holder.message.text = message.message
            holder.image.visibility = View.GONE
        }else if (message.urlToImage != null){
            holder.message.text = message.message
            holder.image.visibility = View.VISIBLE
            Picasso.get().load(message.urlToImage.toUri()).into(holder.image)
        }

    }

    override fun getItemCount() = messages.size
    fun setMessages(messages: ArrayList<Message>){
        this.messages.clear()
        this.messages.addAll(messages)
        notifyDataSetChanged()
    }
}