package com.example.calanderapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.calanderapp.`interface`.OnItemClickListener
import com.example.calanderapp.data.Events
import com.example.calanderapp.databinding.ReminderRvBinding

class ReminderAdapter(private val reminderList: MutableList<Events>, private val listener: OnItemClickListener): RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ReminderRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            reminderName.text = reminderList[position].reminder
        }

    }


    inner class ViewHolder(val binding: ReminderRvBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                // logic to make reminders clickable
                itemView.setOnClickListener{
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClicked(position)
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return reminderList.size
    }
}