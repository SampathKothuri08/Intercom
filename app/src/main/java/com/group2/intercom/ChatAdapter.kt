package com.group2.intercom

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val currentUserId: String, private val messages: List<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val MSG_TYPE_RIGHT = 1
    private val MSG_TYPE_LEFT = 2

    override fun getItemViewType(position: Int): Int {
        Log.w("CHECK",messages[position].senderID.toString())
        return if (messages[position].senderID == currentUserId) MSG_TYPE_RIGHT else MSG_TYPE_LEFT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MSG_TYPE_RIGHT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right, parent, false)
            RightMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left, parent, false)
            LeftMessageViewHolder(view)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is RightMessageViewHolder) {
            holder.bind(message)
        } else if (holder is LeftMessageViewHolder) {
            holder.bind(message)
        }
        Log.w("CHECK", messages.toString())
    }

    class RightMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMessage: TextView = itemView.findViewById(R.id.txtRightMessage)
        fun bind(message: Message) {
            txtMessage.text = message.msg_sent
        }
    }

    class LeftMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMessage: TextView = itemView.findViewById(R.id.txtLeftMessage)
        fun bind(message: Message) {
            txtMessage.text = message.msg_sent
        }
    }
}
