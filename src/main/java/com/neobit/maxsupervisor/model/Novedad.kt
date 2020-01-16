package com.neobit.maxsupervisor.data.model

data class Novedad(
    val id_novedad: Int,
    val creador: String,
    val tipo: String,
    val descripcion: String,
    val imagen: String,
    val cliente: Int,
    val fecha_creacion: String
)