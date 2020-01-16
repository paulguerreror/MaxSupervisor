package com.neobit.maxsupervisor.adapters

import android.content.res.Resources
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.Utils.Utils
import com.neobit.maxsupervisor.data.model.Mensaje
import com.squareup.picasso.Picasso

class MensajeAdapter(val mensajes:ArrayList<Mensaje>): RecyclerView.Adapter<MensajeAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v  = LayoutInflater.from(p0?.context).inflate(R.layout.mensajes,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
       return mensajes.size
    }

    override fun onBindViewHolder(holder: ViewHolder,position: Int) {
        holder.bindItems(mensajes[position])
    }

    override fun getItemId(position: Int): Long {
        try {
            return mensajes[position].id_chat.toLong()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return 0
    }

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        fun bindItems(mensaje: Mensaje) {
            //their Message
            val their_message = itemView.findViewById<RelativeLayout>(R.id.their_message)
            val avatar = itemView.findViewById<ImageView>(R.id.avatar)
            val their_nombre = itemView.findViewById<TextView>(R.id.their_nombre)
            val their_mensaje = itemView.findViewById<TextView>(R.id.their_mensaje)
            //my  Message
            val my_message = itemView.findViewById<RelativeLayout>(R.id.my_message)
            val my_mensaje = itemView.findViewById<TextView>(R.id.my_mensaje)
            if(mensaje.mio == 1){
                their_message.visibility = View.GONE
               // my_message.visibility = View.VISIBLE
                my_mensaje.setText(mensaje.mensaje)
            }else{
                their_nombre.setText(mensaje.nombre_creador)
                my_mensaje.visibility = View.GONE
                //their_message.visibility = View.VISIBLE
                Picasso.get().load(Utils.URL_MEDIA + mensaje.imagen_creador).error(R.drawable.placeholder).placeholder(R.drawable.placeholder).noFade().into(avatar)
                their_mensaje.setText(mensaje.mensaje)
            }
        }
    }
}