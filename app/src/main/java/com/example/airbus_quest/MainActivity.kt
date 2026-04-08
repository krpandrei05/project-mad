package com.example.airbus_quest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.lifecycle.lifecycleScope
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.EmtStations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate: MainActivity created as shell container")

        // 1. Toolbar Config
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 2. Drawer Config
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.nav_dashboard,
            R.string.nav_dashboard
        )

        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawer_settings -> {
                    Log.d(TAG, "Drawer: Settings clicked")
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }

                R.id.drawer_about -> {
                    Log.d(TAG, "Drawer: About clicked")
                    val intent = Intent(this, ThirdActivity::class.java)
                    startActivity(intent)
                }

                R.id.drawer_logout -> {
                    Log.d(TAG, "Drawer: Logout clicked")
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // 3. Bottom Navigation Config
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    toolbar.title = getString(R.string.dashboard_title)
                    true
                }
                R.id.nav_map -> {
                    loadFragment(MapFragment())
                    toolbar.title = getString(R.string.map_title)
                    true
                }
                R.id.nav_report -> {
                    loadFragment(ReportFragment())
                    toolbar.title = getString(R.string.report_title)
                    true
                }
                R.id.nav_history -> {
                    loadFragment(HistoryFragment())
                    toolbar.title = getString(R.string.history_title)
                    true
                }
                else -> false
            }
        }

        // Initial State/Fragment
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_dashboard
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val count = db.stationDao().getCount()
            if (count == 0) {
                db.stationDao().insertAll(EmtStations.getStations())
                Log.d(TAG, "EMT stations dataset inserted: ${EmtStations.getStations().size} stations")
            }
        }
    }

    // Helper
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        Log.d(TAG, "Fragment loaded: ${fragment.javaClass.simpleName}")
    }
}