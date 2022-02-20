package developer.unam.testpractico.singleton

import com.google.firebase.auth.FirebaseUser

object UserFirebaseSingleton {
    private var instance:UserFirebaseSingleton? = null
    var userFirebase :FirebaseUser?=null

    @Synchronized
    private fun createInstance(){
        if (instance == null)
            instance = UserFirebaseSingleton
    }
    fun getInstance():UserFirebaseSingleton{
        if(instance == null)
            createInstance()
        return instance!!
    }

}