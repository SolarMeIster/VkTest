package ru.solarmeister.vktest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import ru.solarmeister.vktest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val activity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (hasPermissions(this))
                startFragment()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //binding.toolbar.title = getString(R.string.app_name)
        if (savedInstanceState == null) {
            if (hasPermissions(this)) {
                startFragment()
            } else {
                requestPermissions(activity, PERMISSION_STORAGE)
            }
        }
    }

    private fun hasPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                    == PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }

    private fun requestPermissions(activity: ActivityResultLauncher<Intent>, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(String.format("package:%s", this.packageName))
                activity.launch(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                activity.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startFragment()
                }
            }
        }
    }

    private fun startFragment() {
        val path = Environment.getExternalStorageDirectory().path
        val bundle = Bundle()
        bundle.putString(FileListFragment.ARG_VALUE, path)
        val fileListFragment = FileListFragment()
        fileListFragment.arguments = bundle
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.fragment_container, fileListFragment)
        }
    }

    companion object {
        const val PERMISSION_STORAGE = 101
    }
}