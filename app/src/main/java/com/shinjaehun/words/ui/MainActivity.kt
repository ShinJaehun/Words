package com.shinjaehun.words.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.navigation.ui.NavigationUI
import com.shinjaehun.words.R

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var navContrller: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // 이것도 소용 없음
//        toolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
//            when(menuItem.itemId) {
//                R.id.action_settings -> {
//                    Log.i(TAG, "settings?")
//                    true
//                }
//                else -> {
//                    false
//                }
//            }
//        }

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        navContrller = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navContrller)
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navContrller, null)
}