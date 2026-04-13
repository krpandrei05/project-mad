package com.example.airbus_quest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MapViewModel"

    // Cache the stations list so the map doesn't reload on every tab switch
    val stations = MutableLiveData<List<Station>>()

    fun loadStations() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(getApplication())
            val result = db.stationDao().getAll()
            Log.d(TAG, "Loaded ${result.size} stations from Room")
            withContext(Dispatchers.Main) {
                stations.postValue(result)
            }
        }
    }
}