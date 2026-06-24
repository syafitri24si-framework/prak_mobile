package com.example.kerenapps.Home.pertemuan_13

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.kerenapps.R
import com.example.kerenapps.databinding.FragmentTabScanBinding
import com.example.kerenapps.utils.NotificationHelper
import com.example.kerenapps.utils.PermissionHelper
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TabScanFragment : Fragment() {
    private var _binding: FragmentTabScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService

    // Variabel kunci agar tidak memproses gambar baru saat QR lama sedang diproses
    private var isScanning = false

    // Khusus hanya format QR Code
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    // Launcher untuk izin kamera
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Notifikasi saat izin kamera diberikan
            NotificationHelper.showNotification(
                requireContext(),
                "Izin Kamera",
                "Izin kamera telah diberikan oleh pengguna",
                Intent(requireContext(), ThirteenthActivity::class.java)
            )
            startCamera()
        } else {
            Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Implementasi PermissionHelper sesuai Pertemuan 14
        if (!PermissionHelper.hasPermission(requireActivity(), Manifest.permission.CAMERA)) {
            PermissionHelper.requestPermission(permissionLauncher, Manifest.permission.CAMERA)
        } else {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Tampilkan Notifikasi saat kamera scan aktif
            NotificationHelper.showNotification(
                requireContext(),
                "Scanner Aktif",
                "Kamera sedang memindai QR Code",
                Intent(requireContext(), ThirteenthActivity::class.java)
            )

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("TabScan", "Gagal mulai kamera", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // Jika sedang memproses data atau binding-nya hilang, lewati frame ini
        if (isScanning || _binding == null) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            isScanning = true // Kunci scanner

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val rawValue = barcodes[0].rawValue ?: "QR Code Kosong"

                        // Memastikan update UI berjalan di Main Thread secara instan
                        activity?.runOnUiThread {
                            binding.tvScanResult.text = "Hasil: $rawValue"
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("TabScan", "Scanner failed", it)
                }
                .addOnCompleteListener {
                    // Beri jeda 1 detik (1000ms) sebelum kamera diizinkan men-scan QR Code baru lagi
                    binding.root.postDelayed({
                        isScanning = false
                    }, 1000)

                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        scanner.close()
        cameraExecutor.shutdown()
    }
}