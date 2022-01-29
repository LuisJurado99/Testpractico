/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package developer.unam.testpractico.servicelocation

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import developer.unam.testpractico.view.MainActivity
import developer.unam.testpractico.R
/*import developer.unam.testpractico.SharedPreferenceUtil*/
import developer.unam.testpractico.toText
import java.util.concurrent.TimeUnit

class ForegroundOnlyLocationService : Service() {

    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    //Manejo de notificaciones
    private lateinit var notificationManager: NotificationManager

    // Provee la o las obicaciones del dispositivo
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentLocation: Location? = null

    @SuppressLint("LongLogTag")
    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        FirebaseApp.initializeApp(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        locationRequest = LocationRequest.create().apply {
            //Setear intervalos de duración para tener la peticion cada 30 min

            interval = TimeUnit.SECONDS.toMillis(60)
            fastestInterval = TimeUnit.SECONDS.toMillis(30)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                //Obtener la ultima localización del usuario
                currentLocation = locationResult.lastLocation
                //Realizar push a Firebase para tener las ubicaciones del usuario
                FirebaseApp.initializeApp(applicationContext)
                val db = Firebase.firestore
                val map = hashMapOf(
                    ("latitude" to currentLocation?.latitude?:0.0) as Pair<String, Double>,
                    ("longitude" to currentLocation?.longitude?:0.0) as Pair<String, Double>,
                )
                db.collection("ubication").add(map).addOnSuccessListener {
                    Log.e("foregroundService","elementros agregados exitosamente ${it.id}")
                }.addOnFailureListener {
                    Log.e("foregroundService","elementros agregados CON ERROR ${it.message}")
                }
                //Verificar que la app corre en segundo plano, y generar la norificación
                if (serviceRunningInForeground) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification(currentLocation))
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind()")
        //Se construye el servicio para que opere
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")
        //Metodo para reconstrución del servicio
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")
        //Se ocupa para generar el servicio en segundo plano
        Log.d(TAG, "Start foreground service")
        val notification = generateNotification(currentLocation)
        startForeground(NOTIFICATION_ID, notification)
        serviceRunningInForeground = true

        return true
    }

    override fun onDestroy() {
        //Se quita la destrucción para mantener el segundo plano
        Log.d(TAG, "onDestroy()")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")
        //Se inicializa el servicio de ubicación en primer plano
        startService(Intent(applicationContext, ForegroundOnlyLocationService::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper())
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    private fun generateNotification(location: Location?): Notification {
        Log.d(TAG, "generateNotification()")
        val mainNotificationText = location?.toText() ?: getString(R.string.no_location_text)
        val titleText = getString(R.string.app_name)


        //Se crea el canal de comunicación para la versiones mayores O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)
        //Crea intent para relanzar la primer actividad
        val launchActivityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)

        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_launch, getString(R.string.launch_activity),
                activityPendingIntent
            )
            .build()
    }

    /**
     * Constructor Local para ejecutar los procesos.
     */
    inner class LocalBinder : Binder() {
        internal val service: ForegroundOnlyLocationService
            get() = this@ForegroundOnlyLocationService
    }

    companion object {
        private const val TAG = "ForegroundOnlyLocation"
        private const val PACKAGE_NAME = "com.example.android.whileinuselocation"
        //Registra el background del servicio
        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
        //Variables para los canales de notificaciones
        private const val NOTIFICATION_ID = 1212
        private const val NOTIFICATION_CHANNEL_ID = "test_practico_chanel"
    }
}
