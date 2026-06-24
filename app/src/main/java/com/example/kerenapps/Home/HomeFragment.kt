package com.example.kerenapps.Home

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kerenapps.AuthActivity
import com.example.kerenapps.Data.Api.PhotoApiClient
import com.example.kerenapps.Home.pertemuan_10.TenthActivity
import com.example.kerenapps.Home.pertemuan_13.ThirteenthActivity
import com.example.kerenapps.Home.pertemuan_3.ThirdActivity
import com.example.kerenapps.Home.pertemuan_4.FourthActivity
import com.example.kerenapps.Home.pertemuan_5.FifthActivity
import com.example.kerenapps.Home.pertemuan_7.SeventhActivity
import com.example.kerenapps.Home.pertemuan_9.NinthActivity
import com.example.kerenapps.Home.photo.PhotoAdapter
import com.example.kerenapps.R
import com.example.kerenapps.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlin.jvm.java


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Home"
        }

        binding.btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
        }
        val sharedPref = requireContext().getSharedPreferences("session_user", MODE_PRIVATE)
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya") { dialog, _ ->
                    val editor = sharedPref.edit()
                    editor.clear()
                    editor.apply()
                    dialog.dismiss()
                    Log.e("Logout", "User logout")
                    requireActivity()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.btnToFourth.setOnClickListener {
            val intent = Intent(requireContext(), FourthActivity::class.java)
            intent.putExtra("name", "Politeknik Caltex Riau")
            intent.putExtra("asal", "Rumbai")
            intent.putExtra("usia", 25)
            startActivity(intent)
            requireActivity()


        }

        binding.btnToSecond.setOnClickListener {
            val intent = Intent(requireContext(), FifthActivity::class.java)
            startActivity(intent)
            requireActivity()
        }
        binding.btnToThird.setOnClickListener {
            val intent = Intent(requireContext(), ThirdActivity::class.java)
            startActivity(intent)
            requireActivity()
        }
        binding.btnToFifth.setOnClickListener {
            val intent = Intent(requireContext(), FifthActivity::class.java)
            startActivity(intent)
            requireActivity()
        }

        binding.btnToSeventh.setOnClickListener {
            val intent = Intent(requireContext(), SeventhActivity::class.java)
            startActivity(intent)
            requireActivity()
        }
        binding.btnToNinth.setOnClickListener {
            val intent = Intent(requireContext(), NinthActivity::class.java)
            startActivity(intent)
            requireActivity()

        }

        binding.btnToTenth.setOnClickListener {
            val intent = Intent(requireContext(), TenthActivity::class.java)
            startActivity(intent)
            requireActivity()

        }
        binding.btnToThirteenth.setOnClickListener {
            val intent = Intent(requireContext(), ThirteenthActivity::class.java)
            startActivity(intent)
        }

        loadPhoto()
    }

    private fun loadPhoto() {
        lifecycleScope.launch {
            try {
                val photos = PhotoApiClient.apiService.getPhotos()
                val adapter = PhotoAdapter(photos)
                binding.rvGallery.adapter = adapter

                /** List Tampil Vertical*/
//                binding.rvGallery.layoutManager = LinearLayoutManager(requireContext())

                /** List Tampil Horizontal */
                //binding.rvGallery.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

                /** List Tampil Grid */
                binding.rvGallery.layoutManager = GridLayoutManager(requireContext(), 2)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}