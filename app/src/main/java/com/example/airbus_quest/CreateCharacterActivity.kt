package com.example.airbus_quest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.GameCharacter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateCharacterActivity : AppCompatActivity() {
    private val TAG = "CreateCharacterActivity"

    // I track the currently selected avatar index (-1 means nothing is selected yet)
    private var selectedAvatarIndex = -1

    // I use lazy so getString() is called after the Activity is fully created
    private val avatars by lazy {
        listOf(
            AvatarItem("commuter", getString(R.string.avatar_commuter), android.R.drawable.ic_menu_directions),
            AvatarItem("cyclist", getString(R.string.avatar_cyclist), android.R.drawable.ic_menu_compass),
            AvatarItem("pedestrian", getString(R.string.avatar_pedestrian), android.R.drawable.ic_menu_myplaces)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_character)

        Log.d(TAG, "onCreate: CreateCharacterActivity created!")

        val toolbar: MaterialToolbar = findViewById(R.id.toolbarCreateCharacter)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val rvAvatarSelection: RecyclerView = findViewById(R.id.rvAvatarSelection)
        val etNickname: TextInputEditText = findViewById(R.id.etNickname)
        val btnCreate: MaterialButton = findViewById(R.id.btnCreate)
        val layoutPreview: android.widget.LinearLayout = findViewById(R.id.layoutPreview)
        val ivPreviewAvatar: ImageView = findViewById(R.id.ivPreviewAvatar)
        val tvPreviewType: TextView = findViewById(R.id.tvPreviewType)

        // I configure the RecyclerView with a horizontal LinearLayoutManager
        // so the avatar cards scroll horizontally
        rvAvatarSelection.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.HORIZONTAL, false
        )

        val adapter = AvatarAdapter(avatars) { index ->
            // I update the selected index and reveal the preview section
            selectedAvatarIndex = index
            val selected = avatars[index]
            layoutPreview.visibility = View.VISIBLE
            ivPreviewAvatar.setImageResource(selected.iconRes)
            tvPreviewType.text = selected.displayName
            // I only enable Create after an avatar is selected
            btnCreate.isEnabled = true
            Log.d(TAG, "Avatar selected: ${selected.type}")
        }
        rvAvatarSelection.adapter = adapter

        btnCreate.setOnClickListener {
            val nickname = etNickname.text.toString().trim()

            if (nickname.isBlank()) {
                Toast.makeText(this, "Please enter a nickname", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedAvatarIndex == -1) {
                Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val avatarType = avatars[selectedAvatarIndex].type
            val avatarLabel = avatars[selectedAvatarIndex].displayName

            // I show a confirmation dialog before saving to Room
            // confirm_create_msg = "Create %s as a %s?"
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_create_title))
                .setMessage(getString(R.string.confirm_create_msg, nickname, avatarLabel))
                .setPositiveButton(getString(R.string.confirm_create_yes)) { _, _ ->
                    saveCharacter(nickname, avatarType)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    // I override this to handle the back arrow in the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // I insert the new character into Room on a background thread (Dispatchers.IO)
    // and close the activity on the main thread after the insert completes
    private fun saveCharacter(nickname: String, avatarType: String) {
        val character = GameCharacter(nickname = nickname, avatarType = avatarType)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val id = db.characterDao().insert(character)
            Log.d(TAG, "Character saved: id=$id, nickname=$nickname, avatar=$avatarType")

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CreateCharacterActivity,
                    "Character '$nickname' created!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    // I use a simple data class to bundle avatar data together
    data class AvatarItem(val type: String, val displayName: String, val iconRes: Int)

    // I define the adapter as an inner class so it has access to selectedAvatarIndex
    inner class AvatarAdapter(
        private val items: List<AvatarItem>,
        private val onAvatarClick: (Int) -> Unit
    ) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

        inner class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivAvatarOption: ImageView = itemView.findViewById(R.id.ivAvatarOption)
            val tvAvatarType: TextView = itemView.findViewById(R.id.tvAvatarType)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
            // I inflate item_avatar_selection.xml for each card in the horizontal list
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_avatar_selection, parent, false)
            return AvatarViewHolder(view)
        }

        override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
            val item = items[position]
            holder.ivAvatarOption.setImageResource(item.iconRes)
            holder.tvAvatarType.text = item.displayName

            // I apply bg_avatar_selected for the chosen card and bg_avatar_unselected
            // for all others to give clear visual feedback
            holder.itemView.background = ContextCompat.getDrawable(
                holder.itemView.context,
                if (position == selectedAvatarIndex) R.drawable.bg_avatar_selected
                else R.drawable.bg_avatar_unselected
            )

            holder.itemView.setOnClickListener {
                val previous = selectedAvatarIndex
                onAvatarClick(position)
                // I notify both the previously and newly selected items to redraw
                notifyItemChanged(previous)
                notifyItemChanged(position)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}