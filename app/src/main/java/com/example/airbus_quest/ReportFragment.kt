package com.example.airbus_quest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.airbus_quest.room.AppDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportFragment : Fragment() {

    private val TAG = "ReportFragment"
    private var photoUri: Uri? = null

    // Camera launcher — opens gallery to pick a photo
    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                photoUri = uri
                view?.findViewById<ImageView>(R.id.ivPhotoPreview)?.let { iv ->
                    iv.visibility = View.VISIBLE
                    Glide.with(this).load(uri).centerCrop().into(iv)
                }
                view?.findViewById<LinearLayout>(R.id.layoutPhotoPlaceholder)?.visibility = View.GONE
                view?.findViewById<MaterialButton>(R.id.btnRetake)?.visibility = View.VISIBLE
                Log.d(TAG, "Photo selected: $uri")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actvStation: AutoCompleteTextView = view.findViewById(R.id.actvStation)
        val tvNearestStation: TextView = view.findViewById(R.id.tvNearestStation)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val tvRatingLabel: TextView = view.findViewById(R.id.tvRatingLabel)
        val etComment: TextInputEditText = view.findViewById(R.id.etComment)
        val btnSubmit: MaterialButton = view.findViewById(R.id.btnSubmitReport)
        val framePhoto: FrameLayout = view.findViewById(R.id.framePhoto)
        val btnRetake: MaterialButton = view.findViewById(R.id.btnRetake)

        // Load stations from Room, fallback to hardcoded list if empty
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val dbStations = db.stationDao().getAll()
            val stationNames = if (dbStations.isNotEmpty()) {
                dbStations.map { it.name }
            } else {
                // Fallback — common Madrid EMT stations
                listOf(
                    "Castellana", "Sol", "Atocha", "Retiro", "Cibeles",
                    "Gran Vía", "Moncloa", "Vallecas", "Legazpi", "Alcalá"
                )
            }

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    stationNames
                )
                actvStation.setAdapter(adapter)
                actvStation.setOnClickListener { actvStation.showDropDown() }
                tvNearestStation.text = "Nearest station: ${stationNames.first()}"
                Log.d(TAG, "Loaded ${stationNames.size} stations")
            }
        }

        // Photo area — open gallery on tap
        framePhoto.setOnClickListener { openGallery() }
        btnRetake.setOnClickListener {
            photoUri = null
            view.findViewById<ImageView>(R.id.ivPhotoPreview).visibility = View.GONE
            view.findViewById<LinearLayout>(R.id.layoutPhotoPlaceholder).visibility = View.VISIBLE
            btnRetake.visibility = View.GONE
            openGallery()
        }

        // Rating label
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            tvRatingLabel.text = when (rating.toInt()) {
                1 -> getString(R.string.rating_1)
                2 -> getString(R.string.rating_2)
                3 -> getString(R.string.rating_3)
                4 -> getString(R.string.rating_4)
                5 -> getString(R.string.rating_5)
                else -> "Tap to rate"
            }
        }

        // Submit to Firebase RTDB
        btnSubmit.setOnClickListener {
            val stationName = actvStation.text.toString().trim()
            val rating = ratingBar.rating.toInt()
            Log.d(TAG, "Submit clicked — station='$stationName', rating=$rating")
            val comment = etComment.text.toString().trim()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

            if (stationName.isBlank()) {
                Toast.makeText(requireContext(), "Please select a station", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (rating == 0) {
                Toast.makeText(requireContext(), "Please add a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val report = mapOf(
                "stationName" to stationName,
                "rating" to rating,
                "comment" to comment,
                "timestamp" to System.currentTimeMillis(),
                "userId" to userId
            )

            FirebaseDatabase.getInstance("https://airbus-quest-default-rtdb.europe-west1.firebasedatabase.app").reference
                .child("reports")
                .child(stationName.replace(".", "_"))
                .push()
                .setValue(report)
                .addOnSuccessListener {
                    Log.i(TAG, "Report submitted: $stationName by $userId")
                    Toast.makeText(requireContext(), getString(R.string.report_success), Toast.LENGTH_SHORT).show()
                    // Reset form
                    actvStation.setText("")
                    ratingBar.rating = 0f
                    tvRatingLabel.text = "Tap to rate"
                    etComment.setText("")
                    photoUri = null
                    view.findViewById<ImageView>(R.id.ivPhotoPreview).visibility = View.GONE
                    view.findViewById<LinearLayout>(R.id.layoutPhotoPlaceholder).visibility = View.VISIBLE
                    btnRetake.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed: ${e.message} — cause: ${e.cause}")
                    Toast.makeText(requireContext(), "Failed to submit report", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoLauncher.launch(intent)
    }
}