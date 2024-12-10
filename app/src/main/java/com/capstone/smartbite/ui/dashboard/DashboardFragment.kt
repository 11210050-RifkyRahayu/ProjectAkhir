package com.capstone.smartbite.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.smartbite.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var dashboardViewModel: DashboardViewModel
    private val binding get() = _binding!!

    private lateinit var adapter: DashboardAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        dashboardViewModel = ViewModelProvider(requireActivity()).get(DashboardViewModel::class.java)

        adapter = DashboardAdapter { event ->
            val eventId = event.id
        }

        binding.rvActive.adapter = adapter
        binding.rvActive.layoutManager = LinearLayoutManager(requireContext())

        dashboardViewModel.event.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        dashboardViewModel.isLoading.observe(viewLifecycleOwner) {
            showLoading(it)
        }

        dashboardViewModel.loadActiveEvents()
        return root
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}