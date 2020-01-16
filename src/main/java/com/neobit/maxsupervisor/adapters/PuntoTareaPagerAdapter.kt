package com.neobit.maxsupervisor.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.ui.Puntos.PuntoTareasFragment

class PuntoTareaPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        return when(position) {
            0-> PuntoTareasFragment.newInstance()
            else-> PuntoTareasFragment.newInstance()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0-> context.resources.getString(R.string.menu_punto_tareas)
            else -> context.resources.getString(R.string.menu_supervisor_tareas)
        }
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}