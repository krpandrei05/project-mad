package com.example.airbus_quest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.GameCharacter
import com.example.airbus_quest.viewmodel.HistoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {
    private val TAG = "HistoryFragment"

    private lateinit var lvCharacters: ListView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var layoutCoordinates: LinearLayout
    private lateinit var tvSelectedCharacterName: TextView
    private lateinit var lvCoordinates: ListView
    private lateinit var tvNoCoordinates: TextView
    private val viewModel: HistoryViewModel by viewModels()

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

        // Bind all views here so they are ready before onResume triggers loadCharacters()
        lvCharacters = view.findViewById(R.id.lvCharacters)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        layoutCoordinates = view.findViewById(R.id.layoutCoordinates)
        tvSelectedCharacterName = view.findViewById(R.id.tvSelectedCharacterName)
        lvCoordinates = view.findViewById(R.id.lvCoordinates)
        tvNoCoordinates = view.findViewById(R.id.tvNoCoordinates)

        // Observe the character list from ViewModel — restored instantly on tab switch
        viewModel.characters.observe(viewLifecycleOwner) { characters ->
            if (characters.isEmpty()) {
                lvCharacters.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
                layoutCoordinates.visibility = View.GONE
            } else {
                lvCharacters.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE
                val adapter = CharacterAdapter(requireContext(), characters)
                lvCharacters.adapter = adapter
                lvCharacters.setOnItemClickListener { _, _, position, _ ->
                    showCoordinatesForCharacter(characters[position])
                }
            }
        }

        val btnCloseCoordinates: android.widget.ImageView = view.findViewById(R.id.btnCloseCoordinates)
        btnCloseCoordinates.setOnClickListener {
            layoutCoordinates.visibility = View.GONE
            Log.d(TAG, "Coordinates section closed")
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload characters on every resume to catch newly created ones
        viewModel.loadCharacters()
    }

    // Reveal the coordinates section and populate it with CSV data for the selected character.
    private fun showCoordinatesForCharacter(character: GameCharacter) {
        layoutCoordinates.visibility = View.VISIBLE
        tvSelectedCharacterName.text = "📍 GPS History — ${character.nickname}"

        val lines = readFileLines()

        if (lines.isEmpty()) {
            lvCoordinates.visibility = View.GONE
            tvNoCoordinates.visibility = View.VISIBLE
        } else {
            lvCoordinates.visibility = View.VISIBLE
            tvNoCoordinates.visibility = View.GONE

            // Use simple_list_item_1 here because coordinates are plain strings from CSV
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                lines
            )
            lvCoordinates.adapter = adapter

            // Send the selected coordinate to ThirdActivity via Intent extras
            lvCoordinates.setOnItemClickListener { _, _, position, _ ->
                val line = lines[position]
                val parts = line.split(";")

                if (parts.size == 4) {
                    val intent = Intent(requireContext(), ThirdActivity::class.java)
                    intent.putExtra("timestamp", parts[0])
                    intent.putExtra("latitude", parts[1])
                    intent.putExtra("longitude", parts[2])
                    intent.putExtra("altitude", parts[3])
                    Log.d(TAG, "Coordinate tapped: $line")
                    startActivity(intent)
                }
            }
        }
    }

    // Read all lines from the gps_coordinates.csv file stored in internal storage.
    // Return an empty list if the file does not exist yet.
    private fun readFileLines(): List<String> {
        val fileName = "gps_coordinates.csv"
        return try {
            requireContext().openFileInput(fileName).bufferedReader().readLines()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading CSV: ${e.message}")
            emptyList()
        }
    }

    // Extend ArrayAdapter<GameCharacter> to inflate item_character.xml for each row.
    // convertView is the recycled view — I reuse it when available to save memory.
    inner class CharacterAdapter(
        context: Context,
        private val characters: List<GameCharacter>
    ) : ArrayAdapter<GameCharacter>(context, R.layout.item_character, characters) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_character, parent, false)

            val character = characters[position]

            val ivCharacterAvatar: ImageView = view.findViewById(R.id.ivCharacterAvatar)
            val tvCharacterName: TextView = view.findViewById(R.id.tvCharacterName)
            val tvCharacterStats: TextView = view.findViewById(R.id.tvCharacterStats)
            val tvCharacterDate: TextView = view.findViewById(R.id.tvCharacterDate)
            val tvCharacterStatus: TextView = view.findViewById(R.id.tvCharacterStatus)

            // Use a placeholder icon for each avatar type until Commit 3 where I will
            // load real images from URLs using Glide
            val iconRes = when (character.avatarType) {
                "commuter" -> android.R.drawable.ic_menu_directions
                "cyclist" -> android.R.drawable.ic_menu_compass
                "pedestrian" -> android.R.drawable.ic_menu_myplaces
                else -> android.R.drawable.ic_menu_myplaces
            }
            ivCharacterAvatar.setImageResource(iconRes)

            tvCharacterName.text = character.nickname

            // Use character_stats_format = "Day %d · HP: %d/100 · %d stations"
            tvCharacterStats.text = getString(
                R.string.character_stats_format,
                character.dayCount,
                character.hp,
                character.stationsVisited
            )

            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvCharacterDate.text = "Created ${sdf.format(Date(character.createdAt))}"

            // Apply bg_badge_alive or bg_badge_dead based on the character's status.
            // Also reduce the alpha of dead characters to visually distinguish them.
            if (character.isAlive) {
                tvCharacterStatus.text = getString(R.string.status_alive)
                tvCharacterStatus.background = ContextCompat.getDrawable(
                    context, R.drawable.bg_badge_alive
                )
                view.alpha = 1.0f
            } else {
                tvCharacterStatus.text = getString(R.string.status_dead)
                tvCharacterStatus.background = ContextCompat.getDrawable(
                    context, R.drawable.bg_badge_dead
                )
                view.alpha = 0.7f
            }
            val btnSelectCharacter: Button = view.findViewById(R.id.btnSelectCharacter)

            if (character.isAlive) {
                btnSelectCharacter.visibility = View.VISIBLE
                btnSelectCharacter.setOnClickListener {
                    val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    prefs.edit().putInt("activeCharacterId", character.id).apply()
                    Log.d(TAG, "Active character set: id=${character.id}, name=${character.nickname}")
                    Toast.makeText(context, "${character.nickname} is now active!", Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                }
            } else {
                btnSelectCharacter.visibility = View.GONE
            }

            return view
        }
    }
}