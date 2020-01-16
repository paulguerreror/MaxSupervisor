package com.neobit.maxsupervisor.model


data class PuntoTarea(
    val id_punto_tarea: Int,
    val nombre: String,
    val info: Int,
    val hora_inicio: String,
    val supervisor: Int,
    val done: Int
)