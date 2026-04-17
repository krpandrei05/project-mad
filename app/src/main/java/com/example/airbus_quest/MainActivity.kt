package com.example.airbus_quest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.airbus_quest.api.RetrofitClient
import com.example.airbus_quest.room.AppDatabase
import com.example.airbus_quest.room.EmtStations
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var navView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // Create fragment instances once — they are never recreated on tab switch
    private val dashboardFragment = DashboardFragment()
    private val mapFragment = MapFragment()
    private val reportFragment = ReportFragment()
    private val historyFragment = HistoryFragment()
    private lateinit var tvDrawerUsername: TextView

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "userIdentifier") {
            val username = prefs.getString("userIdentifier", "Username") ?: "Username"
            tvDrawerUsername.text = username
            Log.d(TAG, "Username updated in drawer: $username")
        }
    }

    private lateinit var prefs: SharedPreferences

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
            this, drawerLayout, toolbar,
            R.string.nav_dashboard, R.string.nav_dashboard
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawer_settings -> {
                    Log.d(TAG, "Drawer: Settings clicked")
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.drawer_create_character -> {
                    Log.d(TAG, "Drawer: Create Character clicked")
                    startActivity(Intent(this, CreateCharacterActivity::class.java))
                }
                R.id.drawer_logout -> {
                    Log.d(TAG, "Drawer: Logout clicked")
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)

        // Bind the drawer header username TextView
        tvDrawerUsername = navView.getHeaderView(0).findViewById(R.id.tvDrawerUsername)
        tvDrawerUsername.text = prefs.getString("userIdentifier", "Username") ?: "Username"

        // 3. Bottom Nav Config
        bottomNav = findViewById(R.id.bottomNav)

        // Add all fragments once at startup — hiding all except Dashboard
        if (savedInstanceState == null) {
            initFragments()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    showFragment(dashboardFragment, getString(R.string.dashboard_title))
                    true
                }
                R.id.nav_map -> {
                    showFragment(mapFragment, getString(R.string.map_title))
                    true
                }
                R.id.nav_report -> {
                    showFragment(reportFragment, getString(R.string.report_title))
                    true
                }
                R.id.nav_history -> {
                    showFragment(historyFragment, getString(R.string.history_title))
                    true
                }
                else -> false
            }
        }

        // Set Dashboard as the default tab
        bottomNav.selectedItemId = R.id.nav_dashboard

        // 4. EMT stations are fetched dynamically on GPS update in DashboardFragment
        Log.d(TAG, "EMT stations will be fetched on first GPS update")
    }

    // Add all fragments to the container at once and hide all except Dashboard.
    // This way fragments are never destroyed and recreated on tab switch.
    private fun initFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, dashboardFragment, "dashboard")
            .add(R.id.fragmentContainer, mapFragment, "map").hide(mapFragment)
            .add(R.id.fragmentContainer, reportFragment, "report").hide(reportFragment)
            .add(R.id.fragmentContainer, historyFragment, "history").hide(historyFragment)
            .commit()
    }

    // I show the selected fragment and hide all others — no recreation, no data loss
    private fun showFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .hide(dashboardFragment)
            .hide(mapFragment)
            .hide(reportFragment)
            .hide(historyFragment)
            .show(fragment)
            .commit()
        toolbar.title = title
    }

    fun navigateToDashboard() {
        bottomNav.selectedItemId = R.id.nav_dashboard
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        Log.d(TAG, "MainActivity destroyed, prefs listener unregistered")
    }
}