package developer.unam.testpractico.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import developer.unam.testpractico.BuildConfig
import developer.unam.testpractico.R
import developer.unam.testpractico.databinding.ActivityArchivoBinding
import java.io.File

class ArchivoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArchivoBinding

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()){ it ->
        val alertDialog = MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.archivo_title)
            setMessage(R.string.cargando)
            setCancelable(false)
        }.create()
        alertDialog.show()
        if (it){
            latestTmUri?.let{ uri->
                val storage = Firebase.storage.getReference(uri.encodedPath.toString())
                storage.child("images/${uri.encodedPath?.split(" / ")?.last().toString()}")
                storage.putFile(uri).apply {
                    addOnCompleteListener {
                        alertDialog.dismiss()
                        if (it.isSuccessful){
                            val alert= MaterialAlertDialogBuilder(this@ArchivoActivity).apply {
                                setTitle(R.string.archivo_title)
                                setMessage(R.string.archivo_update_successful)
                                setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                            }
                            alert.create().show()
                        }else{
                            alertDialog.dismiss()
                            val alert= MaterialAlertDialogBuilder(this@ArchivoActivity).apply {
                                setTitle(R.string.archivo_title)
                                setMessage(R.string.archivo_update_error)
                                setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                            }
                            alert.create().show()
                        }
                    }
                    addOnFailureListener {
                        alertDialog.dismiss()
                        val alert= MaterialAlertDialogBuilder(this@ArchivoActivity).apply {
                            setTitle(R.string.archivo_title)
                            setMessage(R.string.archivo_update_error)
                            setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                        }
                        alert.create().show()
                    }
                }
                Log.e("uriCamera", uri.encodedPath?.split("/")?.last().toString())
                binding.previewImage.setImageURI(uri)
            }
        }
    }

    private val selectImageGallery = registerForActivityResult(ActivityResultContracts.GetContent()){ uriNull ->
        uriNull?.let { uri->
            val alertDialog = MaterialAlertDialogBuilder(this).apply {
                setTitle(R.string.archivo_title)
                setMessage(R.string.cargando)
                setCancelable(false)
            }.create()
            alertDialog.show()
            val storage = Firebase.storage.getReference(uri.encodedPath.toString())
            storage.child("images/${uri.encodedPath?.split(" / ")?.last().toString()}")
            storage.putFile(uri).apply {
                addOnCompleteListener {
                    alertDialog.dismiss()
                    if (it.isSuccessful){
                        val alert= MaterialAlertDialogBuilder(this@ArchivoActivity).apply {
                            setTitle(R.string.archivo_title)
                            setMessage(R.string.archivo_update_successful)
                            setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                        }
                        alert.create().show()
                    }else{
                        val alert= MaterialAlertDialogBuilder(this@ArchivoActivity).apply {
                            setTitle(R.string.archivo_title)
                            setMessage(R.string.archivo_update_error)
                            setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                        }
                        alert.create().show()
                    }
                }
                addOnFailureListener {
                    alertDialog.dismiss()
                    val alert= MaterialAlertDialogBuilder(this@ArchivoActivity).apply {
                        setTitle(R.string.archivo_title)
                        setMessage(R.string.archivo_update_error)
                        setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    }
                    alert.create().show()
                }
            }
            binding.previewImage.setImageURI(uri)
        }
    }

    private var latestTmUri: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArchivoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.tbArchivo)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.btnCamara.setOnClickListener {
            takeImage()
        }

        binding.imageGalery.setOnClickListener {
            selectImageFromGallery()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun selectImageFromGallery() = selectImageGallery.launch("image/*")

    private fun takeImage(){
        lifecycleScope.launchWhenCreated {
            getTmpFileUri().let {
                latestTmUri = it
                takeImageResult.launch(it)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }


}