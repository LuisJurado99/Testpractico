package developer.unam.testpractico.view.login

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import developer.unam.testpractico.R
import developer.unam.testpractico.databinding.ActivityLoginBinding
import developer.unam.testpractico.singleton.UserFirebaseSingleton
import developer.unam.testpractico.view.home.view.MainActivity

class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleSignClient: GoogleSignInClient
    private lateinit var auth:FirebaseAuth

    private val selectLoginGoogle = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        when(it.resultCode){
            Activity.RESULT_OK->{
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken.toString())

                } catch (e: ApiException) {
                    Log.e("errorLogin","error ${e.message}")

                }
            }
            Activity.RESULT_CANCELED->{
                val material = MaterialAlertDialogBuilder(this@LoginActivity)
                material.setTitle(R.string.login)
                material.setMessage("ResultCancel")
                material.setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                material.setNegativeButton("No") { dialog, _ ->

                    dialog.dismiss()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                material.create().show()

            }

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth
    }

    override fun onResume() {
        super.onResume()
        binding.btnSign.setOnClickListener {
            //Marca error porque no reconoce el string pero se obtiene del JsonGoogleService
            val optionSign =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

            googleSignClient = GoogleSignIn.getClient(this, optionSign)
            //googleSignClient.signOut()
            lifecycleScope.launchWhenCreated {
                selectLoginGoogle.launch(googleSignClient.signInIntent)
            }

            Log.e("click","clickButtonLog")


        }

    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e(ContentValues.TAG, "signInWithCredential:success ${Gson().toJson(auth.currentUser)}")
                    val user = auth.currentUser
                    UserFirebaseSingleton.userFirebase = user
                    startActivity(Intent(this,MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    val material = MaterialAlertDialogBuilder(this@LoginActivity)
                    material.setTitle(R.string.login)
                    material.setMessage(R.string.error_generic)
                    material.setNeutralButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    material.create().show()

                }
            }

    }
}