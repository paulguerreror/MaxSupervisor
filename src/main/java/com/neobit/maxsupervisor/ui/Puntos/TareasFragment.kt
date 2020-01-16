package com.neobit.maxsupervisor.ui.Puntos

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.neobit.maxsupervisor.adapters.PuntoProductoAdapter
import kotlinx.android.synthetic.main.tareas_fragment.*

import com.neobit.maxsupervisor.R
import com.neobit.maxsupervisor.adapters.PuntoTareaPagerAdapter

class TareasFragment : Fragment() {

    companion object {
        fun newInstance() = TareasFragment()
    }

    private lateinit var viewModel: TareasViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tareas_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TareasViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sectionsPagerAdapter = PuntoTareaPagerAdapter(this@TareasFragment.context!!, childFragmentManager!!)
        view_pager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(view_pager)
    }


}
