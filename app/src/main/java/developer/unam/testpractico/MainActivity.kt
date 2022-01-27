package developer.unam.testpractico

import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import developer.unam.testpractico.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),LocationListener {

    private lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    override fun onLocationChanged(location: Location) {

    }
}