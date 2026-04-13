package com.example.airbus_quest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.GameCharacter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "HistoryViewModel"

    // Cache the character list so the Fragment restores it instantly on tab switch
    val characters = MutableLiveData<List<GameCharacter>>()

    // Call this from HistoryFragment — it loads from Room and caches in LiveData
    fun loadCharacters() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(getApplication())
            val result = db.characterDao().getAll()
            Log.d(TAG, "Loaded ${result.size} characters from Room")
            withContext(Dispatchers.Main) {
                characters.postValue(result)
            }
        }
    }
}