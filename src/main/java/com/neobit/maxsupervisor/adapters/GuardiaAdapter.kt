package com.neobit.maxsupervisor.adapters

import android.content.res.Resources
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.data.model.Guardia
import com.neobit.maxsupervisor.data.model.Punto
import com.squareup.picasso.Picasso

class GuardiaAdapter(val guardias:ArrayList<Guardia>): RecyclerView.Adapter<GuardiaAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.guardias,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return guardias.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(guardias[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return guardias[position].id_guardia.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(guardia: Guardia) {
            val cardGuardia = itemView.findViewById<CardView>(R.id.cardGuardia)
            val imgGuardia = itemView.findViewById<ImageView>(R.id.imgGuardia)
            val txtGuardia = itemView.findViewById<TextView>(R.id.txtGuardia)
            txtGuardia.text = guardia.nombres
            imgGuardia.setTag(R.id.id,guardia.id_guardia.toString())
            cardGuardia.setTag(R.id.id,guardia.id_guardia)
            if(guardia.imagen == "" || guardia.imagen == null){
                imgGuardia.visibility = View.GONE
            }else{
                Picasso.get().load(Utils.URL_MEDIA + guardia.imagen).error(R.drawable.placeholder_profile).placeholder(R.drawable.placeholder_profile).noFade().into(imgGuardia)
            }
        }


    }
}