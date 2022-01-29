package developer.unam.testpractico.view

import android.annotation.SuppressLint
import android.icu.text.DateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import developer.unam.testpractico.R
import developer.unam.testpractico.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    @SuppressLint("UseSupportActionBar")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar(binding.tlbMaps)
        actionBar?.setHomeButtonEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        binding.tlbMaps.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Firebase.firestore.collection("ubication").get().addOnSuccessListener {
            var last:QueryDocumentSnapshot? =null
            for (doc in it){
                Log.d("MapsActivity", "${doc.id} => ${doc.data["latitude"] as Double}, ${doc.data["longitude"] as Double}")
                mMap.addMarker(MarkerOptions().position(LatLng(
                    doc.data["latitude"] as Double,
                    doc.data["longitude"] as Double
                )).title(doc.id))
                last = doc
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(
                (last?.data?.get("latitude") ?:0.0) as Double,
                (last?.data?.get("longitude") ?:0.0) as Double
            )))

        }
    }
}
