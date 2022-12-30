package com.catndev.gnssserver

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.catndev.gnssserver.databinding.ActivityMainBinding
import com.catndev.gnssserver.ui.status.StatusFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), LocationCallbacks, ServiceConnection {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var mLocationService: LocationService? = null

    private lateinit var mNavController: NavController
    private lateinit var mChildFragmentManager: FragmentManager
    private val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.getDefault(Locale.Category.FORMAT))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        mNavController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_status, R.id.nav_records, R.id.nav_settings, R.id.nav_about
            ), drawerLayout
        )
        setupActionBarWithNavController(mNavController, appBarConfiguration)
        navView.setupWithNavController(mNavController)
        mChildFragmentManager = (supportFragmentManager.fragments.first()  as? NavHostFragment)!!.childFragmentManager
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            permissionWarning()
        }
        else {
            val intent = Intent(this, LocationService::class.java)
            bindService(intent, this, BIND_AUTO_CREATE)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var granted = true
        for (result in grantResults) {
            if (result == -1) {
                permissionWarning()
                granted = false
                break
            }
        }
        if (granted) {
            val intent = Intent(this, LocationService::class.java)
            bindService(intent, this, BIND_AUTO_CREATE)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mLocationService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mLocationService = (service as LocationService.ServiceBinder).getService()
        mLocationService!!.setCallbacks(this)
        if (!mLocationService!!.isRunning) {
            val intent = Intent(this, LocationService::class.java)
            startService(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun permissionWarning() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(R.string.attention_title)
        dialogBuilder.setMessage(R.string.permissions_message)
        dialogBuilder.setPositiveButton(android.R.string.ok) { _, _ ->
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        dialogBuilder.setNegativeButton(android.R.string.cancel) { _, _ ->
            exitProcess(0)
        }
        dialogBuilder.show()
    }

    override fun satInfo(satellitesInFix: Int, satList: List<LocationService.Satellite>) {
        if (mChildFragmentManager.primaryNavigationFragment is StatusFragment) {
            (mChildFragmentManager.primaryNavigationFragment as StatusFragment).setSatTextView(
                satellitesInFix.toString() + "/" + satList.count().toString()
            )
        }
    }

    override fun setTTFF(ttffMillis: Int) {
        if (mChildFragmentManager.primaryNavigationFragment is StatusFragment){
            (mChildFragmentManager.primaryNavigationFragment as StatusFragment).setTTFF(ttffMillis)
        }
    }

    override fun setSats(data: ArrayList<LocationService.Satellite>) {
        if (mChildFragmentManager.primaryNavigationFragment is StatusFragment) {
            (mChildFragmentManager.primaryNavigationFragment as StatusFragment).setSatRVData(data)

        }
    }

    override fun setUTC(date: Date) {
        if (mChildFragmentManager.primaryNavigationFragment is StatusFragment) {
            (mChildFragmentManager.primaryNavigationFragment as StatusFragment).setUTCTextView(sdf.format(date))
        }
    }
}