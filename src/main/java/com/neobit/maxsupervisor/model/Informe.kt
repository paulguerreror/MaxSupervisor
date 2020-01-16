package com.neobit.maxsupervisor.data.model

data class Informe(
    val id_informe: Int,
    val descripcion: String,
    val id_creador: Int,
    val nombre_creador: String,
    val id_editor: Int,
    val nombre_editor: String,
    val fecha_creacion: String
)