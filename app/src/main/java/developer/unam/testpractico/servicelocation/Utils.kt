package developer.unam.testpractico

import android.content.Context
import android.location.Location
import androidx.core.content.edit

/**
 * Returns latitud y longitud del objeto Location obtenido
 */
fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}


