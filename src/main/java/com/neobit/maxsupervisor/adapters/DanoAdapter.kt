package com.neobit.maxsupervisor.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.model.Dano

class DanoAdapter(val danos:ArrayList<Dano>): androidx.recyclerview.widget.RecyclerView.Adapter<DanoAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.danos,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return danos.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(danos[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return danos[position].id_dano.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(dano: Dano) {
            val tvdano = itemView.findViewById<TextView>(R.id.tvdano)
            val ckDano = itemView.findViewById<CheckBox>(R.id.ckDano)
            ckDano.setTag(dano.id_dano.toString())
            tvdano.text = dano.codigo
        }
    }
}