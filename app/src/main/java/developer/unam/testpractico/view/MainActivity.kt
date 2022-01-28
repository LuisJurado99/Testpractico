package developer.unam.testpractico

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import developer.unam.testpractico.adapters.AdapterMovieMain
import developer.unam.testpractico.databinding.ActivityMainBinding
import developer.unam.testpractico.db.AppDatabase
import developer.unam.testpractico.retrofit.RetrofitInstance
import developer.unam.testpractico.retrofit.movies.Movies
import developer.unam.testpractico.servicelocation.ForegroundOnlyLocationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.canonicalName
    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        //sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        val db = AppDatabase(this)
        val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = conMgr.getActiveNetworkInfo();
        val complete = binding.txtSelectFilter.editText as AutoCompleteTextView
        val network = activeNetwork != null && activeNetwork.isConnected()
        callServiceOrDataBase("popular", network, db)
        complete.doOnTextChanged { text, start, before, count ->
            when (text.toString()) {
                getString(R.string.popular) -> callServiceOrDataBase("popular", network, db)
                getString(R.string.now_playing) -> callServiceOrDataBase("now_playing", network, db)
                getString(R.string.upcoming) -> callServiceOrDataBase("upcoming", network, db)
                getString(R.string.top_rated) -> callServiceOrDataBase("top_rated", network, db)
                else -> callServiceOrDataBase("popular", network, db)

            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
            )
        )
        Handler().postDelayed(
            {
                foregroundOnlyLocationService?.subscribeToLocationUpdates()
                    ?: Log.e(TAG, "Service Not Bound")
            }, 1000
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    private fun callServiceOrDataBase(tipo: String, conection: Boolean, db: AppDatabase) {
        Log.e("tippo", "tipo $tipo")
        if (conection) {
            val api =
                RetrofitInstance.getApi().getMoviesPopular(tipo, getString(R.string.key_api), 1)
            api.enqueue(object : Callback<Movies> {
                override fun onResponse(call: Call<Movies>, response: Response<Movies>) {
                    val listResult = response.body()
                    if (listResult != null) {
                        when (tipo) {
                            "popular" -> db.deleteMoviesPopular()
                            "now_playing" -> db.deleteMoviesNowPlaying()
                            "upcoming" -> db.deleteMoviesUpcoming()
                            "top_rated" -> db.deleteMoviesTopRated()
                            else -> db.deleteMoviesPopular()
                        }
                        val adapter = AdapterMovieMain(
                            tipo,
                            listResult.results,
                            this@MainActivity,
                            false
                        )
                        binding.rvMainMovie.adapter = adapter
                        binding.rvMainMovie.layoutManager = GridLayoutManager(this@MainActivity, 2)

                    }else{
                        val alert = MaterialAlertDialogBuilder(this@MainActivity)
                        alert.apply {
                            title = "No hay datos"
                            setMessage("No hay datos en la petición")
                            setPositiveButton(android.R.string.ok) { dialog, which ->
                                val intent = Intent(Intent.ACTION_MAIN)
                                intent.addCategory(Intent.CATEGORY_HOME)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                dialog.dismiss()
                            }
                        }.create()
                        alert.show()
                    }
                }

                override fun onFailure(call: Call<Movies>, t: Throwable) {
                    Log.e(MainActivity::class.java.simpleName, Gson().toJson(t))
                }

            })
        } else {
            val listNow = when (tipo) {
                "popular" -> db.getAllMoviesPopular()
                "now_playing" -> db.getAllMoviesNowPlaying()
                "upcoming" -> db.getAllMoviesUpcoming()
                "top_rated" -> db.getAllMoviesTopRated()
                else -> db.getAllMoviesPopular()
            }
            if (listNow.isNotEmpty()) {
                val adapter = AdapterMovieMain(tipo, listNow, this@MainActivity, false)
                binding.rvMainMovie.adapter = adapter
                binding.rvMainMovie.layoutManager = GridLayoutManager(this@MainActivity, 2)
            } else {
                val alert = MaterialAlertDialogBuilder(this)
                alert.apply {
                    title = "No hay datos"
                    setMessage("No se encontraron datos ni conexion ")
                    setPositiveButton(android.R.string.ok) { dialog, which ->
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_HOME)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        dialog.dismiss()
                    }
                }.create()
                alert.show()
            }

        }

    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        //sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
        if (!foregroundPermissionApproved())
            requestForegroundPermissions()
        val list = listOf<String>(
            getString(R.string.popular),
            getString(R.string.now_playing),
            getString(R.string.upcoming),
            getString(R.string.top_rated)
        )
        val edit = binding.txtSelectFilter.editText as AutoCompleteTextView
        edit.setAdapter(ArrayAdapter(this, R.layout.list_item, list))
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                binding.root,
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(android.R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        Companion.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Companion.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun logResultsToScreen(output: String) {
        /*val outputWithPreviousLogs = "$output\n${binding.tvUbication.text}"
        binding.tvUbication.text = outputWithPreviousLogs*/
    }


    /**
     * Receiver for location broadcasts from [ForegroundOnlyLocationService].
     */
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )

            if (location != null) {
                logResultsToScreen("Foreground location: ${location.toText()}")
            }
        }
    }

    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

}