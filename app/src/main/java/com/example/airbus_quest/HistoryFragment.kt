package com.example.airbus_quest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import java.io.IOException
import android.widget.LinearLayout

class HistoryFragment : Fragment() {
    private val TAG = "HistoryFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: HistoryFragment ready")

        val lvCoordinates: ListView = view.findViewById(R.id.lvCoordinates)
        val layoutEmpty: LinearLayout = view.findViewById(R.id.layoutEmpty)

        val lines = readFileLines()

        if (lines.isEmpty()) {
            lvCoordinates.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
            Log.d(TAG, "No CSV data found")
        } else {
            lvCoordinates.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                lines
            )
            lvCoordinates.adapter = adapter
            Log.d(TAG, "Loaded ${lines.size} coordinates")

            lvCoordinates.setOnItemClickListener { _, _, position, _ ->
                val line = lines[position]
                val parts = line.split(";")

                if (parts.size == 4) {
                    val intent = Intent(requireContext(), ThirdActivity::class.java)

                    intent.putExtra("timestamp", parts[0])
                    intent.putExtra("latitude", parts[1])
                    intent.putExtra("longitude", parts[2])
                    intent.putExtra("altitude", parts[3])

                    Log.d(TAG, "Click on: $line")
                    startActivity(intent)
                }
            }
        }
    }

    private fun readFileLines(): List<String> {
        val fileName = "gps_coordinates.csv"
        return try {
            requireContext().openFileInput(fileName).bufferedReader().readLines()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading CSV: ${e.message}")
            emptyList()
        }
    }
}