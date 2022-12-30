package com.catndev.gnssserver.ui.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catndev.gnssserver.LocationService
import com.catndev.gnssserver.SatsRVAdapter
import com.catndev.gnssserver.databinding.FragmentStatusBinding

class StatusFragment : Fragment() {

    private var _binding: FragmentStatusBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var satTextView: TextView
    private lateinit var satRecyclerView: RecyclerView
    private lateinit var utcTextView: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val statusViewModel =
                ViewModelProvider(this).get(StatusViewModel::class.java)

        _binding = FragmentStatusBinding.inflate(inflater, container, false)
        val root: View = binding.root
        utcTextView = binding.utcTextView
        satTextView = binding.satTextView
        satRecyclerView = binding.satRecyclerView
        satRecyclerView.layoutManager = LinearLayoutManager(context)
        satRecyclerView.adapter = SatsRVAdapter()
/*
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
        return root
    }

    fun setTTFF(ttfMillis: Int) {

    }

    fun setSatRVData(data: ArrayList<LocationService.Satellite>) {
        val adapter = (satRecyclerView.adapter as SatsRVAdapter)
        adapter.satellitesList = data
        adapter.notifyDataSetChanged()
    }

    fun setSatTextView(string: String) {
        satTextView.text = string
    }

    fun setUTCTextView(string: String) {
        utcTextView.text = string
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}