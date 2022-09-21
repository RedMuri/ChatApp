package com.example.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class MessagesAdapter: Adapter<MessagesAdapter.MessageViewHolder>() {
    private var messages: ArrayList<Message> = arrayListOf()
    class MessageViewHolder(itemView: View) : ViewHolder(itemView){
        val author: TextView = itemView.findViewById(R.id.textViewAuthor)
        val message: TextView = itemView.findViewById(R.id.textViewMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val message = LayoutInflater.from(parent.context).inflate(R.layout.item_message,parent,false)
        return MessageViewHolder(message)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.author.text = message.author
        holder.message.text = message.message
    }

    override fun getItemCount() = messages.size
    fun setMessages(messages: ArrayList<Message>){
        this.messages = messages
        notifyDataSetChanged()
    }
}