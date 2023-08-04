package com.example.storyappdicoding.ui.view.upload

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.storyappdicoding.data.db.StoryDatabase
import com.example.storyappdicoding.data.remote.ApiConfig
import com.example.storyappdicoding.databinding.ActivityUploadStoryBinding
import com.example.storyappdicoding.ui.view.camera.CameraActivity
import com.example.storyappdicoding.ui.viewmodel.UploadStoryViewModel
import com.example.storyappdicoding.ui.viewmodel.factory.MainViewModelFactory
import com.example.storyappdicoding.utils.AccountPreferences
import com.example.storyappdicoding.utils.Event
import com.example.storyappdicoding.utils.Result
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class UploadStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val tokenId by lazy { intent.getStringExtra(AccountPreferences.TOKEN_ID) }

    private val uploadStoryViewModel by viewModels<UploadStoryViewModel> {
        MainViewModelFactory(
            tokenId!!,
            StoryDatabase.getDatabaseInstance(this),
            ApiConfig.getApiService()
        )
    }

    private val cameraxLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra(KEY_IMAGE, File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra(KEY_IMAGE)
            } as? File

            val isBackCamera = it.data?.getBooleanExtra(IS_BACK_CAMERA, true) as Boolean
            val isFromGallery = it.data?.getBooleanExtra(IS_FROM_GALLERY, false) as Boolean

            if (isFromGallery) {
                binding.ivStory.setImageBitmap(BitmapFactory.decodeFile(myFile!!.path))
                uploadStoryViewModel.imageFile.postValue(myFile)
            } else {
                myFile?.let { file ->
                    rotateFile(file, isBackCamera)
                    binding.ivStory.setImageBitmap(BitmapFactory.decodeFile(file.path))
                    uploadStoryViewModel.imageFile.postValue(myFile)
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                else -> {
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        observeLoading()
        observeToast()
        observeUploadButton()
        observeImageFile()
        observeCurrentLocation()

        setListeners()
    }

    private fun observeLoading() {
        uploadStoryViewModel.isLoading.observe(this) {
            showLoading(it)
        }
    }

    private fun observeToast() {
        uploadStoryViewModel.toastText.observe(this) {
            it.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeUploadButton() {
        uploadStoryViewModel.canUpload.observe(this) { upload ->
            binding.btnUpload.isEnabled = upload
        }
    }

    private fun observeImageFile() {
        uploadStoryViewModel.imageFile.observe(this) { imageFile ->
            binding.ivStory.setImageBitmap(BitmapFactory.decodeFile(imageFile!!.path))
        }
    }

    private fun observeCurrentLocation() {
        uploadStoryViewModel.latCoor.observe(this) { lat ->
            uploadStoryViewModel.lonCoor.observe(this) { lon ->
                val isLocationShowed = lat != null && lon != null
                showLocation(isLocationShowed)

                if (isLocationShowed) {
                    binding.tvStoryLocation.text = getAddress(lat, lon)
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            edStoryDesc.addTextChangedListener(onTextChanged = { desc, _, _, _ ->
                uploadStoryViewModel.descText.postValue(desc.toString())
            })

            btnAddImage.setOnClickListener {
                if (!allPermissionsGranted()) {
                    ActivityCompat.requestPermissions(
                        this@UploadStoryActivity,
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_PERMISSIONS
                    )
                } else {
                    val iCamera = Intent(this@UploadStoryActivity, CameraActivity::class.java)
                    cameraxLauncher.launch(iCamera)
                }
            }

            switchLocation.setOnCheckedChangeListener { buttonView, _ ->
                if (buttonView.isChecked) {
                    getMyLastLocation()
                } else {
                    uploadStoryViewModel.latCoor.postValue(null)
                    uploadStoryViewModel.lonCoor.postValue(null)
                }
            }

            btnUpload.setOnClickListener {
                uploadStoryViewModel.isLoading.postValue(true)

                val newReducedFile = reduceImage(uploadStoryViewModel.imageFile.value!!)
                val myDesc = uploadStoryViewModel.descText.value

                uploadStoryViewModel.uploadStory(newReducedFile, myDesc!!).observe(this@UploadStoryActivity) { result ->
                    when (result) {
                        is Result.Loading -> {
                            uploadStoryViewModel.isLoading.postValue(true)
                        }
                        is Result.Error -> {
                            uploadStoryViewModel.isLoading.postValue(false)
                            uploadStoryViewModel.toastText.postValue(Event(result.error))
                        }
                        is Result.Success -> {
                            uploadStoryViewModel.isLoading.postValue(false)
                            uploadStoryViewModel.toastText.postValue(Event("Succesfully uploaded!"))
                            val intent = Intent()
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun rotateFile(file: File, isBackCamera: Boolean = false) {
        val matrix = Matrix()
        val bitmap = BitmapFactory.decodeFile(file.path)
        val rotation = if (isBackCamera) 90f else -90f
        matrix.postRotate(rotation)
        if (!isBackCamera) {
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        val result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        result.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Can't receive permissions.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                val iCamera = Intent(this@UploadStoryActivity, CameraActivity::class.java)
                cameraxLauncher.launch(iCamera)
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    uploadStoryViewModel.latCoor.postValue(location.latitude)
                    uploadStoryViewModel.lonCoor.postValue(location.longitude)
                } else {
                    Toast.makeText(
                        this@UploadStoryActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showLocation(isLocationShowed: Boolean) {
        binding.tvStoryLocation.isVisible = isLocationShowed
    }

    private fun getAddress(lat: Double, lng: Double): String? {
        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat, lng, 1)
        return list?.get(0)!!.getAddressLine(0)
    }

    private fun reduceImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000000)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressbar.isVisible = isLoading
            btnUpload.isVisible = !isLoading
            switchLocation.isEnabled = !isLoading
            btnAddImage.isEnabled = !isLoading
            edStoryDesc.isEnabled = !isLoading
        }
    }

    companion object {
        const val KEY_IMAGE = "key_image"
        const val IS_BACK_CAMERA = "is_back_camera"
        const val IS_FROM_GALLERY = "is_from_gallery"

        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}