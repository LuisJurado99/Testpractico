package developer.unam.testpractico.application

import android.app.Application
import com.google.firebase.FirebaseApp

class TestPractico : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
    }
}