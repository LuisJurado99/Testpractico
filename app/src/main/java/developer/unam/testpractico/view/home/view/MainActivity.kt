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
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import developer.unam.testpractico.R
import developer.unam.testpractico.adapters.AdapterMovieMain
import developer.unam.testpractico.databinding.ActivityMainBinding
import developer.unam.testpractico.retrofit.movies.Result
import developer.unam.testpractico.servicelocation.ForegroundOnlyLocationService
import developer.unam.testpractico.singleton.UserFirebaseSingleton
import developer.unam.testpractico.view.ArchivoActivity
import developer.unam.testpractico.view.MapsActivity
import developer.unam.testpractico.view.home.IHomeContract
import developer.unam.testpractico.view.home.presenter.HomePresenter

class MainActivity : AppCompatActivity(), IHomeContract.View {

    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.canonicalName
    private var foregroundOnlyLocationServiceBound = false
    private lateinit var presenter: IHomeContract.Presenter
    private var page=1
    private var pageTotal=0
    private var pathLocal = ""
    private lateinit var adapter:AdapterMovieMain

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
        adapter = AdapterMovieMain(this)
        binding.rvMainMovie.adapter = adapter
        val user = UserFirebaseSingleton.userFirebase
        if (user != null) {
            Picasso.Builder(this).build().load(user.photoUrl).error(R.drawable.ic_cancel)
                .into(binding.imgUserEntry)

        }
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
        presenter = HomePresenter(this, getString(R.string.key_api), this)
        pathLocal =getString(R.string.popular_r)
        presenter.changePath(pathLocal,page)
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvMainMovie.layoutManager = layoutManager
        var isScrolling = false
        binding.rvMainMovie.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItems: IntArray? = layoutManager.findFirstVisibleItemPositions(null)
                val pastVisibleItems =
                    if (firstVisibleItems != null && firstVisibleItems.isNotEmpty()) {
                        firstVisibleItems[0]
                    } else 0

                if (isScrolling) {
                    if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                        isScrolling = false
                        page+=1
                        if (page<pageTotal)
                            presenter.changePath(pathLocal,page)
                        Log.e("page","page $page")
                    }
                }

            }
        })



        binding.btnMap.setOnClickListener {
            startActivity(Intent(this, MapsActivity::class.java))
        }

        binding.btnCamara.setOnClickListener {
            startActivity(Intent(this, ArchivoActivity::class.java))
        }

        complete.doOnTextChanged { text, _, _, _ ->
            pathLocal = text.toString()
            pathLocal =when (text.toString()) {
                getString(R.string.popular) -> getString(R.string.popular_r)
                getString(R.string.now_playing) -> getString(R.string.now_playing_r)
                getString(R.string.upcoming) -> getString(R.string.upcoming_r)
                getString(R.string.top_rated) -> getString(R.string.top_rated_r)
                else -> getString(R.string.popular_r)
            }
            presenter.changePath(pathLocal,page)
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

    override fun showListMovies(listMoviesShow: List<Result>, statusCode: Int, totalPages: Int) {
        this.pageTotal=totalPages
        binding.rvMainMovie.visibility = View.VISIBLE
        binding.tvNotElement.visibility = View.GONE
        adapter.addItems(listMoviesShow.toMutableList())

        if (statusCode == 400)
            Toast.makeText(this, getString(R.string.error_conection), Toast.LENGTH_SHORT).show()
    }

    override fun errorListMovies(statusCode: Int) {
        Log.e("listShow", "list $statusCode")
        binding.rvMainMovie.visibility = View.GONE
        binding.tvNotElement.visibility = View.VISIBLE
    }

}