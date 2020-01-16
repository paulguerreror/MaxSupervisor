package com.neobit.maxsupervisor.adapters

import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.model.PuntoTarea
import com.squareup.picasso.Picasso

class PuntoTareaAdapter(val puntotareas:ArrayList<PuntoTarea>):
    androidx.recyclerview.widget.RecyclerView.Adapter<PuntoTareaAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.puntotareas,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return puntotareas.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(puntotareas[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return puntotareas[position].id_punto_tarea.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(puntotarea: PuntoTarea) {
            val punto_tarea_descripcion = itemView.findViewById<TextView>(R.id.punto_tarea_descripcion)
            val punto_tarea_fecha = itemView.findViewById<TextView>(R.id.punto_tarea_fecha)
            val btnPuntoTarea = itemView.findViewById<ImageButton>(R.id.btnPuntoTarea)
            btnPuntoTarea.setTag(R.id.id,puntotarea.id_punto_tarea)
            if(puntotarea.hora_inicio == ""){
                punto_tarea_fecha.visibility = View.GONE
            }
            if(puntotarea.supervisor == 0){
                btnPuntoTarea.isClickable = false
                btnPuntoTarea.isEnabled = false
            }
            if(puntotarea.info==1){
                val id = "i" + puntotarea.id_punto_tarea.toString()
            }else{
                if(puntotarea.done==1){
                }
            }
            punto_tarea_descripcion.text = puntotarea.nombre
            punto_tarea_fecha.text = puntotarea.hora_inicio
        }


    }
}