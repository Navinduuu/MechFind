package com.version1.test1

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import android.net.Uri
import java.util.HashMap

object SessionManager {
    private const val PREFS_NAME = "user_session"
    private const val KEY_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_KEY = "user_key"
    private const val KEY_NODE_KEY = "node_key"
    private const val KEY_USER_TYPE = "user_type"
    private const val KEY_LAT = "latitude"
    private const val KEY_LON = "longitude"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isLoggedIn(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_LOGGED_IN, false)
    }

    fun saveSession(context: Context, userKey: String, nodeKey: String, userType: String, latitude: Double, longitude: Double) {
        val editor = prefs(context).edit()
        editor.putBoolean(KEY_LOGGED_IN, true)
        editor.putString(KEY_USER_KEY, userKey)
        editor.putString(KEY_NODE_KEY, nodeKey)
        editor.putString(KEY_USER_TYPE, userType)
        editor.putFloat(KEY_LAT, latitude.toFloat())
        editor.putFloat(KEY_LON, longitude.toFloat())
        editor.apply()
    }

    fun getUserKey(context: Context): String {
        return prefs(context).getString(KEY_USER_KEY, "") ?: ""
    }

    fun getNodeKey(context: Context): String {
        return prefs(context).getString(KEY_NODE_KEY, "") ?: ""
    }

    fun getUserType(context: Context): String {
        return prefs(context).getString(KEY_USER_TYPE, "") ?: "Customer"
    }

    fun getLatitude(context: Context): Double {
        return prefs(context).getFloat(KEY_LAT, 0f).toDouble()
    }

    fun getLongitude(context: Context): Double {
        return prefs(context).getFloat(KEY_LON, 0f).toDouble()
    }

    fun clearSession(context: Context) {
        val editor = prefs(context).edit()
        editor.clear()
        editor.apply()
    }
}


class MainActivity : AppCompatActivity() {

    interface ImageCallback {
        fun onImageReady(bitmap: Bitmap?)
    }

    var pendingVehicleImageCallback: ImageCallback? = null
    private var pendingCameraPhotoUri: Uri? = null

    private lateinit var galleryImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        galleryImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    var bitmap: Bitmap? = null
                    if (result.resultCode == Activity.RESULT_OK) {
                        val selectedUri = result.data?.data
                        try {
                            if (selectedUri != null) {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(contentResolver, selectedUri)
                                    bitmap = ImageDecoder.decodeBitmap(source)
                                } else {
                                    @Suppress("DEPRECATION")
                                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedUri)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val callback = pendingVehicleImageCallback
                    if (callback != null) {
                        callback.onImageReady(bitmap)
                    }
                }
            }
        )

        cameraImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    var bitmap: Bitmap? = null
                    if (result.resultCode == Activity.RESULT_OK) {
                        val uri = pendingCameraPhotoUri
                        try {
                            if (uri != null) {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(contentResolver, uri)
                                    bitmap = ImageDecoder.decodeBitmap(source)
                                } else {
                                    @Suppress("DEPRECATION")
                                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    val callback = pendingVehicleImageCallback
                    if (callback != null) {
                        callback.onImageReady(bitmap)
                    }
                }
            }
        )

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            object : ActivityResultCallback<Boolean> {
                override fun onActivityResult(granted: Boolean) {
                    if (granted) {
                        launchCameraCapture()
                    } else {
                        Toast.makeText(this@MainActivity, "Camera permission is needed to take a photo.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        val viewFlipper = ViewFlipper(this)
        viewFlipper.id = View.generateViewId()

        setContentView(viewFlipper)

        val manager = UiManager()

        if (SessionManager.isLoggedIn(this)) {
            manager.restoreUserSession(this)
            val sessionType = SessionManager.getUserType(this)
            if (sessionType == "Mechanic" || sessionType == "TowTruck") {
                manager.navigateToMechanicDashboardUi(this, viewFlipper)
            } else {
                manager.navigateToDashboardUi(this, viewFlipper)
            }
        } else {
            val splashView = layoutInflater.inflate(R.layout.splash_screen, null)
            viewFlipper.addView(splashView)
            manager.initializeFlow(this, viewFlipper)
        }
    }

    fun launchGalleryPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryImageLauncher.launch(intent)
    }

    fun launchCameraCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        val photoFile = File.createTempFile("vehicle_photo_", ".jpg", cacheDir)
        val photoUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
        pendingCameraPhotoUri = photoUri

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        cameraImageLauncher.launch(intent)
    }

    class UiManager {
        private var fusedLocationClient: FusedLocationProviderClient? = null
        private val userRegistrationProfile = RegistrationDto()
        private var generatedEmailOtpCode: String = ""
        private var modelTrainer: ChatModelTrainer? = null
        private val appNotifications = ArrayList<NotificationDto>()
        private var liveLocationCallback: LocationCallback? = null
        private var currentRadarOverlay: RadarMapOverlay? = null

        init {
            val defaultNotif = NotificationDto()
            defaultNotif.title = "Welcome!"
            defaultNotif.message = "Welcome to MachFind. Your account is verified and ready to use."
            defaultNotif.time = "Just now"
            appNotifications.add(defaultNotif)
        }

        fun restoreUserSession(activity: MainActivity) {
            userRegistrationProfile.latitude = SessionManager.getLatitude(activity)
            userRegistrationProfile.longitude = SessionManager.getLongitude(activity)
        }

        class MapSearchManager {
            var searchQuery: String = ""

            fun validateAndParse(): String {
                if (searchQuery.isEmpty()) {
                    return ""
                }
                return searchQuery.trim().lowercase()
            }
        }

        class ProfileManager {
            var fullName: String = ""
            var mobileNumber: String = ""
            var email: String = ""
            var street: String = ""
            var gender: String = ""

            fun validateAndParse(): Boolean {
                if (fullName.isEmpty() || email.isEmpty() || mobileNumber.isEmpty()) {
                    return false
                }
                return true
            }
        }

        class BookingManager {
            var vehicleMake: String = ""
            var bookingDate: String = ""
            var bookingTime: String = ""
            var issueDescription: String = ""
            var locationAddress: String = ""

            fun validateAndParse(): Boolean {
                if (vehicleMake.isEmpty() || bookingDate.isEmpty() || locationAddress.isEmpty()) {
                    return false
                }
                return true
            }
        }

        class PaymentManager {
            var cardName: String = ""
            var cardNumber: String = ""
            var cardExpiry: String = ""
            var cardCvv: String = ""
            var isCashSelected: Boolean = false

            /** Standard Luhn checksum used by every major card network. */
            private fun passesLuhnCheck(rawDigits: String): Boolean {
                var sum = 0
                var alternate = false
                for (i in rawDigits.length - 1 downTo 0) {
                    var digit = rawDigits[i] - '0'
                    if (alternate) {
                        digit *= 2
                        if (digit > 9) digit -= 9
                    }
                    sum += digit
                    alternate = !alternate
                }
                return sum % 10 == 0
            }

            fun validateAndParse(): Boolean {
                if (isCashSelected) {
                    return true
                }
                if (cardName.isEmpty() || cardNumber.isEmpty() || cardExpiry.isEmpty() || cardCvv.isEmpty()) {
                    return false
                }

                val digitsOnly = cardNumber.filter { it.isDigit() }
                if (digitsOnly.length !in 13..19 || !passesLuhnCheck(digitsOnly)) {
                    return false
                }

                if (!cardExpiry.matches(Regex("^(0[1-9]|1[0-2])/\\d{2}$"))) {
                    return false
                }

                if (cardCvv.length !in 3..4 || !cardCvv.all { it.isDigit() }) {
                    return false
                }

                return true
            }

            /** Only the last 4 digits are ever kept, for the receipt/reference display. */
            fun lastFourDigits(): String {
                val digitsOnly = cardNumber.filter { it.isDigit() }
                return if (digitsOnly.length >= 4) digitsOnly.takeLast(4) else digitsOnly
            }
        }

        fun initializeFlow(activity: MainActivity, viewFlipper: ViewFlipper) {
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    val smoothInterpolator = AccelerateDecelerateInterpolator()

                    val slideInRight = TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f
                    )
                    slideInRight.duration = 550
                    slideInRight.interpolator = smoothInterpolator

                    val slideOutLeft = TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, -1.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f,
                        Animation.RELATIVE_TO_PARENT, 0.0f
                    )
                    slideOutLeft.duration = 550
                    slideOutLeft.interpolator = smoothInterpolator

                    viewFlipper.inAnimation = slideInRight
                    viewFlipper.outAnimation = slideOutLeft

                    val mainView1 = activity.layoutInflater.inflate(R.layout.main_screen, null)
                    viewFlipper.addView(mainView1)
                    viewFlipper.showNext()

                    viewFlipper.inAnimation = null
                    viewFlipper.outAnimation = null

                    setupMainOnboardingWalkthrough(activity, viewFlipper, mainView1)
                }
            }, 3000)
        }

        private fun setupMainOnboardingWalkthrough(activity: MainActivity, viewFlipper: ViewFlipper, mainView1: View) {
            val btnNext = mainView1.findViewById<Button>(R.id.btnNext)
            if (btnNext != null) {
                btnNext.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToLocationUi(activity, viewFlipper)
                    }
                })
            }

            val btnNext2 = mainView1.findViewById<Button>(R.id.btnNext2)
            if (btnNext2 != null) {
                btnNext2.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val mainView2 = activity.layoutInflater.inflate(R.layout.main_screen2, null)
                        viewFlipper.addView(mainView2)
                        viewFlipper.showNext()

                        val btnNextSub1 = mainView2.findViewById<Button>(R.id.btnNext)
                        if (btnNextSub1 != null) {
                            btnNextSub1.setOnClickListener(object : View.OnClickListener {
                                override fun onClick(v: View?) {
                                    navigateToLocationUi(activity, viewFlipper)
                                }
                            })
                        }

                        val logoImageView3 = mainView2.findViewById<ImageView>(R.id.logoImageView2)
                        if (logoImageView3 != null) {
                            logoImageView3.setOnClickListener(object : View.OnClickListener {
                                override fun onClick(v: View?) {
                                    val mainView3 = activity.layoutInflater.inflate(R.layout.main_screen3, null)
                                    viewFlipper.addView(mainView3)
                                    viewFlipper.showNext()

                                    val btnNextSub2 = mainView3.findViewById<Button>(R.id.btnNext)
                                    if (btnNextSub2 != null) {
                                        btnNextSub2.setOnClickListener(object : View.OnClickListener {
                                            override fun onClick(v: View?) {
                                                navigateToLocationUi(activity, viewFlipper)
                                            }
                                        })
                                    }

                                    val logoImageView4 = mainView3.findViewById<ImageView>(R.id.logoImageView4)
                                    if (logoImageView4 != null) {
                                        logoImageView4.setOnClickListener(object : View.OnClickListener {
                                            override fun onClick(v: View?) {
                                                navigateToLocationUi(activity, viewFlipper)
                                            }
                                        })
                                    }
                                }
                            })
                        }
                    }
                })
            }
        }

        private fun navigateToLocationUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val locationView = activity.layoutInflater.inflate(R.layout.enable_location, null)
            viewFlipper.addView(locationView)
            viewFlipper.showNext()

            val btnUseLocation = locationView.findViewById<Button>(R.id.btnUseLocation)
            if (btnUseLocation != null) {
                btnUseLocation.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        getUserCurrentLocation(activity, viewFlipper)
                    }
                })
            }

            val btnSkipNow = locationView.findViewById<Button>(R.id.btnSkipNow)
            if (btnSkipNow != null) {
                btnSkipNow.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToWelcomeUi(activity, viewFlipper)
                    }
                })
            }
        }

        private fun getUserCurrentLocation(activity: MainActivity, viewFlipper: ViewFlipper) {
            if (fusedLocationClient == null) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
            }

            val fineLocationCheck = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarseLocationCheck = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)

            if (fineLocationCheck != PackageManager.PERMISSION_GRANTED && coarseLocationCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1001
                )
                return
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val currentLiveLocation = locationResult.lastLocation
                    if (currentLiveLocation != null) {
                        userRegistrationProfile.latitude = currentLiveLocation.latitude
                        userRegistrationProfile.longitude = currentLiveLocation.longitude
                    }
                    fusedLocationClient?.removeLocationUpdates(this)
                    navigateToWelcomeUi(activity, viewFlipper)
                }
            }

            try {
                fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            } catch (unauthorizedSecurityException: SecurityException) {
                navigateToWelcomeUi(activity, viewFlipper)
            }
        }

        private fun navigateToWelcomeUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val welcomeView = activity.layoutInflater.inflate(R.layout.activity_welcome, null)
            viewFlipper.addView(welcomeView)
            viewFlipper.showNext()

            val btnCreateAccount = welcomeView.findViewById<Button>(R.id.btnCreateAccount)
            if (btnCreateAccount != null) {
                btnCreateAccount.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToSignUpUi(activity, viewFlipper)
                    }
                })
            }

            val btnLogIn = welcomeView.findViewById<Button>(R.id.btnLogIn)
            if (btnLogIn != null) {
                btnLogIn.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToSignInUi(activity, viewFlipper)
                    }
                })
            }
        }

        private fun navigateToSignUpUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val signUpView = activity.layoutInflater.inflate(R.layout.activity_sign_up, null)
            viewFlipper.addView(signUpView)
            viewFlipper.showNext()

            val layoutBack = signUpView.findViewById<View>(R.id.layoutBack)
            if (layoutBack != null) {
                layoutBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                    }
                })
            }

            val tvAlreadyHaveAccount = signUpView.findViewById<TextView>(R.id.tvAlreadyHaveAccount)
            if (tvAlreadyHaveAccount != null) {
                tvAlreadyHaveAccount.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToSignInUi(activity, viewFlipper)
                    }
                })
            }

            val spinnerGender = signUpView.findViewById<Spinner>(R.id.spinnerGender)
            if (spinnerGender != null) {
                val adapter = ArrayAdapter.createFromResource(
                    activity,
                    R.array.gender_array,
                    android.R.layout.simple_spinner_item
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerGender.adapter = adapter
            }

            val spinnerRole = signUpView.findViewById<Spinner>(R.id.spinnerRole)
            val spinnerSpecialization = signUpView.findViewById<Spinner>(R.id.spinnerSpecialization)
            val layoutSpecializationWrapper = signUpView.findViewById<View>(R.id.layoutSpecializationWrapper)

            if (spinnerRole != null) {
                val roleAdapter = ArrayAdapter.createFromResource(
                    activity,
                    R.array.role_array,
                    android.R.layout.simple_spinner_item
                )
                roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerRole.adapter = roleAdapter

                spinnerRole.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                        // Position 1 == "I am a Mechanic" -> reveal specialization picker.
                        layoutSpecializationWrapper?.visibility = if (position == 1) View.VISIBLE else View.GONE
                    }
                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
            }

            if (spinnerSpecialization != null) {
                val specAdapter = ArrayAdapter.createFromResource(
                    activity,
                    R.array.specialization_array,
                    android.R.layout.simple_spinner_item
                )
                specAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerSpecialization.adapter = specAdapter
            }

            val btnSignUp = signUpView.findViewById<Button>(R.id.btnSignUp)
            if (btnSignUp != null) {
                btnSignUp.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val etName = signUpView.findViewById<EditText>(R.id.etName)
                        val etEmail = signUpView.findViewById<EditText>(R.id.etEmail)
                        val etMobileNumber = signUpView.findViewById<EditText>(R.id.etMobileNumber)
                        val cbTerms = signUpView.findViewById<CheckBox>(R.id.cbTerms)

                        val inputName = etName.text.toString().trim()
                        val inputEmail = etEmail.text.toString().trim()
                        val inputPhone = etMobileNumber.text.toString().trim()
                        val selectedGenderPosition = spinnerGender?.selectedItemPosition ?: 0
                        val selectedGender = spinnerGender?.selectedItem?.toString() ?: ""

                        if (inputName.isEmpty() || inputEmail.isEmpty() || inputPhone.isEmpty()) {
                            Toast.makeText(activity, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        if (selectedGenderPosition == 0 || selectedGender == "Gender") {
                            Toast.makeText(activity, "Please select a valid gender.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
                            Toast.makeText(activity, "Invalid email address structure.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        if (inputPhone.length > 20) {
                            Toast.makeText(activity, "Mobile input cannot exceed 20 digits.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        if (!cbTerms.isChecked) {
                            Toast.makeText(activity, "You must accept the terms.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val selectedRolePosition = spinnerRole?.selectedItemPosition ?: 0
                        val resolvedUserType = when (selectedRolePosition) {
                            1 -> "Mechanic"
                            2 -> "TowTruck"
                            else -> "User"
                        }

                        var resolvedSpecialization = ""
                        if (resolvedUserType == "Mechanic") {
                            val specPosition = spinnerSpecialization?.selectedItemPosition ?: 0
                            if (specPosition == 0) {
                                Toast.makeText(activity, "Please select your specialization.", Toast.LENGTH_SHORT).show()
                                return
                            }
                            resolvedSpecialization = spinnerSpecialization?.selectedItem?.toString() ?: ""
                        }

                        userRegistrationProfile.name = inputName
                        userRegistrationProfile.email = inputEmail
                        userRegistrationProfile.phone = "+94$inputPhone"
                        userRegistrationProfile.gender = selectedGender
                        userRegistrationProfile.userType = resolvedUserType
                        userRegistrationProfile.specialization = resolvedSpecialization
                        // Only service providers need admin approval before they show up
                        // in search results; ordinary customer accounts are fine immediately.
                        userRegistrationProfile.isVerified = (resolvedUserType == "User")

                        dispatchVerificationEmailBackground(activity, userRegistrationProfile.email)
                        navigateToPhoneVerificationUi(activity, viewFlipper)
                    }
                })
            }
        }

        private fun dispatchVerificationEmailBackground(activity: MainActivity, targetRecipientEmail: String) {
            val randomToken = ((Math.random() * 90000) + 10000).toInt()
            generatedEmailOtpCode = randomToken.toString()

            val networkWorkerThread = Thread(object : Runnable {
                override fun run() {
                    try {
                        val hostSmtpServer = "smtp.gmail.com"
                        val senderSystemEmail = "dilishapadhananjaya@gmail.com"
                        val senderAppPassword = "sugo prey vcfu cgog"

                        val configurations = Properties()
                        configurations["mail.smtp.host"] = hostSmtpServer
                        configurations["mail.smtp.socketFactory.port"] = "465"
                        configurations["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
                        configurations["mail.smtp.auth"] = "true"
                        configurations["mail.smtp.port"] = "465"
                        configurations["mail.smtp.connecttimeout"] = "10000"
                        configurations["mail.smtp.timeout"] = "10000"

                        val loginSession = Session.getInstance(configurations, object : javax.mail.Authenticator() {
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(senderSystemEmail, senderAppPassword)
                            }
                        })

                        val structuralEmailMessage = MimeMessage(loginSession)
                        structuralEmailMessage.setFrom(InternetAddress(senderSystemEmail))
                        structuralEmailMessage.addRecipient(Message.RecipientType.TO, InternetAddress(targetRecipientEmail))
                        structuralEmailMessage.subject = "MachFind App - Registration Verification Code"

                        // Set the body AND the MIME headers explicitly. On stock Android the
                        // javax.mail/javax.activation "auto content-handler" lookup used by
                        // setText()+saveChanges() crashes with a VerifyError (it references
                        // java.awt.datatransfer which does not exist on Android). Setting the
                        // Content-Type/Content-Transfer-Encoding headers ourselves means
                        // Transport.send() never has to auto-detect them, so that broken code
                        // path is never touched.
                        structuralEmailMessage.setText(
                            "Hello,\n\nYour dynamic registration code is: $generatedEmailOtpCode\n\nEnter this inside your registration screen to finish verification."
                        )
                        structuralEmailMessage.setHeader("Content-Type", "text/plain; charset=us-ascii")
                        structuralEmailMessage.setHeader("Content-Transfer-Encoding", "7bit")
                        structuralEmailMessage.saveChanges()

                        Transport.send(structuralEmailMessage)

                        val uiThreadHandler = Handler(Looper.getMainLooper())
                        uiThreadHandler.post(object : Runnable {
                            override fun run() {
                                Toast.makeText(activity, "Verification email dispatched successfully!", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } catch (emailMessagingException: Exception) {
                        Log.e("EMAIL_SMTP_DEBUG", "CRITICAL SMTP FAULT: ${emailMessagingException.message}")
                        val uiThreadHandler = Handler(Looper.getMainLooper())
                        uiThreadHandler.post(object : Runnable {
                            override fun run() {
                                Toast.makeText(activity, "Could not send verification email. Check your connection.", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            })
            networkWorkerThread.start()
        }

        private fun navigateToPhoneVerificationUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val verificationView = activity.layoutInflater.inflate(R.layout.activity_phone_verification, null)
            viewFlipper.addView(verificationView)
            viewFlipper.showNext()

            val tvSubtitle = verificationView.findViewById<TextView>(R.id.tvSubtitle)
            if (tvSubtitle != null) {
                tvSubtitle.text = "Enter your Email OTP code"
            }

            val layoutBack = verificationView.findViewById<View>(R.id.layoutBack)
            if (layoutBack != null) {
                layoutBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                    }
                })
            }

            val btnVerify = verificationView.findViewById<Button>(R.id.btnVerify)
            if (btnVerify != null) {
                btnVerify.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val etOtp1 = verificationView.findViewById<EditText>(R.id.etOtp1)
                        val etOtp2 = verificationView.findViewById<EditText>(R.id.etOtp2)
                        val etOtp3 = verificationView.findViewById<EditText>(R.id.etOtp3)
                        val etOtp4 = verificationView.findViewById<EditText>(R.id.etOtp4)
                        val etOtp5 = verificationView.findViewById<EditText>(R.id.etOtp5)

                        val compiledInputOtp = etOtp1.text.toString().trim() +
                                etOtp2.text.toString().trim() +
                                etOtp3.text.toString().trim() +
                                etOtp4.text.toString().trim() +
                                etOtp5.text.toString().trim()

                        if (compiledInputOtp != generatedEmailOtpCode) {
                            Toast.makeText(activity, "Incorrect verification security code.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        navigateToSetPasswordUi(activity, viewFlipper)
                    }
                })
            }
        }

        private fun navigateToSetPasswordUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val setPasswordView = activity.layoutInflater.inflate(R.layout.activity_set_password, null)
            viewFlipper.addView(setPasswordView)
            viewFlipper.showNext()

            val layoutBack = setPasswordView.findViewById<View>(R.id.layoutBack)
            if (layoutBack != null) {
                layoutBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                    }
                })
            }

            val btnRegister = setPasswordView.findViewById<Button>(R.id.btnRegister)
            if (btnRegister != null) {
                btnRegister.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val etPassword = setPasswordView.findViewById<EditText>(R.id.etPassword)
                        val etConfirmPassword = setPasswordView.findViewById<EditText>(R.id.etConfirmPassword)

                        val passStr = etPassword.text.toString()
                        val confirmStr = etConfirmPassword.text.toString()

                        if (passStr.isEmpty() || confirmStr.isEmpty()) {
                            Toast.makeText(activity, "Fields cannot be blank.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        if (passStr != confirmStr) {
                            Toast.makeText(activity, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        userRegistrationProfile.password = passStr
                        commitUserToFirebaseDatabase(activity, viewFlipper)
                    }
                })
            }
        }

        private fun commitUserToFirebaseDatabase(activity: MainActivity, viewFlipper: ViewFlipper) {
            val dbReference = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("RegisteredUsers")

            val userIdNode = dbReference.push().key ?: "user_${System.currentTimeMillis()}"

            dbReference.child(userIdNode).setValue(userRegistrationProfile)
                .addOnSuccessListener(object : com.google.android.gms.tasks.OnSuccessListener<Void> {
                    override fun onSuccess(p0: Void?) {
                        Toast.makeText(activity, "Account registration complete!", Toast.LENGTH_LONG).show()
                        navigateToSignInUi(activity, viewFlipper)
                    }
                })
                .addOnFailureListener(object : com.google.android.gms.tasks.OnFailureListener {
                    override fun onFailure(exception: Exception) {
                        Toast.makeText(activity, "Database network push error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        private fun navigateToSignInUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val signInView = activity.layoutInflater.inflate(R.layout.activity_sign_in, null)
            viewFlipper.addView(signInView)
            viewFlipper.showNext()

            val layoutBack = signInView.findViewById<View>(R.id.layoutBack)
            if (layoutBack != null) {
                layoutBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                    }
                })
            }

            val tvDontHaveAccount = signInView.findViewById<TextView>(R.id.tvDontHaveAccount)
            if (tvDontHaveAccount != null) {
                tvDontHaveAccount.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToSignUpUi(activity, viewFlipper)
                    }
                })
            }

            val btnSignIn = signInView.findViewById<Button>(R.id.btnSignIn)
            if (btnSignIn != null) {
                btnSignIn.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val etEmailOrPhone = signInView.findViewById<EditText>(R.id.etEmailOrPhone)
                        val etPassword = signInView.findViewById<EditText>(R.id.etPassword)

                        val loginUserStr = etEmailOrPhone.text.toString().trim()
                        val loginPassStr = etPassword.text.toString()

                        if (loginUserStr.isEmpty() || loginPassStr.isEmpty()) {
                            Toast.makeText(activity, "Please fill in input credentials.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        authenticateUserFromDatabase(activity, viewFlipper, loginUserStr, loginPassStr)
                    }
                })
            }
        }

        private fun authenticateUserFromDatabase(activity: MainActivity, viewFlipper: ViewFlipper, userKey: String, passKey: String) {
            val dbQuery = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("RegisteredUsers")

            dbQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var accessGranted = false
                    val iterator = snapshot.children.iterator()
                    var nodeKeyStr = ""
                    var userTypeStr = "User"

                    while (iterator.hasNext()) {
                        val recordNode = iterator.next()
                        val storedEmail = recordNode.child("email").getValue(String::class.java)
                        val storedPhone = recordNode.child("phone").getValue(String::class.java)
                        val storedPassword = recordNode.child("password").getValue(String::class.java)
                        val storedLat = recordNode.child("latitude").getValue(Double::class.java) ?: 0.0
                        val storedLon = recordNode.child("longitude").getValue(Double::class.java) ?: 0.0
                        val storedType = recordNode.child("userType").getValue(String::class.java) ?: "User"

                        if ((userKey == storedEmail || userKey == storedPhone || "+94$userKey" == storedPhone) && passKey == storedPassword) {
                            accessGranted = true
                            userRegistrationProfile.latitude = storedLat
                            userRegistrationProfile.longitude = storedLon
                            nodeKeyStr = recordNode.key ?: ""
                            userTypeStr = storedType
                            SessionManager.saveSession(
                                activity,
                                userKey,
                                nodeKeyStr,
                                userTypeStr,
                                userRegistrationProfile.latitude,
                                userRegistrationProfile.longitude
                            )
                            break
                        }
                    }

                    if (accessGranted) {
                        Toast.makeText(activity, "Access Authorized!", Toast.LENGTH_SHORT).show()
                        if (userTypeStr == "Mechanic" || userTypeStr == "TowTruck") {
                            navigateToMechanicDashboardUi(activity, viewFlipper)
                        } else {
                            navigateToDashboardUi(activity, viewFlipper)
                        }
                    } else {
                        Toast.makeText(activity, "Invalid username or password mismatch.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(activity, "Database request query failed.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        fun navigateToDashboardUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val dashboardView = activity.layoutInflater.inflate(R.layout.activity_dashboard, null)
            viewFlipper.addView(dashboardView)
            viewFlipper.showNext()

            val mapWrapper = dashboardView.findViewById<FrameLayout>(R.id.mapWrapper)
            val popupCard = LinearLayout(activity)

            val density = activity.resources.displayMetrics.density

            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            layoutParams.topMargin = (160 * density).toInt()
            layoutParams.leftMargin = (24 * density).toInt()
            layoutParams.rightMargin = (24 * density).toInt()

            popupCard.layoutParams = layoutParams
            popupCard.orientation = LinearLayout.VERTICAL
            popupCard.setBackgroundResource(R.drawable.edittext_rounded)
            popupCard.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            popupCard.setPadding((24 * density).toInt(), (24 * density).toInt(), (24 * density).toInt(), (24 * density).toInt())
            popupCard.visibility = View.GONE
            popupCard.elevation = 40f
            popupCard.translationZ = 40f

            val tvMechanicNamePopup = TextView(activity)
            tvMechanicNamePopup.textSize = 20f
            tvMechanicNamePopup.setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            tvMechanicNamePopup.setTypeface(null, android.graphics.Typeface.BOLD)

            val tvMechanicTypePopup = TextView(activity)
            tvMechanicTypePopup.textSize = 15f
            tvMechanicTypePopup.setTextColor(android.graphics.Color.parseColor("#808080"))
            tvMechanicTypePopup.setPadding(0, (4 * density).toInt(), 0, (20 * density).toInt())

            val btnViewProfile = Button(activity)
            btnViewProfile.text = "View Professional"
            btnViewProfile.setBackgroundResource(R.drawable.btn_signup_rounded)
            btnViewProfile.setTextColor(android.graphics.Color.WHITE)
            btnViewProfile.textSize = 16f
            btnViewProfile.isAllCaps = false

            val btnParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (56 * density).toInt())
            btnViewProfile.layoutParams = btnParams

            popupCard.addView(tvMechanicNamePopup)
            popupCard.addView(tvMechanicTypePopup)
            popupCard.addView(btnViewProfile)

            mapWrapper?.addView(popupCard)

            val mapView = dashboardView.findViewById<MapView>(R.id.mapView)
            if (mapView != null) {
                mapView.setMultiTouchControls(true)
                mapView.setTileSource(TileSourceFactory.MAPNIK)

                val mapTouchReceiver = object : org.osmdroid.views.overlay.Overlay() {
                    override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
                        val mainHandler = Handler(Looper.getMainLooper())
                        mainHandler.post(object : Runnable {
                            override fun run() {
                                popupCard.visibility = View.GONE
                            }
                        })
                        return false
                    }
                }
                mapView.overlays.add(mapTouchReceiver)

                var targetLat = userRegistrationProfile.latitude
                var targetLon = userRegistrationProfile.longitude

                if (targetLat == 0.0 && targetLon == 0.0) {
                    targetLat = 7.2906
                    targetLon = 80.6337
                }

                val userPositionPoint = GeoPoint(targetLat, targetLon)
                mapView.controller.setZoom(17.2)
                mapView.controller.setCenter(userPositionPoint)

                val radarOverlay = RadarMapOverlay(userPositionPoint)
                mapView.overlays.add(radarOverlay)
                currentRadarOverlay = radarOverlay

                fetchMechanicsFromDatabase(activity, mapView, "", tvMechanicNamePopup, tvMechanicTypePopup, popupCard, btnViewProfile, viewFlipper)

                // Start following the user's real-time GPS position instead of only using
                // the lat/lon that was captured once at onboarding/login time.
                startLiveLocationUpdates(activity, mapView)
            }

            val etSearchMechanic = dashboardView.findViewById<EditText>(R.id.etSearchMechanic)
            val btnSearchMechanic = dashboardView.findViewById<TextView>(R.id.btnSearchMechanic)
            val spinnerServiceTypeFilter = dashboardView.findViewById<Spinner>(R.id.spinnerServiceTypeFilter)
            val spinnerSpecializationFilter = dashboardView.findViewById<Spinner>(R.id.spinnerSpecializationFilter)

            fun currentTypeFilter(): String = spinnerServiceTypeFilter?.selectedItem?.toString() ?: "All providers"
            fun currentSpecializationFilter(): String = spinnerSpecializationFilter?.selectedItem?.toString() ?: "All specializations"

            if (btnSearchMechanic != null && etSearchMechanic != null && mapView != null) {
                btnSearchMechanic.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val searchManager = MapSearchManager()
                        searchManager.searchQuery = etSearchMechanic.text.toString()
                        val parsedQuery = searchManager.validateAndParse()

                        fetchMechanicsFromDatabase(activity, mapView, parsedQuery, tvMechanicNamePopup, tvMechanicTypePopup, popupCard, btnViewProfile, viewFlipper, currentTypeFilter(), currentSpecializationFilter())
                    }
                })

                etSearchMechanic.addTextChangedListener(object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        val searchManager = MapSearchManager()
                        searchManager.searchQuery = s?.toString() ?: ""
                        val parsedQuery = searchManager.validateAndParse()

                        fetchMechanicsFromDatabase(activity, mapView, parsedQuery, tvMechanicNamePopup, tvMechanicTypePopup, popupCard, btnViewProfile, viewFlipper, currentTypeFilter(), currentSpecializationFilter())
                    }
                    override fun afterTextChanged(s: android.text.Editable?) {}
                })
            }

            if (mapView != null) {
                val filterChangeListener = object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val currentQuery = etSearchMechanic?.text?.toString() ?: ""
                        fetchMechanicsFromDatabase(activity, mapView, currentQuery, tvMechanicNamePopup, tvMechanicTypePopup, popupCard, btnViewProfile, viewFlipper, currentTypeFilter(), currentSpecializationFilter())
                    }
                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
                spinnerServiceTypeFilter?.onItemSelectedListener = filterChangeListener
                spinnerSpecializationFilter?.onItemSelectedListener = filterChangeListener
            }

            val bottomNav = dashboardView.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            if (bottomNav != null) {
                bottomNav.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
                    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
                        if (item.itemId == R.id.nav_bot) {
                            navigateToChatUi(activity, viewFlipper)
                            return false
                        } else if (item.itemId == R.id.nav_profile) {
                            navigateToProfileUi(activity, viewFlipper)
                            return false
                        }
                        return true
                    }
                })
            }

            val btnLogout = dashboardView.findViewById<ImageButton>(R.id.btnLogout)
            if (btnLogout != null) {
                btnLogout.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        stopLiveLocationUpdates()
                        SessionManager.clearSession(activity)
                        viewFlipper.removeAllViews()
                        navigateToSignInUi(activity, viewFlipper)
                    }
                })
            }

            val btnnotfy = dashboardView.findViewById<ImageButton>(R.id.btnnotfy)
            if (btnnotfy != null) {
                btnnotfy.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToNotificationsUi(activity, viewFlipper)
                    }
                })
            }
        }

        // Requests continuous GPS updates while the dashboard is open. Each fix recenters
        // the radar overlay on the map and pushes the fresh coordinates to Firebase so the
        // stored/session location always reflects where the user actually is right now,
        // instead of the one-time location captured during onboarding/login.
        private fun startLiveLocationUpdates(activity: MainActivity, mapView: MapView) {
            if (fusedLocationClient == null) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
            }

            val fineLocationCheck = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarseLocationCheck = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)

            if (fineLocationCheck != PackageManager.PERMISSION_GRANTED && coarseLocationCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1002
                )
                return
            }

            stopLiveLocationUpdates()

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val freshLocation = locationResult.lastLocation ?: return

                    userRegistrationProfile.latitude = freshLocation.latitude
                    userRegistrationProfile.longitude = freshLocation.longitude

                    val freshPoint = GeoPoint(freshLocation.latitude, freshLocation.longitude)

                    // Swap out the old radar overlay for a fresh one centered on the new fix
                    val oldOverlay = currentRadarOverlay
                    if (oldOverlay != null) {
                        mapView.overlays.remove(oldOverlay)
                    }
                    val newOverlay = RadarMapOverlay(freshPoint)
                    mapView.overlays.add(newOverlay)
                    currentRadarOverlay = newOverlay

                    mapView.controller.animateTo(freshPoint)
                    mapView.invalidate()

                    val activeNodeKey = SessionManager.getNodeKey(activity)
                    if (activeNodeKey.isNotEmpty()) {
                        val locationUpdates = HashMap<String, Any>()
                        locationUpdates["latitude"] = freshLocation.latitude
                        locationUpdates["longitude"] = freshLocation.longitude

                        FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("RegisteredUsers")
                            .child(activeNodeKey)
                            .updateChildren(locationUpdates)

                        SessionManager.saveSession(
                            activity,
                            SessionManager.getUserKey(activity),
                            activeNodeKey,
                            SessionManager.getUserType(activity),
                            freshLocation.latitude,
                            freshLocation.longitude
                        )
                    }
                }
            }

            liveLocationCallback = callback

            try {
                fusedLocationClient?.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
            } catch (unauthorizedSecurityException: SecurityException) {
                unauthorizedSecurityException.printStackTrace()
            }
        }

        private fun stopLiveLocationUpdates() {
            val callback = liveLocationCallback
            if (callback != null) {
                fusedLocationClient?.removeLocationUpdates(callback)
                liveLocationCallback = null
            }
        }

        private fun fetchMechanicsFromDatabase(
            activity: MainActivity,
            mapView: MapView,
            searchQuery: String,
            popupTitle: TextView,
            popupType: TextView,
            popupCard: LinearLayout,
            btnViewProfile: Button,
            viewFlipper: ViewFlipper,
            typeFilter: String = "All providers",
            specializationFilter: String = "All specializations"
        ) {
            val originLat = userRegistrationProfile.latitude
            val originLon = userRegistrationProfile.longitude
            val dbQuery = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("RegisteredUsers")

            dbQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val markersToRemove = mapView.overlays.filterIsInstance<org.osmdroid.views.overlay.Marker>()
                    mapView.overlays.removeAll(markersToRemove)

                    var matchedPosition: GeoPoint? = null
                    var matchCount = 0

                    val iterator = snapshot.children.iterator()
                    while (iterator.hasNext()) {
                        val recordNode = iterator.next()
                        val type = recordNode.child("userType").getValue(String::class.java)
                        val recordIsVerified = recordNode.child("isVerified").getValue(Boolean::class.java) ?: false

                        if ((type == "Mechanic" || type == "TowTruck") && recordIsVerified) {
                            val mechName = recordNode.child("name").getValue(String::class.java) ?: ""
                            val mechLat = recordNode.child("latitude").getValue(Double::class.java) ?: 0.0
                            val mechLon = recordNode.child("longitude").getValue(Double::class.java) ?: 0.0
                            val mechSpecialization = recordNode.child("specialization").getValue(String::class.java) ?: ""
                            val distanceKm = distanceInKm(originLat, originLon, mechLat, mechLon)

                            var matchesSearch = true
                            if (searchQuery.isNotEmpty()) {
                                if (!mechName.lowercase().contains(searchQuery.lowercase())) {
                                    matchesSearch = false
                                }
                            }

                            var matchesTypeFilter = true
                            if (typeFilter == "Mechanics only" && type != "Mechanic") matchesTypeFilter = false
                            if (typeFilter == "Tow trucks only" && type != "TowTruck") matchesTypeFilter = false

                            var matchesSpecializationFilter = true
                            if (specializationFilter != "All specializations") {
                                // Specialization filtering only applies to mechanics; tow trucks
                                // are excluded automatically once a specific specialization is chosen.
                                matchesSpecializationFilter = (type == "Mechanic" && mechSpecialization.equals(specializationFilter, ignoreCase = true))
                            }

                            if (matchesSearch && matchesTypeFilter && matchesSpecializationFilter) {
                                matchCount++
                                val currentGeoPoint = GeoPoint(mechLat, mechLon)
                                if (matchedPosition == null) {
                                    matchedPosition = currentGeoPoint
                                }

                                val marker = org.osmdroid.views.overlay.Marker(mapView)
                                marker.position = currentGeoPoint
                                marker.title = mechName
                                marker.snippet = type

                                val defaultIcon = ContextCompat.getDrawable(activity, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                                if (type == "Mechanic") {
                                    defaultIcon?.setTint(android.graphics.Color.parseColor("#00FF00"))
                                } else if (type == "TowTruck") {
                                    defaultIcon?.setTint(android.graphics.Color.parseColor("#FF0000"))
                                }
                                marker.icon = defaultIcon

                                marker.setAnchor(
                                    org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                                    org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                                )

                                marker.setOnMarkerClickListener(object : org.osmdroid.views.overlay.Marker.OnMarkerClickListener {
                                    override fun onMarkerClick(m: org.osmdroid.views.overlay.Marker?, v: MapView?): Boolean {
                                        val mainHandler = Handler(Looper.getMainLooper())
                                        mainHandler.post(object : Runnable {
                                            override fun run() {
                                                popupTitle.text = mechName
                                                val roundedDistance = String.format("%.1f", distanceKm)
                                                popupType.text = if (type == "TowTruck") {
                                                    "Tow Truck Operator • ${roundedDistance} km away"
                                                } else if (mechSpecialization.isNotEmpty()) {
                                                    "$mechSpecialization • ${roundedDistance} km away"
                                                } else {
                                                    "Professional Mechanic • ${roundedDistance} km away"
                                                }
                                                popupCard.visibility = View.VISIBLE
                                                popupCard.bringToFront()

                                                btnViewProfile.setOnClickListener(object : View.OnClickListener {
                                                    override fun onClick(v: View?) {
                                                        popupCard.visibility = View.GONE
                                                        navigateToMechanicProfileUi(activity, viewFlipper, recordNode.key ?: "", type, mechName)
                                                    }
                                                })
                                            }
                                        })
                                        v?.controller?.animateTo(m?.position)
                                        return true
                                    }
                                })
                                mapView.overlays.add(marker)
                            }
                        }
                    }

                    mapView.invalidate()

                    if (searchQuery.isNotEmpty()) {
                        if (matchCount > 0 && matchedPosition != null) {
                            mapView.controller.animateTo(matchedPosition)
                            mapView.controller.setZoom(17.5)
                        } else {
                            Toast.makeText(activity, "No professional found with name: $searchQuery", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(activity, "Database request query failed.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        fun navigateToMechanicDashboardUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val mechanicDashboardView = activity.layoutInflater.inflate(R.layout.activity_mechanic_dashboard, null)
            viewFlipper.addView(mechanicDashboardView)
            viewFlipper.showNext()

            val btnLogout2 = mechanicDashboardView.findViewById<ImageButton>(R.id.btnLogout2)
            if (btnLogout2 != null) {
                btnLogout2.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        stopLiveLocationUpdates()
                        SessionManager.clearSession(activity)
                        viewFlipper.removeAllViews()
                        navigateToSignInUi(activity, viewFlipper)
                    }
                })
            }



            val bottomNav = mechanicDashboardView.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            if (bottomNav != null) {
                bottomNav.setOnItemSelectedListener(object : NavigationBarView.OnItemSelectedListener {
                    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
                        if (item.itemId == R.id.nav_chats) {
                            navigateToChatUi(activity, viewFlipper)
                            return false
                        } else if (item.itemId == R.id.nav_profile) {
                            navigateToProfileUi(activity, viewFlipper)
                            return false
                        }
                        return true
                    }
                })
            }

            // "See All" and the "Jobs" quick action both open the real, Firebase-backed
            // service request list — the static demo card above them is left as visual
            // filler only, it no longer represents real data.
            val tvSeeAll = mechanicDashboardView.findViewById<View>(R.id.tvSeeAll)
            if (tvSeeAll != null) {
                tvSeeAll.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToMechanicBookingsUi(activity, viewFlipper)
                    }
                })
            }

            val actionJobs = mechanicDashboardView.findViewById<View>(R.id.actionJobs)
            if (actionJobs != null) {
                actionJobs.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToMechanicBookingsUi(activity, viewFlipper)
                    }
                })
            }
        }

        /**
         * Real booking inbox for mechanics/tow trucks: pulls every booking (from both
         * MechanicBookings and TowTruckBookings) addressed to the signed-in provider,
         * and lets them Accept / Decline pending requests or mark an accepted job
         * Complete. Every status change is written back to Firebase and a local
         * notification entry is queued for the customer's side of the app.
         */
        private fun buildSmallActionButton(activity: MainActivity, label: String, backgroundRes: Int, textColorHex: String): androidx.appcompat.widget.AppCompatButton {
            val button = androidx.appcompat.widget.AppCompatButton(activity)
            button.text = label
            button.setBackgroundResource(backgroundRes)
            button.setTextColor(android.graphics.Color.parseColor(textColorHex))
            button.textSize = 13f
            button.isAllCaps = false
            return button
        }

        private fun navigateToMechanicBookingsUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val bookingsView = activity.layoutInflater.inflate(R.layout.activity_mechanic_bookings, null)
            viewFlipper.addView(bookingsView)
            viewFlipper.showNext()

            val btnBack = bookingsView.findViewById<View>(R.id.btnBack)
            if (btnBack != null) {
                btnBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(bookingsView)
                    }
                })
            }

            val container = bookingsView.findViewById<LinearLayout>(R.id.containerBookings)
            val tvEmpty = bookingsView.findViewById<TextView>(R.id.tvEmptyBookings)
            val myProviderId = SessionManager.getUserKey(activity)
            val database = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")

            loadMechanicBookings(activity, viewFlipper, container, tvEmpty, myProviderId, database)
        }

        private fun loadMechanicBookings(
            activity: MainActivity,
            viewFlipper: ViewFlipper,
            container: LinearLayout?,
            tvEmpty: TextView?,
            myProviderId: String,
            database: FirebaseDatabase
        ) {
            val tableNames = listOf("MechanicBookings", "TowTruckBookings")
            var tablesLoaded = 0
            val allRows = ArrayList<Triple<String, String, BookingDto>>() // (table, key, booking)

            for (tableName in tableNames) {
                database.getReference(tableName).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (bookingNode in snapshot.children) {
                            val mechanicId = bookingNode.child("mechanicId").getValue(String::class.java) ?: ""
                            if (mechanicId != myProviderId) continue
                            val dto = BookingDto()
                            dto.mechanicId = mechanicId
                            dto.customerId = bookingNode.child("customerId").getValue(String::class.java) ?: ""
                            dto.vehicleMake = bookingNode.child("vehicleMake").getValue(String::class.java) ?: ""
                            dto.bookingDate = bookingNode.child("bookingDate").getValue(String::class.java) ?: ""
                            dto.bookingTime = bookingNode.child("bookingTime").getValue(String::class.java) ?: ""
                            dto.issueDescription = bookingNode.child("issueDescription").getValue(String::class.java) ?: ""
                            dto.locationAddress = bookingNode.child("locationAddress").getValue(String::class.java) ?: ""
                            dto.pickupLocation = bookingNode.child("pickupLocation").getValue(String::class.java) ?: ""
                            dto.dropoffLocation = bookingNode.child("dropoffLocation").getValue(String::class.java) ?: ""
                            dto.vehicleCondition = bookingNode.child("vehicleCondition").getValue(String::class.java) ?: ""
                            dto.bookingType = bookingNode.child("bookingType").getValue(String::class.java) ?: "Mechanic"
                            dto.status = bookingNode.child("status").getValue(String::class.java) ?: "pending"
                            allRows.add(Triple(tableName, bookingNode.key ?: "", dto))
                        }

                        tablesLoaded++
                        if (tablesLoaded == tableNames.size) {
                            renderMechanicBookingRows(activity, viewFlipper, container, tvEmpty, allRows, myProviderId, database)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        tablesLoaded++
                    }
                })
            }
        }

        private fun renderMechanicBookingRows(
            activity: MainActivity,
            viewFlipper: ViewFlipper,
            container: LinearLayout?,
            tvEmpty: TextView?,
            rows: List<Triple<String, String, BookingDto>>,
            myProviderId: String,
            database: FirebaseDatabase
        ) {
            if (container == null) return
            container.removeAllViews()

            if (rows.isEmpty()) {
                tvEmpty?.visibility = View.VISIBLE
                return
            }
            tvEmpty?.visibility = View.GONE

            // Newest requests first isn't derivable from the push key alone here, so we
            // simply show pending requests before everything else, which is what a
            // mechanic actually needs to act on.
            val sortedRows = rows.sortedBy { if (it.third.status == "pending") 0 else 1 }
            val density = activity.resources.displayMetrics.density

            for ((tableName, bookingKey, dto) in sortedRows) {
                val row = LinearLayout(activity)
                row.orientation = LinearLayout.VERTICAL
                row.setBackgroundResource(R.drawable.dashboard_card)
                val padding = (14 * density).toInt()
                row.setPadding(padding, padding, padding, padding)
                val rowParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                rowParams.topMargin = (10 * density).toInt()
                row.layoutParams = rowParams

                val title = TextView(activity)
                title.text = if (dto.bookingType == "TowTruck") "Tow Request \u2022 ${dto.vehicleMake}" else "Mechanic Booking \u2022 ${dto.vehicleMake}"
                title.setTextColor(android.graphics.Color.parseColor("#222222"))
                title.textSize = 15f
                title.setTypeface(null, android.graphics.Typeface.BOLD)
                row.addView(title)

                val details = TextView(activity)
                details.text = if (dto.bookingType == "TowTruck") {
                    "Pickup: ${dto.pickupLocation}\nDrop-off: ${dto.dropoffLocation}\nCondition: ${dto.vehicleCondition}"
                } else {
                    "When: ${dto.bookingDate} ${dto.bookingTime}\nAt: ${dto.locationAddress}"
                }
                details.setTextColor(android.graphics.Color.parseColor("#777777"))
                details.textSize = 12f
                val detailsParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                detailsParams.topMargin = (6 * density).toInt()
                details.layoutParams = detailsParams
                row.addView(details)

                if (dto.issueDescription.isNotEmpty()) {
                    val issue = TextView(activity)
                    issue.text = "Issue: ${dto.issueDescription}"
                    issue.setTextColor(android.graphics.Color.parseColor("#999999"))
                    issue.textSize = 11f
                    val issueParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    issueParams.topMargin = (4 * density).toInt()
                    issue.layoutParams = issueParams
                    row.addView(issue)
                }

                val statusView = TextView(activity)
                statusView.text = "Status: ${dto.status.replaceFirstChar { it.uppercase() }}"
                statusView.setTextColor(android.graphics.Color.parseColor("#D59F00"))
                statusView.textSize = 11f
                statusView.setTypeface(null, android.graphics.Typeface.BOLD)
                val statusParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                statusParams.topMargin = (8 * density).toInt()
                statusView.layoutParams = statusParams
                row.addView(statusView)

                val actionsRow = LinearLayout(activity)
                actionsRow.orientation = LinearLayout.HORIZONTAL
                val actionsParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (44 * density).toInt())
                actionsParams.topMargin = (12 * density).toInt()
                actionsRow.layoutParams = actionsParams

                fun updateStatus(newStatus: String, notificationTitle: String, notificationMessage: String) {
                    database.getReference(tableName).child(bookingKey).child("status").setValue(newStatus)
                        .addOnSuccessListener(object : com.google.android.gms.tasks.OnSuccessListener<Void> {
                            override fun onSuccess(p0: Void?) {
                                val newNotificationItem = NotificationDto()
                                newNotificationItem.title = notificationTitle
                                newNotificationItem.message = notificationMessage
                                newNotificationItem.time = "Just now"
                                appNotifications.add(0, newNotificationItem)
                                Toast.makeText(activity, "Booking marked $newStatus.", Toast.LENGTH_SHORT).show()
                                loadMechanicBookings(activity, viewFlipper, container, tvEmpty, myProviderId, database)
                            }
                        })
                        .addOnFailureListener(object : com.google.android.gms.tasks.OnFailureListener {
                            override fun onFailure(exception: Exception) {
                                Toast.makeText(activity, "Could not update booking.", Toast.LENGTH_SHORT).show()
                            }
                        })
                }

                when (dto.status) {
                    "pending" -> {
                        val btnDecline = buildSmallActionButton(activity, "Decline", R.drawable.btn_decline, "#666666")
                        val declineParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                        declineParams.marginEnd = (5 * density).toInt()
                        btnDecline.layoutParams = declineParams
                        btnDecline.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                updateStatus("declined", "Booking Declined", "Your service request was declined by the provider.")
                            }
                        })
                        actionsRow.addView(btnDecline)

                        val btnAccept = buildSmallActionButton(activity, "Accept", R.drawable.btn_accept, "#FFFFFF")
                        val acceptParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                        acceptParams.marginStart = (5 * density).toInt()
                        btnAccept.layoutParams = acceptParams
                        btnAccept.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                updateStatus("accepted", "Booking Accepted", "Your service request was accepted by the provider.")
                            }
                        })
                        actionsRow.addView(btnAccept)
                        row.addView(actionsRow)
                    }
                    "accepted" -> {
                        val btnComplete = buildSmallActionButton(activity, "Mark Completed", R.drawable.btn_accept, "#FFFFFF")
                        btnComplete.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                        btnComplete.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                updateStatus("completed", "Service Completed", "Your service request has been marked completed. Please leave a review!")
                            }
                        })
                        actionsRow.addView(btnComplete)
                        row.addView(actionsRow)
                    }
                    else -> {
                        // completed / declined / cancelled: no further action needed here.
                    }
                }

                container.addView(row)
            }
        }

        /**
         * Haversine great-circle distance in kilometers between two lat/lon points.
         * Used to rank candidate mechanics/tow trucks by real proximity instead of
         * just taking whichever record happens to come first from Firebase.
         */
        private fun distanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadiusKm = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return earthRadiusKm * c
        }

        /**
         * Real specialization-aware matching: looks for Mechanic records whose
         * `specialization` field equals the AI's diagnosis tag, and among those
         * picks the nearest one to the user's current location. Falls back to
         * "any mechanic" only if nobody with the exact specialization is registered,
         * so the user is never left with zero options.
         */
        private fun findSuggestedMechanicFromDatabase(activity: MainActivity, viewFlipper: ViewFlipper, specialization: String) {
            val dbQuery = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("RegisteredUsers")

            val originLat = userRegistrationProfile.latitude
            val originLon = userRegistrationProfile.longitude

            dbQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var bestExactId = ""
                    var bestExactName = ""
                    var bestExactDistance = Double.MAX_VALUE

                    var bestFallbackId = ""
                    var bestFallbackName = ""
                    var bestFallbackDistance = Double.MAX_VALUE

                    val iterator = snapshot.children.iterator()
                    while (iterator.hasNext()) {
                        val recordNode = iterator.next()
                        val type = recordNode.child("userType").getValue(String::class.java)
                        if (type != "Mechanic") continue

                        val isVerified = recordNode.child("isVerified").getValue(Boolean::class.java) ?: false
                        if (!isVerified) continue

                        val candidateName = recordNode.child("name").getValue(String::class.java) ?: "Professional Mechanic"
                        val candidateSpecialization = recordNode.child("specialization").getValue(String::class.java) ?: ""
                        val candidateLat = recordNode.child("latitude").getValue(Double::class.java) ?: 0.0
                        val candidateLon = recordNode.child("longitude").getValue(Double::class.java) ?: 0.0
                        val distance = distanceInKm(originLat, originLon, candidateLat, candidateLon)

                        if (distance < bestFallbackDistance) {
                            bestFallbackDistance = distance
                            bestFallbackId = recordNode.key ?: ""
                            bestFallbackName = candidateName
                        }

                        if (candidateSpecialization.equals(specialization, ignoreCase = true) && distance < bestExactDistance) {
                            bestExactDistance = distance
                            bestExactId = recordNode.key ?: ""
                            bestExactName = candidateName
                        }
                    }

                    when {
                        bestExactId.isNotEmpty() -> {
                            navigateToMechanicProfileUi(activity, viewFlipper, bestExactId, specialization, bestExactName)
                        }
                        bestFallbackId.isNotEmpty() -> {
                            Toast.makeText(
                                activity,
                                "No $specialization registered nearby — showing the closest available mechanic instead.",
                                Toast.LENGTH_LONG
                            ).show()
                            navigateToMechanicProfileUi(activity, viewFlipper, bestFallbackId, specialization, bestFallbackName)
                        }
                        else -> {
                            Toast.makeText(activity, "No mechanics currently available.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun navigateToMechanicProfileUi(activity: MainActivity, viewFlipper: ViewFlipper, targetNodeId: String, targetType: String, targetName: String) {
            val mechanicProfileView = activity.layoutInflater.inflate(R.layout.activity_mechanic_profile, null)
            viewFlipper.addView(mechanicProfileView)
            viewFlipper.showNext()

            val btnBack = mechanicProfileView.findViewById<View>(R.id.btnBack)
            if (btnBack != null) {
                btnBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(mechanicProfileView)
                    }
                })
            }

            val tvMechanicName = mechanicProfileView.findViewById<TextView>(R.id.tvMechanicName)
            if (tvMechanicName != null) {
                tvMechanicName.text = targetName
            }

            val tvMechanicRating = mechanicProfileView.findViewById<TextView>(R.id.tvMechanicRating)
            if (tvMechanicRating != null) {
                val ratingsRef = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("Ratings").child(targetNodeId)
                ratingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var total = 0
                        var count = 0
                        for (ratingNode in snapshot.children) {
                            val stars = ratingNode.child("stars").getValue(Int::class.java) ?: continue
                            total += stars
                            count++
                        }
                        tvMechanicRating.text = if (count > 0) {
                            String.format("%.1f (%d review%s)", total.toDouble() / count, count, if (count == 1) "" else "s")
                        } else {
                            "No reviews yet"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            val btnProceedToBook = mechanicProfileView.findViewById<View>(R.id.btnProceedToBook)
            if (btnProceedToBook != null) {
                btnProceedToBook.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToBookMechanicUi(activity, viewFlipper, targetNodeId, targetType, targetName)
                    }
                })
            }
        }

        private fun navigateToBookMechanicUi(activity: MainActivity, viewFlipper: ViewFlipper, targetNodeId: String, targetType: String, targetName: String) {
            if (targetType == "TowTruck") {
                navigateToBookTowUi(activity, viewFlipper, targetNodeId, targetName)
                return
            }

            val bookMechanicView = activity.layoutInflater.inflate(R.layout.activity_book_mechanic, null)
            viewFlipper.addView(bookMechanicView)
            viewFlipper.showNext()

            val btnBack = bookMechanicView.findViewById<View>(R.id.btnBack)
            if (btnBack != null) {
                btnBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(bookMechanicView)
                    }
                })
            }

            val tvBookingMechanicName = bookMechanicView.findViewById<TextView>(R.id.tvBookingMechanicName)
            if (tvBookingMechanicName != null) {
                tvBookingMechanicName.text = targetName
            }

            val etVehicleMake = bookMechanicView.findViewById<EditText>(R.id.etVehicleMake)
            val etBookingDate = bookMechanicView.findViewById<EditText>(R.id.etBookingDate)
            val etBookingTime = bookMechanicView.findViewById<EditText>(R.id.etBookingTime)
            val etIssueDescription = bookMechanicView.findViewById<EditText>(R.id.etIssueDescription)
            val etLocationAddress = bookMechanicView.findViewById<EditText>(R.id.etLocationAddress)
            val btnConfirmBooking = bookMechanicView.findViewById<View>(R.id.btnConfirmBooking)

            if (btnConfirmBooking != null) {
                btnConfirmBooking.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val inputManager = BookingManager()
                        if (etVehicleMake != null) inputManager.vehicleMake = etVehicleMake.text.toString()
                        if (etBookingDate != null) inputManager.bookingDate = etBookingDate.text.toString()
                        if (etBookingTime != null) inputManager.bookingTime = etBookingTime.text.toString()
                        if (etIssueDescription != null) inputManager.issueDescription = etIssueDescription.text.toString()
                        if (etLocationAddress != null) inputManager.locationAddress = etLocationAddress.text.toString()

                        if (inputManager.validateAndParse()) {
                            val bookingDto = BookingDto()
                            bookingDto.mechanicId = targetNodeId
                            bookingDto.customerId = SessionManager.getUserKey(activity)
                            bookingDto.vehicleMake = inputManager.vehicleMake
                            bookingDto.bookingDate = inputManager.bookingDate
                            bookingDto.bookingTime = inputManager.bookingTime
                            bookingDto.issueDescription = inputManager.issueDescription
                            bookingDto.locationAddress = inputManager.locationAddress
                            bookingDto.bookingType = "Mechanic"
                            bookingDto.status = "pending"

                            val tableName = if (targetType == "TowTruck") "TowTruckBookings" else "MechanicBookings"
                            val dbReference = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .getReference(tableName)

                            val generatedBookingKey = dbReference.push().key ?: ""
                            dbReference.child(generatedBookingKey).setValue(bookingDto)
                                .addOnSuccessListener(object : com.google.android.gms.tasks.OnSuccessListener<Void> {
                                    override fun onSuccess(p0: Void?) {
                                        navigateToPaymentUi(activity, viewFlipper, generatedBookingKey, targetName)
                                    }
                                })
                                .addOnFailureListener(object : com.google.android.gms.tasks.OnFailureListener {
                                    override fun onFailure(exception: Exception) {
                                        Toast.makeText(activity, "Booking failed.", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        } else {
                            Toast.makeText(activity, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }

        /**
         * Tow trucks need a different form than mechanics: a pickup point, a
         * drop-off/destination garage, and whether the vehicle can still be driven,
         * instead of an appointment date/time. Writes to the same TowTruckBookings
         * table as before, just with the tow-specific fields populated.
         */
        private fun navigateToBookTowUi(activity: MainActivity, viewFlipper: ViewFlipper, targetNodeId: String, targetName: String) {
            val bookTowView = activity.layoutInflater.inflate(R.layout.activity_book_tow, null)
            viewFlipper.addView(bookTowView)
            viewFlipper.showNext()

            val btnBack = bookTowView.findViewById<View>(R.id.btnBack)
            if (btnBack != null) {
                btnBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(bookTowView)
                    }
                })
            }

            val tvBookingMechanicName = bookTowView.findViewById<TextView>(R.id.tvBookingMechanicName)
            if (tvBookingMechanicName != null) {
                tvBookingMechanicName.text = targetName
            }

            val etVehicleMake = bookTowView.findViewById<EditText>(R.id.etVehicleMake)
            val etPickupLocation = bookTowView.findViewById<EditText>(R.id.etPickupLocation)
            val etDropoffLocation = bookTowView.findViewById<EditText>(R.id.etDropoffLocation)
            val rgVehicleCondition = bookTowView.findViewById<RadioGroup>(R.id.rgVehicleCondition)
            val rbNotDrivable = bookTowView.findViewById<RadioButton>(R.id.rbNotDrivable)
            val etIssueDescription = bookTowView.findViewById<EditText>(R.id.etIssueDescription)
            val btnConfirmBooking = bookTowView.findViewById<View>(R.id.btnConfirmBooking)

            if (btnConfirmBooking != null) {
                btnConfirmBooking.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val vehicleMake = etVehicleMake?.text?.toString()?.trim() ?: ""
                        val pickup = etPickupLocation?.text?.toString()?.trim() ?: ""
                        val dropoff = etDropoffLocation?.text?.toString()?.trim() ?: ""
                        val issue = etIssueDescription?.text?.toString()?.trim() ?: ""
                        val isNotDrivable = rbNotDrivable != null && rgVehicleCondition?.checkedRadioButtonId == rbNotDrivable.id
                        val condition = if (isNotDrivable) "Not drivable" else "Drivable"

                        if (vehicleMake.isEmpty() || pickup.isEmpty() || dropoff.isEmpty()) {
                            Toast.makeText(activity, "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
                            return
                        }

                        val bookingDto = BookingDto()
                        bookingDto.mechanicId = targetNodeId
                        bookingDto.customerId = SessionManager.getUserKey(activity)
                        bookingDto.vehicleMake = vehicleMake
                        bookingDto.issueDescription = issue
                        bookingDto.locationAddress = pickup
                        bookingDto.pickupLocation = pickup
                        bookingDto.dropoffLocation = dropoff
                        bookingDto.vehicleCondition = condition
                        bookingDto.bookingType = "TowTruck"
                        bookingDto.status = "pending"

                        val dbReference = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("TowTruckBookings")

                        val generatedBookingKey = dbReference.push().key ?: ""
                        dbReference.child(generatedBookingKey).setValue(bookingDto)
                            .addOnSuccessListener(object : com.google.android.gms.tasks.OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    navigateToPaymentUi(activity, viewFlipper, generatedBookingKey, targetName)
                                }
                            })
                            .addOnFailureListener(object : com.google.android.gms.tasks.OnFailureListener {
                                override fun onFailure(exception: Exception) {
                                    Toast.makeText(activity, "Tow request failed.", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                })
            }
        }

        private fun navigateToPaymentUi(activity: MainActivity, viewFlipper: ViewFlipper, bookingId: String, targetName: String) {
            val paymentView = activity.layoutInflater.inflate(R.layout.activity_payment, null)
            viewFlipper.addView(paymentView)
            viewFlipper.showNext()

            val btnBack = paymentView.findViewById<View>(R.id.btnBack)
            if (btnBack != null) {
                btnBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(paymentView)
                    }
                })
            }

            val tvPaymentMechanicName = paymentView.findViewById<TextView>(R.id.tvPaymentMechanicName)
            if (tvPaymentMechanicName != null) {
                tvPaymentMechanicName.text = targetName
            }

            val rgPaymentMethods = paymentView.findViewById<RadioGroup>(R.id.rgPaymentMethods)
            val rbCash = paymentView.findViewById<RadioButton>(R.id.rbCash)
            val etCardName = paymentView.findViewById<EditText>(R.id.etCardName)
            val etCardNumber = paymentView.findViewById<EditText>(R.id.etCardNumber)
            val etCardExpiry = paymentView.findViewById<EditText>(R.id.etCardExpiry)
            val etCardCvv = paymentView.findViewById<EditText>(R.id.etCardCvv)
            val btnPayNow = paymentView.findViewById<View>(R.id.btnPayNow)

            if (btnPayNow != null) {
                btnPayNow.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val paymentManager = PaymentManager()
                        if (rbCash != null) paymentManager.isCashSelected = rbCash.isChecked
                        if (etCardName != null) paymentManager.cardName = etCardName.text.toString()
                        if (etCardNumber != null) paymentManager.cardNumber = etCardNumber.text.toString()
                        if (etCardExpiry != null) paymentManager.cardExpiry = etCardExpiry.text.toString()
                        if (etCardCvv != null) paymentManager.cardCvv = etCardCvv.text.toString()

                        if (paymentManager.validateAndParse()) {
                            val paymentDto = PaymentDto()
                            paymentDto.bookingId = bookingId
                            paymentDto.paymentMethod = if (paymentManager.isCashSelected) "Cash" else "Card"
                            paymentDto.cardName = paymentManager.cardName
                            paymentDto.cardLastFourDigits = if (paymentManager.isCashSelected) "" else paymentManager.lastFourDigits()
                            paymentDto.cardExpiry = if (paymentManager.isCashSelected) "" else paymentManager.cardExpiry
                            // cardNumber and cardCvv are intentionally never written to Firebase.
                            // In production this screen should hand off to a PCI-DSS-compliant
                            // gateway (e.g. PayHere/Stripe hosted checkout) instead of collecting
                            // raw card data in-app at all.

                            val dbReference = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .getReference("Payments")

                            val generatedPaymentKey = dbReference.push().key ?: ""
                            dbReference.child(generatedPaymentKey).setValue(paymentDto)
                                .addOnSuccessListener(object : com.google.android.gms.tasks.OnSuccessListener<Void> {
                                    override fun onSuccess(p0: Void?) {
                                        Toast.makeText(activity, "Booking Confirmed Successfully!", Toast.LENGTH_LONG).show()

                                        val newNotificationItem = NotificationDto()
                                        newNotificationItem.title = "Booking Confirmed"
                                        newNotificationItem.message = "Your service request with $targetName has been confirmed."
                                        newNotificationItem.time = "Just now"
                                        appNotifications.add(0, newNotificationItem)

                                        val count = viewFlipper.childCount
                                        for (i in 0 until count - 1) {
                                            viewFlipper.showPrevious()
                                        }
                                    }
                                })
                                .addOnFailureListener(object : com.google.android.gms.tasks.OnFailureListener {
                                    override fun onFailure(exception: Exception) {
                                        Toast.makeText(activity, "Payment recording failed.", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        } else {
                            Toast.makeText(activity, "Please check your card number, expiry (MM/YY) and CVV.", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
        }

        fun navigateToProfileUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val profileView = activity.layoutInflater.inflate(R.layout.activity_profile, null)
            viewFlipper.addView(profileView)
            viewFlipper.showNext()

            val layoutBack = profileView.findViewById<View>(R.id.layoutBack)
            if (layoutBack != null) {
                layoutBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(profileView)
                    }
                })
            }

            val btnCancel = profileView.findViewById<Button>(R.id.btnCancel)
            if (btnCancel != null) {
                btnCancel.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(profileView)
                    }
                })
            }

            val etFullName = profileView.findViewById<EditText>(R.id.etFullName)
            val etMobileNumber = profileView.findViewById<EditText>(R.id.etMobileNumber)
            val etEmail = profileView.findViewById<EditText>(R.id.etEmail)
            val etStreet = profileView.findViewById<EditText>(R.id.etStreet)
            val etGender = profileView.findViewById<EditText>(R.id.etStreet2)
            val btnSave = profileView.findViewById<Button>(R.id.btnSave)

            val activeNodeKey = SessionManager.getNodeKey(activity)
            if (activeNodeKey.isEmpty()) {
                Toast.makeText(activity, "Authentication invalid. Please login again.", Toast.LENGTH_SHORT).show()
                return
            }

            val databaseTargetReference = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("RegisteredUsers")
                .child(activeNodeKey)

            databaseTargetReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (etFullName != null) {
                        etFullName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                    }
                    if (etMobileNumber != null) {
                        val retrievedPhone = snapshot.child("phone").getValue(String::class.java) ?: ""
                        etMobileNumber.setText(retrievedPhone.replace("+94", ""))
                    }
                    if (etEmail != null) {
                        etEmail.setText(snapshot.child("email").getValue(String::class.java) ?: "")
                    }
                    if (etStreet != null) {
                        etStreet.setText(snapshot.child("street").getValue(String::class.java) ?: "")
                    }
                    if (etGender != null) {
                        etGender.setText(snapshot.child("gender").getValue(String::class.java) ?: "")
                    }

                    val btnAdminPanel = profileView.findViewById<View>(R.id.btnAdminPanel)
                    val isAdminAccount = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
                    if (btnAdminPanel != null) {
                        btnAdminPanel.visibility = if (isAdminAccount) View.VISIBLE else View.GONE
                        btnAdminPanel.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                navigateToAdminVerificationUi(activity, viewFlipper)
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(activity, "Network retrieval error.", Toast.LENGTH_SHORT).show()
                }
            })

            val btnMyBookings = profileView.findViewById<View>(R.id.btnMyBookings)
            if (btnMyBookings != null) {
                btnMyBookings.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        navigateToMyBookingsUi(activity, viewFlipper)
                    }
                })
            }

            if (btnSave != null) {
                btnSave.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val inputManager = ProfileManager()
                        if (etFullName != null) inputManager.fullName = etFullName.text.toString()
                        if (etMobileNumber != null) inputManager.mobileNumber = etMobileNumber.text.toString()
                        if (etEmail != null) inputManager.email = etEmail.text.toString()
                        if (etStreet != null) inputManager.street = etStreet.text.toString()
                        if (etGender != null) inputManager.gender = etGender.text.toString()

                        if (inputManager.validateAndParse()) {
                            val mapUpdates = HashMap<String, Any>()
                            mapUpdates["name"] = inputManager.fullName
                            mapUpdates["phone"] = "+94" + inputManager.mobileNumber
                            mapUpdates["email"] = inputManager.email
                            mapUpdates["street"] = inputManager.street
                            mapUpdates["gender"] = inputManager.gender

                            databaseTargetReference.updateChildren(mapUpdates).addOnSuccessListener(object : com.google.android.gms.tasks.OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    Toast.makeText(activity, "Profile saved.", Toast.LENGTH_SHORT).show()
                                    val newNotificationItem = NotificationDto()
                                    newNotificationItem.title = "Profile Updated"
                                    newNotificationItem.message = "Your profile details have been successfully updated."
                                    newNotificationItem.time = "Just now"
                                    appNotifications.add(0, newNotificationItem)
                                    viewFlipper.showPrevious()
                                    viewFlipper.removeView(profileView)
                                }
                            }).addOnFailureListener(object : com.google.android.gms.tasks.OnFailureListener {
                                override fun onFailure(exception: Exception) {
                                    Toast.makeText(activity, "Save mechanism failed.", Toast.LENGTH_SHORT).show()
                                }
                            })
                        } else {
                            Toast.makeText(activity, "Required fields are missing.", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }

        /**
         * Customer-facing booking list: shows every booking the signed-in user has
         * made (mechanic + tow truck), its live status, and — once a job is marked
         * completed — a "Rate" button that opens a 1-5 star + comment dialog and
         * writes the review under Ratings/{mechanicId}/...
         */
        private fun navigateToMyBookingsUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val myBookingsView = activity.layoutInflater.inflate(R.layout.activity_my_bookings, null)
            viewFlipper.addView(myBookingsView)
            viewFlipper.showNext()

            val btnBack = myBookingsView.findViewById<View>(R.id.btnBack)
            if (btnBack != null) {
                btnBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(myBookingsView)
                    }
                })
            }

            val container = myBookingsView.findViewById<LinearLayout>(R.id.containerMyBookings)
            val tvEmpty = myBookingsView.findViewById<TextView>(R.id.tvEmptyMyBookings)
            val myCustomerId = SessionManager.getUserKey(activity)
            val database = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")

            loadMyBookings(activity, viewFlipper, container, tvEmpty, myCustomerId, database)
        }

        private fun loadMyBookings(
            activity: MainActivity,
            viewFlipper: ViewFlipper,
            container: LinearLayout?,
            tvEmpty: TextView?,
            myCustomerId: String,
            database: FirebaseDatabase
        ) {
            val tableNames = listOf("MechanicBookings", "TowTruckBookings")
            var tablesLoaded = 0
            val allRows = ArrayList<Triple<String, String, BookingDto>>()

            for (tableName in tableNames) {
                database.getReference(tableName).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (bookingNode in snapshot.children) {
                            val customerId = bookingNode.child("customerId").getValue(String::class.java) ?: ""
                            if (customerId != myCustomerId) continue
                            val dto = BookingDto()
                            dto.mechanicId = bookingNode.child("mechanicId").getValue(String::class.java) ?: ""
                            dto.customerId = customerId
                            dto.vehicleMake = bookingNode.child("vehicleMake").getValue(String::class.java) ?: ""
                            dto.bookingDate = bookingNode.child("bookingDate").getValue(String::class.java) ?: ""
                            dto.bookingTime = bookingNode.child("bookingTime").getValue(String::class.java) ?: ""
                            dto.issueDescription = bookingNode.child("issueDescription").getValue(String::class.java) ?: ""
                            dto.locationAddress = bookingNode.child("locationAddress").getValue(String::class.java) ?: ""
                            dto.pickupLocation = bookingNode.child("pickupLocation").getValue(String::class.java) ?: ""
                            dto.dropoffLocation = bookingNode.child("dropoffLocation").getValue(String::class.java) ?: ""
                            dto.vehicleCondition = bookingNode.child("vehicleCondition").getValue(String::class.java) ?: ""
                            dto.bookingType = bookingNode.child("bookingType").getValue(String::class.java) ?: "Mechanic"
                            dto.status = bookingNode.child("status").getValue(String::class.java) ?: "pending"
                            dto.isRated = bookingNode.child("isRated").getValue(Boolean::class.java) ?: false
                            allRows.add(Triple(tableName, bookingNode.key ?: "", dto))
                        }

                        tablesLoaded++
                        if (tablesLoaded == tableNames.size) {
                            renderMyBookingRows(activity, viewFlipper, container, tvEmpty, allRows, myCustomerId, database)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        tablesLoaded++
                    }
                })
            }
        }

        private fun renderMyBookingRows(
            activity: MainActivity,
            viewFlipper: ViewFlipper,
            container: LinearLayout?,
            tvEmpty: TextView?,
            rows: List<Triple<String, String, BookingDto>>,
            myCustomerId: String,
            database: FirebaseDatabase
        ) {
            if (container == null) return
            container.removeAllViews()

            if (rows.isEmpty()) {
                tvEmpty?.visibility = View.VISIBLE
                return
            }
            tvEmpty?.visibility = View.GONE
            val density = activity.resources.displayMetrics.density

            for ((tableName, bookingKey, dto) in rows) {
                val row = LinearLayout(activity)
                row.orientation = LinearLayout.VERTICAL
                row.setBackgroundResource(R.drawable.dashboard_card)
                val padding = (14 * density).toInt()
                row.setPadding(padding, padding, padding, padding)
                val rowParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                rowParams.topMargin = (10 * density).toInt()
                row.layoutParams = rowParams

                val title = TextView(activity)
                title.text = if (dto.bookingType == "TowTruck") "Tow Request \u2022 ${dto.vehicleMake}" else "Mechanic Booking \u2022 ${dto.vehicleMake}"
                title.setTextColor(android.graphics.Color.parseColor("#222222"))
                title.textSize = 15f
                title.setTypeface(null, android.graphics.Typeface.BOLD)
                row.addView(title)

                val details = TextView(activity)
                details.text = if (dto.bookingType == "TowTruck") {
                    "Pickup: ${dto.pickupLocation}\nDrop-off: ${dto.dropoffLocation}"
                } else {
                    "When: ${dto.bookingDate} ${dto.bookingTime}\nAt: ${dto.locationAddress}"
                }
                details.setTextColor(android.graphics.Color.parseColor("#777777"))
                details.textSize = 12f
                val detailsParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                detailsParams.topMargin = (6 * density).toInt()
                details.layoutParams = detailsParams
                row.addView(details)

                val statusColor = when (dto.status) {
                    "accepted" -> "#2E7D32"
                    "completed" -> "#1565C0"
                    "declined", "cancelled" -> "#B71C1C"
                    else -> "#D59F00"
                }
                val statusView = TextView(activity)
                statusView.text = "Status: ${dto.status.replaceFirstChar { it.uppercase() }}"
                statusView.setTextColor(android.graphics.Color.parseColor(statusColor))
                statusView.textSize = 11f
                statusView.setTypeface(null, android.graphics.Typeface.BOLD)
                val statusParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                statusParams.topMargin = (8 * density).toInt()
                statusView.layoutParams = statusParams
                row.addView(statusView)

                if (dto.status == "completed" && !dto.isRated) {
                    val btnRate = buildSmallActionButton(activity, "Rate this service", R.drawable.btn_accept, "#FFFFFF")
                    val btnRateParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (44 * density).toInt())
                    btnRateParams.topMargin = (10 * density).toInt()
                    btnRate.layoutParams = btnRateParams
                    btnRate.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            showRatingDialog(activity, viewFlipper, container, tvEmpty, myCustomerId, database, tableName, bookingKey, dto)
                        }
                    })
                    row.addView(btnRate)
                } else if (dto.status == "completed" && dto.isRated) {
                    val tvRated = TextView(activity)
                    tvRated.text = "You rated this service. Thank you!"
                    tvRated.setTextColor(android.graphics.Color.parseColor("#999999"))
                    tvRated.textSize = 11f
                    val tvRatedParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    tvRatedParams.topMargin = (8 * density).toInt()
                    tvRated.layoutParams = tvRatedParams
                    row.addView(tvRated)
                }

                container.addView(row)
            }
        }

        /**
         * Simple 1-5 star + comment dialog built programmatically (no extra layout
         * resource needed). Writes a RatingDto under Ratings/{mechanicId}, then flags
         * the originating booking isRated=true so the "Rate" button doesn't show again.
         */
        private fun showRatingDialog(
            activity: MainActivity,
            viewFlipper: ViewFlipper,
            container: LinearLayout?,
            tvEmpty: TextView?,
            myCustomerId: String,
            database: FirebaseDatabase,
            tableName: String,
            bookingKey: String,
            dto: BookingDto
        ) {
            val density = activity.resources.displayMetrics.density
            val dialogLayout = LinearLayout(activity)
            dialogLayout.orientation = LinearLayout.VERTICAL
            val dialogPadding = (24 * density).toInt()
            dialogLayout.setPadding(dialogPadding, dialogPadding, dialogPadding, dialogPadding)

            val ratingBar = RatingBar(activity)
            ratingBar.numStars = 5
            ratingBar.stepSize = 1f
            ratingBar.rating = 5f
            dialogLayout.addView(ratingBar)

            val commentInput = EditText(activity)
            commentInput.hint = "Leave a comment (optional)"
            val commentParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            commentParams.topMargin = (12 * density).toInt()
            commentInput.layoutParams = commentParams
            dialogLayout.addView(commentInput)

            AlertDialog.Builder(activity)
                .setTitle("Rate this service")
                .setView(dialogLayout)
                .setPositiveButton("Submit", object : android.content.DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: android.content.DialogInterface?, which: Int) {
                        val ratingDto = RatingDto()
                        ratingDto.bookingId = bookingKey
                        ratingDto.mechanicId = dto.mechanicId
                        ratingDto.customerId = myCustomerId
                        ratingDto.stars = ratingBar.rating.toInt().coerceIn(1, 5)
                        ratingDto.comment = commentInput.text.toString().trim()
                        ratingDto.timestamp = System.currentTimeMillis()

                        val ratingsRef = database.getReference("Ratings").child(dto.mechanicId)
                        val newRatingKey = ratingsRef.push().key ?: ""
                        ratingsRef.child(newRatingKey).setValue(ratingDto)
                            .addOnSuccessListener(object : com.google.android.gms.tasks.OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    database.getReference(tableName).child(bookingKey).child("isRated").setValue(true)
                                    Toast.makeText(activity, "Thanks for your feedback!", Toast.LENGTH_SHORT).show()
                                    loadMyBookings(activity, viewFlipper, container, tvEmpty, myCustomerId, database)
                                }
                            })
                            .addOnFailureListener(object : com.google.android.gms.tasks.OnFailureListener {
                                override fun onFailure(exception: Exception) {
                                    Toast.makeText(activity, "Could not submit rating.", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                })
                .setNegativeButton("Cancel", null)
                .show()
        }

        /**
         * Admin-only screen (gated by RegistrationDto.isAdmin, checked in
         * navigateToProfileUi before this is ever reachable): lists every Mechanic /
         * TowTruck account that has not yet been verified, with a one-tap Approve
         * button. There is no self-serve way to become an admin — that flag must be
         * set directly in the Firebase console for a trusted account.
         */
        private fun navigateToAdminVerificationUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val adminView = activity.layoutInflater.inflate(R.layout.activity_admin_verification, null)
            viewFlipper.addView(adminView)
            viewFlipper.showNext()

            val btnBack = adminView.findViewById<View>(R.id.btnBack)
            if (btnBack != null) {
                btnBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(adminView)
                    }
                })
            }

            val container = adminView.findViewById<LinearLayout>(R.id.containerAdminVerify)
            val tvEmpty = adminView.findViewById<TextView>(R.id.tvEmptyAdminVerify)
            val database = FirebaseDatabase.getInstance("https://machfind-6ce35-default-rtdb.asia-southeast1.firebasedatabase.app")

            loadUnverifiedProviders(activity, container, tvEmpty, database)
        }

        private fun loadUnverifiedProviders(
            activity: MainActivity,
            container: LinearLayout?,
            tvEmpty: TextView?,
            database: FirebaseDatabase
        ) {
            database.getReference("RegisteredUsers").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (container == null) return
                    container.removeAllViews()
                    val density = activity.resources.displayMetrics.density
                    var unverifiedCount = 0

                    for (recordNode in snapshot.children) {
                        val type = recordNode.child("userType").getValue(String::class.java) ?: ""
                        if (type != "Mechanic" && type != "TowTruck") continue
                        val isVerified = recordNode.child("isVerified").getValue(Boolean::class.java) ?: false
                        if (isVerified) continue

                        unverifiedCount++
                        val nodeKey = recordNode.key ?: continue
                        val name = recordNode.child("name").getValue(String::class.java) ?: "Unnamed"
                        val email = recordNode.child("email").getValue(String::class.java) ?: ""
                        val specialization = recordNode.child("specialization").getValue(String::class.java) ?: ""

                        val row = LinearLayout(activity)
                        row.orientation = LinearLayout.VERTICAL
                        row.setBackgroundResource(R.drawable.dashboard_card)
                        val padding = (14 * density).toInt()
                        row.setPadding(padding, padding, padding, padding)
                        val rowParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        rowParams.topMargin = (10 * density).toInt()
                        row.layoutParams = rowParams

                        val title = TextView(activity)
                        title.text = "$name \u2022 $type"
                        title.setTextColor(android.graphics.Color.parseColor("#222222"))
                        title.textSize = 15f
                        title.setTypeface(null, android.graphics.Typeface.BOLD)
                        row.addView(title)

                        val details = TextView(activity)
                        details.text = if (specialization.isNotEmpty()) "$email \u2022 $specialization" else email
                        details.setTextColor(android.graphics.Color.parseColor("#777777"))
                        details.textSize = 12f
                        val detailsParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        detailsParams.topMargin = (4 * density).toInt()
                        details.layoutParams = detailsParams
                        row.addView(details)

                        val btnApprove = buildSmallActionButton(activity, "Approve", R.drawable.btn_accept, "#FFFFFF")
                        val btnApproveParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (44 * density).toInt())
                        btnApproveParams.topMargin = (10 * density).toInt()
                        btnApprove.layoutParams = btnApproveParams
                        btnApprove.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                database.getReference("RegisteredUsers").child(nodeKey).child("isVerified").setValue(true)
                                    .addOnSuccessListener(object : com.google.android.gms.tasks.OnSuccessListener<Void> {
                                        override fun onSuccess(p0: Void?) {
                                            Toast.makeText(activity, "$name approved.", Toast.LENGTH_SHORT).show()
                                            loadUnverifiedProviders(activity, container, tvEmpty, database)
                                        }
                                    })
                                    .addOnFailureListener(object : com.google.android.gms.tasks.OnFailureListener {
                                        override fun onFailure(exception: Exception) {
                                            Toast.makeText(activity, "Could not approve.", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            }
                        })
                        row.addView(btnApprove)

                        container.addView(row)
                    }

                    tvEmpty?.visibility = if (unverifiedCount == 0) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun navigateToNotificationsUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val notificationsView = activity.layoutInflater.inflate(R.layout.activity_notifications, null)
            viewFlipper.addView(notificationsView)
            viewFlipper.showNext()

            val btnBack = notificationsView.findViewById<View>(R.id.btnBack)
            if (btnBack != null) {
                btnBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(notificationsView)
                    }
                })
            }

            val rootLayout = notificationsView as? androidx.constraintlayout.widget.ConstraintLayout
            if (rootLayout != null) {
                var listLayout: LinearLayout? = null
                for (i in 0 until rootLayout.childCount) {
                    val childNode = rootLayout.getChildAt(i)
                    if (childNode is ScrollView) {
                        listLayout = childNode.getChildAt(0) as? LinearLayout
                        break
                    }
                }
                if (listLayout != null) {
                    listLayout.removeAllViews()
                    val iterator = appNotifications.iterator()
                    while (iterator.hasNext()) {
                        val currentDto = iterator.next()
                        buildNotificationCard(activity, listLayout, currentDto)
                    }
                }
            }
        }

        private fun buildNotificationCard(activity: MainActivity, container: LinearLayout, dto: NotificationDto) {
            val themedContext = android.view.ContextThemeWrapper(activity, com.google.android.material.R.style.Theme_MaterialComponents_DayNight)

            val cardLayout = LinearLayout(themedContext)
            cardLayout.orientation = LinearLayout.HORIZONTAL
            cardLayout.setBackgroundResource(R.drawable.edittext_rounded)
            cardLayout.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            cardLayout.setPadding(32, 32, 32, 32)

            val cardParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            cardParams.bottomMargin = 24
            cardLayout.layoutParams = cardParams

            val iconView = ImageView(themedContext)
            iconView.setImageResource(R.drawable.bellnew)
            iconView.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
            iconView.setPadding(16, 16, 16, 16)
            iconView.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F0B400"))
            val iconParams = LinearLayout.LayoutParams(110, 110)
            iconView.layoutParams = iconParams
            cardLayout.addView(iconView)

            val textContainer = LinearLayout(themedContext)
            textContainer.orientation = LinearLayout.VERTICAL
            val textContainerParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            textContainerParams.marginStart = 24
            textContainer.layoutParams = textContainerParams

            val senderLayout = LinearLayout(themedContext)
            senderLayout.orientation = LinearLayout.HORIZONTAL
            senderLayout.gravity = Gravity.CENTER_VERTICAL

            val senderText = TextView(themedContext)
            senderText.text = "MachFind App"
            senderText.setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            senderText.textSize = 16f
            senderText.setTypeface(null, android.graphics.Typeface.BOLD)
            senderLayout.addView(senderText)

            val verifiedIcon = ImageView(themedContext)
            verifiedIcon.setImageResource(R.drawable.ic_verified)
            verifiedIcon.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F0B400"))
            val verifiedParams = LinearLayout.LayoutParams(36, 36)
            verifiedParams.marginStart = 8
            verifiedIcon.layoutParams = verifiedParams
            senderLayout.addView(verifiedIcon)
            textContainer.addView(senderLayout)

            val titleText = TextView(themedContext)
            titleText.text = dto.title
            titleText.setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            titleText.textSize = 15f
            titleText.setTypeface(null, android.graphics.Typeface.BOLD)
            val titleParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            titleParams.topMargin = 4
            titleText.layoutParams = titleParams
            textContainer.addView(titleText)

            val messageText = TextView(themedContext)
            messageText.text = dto.message
            messageText.setTextColor(android.graphics.Color.parseColor("#808080"))
            messageText.textSize = 14f
            val messageParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            messageParams.topMargin = 8
            messageText.layoutParams = messageParams
            textContainer.addView(messageText)

            val timeText = TextView(themedContext)
            timeText.text = dto.time
            timeText.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
            timeText.textSize = 12f
            val timeParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            timeParams.topMargin = 16
            timeText.layoutParams = timeParams
            textContainer.addView(timeText)

            cardLayout.addView(textContainer)
            container.addView(cardLayout)
        }

        private fun navigateToChatUi(activity: MainActivity, viewFlipper: ViewFlipper) {
            val chatView = activity.layoutInflater.inflate(R.layout.layout_chat_screen, null)
            viewFlipper.addView(chatView)
            viewFlipper.showNext()

            if (modelTrainer == null) {
                modelTrainer = ChatModelTrainer(activity)
            }

            val btnChatBack = chatView.findViewById<ImageButton>(R.id.btnChatBack)
            if (btnChatBack != null) {
                btnChatBack.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        viewFlipper.showPrevious()
                        viewFlipper.removeView(chatView)
                    }
                })
            }

            val layoutMessageContainer = chatView.findViewById<LinearLayout>(R.id.layoutMessageContainer)
            val scrollChatContainer = chatView.findViewById<ScrollView>(R.id.scrollChatContainer)
            val etChatMessageInput = chatView.findViewById<EditText>(R.id.etChatMessageInput)
            val btnSendChatMessage = chatView.findViewById<ImageButton>(R.id.btnSendChatMessage)
            val btnChatAttach = chatView.findViewById<ImageButton>(R.id.btnChatAttach)

            appendChatBubbleView(
                activity, layoutMessageContainer, scrollChatContainer,
                "Hello! I am your Gemini-powered vehicle diagnostic assistant. Describe your automotive symptoms to begin, or attach a photo of the problem.",
                false, viewFlipper
            )

            if (btnSendChatMessage != null && etChatMessageInput != null && layoutMessageContainer != null && scrollChatContainer != null) {
                btnSendChatMessage.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val messageText = etChatMessageInput.text.toString().trim()
                        if (messageText.isEmpty()) return

                        etChatMessageInput.setText("")
                        appendChatBubbleView(activity, layoutMessageContainer, scrollChatContainer, messageText, true, viewFlipper)

                        val engineReference = modelTrainer
                        if (engineReference != null) {
                            val executionDispatcherThread = Thread(object : Runnable {
                                override fun run() {
                                    try {
                                        val finalOutputResponse = engineReference.processSymptomAnalysisWithAi(messageText)

                                        val mainHandler = Handler(Looper.getMainLooper())
                                        mainHandler.post(object : Runnable {
                                            override fun run() {
                                                appendChatBubbleView(activity, layoutMessageContainer, scrollChatContainer, finalOutputResponse, false, viewFlipper)
                                            }
                                        })
                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }
                                }
                            })
                            executionDispatcherThread.start()
                        }
                    }
                })
            }

            if (btnChatAttach != null && layoutMessageContainer != null && scrollChatContainer != null) {
                btnChatAttach.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val popupMenu = PopupMenu(activity, btnChatAttach)
                        popupMenu.menu.add(0, 1, 0, "Choose from Gallery")
                        popupMenu.menu.add(0, 2, 1, "Take Photo")

                        popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                            override fun onMenuItemClick(menuItem: android.view.MenuItem): Boolean {
                                if (menuItem.itemId == 1) {
                                    activity.pendingVehicleImageCallback = object : ImageCallback {
                                        override fun onImageReady(bitmap: Bitmap?) {
                                            handleCapturedVehicleImage(activity, bitmap, layoutMessageContainer, scrollChatContainer, etChatMessageInput, viewFlipper)
                                        }
                                    }
                                    activity.launchGalleryPicker()
                                    return true
                                }
                                if (menuItem.itemId == 2) {
                                    activity.pendingVehicleImageCallback = object : ImageCallback {
                                        override fun onImageReady(bitmap: Bitmap?) {
                                            handleCapturedVehicleImage(activity, bitmap, layoutMessageContainer, scrollChatContainer, etChatMessageInput, viewFlipper)
                                        }
                                    }
                                    activity.launchCameraCapture()
                                    return true
                                }
                                return false
                            }
                        })
                        popupMenu.show()
                    }
                })
            }
        }

        private fun handleCapturedVehicleImage(
            activity: MainActivity,
            bitmap: Bitmap?,
            layoutMessageContainer: LinearLayout,
            scrollChatContainer: ScrollView,
            etChatMessageInput: EditText?,
            viewFlipper: ViewFlipper
        ) {
            if (bitmap == null) {
                Toast.makeText(activity, "Could not load that photo. Please try again.", Toast.LENGTH_SHORT).show()
                return
            }

            var captionText = ""
            if (etChatMessageInput != null) {
                val inputStr = etChatMessageInput.text.toString()
                if (inputStr.trim().isNotEmpty()) {
                    captionText = inputStr.trim()
                }
                etChatMessageInput.setText("")
            }

            appendImageBubbleView(activity, layoutMessageContainer, scrollChatContainer, bitmap, captionText, true)

            val engineReference = modelTrainer
            if (engineReference != null) {
                val executionDispatcherThread = Thread(object : Runnable {
                    override fun run() {
                        try {
                            val finalOutputResponse = engineReference.processImageSymptomAnalysisWithAi(bitmap, captionText)

                            val mainHandler = Handler(Looper.getMainLooper())
                            mainHandler.post(object : Runnable {
                                override fun run() {
                                    appendChatBubbleView(activity, layoutMessageContainer, scrollChatContainer, finalOutputResponse, false, viewFlipper)
                                }
                            })
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        }
                    }
                })
                executionDispatcherThread.start()
            }
        }

        private fun appendChatBubbleView(activity: MainActivity, container: LinearLayout?, scrollView: ScrollView?, messageText: String, isUser: Boolean, viewFlipper: ViewFlipper?) {
            if (container == null) return

            var displayOutputText = messageText
            var resolvedSpecialization = ""
            val specializationKeyTag = "|SPECIALIZATION:"

            if (!isUser && displayOutputText.contains(specializationKeyTag)) {
                val substringStartIndex = displayOutputText.indexOf(specializationKeyTag)
                val substringEndIndex = displayOutputText.indexOf("|", substringStartIndex + specializationKeyTag.length)

                if (substringEndIndex != -1) {
                    resolvedSpecialization = displayOutputText.substring(substringStartIndex + specializationKeyTag.length, substringEndIndex).trim()
                    val targetTagToRemove = displayOutputText.substring(substringStartIndex, substringEndIndex + 1)
                    displayOutputText = displayOutputText.replace(targetTagToRemove, "").trim()
                }
            }

            val bubbleTextView = TextView(activity)
            bubbleTextView.text = displayOutputText
            bubbleTextView.textSize = 16.0f
            bubbleTextView.setPadding(32, 24, 32, 24)
            bubbleTextView.setTextColor(if (isUser) android.graphics.Color.WHITE else android.graphics.Color.BLACK)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.topMargin = 16
            layoutParams.bottomMargin = 8

            if (isUser) {
                layoutParams.gravity = Gravity.END
                layoutParams.marginStart = 120
                bubbleTextView.setBackgroundResource(R.drawable.btn_signup_rounded)
                bubbleTextView.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F0B400"))
            } else {
                layoutParams.gravity = Gravity.START
                layoutParams.marginEnd = 120
                bubbleTextView.setBackgroundResource(R.drawable.edittext_rounded)
                bubbleTextView.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EAEAEA"))
            }

            bubbleTextView.layoutParams = layoutParams
            container.addView(bubbleTextView)

            if (resolvedSpecialization.isNotEmpty() && viewFlipper != null) {
                val buttonParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                buttonParams.gravity = Gravity.START
                buttonParams.topMargin = 8
                buttonParams.bottomMargin = 24

                val btnBookNow = Button(activity)
                btnBookNow.text = "Book a $resolvedSpecialization"
                btnBookNow.setBackgroundResource(R.drawable.btn_signup_rounded)
                btnBookNow.setTextColor(android.graphics.Color.WHITE)
                btnBookNow.isAllCaps = false
                btnBookNow.setPadding(40, 0, 40, 0)
                btnBookNow.layoutParams = buttonParams

                btnBookNow.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        findSuggestedMechanicFromDatabase(activity, viewFlipper, resolvedSpecialization)
                    }
                })
                container.addView(btnBookNow)
            }

            if (scrollView != null) {
                scrollView.post(object : Runnable {
                    override fun run() {
                        scrollView.fullScroll(View.FOCUS_DOWN)
                    }
                })
            }
        }

        private fun appendImageBubbleView(
            activity: MainActivity,
            layoutMessageContainer: LinearLayout,
            scrollChatContainer: ScrollView,
            bitmap: Bitmap,
            captionText: String,
            isUser: Boolean
        ) {
            val themedContext = android.view.ContextThemeWrapper(
                activity,
                com.google.android.material.R.style.Theme_MaterialComponents_DayNight
            )

            val inflater = LayoutInflater.from(themedContext)
            val bubbleWrapper = LinearLayout(themedContext)
            bubbleWrapper.orientation = LinearLayout.VERTICAL
            val wrapperParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            wrapperParams.gravity = if (isUser) Gravity.END else Gravity.START
            wrapperParams.setMargins(12, 8, 12, 8)
            bubbleWrapper.layoutParams = wrapperParams

            val cardView = com.google.android.material.card.MaterialCardView(themedContext)
            cardView.radius = 24f
            cardView.cardElevation = 3f
            cardView.setCardBackgroundColor(if (isUser) android.graphics.Color.parseColor("#F0B400") else android.graphics.Color.WHITE)

            val innerColumn = LinearLayout(themedContext)
            innerColumn.orientation = LinearLayout.VERTICAL
            innerColumn.setPadding(10, 10, 10, 10)

            val imageView = ImageView(themedContext)
            imageView.setImageBitmap(bitmap)
            imageView.adjustViewBounds = true
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            val imageParams = LinearLayout.LayoutParams(480, 360)
            imageView.layoutParams = imageParams
            innerColumn.addView(imageView)

            if (captionText.isNotEmpty()) {
                val captionView = TextView(themedContext)
                captionView.text = captionText
                captionView.setTextColor(if (isUser) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#1A1A1A"))
                captionView.textSize = 15f
                captionView.setPadding(6, 10, 6, 0)
                innerColumn.addView(captionView)
            }

            cardView.addView(innerColumn)
            bubbleWrapper.addView(cardView)
            layoutMessageContainer.addView(bubbleWrapper)

            scrollChatContainer.post(object : Runnable {
                override fun run() {
                    scrollChatContainer.fullScroll(View.FOCUS_DOWN)
                }
            })
        }
    }

    class RegistrationDto {
        var name: String = ""
        var email: String = ""
        var phone: String = ""
        var gender: String = ""
        var password: String = ""
        var latitude: Double = 0.0
        var longitude: Double = 0.0
        var userType: String = "User"
        // Only meaningful when userType == "Mechanic". Empty for "User" / "TowTruck".
        var specialization: String = ""
        // Service-provider accounts (Mechanic / TowTruck) start unverified and only
        // appear in search/matching once an admin approves them. Plain "User"
        // accounts are auto-verified at registration since they never provide a
        // service that needs vetting.
        var isVerified: Boolean = false
        // Grants access to the in-app admin verification screen. Never set from the
        // client UI — must be flipped to true directly in the Firebase console for
        // a trusted account.
        var isAdmin: Boolean = false
    }

    class BookingDto {
        var mechanicId: String = ""
        var customerId: String = ""
        var vehicleMake: String = ""
        var bookingDate: String = ""
        var bookingTime: String = ""
        var issueDescription: String = ""
        var locationAddress: String = ""
        // "Mechanic" or "TowTruck" — which booking form produced this record.
        var bookingType: String = "Mechanic"
        // "pending" -> "accepted" / "declined" -> "completed" (mechanic-driven),
        // or "cancelled" (customer-driven). Always starts as "pending".
        var status: String = "pending"
        // Tow-truck-only fields. Empty/unused for ordinary mechanic bookings.
        var pickupLocation: String = ""
        var dropoffLocation: String = ""
        var vehicleCondition: String = ""
        // True once the customer has left a rating for this booking, so we don't
        // prompt them twice.
        var isRated: Boolean = false
    }

    class RatingDto {
        var bookingId: String = ""
        var mechanicId: String = ""
        var customerId: String = ""
        var customerName: String = ""
        var stars: Int = 0
        var comment: String = ""
        var timestamp: Long = 0L
    }

    class PaymentDto {
        var bookingId: String = ""
        var cardName: String = ""
        // SECURITY: never persist the full PAN or CVV. Only the last 4 digits are
        // kept as a human-readable reference; the CVV is used in-memory for the
        // validation pass (Luhn check) and discarded immediately after.
        var cardLastFourDigits: String = ""
        var cardExpiry: String = ""
        var paymentMethod: String = ""
    }

    class NotificationDto {
        var title: String = ""
        var message: String = ""
        var time: String = ""
    }
}