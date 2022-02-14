package developer.unam.testpractico.view.home.view

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
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import developer.unam.testpractico.R
import developer.unam.testpractico.adapters.AdapterMovieMain
import developer.unam.testpractico.databinding.ActivityMainBinding
import developer.unam.testpractico.db.AppDatabase
import developer.unam.testpractico.retrofit.RetrofitInstance
import developer.unam.testpractico.retrofit.movies.Movies
import developer.unam.testpractico.retrofit.movies.Result
import developer.unam.testpractico.servicelocation.ForegroundOnlyLocationService
import developer.unam.testpractico.view.ArchivoActivity
import developer.unam.testpractico.view.MapsActivity
import developer.unam.testpractico.view.home.IHomeContract
import developer.unam.testpractico.view.home.presenter.HomePresenter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(),IHomeContract.View{

    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.canonicalName
    private var foregroundOnlyLocationServiceBound = false
    private var latitude:Double = 0.0
    private var longitud:Double = 0.0
    private lateinit var presenter:IHomeContract.Presenter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()

        FirebaseApp.initializeApp(this)
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

    override fun onStart() {
        super.onStart()
        //sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        //val db = AppDatabase(this)
        val conMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = conMgr.getActiveNetworkInfo();
        val complete = binding.txtSelectFilter.editText as AutoCompleteTextView
        //val network = activeNetwork != null && activeNetwork.isConnected()
        presenter = HomePresenter(this,getString(R.string.popular),getString(R.string.key_api))
        //callServiceOrDataBase("popular", network, db)
        binding.btnMap.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java).apply {
                putExtra("latitude",latitude)
                putExtra("longitud",longitud)
            })
        }

        binding.btnCamara.setOnClickListener {
            startActivity(Intent(this, ArchivoActivity::class.java))
        }

        complete.doOnTextChanged { text, _, _, _ ->
            when (text.toString()) {
                getString(R.string.popular) -> presenter.changePath(getString(R.string.popular_r))
                getString(R.string.now_playing) -> presenter.changePath(getString(R.string.now_playing_r))
                getString(R.string.upcoming) -> presenter.changePath(getString(R.string.upcoming_r))
                getString(R.string.top_rated) -> presenter.changePath(getString(R.string.top_rated_r))
                else -> presenter.changePath(getString(R.string.popular_r))

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

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        super.onStop()
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()
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
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )

            if (location != null) {
                Log.e("latlong",Gson().toJson(location))
                latitude = location.latitude
                longitud = location.longitude
            }
        }
    }

    companion object {
        private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    }

    override fun showLoader() {
        binding.progressCircular.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        binding.progressCircular.visibility = View.GONE
    }

    override fun showListMovies(listMoviesShow: List<Result>) {
        binding.rvMainMovie.layoutManager = GridLayoutManager(this,2)
        binding.rvMainMovie.adapter = AdapterMovieMain(listMoviesShow,this)
    }

    override fun errorListMovies(statusCode: Int) {
        binding.rvMainMovie.visibility = View.GONE
        binding.tvNotElement.visibility = View.VISIBLE
    }

}