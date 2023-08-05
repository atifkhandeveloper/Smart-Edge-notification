package com.abh80.smartedge.flashalert

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.abh80.smartedge.R
import com.abh80.smartedge.flashalert.ui.AppListPreferenceFragment
import com.abh80.smartedge.flashalert.ui.Informations
import com.abh80.smartedge.flashalert.ui.SettingsPreferenceFragment


class FlashMainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        val hasFlash = this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (!hasFlash) {
            val alert = AlertDialog.Builder(this)
            alert.apply {
                setTitle(getString(R.string.error))
                setMessage(getString(R.string.alert_no_flash))
                setCancelable(false)
                setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
            }
            alert.create().show()
        } else {
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction().add(android.R.id.content, SettingsPreferenceFragment()).addToBackStack("SETTINGS").commit()
            }
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

//    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        val fragment = supportFragmentManager.findFragmentByTag("NOTIFYAPP")
//        val refresh = fragment != null && fragment.isVisible
//        menu?.findItem(R.id.action_refresh_apps)?.apply { isVisible = refresh }
//        return super.onPrepareOptionsMenu(menu)
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.run {
            when(this.itemId){
                R.id.action_info -> {
                    startActivity(Intent(this@FlashMainActivity, Informations::class.java))
                }
                R.id.action_refresh_apps -> {
                    val fragment = supportFragmentManager.findFragmentByTag("NOTIFYAPP")
                    if (fragment != null && fragment is AppListPreferenceFragment) {
                        fragment.refreshAppList()
                    }
                }
                android.R.id.home -> {
                    supportFragmentManager.popBackStackImmediate()
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    invalidateOptionsMenu()
                }
                else -> {
                }
            }
        }
        return true
    }

    override fun onBackPressed() {
        invalidateOptionsMenu()
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragment = supportFragmentManager.findFragmentById(android.R.id.content)
        fragment?.takeIf {
            it is SettingsPreferenceFragment
        }?.let {
            (it as SettingsPreferenceFragment).onPermissionEvent(requestCode,permissions,grantResults)
        }
    }

    private fun setupActionBar() {
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            val title = getString(R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.title = Html.fromHtml(title, Html.FROM_HTML_MODE_COMPACT)
            } else {
                @Suppress("DEPRECATION")
                this.title = Html.fromHtml(title)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 6719){
            val fragment = supportFragmentManager.findFragmentById(android.R.id.content)
            fragment?.takeIf {
                it is SettingsPreferenceFragment
            }?.let {
                (it as SettingsPreferenceFragment).onPermissionEvent(requestCode, arrayOf("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"), IntArray(1){PackageManager.PERMISSION_GRANTED})
            }
        }
    }
}
