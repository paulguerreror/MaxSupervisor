package com.neobit.maxsupervisor.data.model

data class Mensaje(
    val id_chat: Int,
    val mensaje: String,
    val imagen_creador: String,
    val nombre_creador: String,
    val fecha_creacion: String,
    val mio: Int
)