package com.aukde.iotdoor

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aukde.iotdoor.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecordAdapter(private var list: ArrayList<HistoryModel>) : RecyclerView.Adapter<RecordAdapter.MyViewHolder>() {

    @SuppressLint("SimpleDateFormat")
    var formatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")


    inner class MyViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordAdapter.MyViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordAdapter.MyViewHolder, position: Int) {
        val model = list[position]
        holder.binding.itName.text = model.fullname
        holder.binding.itDate.text = formatter.format(Date(model.date))
    }

    override fun getItemCount(): Int {
        return list.size
    }
}