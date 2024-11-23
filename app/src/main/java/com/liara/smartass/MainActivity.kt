package com.liara.smartass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.ui.IconGenerator
import com.liara.smartass.GlobalVars.account
import com.liara.smartass.GlobalVars.baseLocationListSize
import com.liara.smartass.GlobalVars.cameraResultDeferred
import com.liara.smartass.GlobalVars.getCameraImage
import com.liara.smartass.GlobalVars.lastLocation
import com.liara.smartass.GlobalVars.latestPicturePath
import com.liara.smartass.GlobalVars.locationList
import com.liara.smartass.GlobalVars.saveLocationList
import com.liara.smartass.GlobalVars.userLoginInfo
import com.liara.smartass.components.MapViewModel
import com.liara.smartass.data.AgendaItem
import com.liara.smartass.data.AgendaItemDao
import com.liara.smartass.data.AgendaItemInterface
import com.liara.smartass.data.AgendaItemTypes
import com.liara.smartass.data.AgendaState
import com.liara.smartass.data.DaySelector
import com.liara.smartass.data.ImageType
import com.liara.smartass.data.Importance
import com.liara.smartass.data.LoginInfo
import com.liara.smartass.data.LoginInfoDao
import com.liara.smartass.data.MapLocation
import com.liara.smartass.data.MapsTransportType
import com.liara.smartass.data.ModifiableItem
import com.liara.smartass.data.RecurringItem
import com.liara.smartass.data.RecurringItemAtDateDao
import com.liara.smartass.data.RecurringItemDao
import com.liara.smartass.data.RecurringItemView
import com.liara.smartass.data.RelativeDayChange
import com.liara.smartass.data.RouteResult
import com.liara.smartass.data.StoredImage
import com.liara.smartass.data.StoredNotificationDao
import com.liara.smartass.data.VerifMethod
import com.liara.smartass.static_objects.AppUpdater
import com.liara.smartass.static_objects.DailyWorker
import com.liara.smartass.static_objects.NetworkHelper
import com.liara.smartass.static_objects.RemoteManager
import com.liara.smartass.static_objects.StoredNotificationManager
import com.liara.smartass.static_objects.TimeSensitiveNotificationBuilder
import com.liara.smartass.static_objects.VisualFeedbackManager
import com.liara.smartass.ui.theme.Black
import com.liara.smartass.ui.theme.FocusedCyan
import com.liara.smartass.ui.theme.SmartAssTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.fortuna.ical4j.model.NumberList
import net.fortuna.ical4j.model.Recur.Builder
import net.fortuna.ical4j.model.Recur.Frequency
import net.fortuna.ical4j.model.WeekDay
import net.fortuna.ical4j.model.WeekDayList
import net.fortuna.ical4j.model.property.RRule
import org.ktorm.entity.Tuple3
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.Normalizer
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    private val cornerShape = RoundedCornerShape(10.dp)
    private lateinit var loadProgress: MutableFloatState
    private val visualSize = 20.dp
    private val textSize = 20.sp
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // inits
        appContext = applicationContext
        GlobalVars.fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        GlobalVars.tempDir = applicationContext.cacheDir.toString()

        StoredNotificationManager.initVars(this)

        GlobalVars.updateLocationList(applicationContext)
        userLoginInfo = mutableStateOf(null)
        lifecycleScope.launch {
            checkLogin()
        }
        var startDestination = "agenda"
        if (userLoginInfo.value == null) {
            startDestination = "settings"
        }

        scheduleDailyWorker()
        GlobalScope.launch(Dispatchers.IO) {
            TimeSensitiveNotificationBuilder.makeNotificationForDay(
                LocalDate.now(), applicationContext
            )
        }

        val openFromNotification = mutableStateOf(intent.extras != null && intent.hasExtra("title"))

        // Fast Update/Sync Handler

        Thread {
            val handler = Handler(Looper.getMainLooper())

            val fastSyncFirstDelay: Long = 5000
            val fastSyncSubsequentDelay: Long = 60000
            handler.postDelayed(object : Runnable {
                override fun run() {
                    Log.d("$TAG Handlers", "Fast Update")
                    Thread {
                        userLoginInfo.value?.let {
                            RemoteManager.getAllFastUpdate(
                                applicationContext,
                                it,
                                agendaItemDao,
                                recurringItemDao,
                                recurringItemAtDateDao
                            )
                        }
                    }.start()
                    handler.postDelayed(this, fastSyncSubsequentDelay)
                }
            }, fastSyncFirstDelay)
        }.start()

        val locationServiceIntent = Intent(this, LocationService::class.java)
        startService(locationServiceIntent)

        checkNewApkUpdate()

        setContent {
            // Remember inits

            val navController = rememberNavController()
            // Loading Bar - At 0, no progress bar, else, app is "frozen" till it's done
            loadProgress = remember {
                mutableFloatStateOf(0.0f)
            }

            SmartAssTheme {
                val animatedProgress by animateFloatAsState(
                    targetValue = loadProgress.floatValue, tween(300, -50), label = ""
                )

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {


                    Column {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.weight(1.0f)
                        ) {
                            composable("agenda") {
                                AgendaComposable(
                                    navController
                                )
                            }// Only show if build type is debug
                            if (BuildConfig.DEBUG) {
                                composable("debug_menu") {
                                    DebugMenu(
                                        navController
                                    )
                                }
                            }
                            composable("map") {
                                SimpleMap()
                            }
                            composable("activities") {
                                ActivityList()
                            }
                            composable("settings") {
                                SettingsMenu()
                            }
                        }
                        if (openFromNotification.value) {
                            AlertDialog(
                                title = {
                                    Text(text = "${intent.extras?.getString("title", "Erreur")}")
                                },
                                text = {
                                    Text(text = "Devrait être terminé")
                                },
                                onDismissRequest = {
                                    openFromNotification.value = false
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        openFromNotification.value = false
                                        Log.d("$TAG", "Compris")
                                    }) {
                                        Text("Confirmé")
                                    }
                                },
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .border(BorderStroke(1.dp, Color.Black))
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = CenterVertically
                        ) {
                            val buttonSize = 52.dp // If too big, can't show all items
                            if (BuildConfig.DEBUG) {
                                Icon(imageVector = Icons.Filled.DeveloperMode,
                                    contentDescription = "Debug Menu",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(buttonSize)
                                        .clickable {
                                            val navigationString = "debug_menu"
                                            if (navController.currentDestination?.route == null || navController.currentDestination?.route != navigationString) {
                                                navController.navigate(navigationString)
                                            }
                                        })
                            }
                            Icon(imageVector = Icons.Filled.Settings, // Navigation, Directions
                                contentDescription = "Settings Menu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(buttonSize)
                                    .clickable {
                                        val navigationString = "settings"
                                        if (navController.currentDestination?.route == null || navController.currentDestination?.route != navigationString) {
                                            navController.navigate(navigationString)
                                        }
                                    })

                            if (userLoginInfo.value != null) {
                                Icon(imageVector = Icons.Filled.Park,
                                    contentDescription = "Activities Nav",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(buttonSize)
                                        .clickable {
                                            val navigationString = "activities"
                                            if (navController.currentDestination?.route == null || navController.currentDestination?.route != navigationString) {
                                                navController.navigate(navigationString)
                                            }
                                        })


                                Icon(imageVector = Icons.Filled.ViewAgenda,
                                    contentDescription = "Agenda Nav",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(buttonSize)
                                        .clickable {
                                            val navigationString = "agenda"
                                            if (navController.currentDestination?.route == null || navController.currentDestination?.route != navigationString) {
                                                navController.navigate(navigationString)
                                            }
                                        })

                                Icon(imageVector = Icons.Filled.Map, // Navigation, Directions
                                    contentDescription = "Map Nav",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(buttonSize)
                                        .clickable {
                                            val navigationString = "map"
                                            if (navController.currentDestination?.route == null || navController.currentDestination?.route != navigationString) {
                                                navController.navigate(navigationString)
                                            }
                                        })
                            }

                        }
                    }


                    // Load overlay
                    if (loadProgress.floatValue != 0.0f) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Black.copy(alpha = 0.1f))
                                .pointerInput(Unit) {}) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .size(150.dp)
                                    .align(Center),
                                strokeWidth = 25.dp,
                            )
                        }
                    }

                    VisualFeedbackManager.Popup()
                }
            }
        }
        // set cameraImage ActivityResultContracts
        getCameraImage =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    Log.i(TAG, "Got image at: $latestPicturePath")
                    cameraResultDeferred.complete(File(latestPicturePath!!))
                } else {
                    val photoFile = File(latestPicturePath!!)
                    photoFile.delete()
                    cameraResultDeferred.complete(null)
                }
            }

    }


    // Launcher for requesting a single permission
    private val requestMultiplePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            for (entry in map.entries) {
                if (entry.value) {
                    // Permission is granted
                    Log.d("$TAG Permissions", "granted - " + entry.key)
                } else {
                    // Permission is denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    Log.d("$TAG Permissions", "denied - " + entry.key)
                }
            }
        }

    private fun scheduleDailyWorker() {
        val constraints = Constraints.Builder().build()

        val dailyWorkRequest = PeriodicWorkRequest.Builder(
            DailyWorker::class.java,
            12, // If it decides to do it incorrectly with 12, it sure to be ran for the next day, 24 hours no.
            TimeUnit.HOURS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyWorker", ExistingPeriodicWorkPolicy.UPDATE, dailyWorkRequest
        )
    }

    private fun checkLogin() { // Need it to get the "this"
        // TODO check if the token is still valid, relog if not, can't seem to know how to decrypt it, maybe an auth0 request to check?
        val loginTable = loginInfoDao.getAll()
        if (loginTable.isEmpty()) {
            WebAuthProvider.login(account).withScheme("demo").withScope("openid profile email")
                .start(this, object : Callback<Credentials, AuthenticationException> {
                    override fun onFailure(error: AuthenticationException) {
                        // Handle failure
                    }

                    override fun onSuccess(result: Credentials) {
                        //println("Refresh_token = " + result.refreshToken) // TODO??
                        LoginInfo.getUserInfo(result.accessToken)
                        checkPermission()
                    }
                })
        } else {
            // Has to be initialized in the main OnCreate
            Log.v(TAG, "Read login info")
            userLoginInfo.value = loginTable[0]
            checkPermission()
        }
    }

    private suspend fun takePictureAndSave(): File {
        // Take picture
        val tempFile = StoredImage.createImageFile(applicationContext)
        val uri = FileProvider.getUriForFile(
            applicationContext, BuildConfig.APPLICATION_ID + ".provider", tempFile
        )

        cameraResultDeferred = CompletableDeferred()

        getCameraImage.launch(uri)

        val imgFile = cameraResultDeferred.await()

        // Resize image
        val options = BitmapFactory.Options()
        options.inSampleSize = 4
        val imgBM = BitmapFactory.decodeFile(imgFile!!.path, options)
        //FixBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gv);

        //FixBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gv);
        val byteArrayOutputStream = ByteArrayOutputStream()
        imgBM.compress(
            Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream
        ) //compress to 50% of original image quality

        val byteArray = byteArrayOutputStream.toByteArray()

        imgFile.writeBytes(byteArray)
        return imgFile
    }

    @Composable
    fun DebugMenu(navController: NavController) {
        val email: MutableState<String> = rememberSaveable { mutableStateOf("") }
        val name: MutableState<String> = rememberSaveable { mutableStateOf("") }
        val userId: MutableState<String> = rememberSaveable { mutableStateOf("") }
        val output: MutableState<String> = rememberSaveable { mutableStateOf("") }

        val hourTest = remember {
            mutableIntStateOf(0)
        }
        val minuteTest = remember {
            mutableIntStateOf(0)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Center),
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                //AppleStyleTimePicker(hourTest, minuteTest)

                userLoginInfo.value?.let {
                    Text(text = it.name)
                    Text(text = it.email)
                    Text(text = it.hashedID)
                }

                Button(enabled = userLoginInfo.value == null, onClick = {
                    loginInfoDao.deleteTable()
                    // Get valid Auth0 Login token
                    lifecycleScope.launch {
                        checkLogin()
                    }
                }) {
                    Text(text = "Connexion", fontSize = textSize)
                }
                Button(enabled = userLoginInfo.value != null, onClick = {
                    email.value = ""
                    name.value = ""
                    userId.value = ""
                    loginInfoDao.deleteTable()
                    userLoginInfo.value = null
                }) {
                    Text(text = "Déconnexion", fontSize = textSize)
                }
                Button(enabled = userLoginInfo.value != null, onClick = {
                    Thread {
                        RemoteManager.getAllAgendaItems(
                            appContext, userLoginInfo.value!!, agendaItemDao
                        )
                    }.start()
                }) {
                    Text(text = "Get All Items")
                }
                Button(onClick = {
                    GlobalScope.launch {
                        takePictureAndSave()
                    }
                }) {
                    Text(text = "Test camera")
                }
                Button(onClick = {
                    VisualFeedbackManager.showMessage("Test error popup")
                }) {
                    Text(text = "Test error popup")
                }
                Button(onClick = {
                    Thread {
                        val home = LatLng(48.4356, -71.1153)
                        val uqac = LatLng(48.418948, -71.052471)
                        RemoteManager.getPolylinePoints(
                            home, uqac, mean = MapsTransportType.Transit
                        )
                    }.start()
                }) {
                    Text(text = "Test get route")
                }
                Button(onClick = {
                    StoredNotificationManager.testNotification(appContext)
                }) {
                    Text(text = "Test Notification Basic")
                }
                Button(onClick = {
                    StoredNotificationManager.testNotification(appContext, Importance.Important)
                }) {
                    Text(text = "Test Notification Important")
                }
                Button(onClick = {

                    GlobalScope.launch {
                        val file = takePictureAndSave()
                        Log.v("$TAG TakePictureButton", "Filepath : ${file.path}")
                        RemoteManager.uploadImage(
                            appContext, file, "test", ImageType.Proof, userLoginInfo.value!!
                        )
                    }

                }) {
                    Text(text = "Take photo and post it")
                }
                val imageBitmap: MutableState<ImageBitmap?> = remember {
                    mutableStateOf(null)
                }
                Button(onClick = {
                    GlobalScope.launch {
                        RemoteManager.getAllRecurringItems(
                            applicationContext,
                            userLoginInfo.value!!,
                            recurringItemDao,
                            recurringItemAtDateDao
                        )
                    }
                }) {
                    Text("Test Get Recurring Item")
                }
                Button(onClick = {
                    // Open this link

                    val link = "https://nextcloud.mattysgervais.com/s/jk4cFiGHzscaSef"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)/*GlobalScope.launch(Dispatchers.IO) {
                        AppUpdater.doInBackground()
                    }
                   startActivity(AppUpdater.onPostExecute(applicationContext))*/
                }) {
                    Text("Test Get Update")
                }
                Button(onClick = {
                    GlobalScope.launch(Dispatchers.IO) {
                        TimeSensitiveNotificationBuilder.makeNotificationForDay(
                            LocalDate.now(), applicationContext
                        )
                    }
                }) {
                    Text("Test Reccurring Notif Builder")
                }
            }
        }
    }

    private fun getMapsUrl(
        origin: String, destination: String, mapsTransportType: MapsTransportType
    ): String {
        fun String.removeNonSpacingMarks() =
            Normalizer.normalize(this, Normalizer.Form.NFD)
                .replace("\\p{Mn}+".toRegex(), "")

        fun String.adaptStringToUrl(): String {
            return this
                .replace(" ", "+")
                .removeNonSpacingMarks()
        }
        /*val returnedUrl =
            "https://www.google.com/maps/dir/?api=1&" + "travelmode=${mapsTransportType.mapsMapping}&" + "origin=${
                adaptStringToUrl(
                    origin
                )
            }&" + "destination=${adaptStringToUrl(destination)}"*/
        val returnedUrl =
            "https://www.google.com/maps?saddr=${origin.adaptStringToUrl()}&daddr=${destination.adaptStringToUrl()}"
        Log.v("$TAG MapsUrl", returnedUrl)
        return returnedUrl
    }

    data class MapSelectionHelper(
        val selectedFrom: MutableIntState,
        val selectedTo: MutableIntState,
        val selectedTransport: MutableState<MapsTransportType>,
        val mutableLocationList: MutableState<MutableList<MapLocation>>
    ) {
        fun getFrom() = mutableLocationList.value[selectedFrom.intValue]
        fun getTo() = mutableLocationList.value[selectedTo.intValue]
        fun getUserMapLocation(): MapLocation {
            if (!userLocationExists()) {
                Log.e("MapSelectionHelper", "No user location")
            }
            return mutableLocationList.value[0]
        }

        fun getTransportType() = selectedTransport
        fun userLocationExists(): Boolean = mutableLocationList.value.size > baseLocationListSize
    }

    @Composable
    fun SimpleMap() {
        // When position updates, adds it to the list, or replaces it
        val mutableLocationList: MutableState<MutableList<MapLocation>> =
            remember { mutableStateOf(locationList.toMutableList()) }
        val selectedFrom = remember { mutableIntStateOf(0) }
        val selectedTo = remember { mutableIntStateOf(1) }
        val selectedTransport: MutableState<MapsTransportType> = remember {
            mutableStateOf(MapsTransportType.Transit)
        }
        val mapHelper = MapSelectionHelper(
            selectedFrom, selectedTo, selectedTransport, mutableLocationList
        )

        fun addOrUpdateLocation() {
            if (lastLocation.value == null) {
                return
            }
            if (mapHelper.userLocationExists()) {
                mutableLocationList.value[0] = lastLocation.value!!
                mutableLocationList.value = mutableLocationList.value
            } else {
                // Should be on open
                mutableLocationList.value.add(0, lastLocation.value!!)
            }
        }

        // Need both of these so ON LAUNCH it works. Even if they can potentially be called both on update.
        if (!mapHelper.userLocationExists() && lastLocation.value != null) {
            // so that when you launch simple map, it adds the users location and selects it by default if available
            addOrUpdateLocation()
        }
        LaunchedEffect(lastLocation.value) {
            if (lastLocation.value != null) addOrUpdateLocation()
        }

        val mapsResult: MutableState<RouteResult> =
            remember { mutableStateOf(RouteResult(listOf(), 0.0, 0)) }

        var fromExpanded by remember { mutableStateOf(false) }
        var toExpanded by remember { mutableStateOf(false) }
        var meanExpanded by remember { mutableStateOf(false) }
        val dropDownModifier: Modifier = Modifier
        Column(horizontalAlignment = CenterHorizontally) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()
            ) {
                fun calculateDirections() {
                    Thread {
                        loadProgress.floatValue = 0.3f

                        RemoteManager.getPolylinePoints(
                            mapHelper.getFrom().location,
                            mapHelper.getTo().location,
                            mapHelper.getTransportType().value
                        ).also { result ->
                            if (result != null) mapsResult.value = result
                        }

                        loadProgress.floatValue = 1.0f
                        loadProgress.floatValue = 0.0f
                    }.start()
                }
                Box(modifier = Modifier.weight(1.0f), contentAlignment = Center) {
                    Button(
                        modifier = dropDownModifier,
                        onClick = { fromExpanded = !fromExpanded }) {
                        Text(mapHelper.getFrom().shortName, fontSize = textSize)
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(visualSize)
                        )
                    }
                    DropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false },
                    ) {
                        mutableLocationList.value.forEachIndexed { newSelectedIndex, newSelectedMapLocation ->
                            DropdownMenuItem(text = {
                                Text(text = newSelectedMapLocation.shortName, fontSize = textSize)
                            }, onClick = {
                                fromExpanded = false

                                if (newSelectedIndex == selectedTo.intValue) {
                                    // Switches them
                                    selectedTo.intValue = selectedFrom.intValue
                                }
                                selectedFrom.intValue = newSelectedIndex
                                calculateDirections()
                            })
                        }
                    }
                }
                Box(modifier = Modifier.weight(0.9f), contentAlignment = Center) {
                    Button(
                        modifier = dropDownModifier,
                        onClick = { meanExpanded = !meanExpanded }) {
                        Icon(
                            selectedTransport.value.icon,
                            "${selectedTransport.value.text} icone",
                            modifier = Modifier.size(visualSize)
                        )
                        Text(selectedTransport.value.text + " ", fontSize = textSize)
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(visualSize)
                        )
                    }
                    DropdownMenu(
                        expanded = meanExpanded,
                        onDismissRequest = { meanExpanded = false },
                    ) {
                        MapsTransportType.entries.forEach { mean ->
                            DropdownMenuItem(text = {
                                Row {
                                    Icon(
                                        mean.icon,
                                        "${mean.text} icone",
                                        modifier = Modifier.size(visualSize)
                                    )
                                    Text(text = " " + mean.text, fontSize = textSize)
                                }

                            }, onClick = {
                                meanExpanded = false
                                selectedTransport.value = mean
                                calculateDirections()
                            })
                        }
                    }
                }
                Box(modifier = Modifier.weight(1.0f), contentAlignment = Center) {
                    Button(modifier = dropDownModifier, onClick = { toExpanded = !toExpanded }) {
                        Text(mapHelper.getTo().shortName, fontSize = textSize)
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(visualSize)
                        )
                    }
                    DropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false },
                    ) {
                        mutableLocationList.value.forEachIndexed { newSelectedIndex, newSelectedMapLocation ->
                            DropdownMenuItem(text = {
                                Text(text = newSelectedMapLocation.shortName, fontSize = textSize)
                            }, onClick = {
                                toExpanded = false

                                if (newSelectedIndex == selectedFrom.intValue) {
                                    // Switches them
                                    selectedFrom.intValue = selectedTo.intValue
                                }
                                selectedTo.intValue = newSelectedIndex
                                calculateDirections()
                            })
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(10.dp))
            Button(modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(0.7f), onClick = {
                val url = getMapsUrl(
                    mutableLocationList.value[selectedFrom.intValue].address.toString(),
                    mutableLocationList.value[selectedTo.intValue].address.toString(),
                    selectedTransport.value
                )
                val urlIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(url)
                )
                startActivity(urlIntent)
            }) {
                Icon(Icons.Default.Directions, "Google Maps Icon", Modifier.size(visualSize))
                Text(text = "Vers Google Maps", fontSize = textSize)
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row {
                    Icon(Icons.Default.Timelapse, "Time Icon", modifier = Modifier.size(visualSize))
                    Text(": ${formatDuration(mapsResult.value.duration)}", fontSize = textSize)
                }
                Row {
                    Icon(
                        Icons.Default.SquareFoot,
                        "Distance Icon",
                        modifier = Modifier.size(visualSize)
                    )
                    Text(": ${formatDistance(mapsResult.value.distance)}", fontSize = textSize)
                }
            }
            MapScreen(mapHelper, mapsResult)
        }
    }

    @Composable
    fun MapScreen(
        mapHelper: MapSelectionHelper,
        mapsResult: MutableState<RouteResult>,
        mapViewModel: MapViewModel = viewModel()
    ) {
        val cameraPositionState = mapViewModel.cameraPositionState

        GoogleMap(
            modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState
        ) {
            val markerColor = Color.Blue // Change this to your desired color
            val iconGenerator = IconGenerator(applicationContext)
            iconGenerator.setColor(markerColor.toArgb())
            val bitmap = iconGenerator.makeIcon()
            val newIcon = BitmapDescriptorFactory.fromBitmap(bitmap)
            if (mapHelper.userLocationExists() && mapHelper.getFrom().userManaged && mapHelper.getTo().userManaged) {
                Marker(
                    state = MarkerState(position = mapHelper.getUserMapLocation().location),
                    title = "Position Actuelle",
                    snippet = "Position de votre appareille",
                    icon = newIcon
                )
            }
            val fromIcon: BitmapDescriptor? = if (mapHelper.getFrom().userManaged) null else newIcon
            Marker(
                state = MarkerState(position = mapHelper.getFrom().location),
                title = "Départ de ${mapHelper.getFrom().shortName}",
                snippet = mapHelper.getFrom().address,
                icon = fromIcon
            )

            val toIcon: BitmapDescriptor? = if (mapHelper.getTo().userManaged) null else newIcon
            Marker(
                state = MarkerState(position = mapHelper.getTo().location),
                title = "Arrivé à ${mapHelper.getTo().shortName}",
                snippet = mapHelper.getTo().address,
                icon = toIcon
            )
            Polyline(points = mapsResult.value.route)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun ActivityList() {
        AndroidView(factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(true)
            }
        }, update = { view ->
            view.loadUrl("https://ville.saguenay.ca/activites-et-loisirs")
        })
    }

    @Composable
    fun AgendaComposable(
        navController: NavController
    ) {
        val selectedDay = remember { mutableStateOf(LocalDate.now()) }
        val daySelector = remember { DaySelector(selectedDay) }
        val openAddItem = remember { mutableStateOf(false) }
        val deleteItemMode = remember { mutableStateOf(false) }
        val agendaItems: Flow<List<AgendaItemInterface>> =
            agendaItemDao.getDateFlow(daySelector.getOnlyDate())

        val recurringItems: Flow<List<AgendaItemInterface>> =
            recurringItemAtDateDao.getFlowFromDate(daySelector.getOnlyDate())
                .map { recurringItemAtDate ->
                    recurringItemAtDate.map {
                        it.getView(recurringItemDao, daySelector.getOnlyDate())
                    }
                }

        // Use remember to hold the state across recompositions
        val combinedFlows =
            combine(agendaItems, recurringItems) { agendaList, recurringList ->
                (agendaList + recurringList).sortedBy { it.getEndTime() }
            }
        val stateList = remember { mutableStateOf<List<AgendaItemInterface>>(emptyList()) }
        Column {
            TopMenu(navController, daySelector, openAddItem)

            LaunchedEffect(combinedFlows) {
                withContext(Dispatchers.IO) {
                    combinedFlows.collectLatest { items ->
                        // Update the state with the collected items
                        stateList.value = items
                    }
                }
            }

            AgendaList(stateList, deleteItemMode)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp), // Fill the parent
            contentAlignment = Alignment.BottomStart // Align children to bottom-end (bottom-right)
        ) {
            Button(
                onClick = { deleteItemMode.value = deleteItemMode.value.not() },
                colors = if (deleteItemMode.value) ButtonDefaults.buttonColors(containerColor = Color.Red) else ButtonDefaults.buttonColors()
            ) {
                if (deleteItemMode.value) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Remove items",
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = "Remove items",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp), // Fill the parent
            contentAlignment = Alignment.BottomEnd // Align children to bottom-end (bottom-right)
        ) {
            Button(
                onClick = { openAddItem.value = true },
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Add new item",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopMenu(
        navController: NavController, daySelector: DaySelector, openAddItem: MutableState<Boolean>
    ) {

        val bgColorToday = if (daySelector.isToday()) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.background
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = bgColorToday)
        ) {
            val showDatePicker = remember { mutableStateOf(false) }
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = bgColorToday)
            ) {
                Button(
                    onClick = { daySelector.changeRelativeDay(RelativeDayChange.Previous) },
                    Modifier.weight(0.2f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                        contentDescription = "Remove items",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Column(
                    Modifier
                        .weight(0.6f)
                        .clickable { showDatePicker.value = true },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = CenterHorizontally
                ) {
                    Text(
                        text = daySelector.getFormattedDate(),
                        textAlign = TextAlign.Center,
                        fontSize = (textSize)
                    )
                    if (daySelector.isToday()) {
                        Text(
                            text = "Aujourd'hui",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
                if (showDatePicker.value) {
                    Popup(alignment = Center) {
                        BackHandler {
                            showDatePicker.value = false
                        }
                        Column(
                            Modifier
                                .background(MaterialTheme.colorScheme.background, cornerShape)
                                .fillMaxSize()
                                .padding(15.dp)
                        ) {
                            val datePickerState = DatePickerState(
                                CalendarLocale.CANADA_FRENCH, Instant.from(
                                    daySelector.getDate().value.atStartOfDay(ZoneId.systemDefault())
                                ).toEpochMilli(), null, 2023..2030, DisplayMode.Picker
                            )
                            DatePicker(state = datePickerState)
                            Button(
                                onClick = {
                                    // Convert from milli to LocalDate

                                    val instant: Instant =
                                        Instant.ofEpochMilli(datePickerState.selectedDateMillis!!)
                                    val localDate: LocalDate =
                                        instant.atZone(ZoneOffset.UTC).toLocalDate()

                                    daySelector.changeToDay(localDate)
                                    showDatePicker.value = false
                                }, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Enregistrer")
                            }
                            Button(
                                onClick = {
                                    showDatePicker.value = false
                                }, modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Annuler")
                            }
                        }
                    }
                }

                Button(
                    onClick = { daySelector.changeRelativeDay(RelativeDayChange.Next) },
                    Modifier.weight(0.2f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = "Remove items",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            val currentItem = remember {
                mutableStateOf<ModifiableItem?>(null)
            }
            if (openAddItem.value) {
                AgendaItemEdit(daySelector.getDate(), openAddItem, currentItem)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AgendaItemEdit(
        atDay: MutableState<LocalDate>, // Should be passed in even if baseItem is not null, but baseItem would override every underlying value
        isOpen: MutableState<Boolean>, baseItem: MutableState<ModifiableItem?>
    ) {
        var importanceExpanded by remember { mutableStateOf(false) }
        var verificationExpanded by remember { mutableStateOf(false) }
        val showDateStartPicker = remember { mutableStateOf(false) }
        var showStartTimePicker by remember { mutableStateOf(false) }
        var showEndTimePicker by remember { mutableStateOf(false) }

        // Common to all items
        val eventStartDate = remember { mutableStateOf(LocalDate.now()) }

        var newItemTitle by remember { mutableStateOf("") }
        var newItemDesc by remember { mutableStateOf("") }
        val importanceValues = Importance.entries
        var importanceSelectedIndex by remember { mutableIntStateOf(0) }
        val verificationValues = VerifMethod.entries
        var verificationSelectedIndex by remember { mutableIntStateOf(0) }

        val eventStartDateTime = remember { mutableStateOf(LocalTime.now()) }
        val eventEndDateTime = remember { mutableStateOf(LocalTime.now().plusHours(1)) }
        val startTimePickerState = remember {
            mutableStateOf(
                TimePickerState(
                    eventStartDateTime.value.hour, eventStartDateTime.value.minute, true
                )
            )
        }
        val endTimePickerState = remember {
            mutableStateOf(
                TimePickerState(
                    eventEndDateTime.value.hour, eventEndDateTime.value.minute, true
                )
            )
        }
        val datePickerState: MutableState<DatePickerState> = remember {
            mutableStateOf(
                DatePickerState(
                    CalendarLocale.CANADA_FRENCH, Instant.from(
                        eventStartDate.value.atStartOfDay(ZoneId.systemDefault())
                    ).toEpochMilli(), null, 2023..2030, DisplayMode.Picker
                )
            )
        }

        // Recurring only
        var isRecurringEvent by remember { mutableStateOf(false) }

        val recurringFrequencies: Array<Pair<Frequency, String>> = arrayOf(
            Pair(Frequency.WEEKLY, "Semaine"),
            Pair(Frequency.DAILY, "Jour"),
            Pair(Frequency.MONTHLY, "Mois"),
            Pair(Frequency.YEARLY, "Année")
        )
        val weeklyOptions = arrayOf(
            Tuple3(WeekDay.MO, "L", remember { mutableStateOf(false) }),
            Tuple3(WeekDay.TU, "Ma", remember { mutableStateOf(false) }),
            Tuple3(WeekDay.WE, "Me", remember { mutableStateOf(false) }),
            Tuple3(WeekDay.TH, "J", remember { mutableStateOf(false) }),
            Tuple3(WeekDay.FR, "V", remember { mutableStateOf(false) }),
            Tuple3(WeekDay.SA, "S", remember { mutableStateOf(false) }),
            Tuple3(WeekDay.SU, "D", remember { mutableStateOf(false) }),
        )
        var monthlyOptionsSelected by remember { mutableIntStateOf(0) }
        val monthlyOptions =
            arrayOf(
                Tuple3("1", "Premier jour du mois", remember { mutableStateOf(false) }),
                Tuple3("${eventStartDate.value.dayOfMonth}",
                    "Jour \'${eventStartDate.value.dayOfMonth}\' du mois",
                    remember { mutableStateOf(false) }),
                Tuple3("-1", "Dernier jour du mois", remember { mutableStateOf(false) })
            )

        var recurringSelectedFreq by remember { mutableIntStateOf(0) }
        var frequenciesExpanded by remember { mutableStateOf(false) }
        var stringInterval by remember { mutableStateOf("1") }

        // Makes it so the item cannot be changed from single to recurring
        var modifiying by remember { mutableStateOf(false) }
        if (baseItem.value != null) {
            modifiying = true
        }
        LaunchedEffect(baseItem.value, Unit) {
            Log.d("BaseItem", baseItem.value.toString())
            when (baseItem.value) {
                is AgendaItem -> {
                    val agendaItem = baseItem.value as AgendaItem

                    eventStartDate.value = agendaItem.dateShown
                    newItemTitle = agendaItem.title
                    newItemDesc = agendaItem.description
                    importanceSelectedIndex =
                        importanceValues.indexOfFirst { it == agendaItem.importance }
                    verificationSelectedIndex =
                        verificationValues.indexOfFirst { it == agendaItem.verifMethod }
                    startTimePickerState.value = TimePickerState(
                        agendaItem.getStartTime().hour, agendaItem.getStartTime().minute, true
                    )
                    endTimePickerState.value = TimePickerState(
                        agendaItem.getEndTime().hour, agendaItem.getEndTime().minute, true
                    )
                    datePickerState.value.selectedDateMillis = Instant.from(
                        agendaItem.dateShown.atStartOfDay(ZoneId.systemDefault())
                    ).toEpochMilli()

                    // should auto-update from eventStartDate datePickerState
                }

                is RecurringItem -> {
                    val recurringItem = baseItem.value as RecurringItem
                    val firstDate = AgendaItem.utcToZonedDateTime(recurringItem.first_occurence)
                    val endTime = firstDate.plusMinutes(recurringItem.duration.toLong())
                    eventStartDate.value = firstDate.toLocalDate()
                    newItemTitle = recurringItem.title
                    newItemDesc = recurringItem.description
                    importanceSelectedIndex =
                        importanceValues.indexOfFirst { it == recurringItem.importance }
                    verificationSelectedIndex =
                        verificationValues.indexOfFirst { it == recurringItem.verif_method }
                    startTimePickerState.value = TimePickerState(
                        firstDate.hour, firstDate.minute, true
                    )
                    endTimePickerState.value = TimePickerState(
                        endTime.hour, endTime.minute, true
                    )
                    datePickerState.value.selectedDateMillis =
                        recurringItem.first_occurence.toEpochMilli()

                    // Recurring only
                    isRecurringEvent = true
                    recurringSelectedFreq =
                        recurringFrequencies.indexOfFirst { it.first == recurringItem.rrule.recur.frequency }
                    stringInterval = recurringItem.rrule.recur.interval.toString()
                    when (recurringItem.rrule.recur.frequency) {
                        Frequency.WEEKLY -> {
                            weeklyOptions.forEach {
                                it.third.value =
                                    recurringItem.rrule.recur.dayList.contains(it.first)
                            }
                        }

                        Frequency.MONTHLY -> {
                            monthlyOptions[1] = Tuple3(
                                "${eventStartDate.value.dayOfMonth}",
                                "Jour \'${eventStartDate.value.dayOfMonth}\' du mois",
                                mutableStateOf(false)
                            )
                            monthlyOptionsSelected = monthlyOptions.indexOfFirst {
                                it.first.toInt() == recurringItem.rrule.recur.monthDayList.toIntArray()
                                    .first()
                            }
                        }

                        else -> {
                            // No options
                        }
                    }
                }

                null -> {
                    modifiying = false
                    eventStartDate.value = atDay.value
                }
            }

        }

        Popup(
            alignment = Center, properties = PopupProperties(focusable = true)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .background(Color(0.0f, 0.0f, 0.0f, 0.6f)), contentAlignment = Center
            ) {
                val generalWidth = 0.85f
                Column(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.background, cornerShape
                        )
                        .fillMaxWidth(0.9f)
                        .padding(top = 20.dp, bottom = 20.dp),
                    horizontalAlignment = CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val titleText = if (modifiying) "Modifier une tâche" else "Ajouter une tâche"
                    Text(text = titleText, fontSize = 25.sp)
                    OutlinedTextField(value = newItemTitle,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(generalWidth),
                        label = { Text(text = "Titre") },
                        onValueChange = { newItemTitle = it })
                    OutlinedTextField(value = newItemDesc,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(generalWidth),
                        label = { Text(text = "Description") },
                        onValueChange = { newItemDesc = it })


                    val importanceBackground = when (importanceValues[importanceSelectedIndex]) {
                        Importance.Basic -> (AgendaState.PassedBasicNotDone.backgroundColor)
                        Importance.Important -> (AgendaState.PassedImportantNotDone.backgroundColor)
                        Importance.Moderate -> (AgendaState.PassedModerateNotDone.backgroundColor)
                    }
                    val importanceText = when (importanceValues[importanceSelectedIndex]) {
                        Importance.Basic -> (AgendaState.PassedBasicNotDone.textColor)
                        Importance.Important -> (AgendaState.PassedImportantNotDone.textColor)
                        Importance.Moderate -> (AgendaState.PassedModerateNotDone.textColor)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(generalWidth),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Text("Importance :")
                        Box {
                            Text(
                                text = importanceValues[importanceSelectedIndex].toFrench(),
                                modifier = Modifier
                                    .clickable(onClick = {
                                        importanceExpanded = true
                                    })
                                    .border(
                                        1.dp, MaterialTheme.colorScheme.onBackground, cornerShape
                                    )
                                    .background(color = importanceBackground, cornerShape)
                                    .padding(10.dp),
                                fontSize = textSize,
                                color = importanceText
                            )
                            DropdownMenu(
                                expanded = importanceExpanded,
                                onDismissRequest = { importanceExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                importanceValues.forEachIndexed { index, s ->
                                    DropdownMenuItem(onClick = {
                                        importanceSelectedIndex = index
                                        importanceExpanded = false
                                    }, text = { Text(text = s.toFrench()) })
                                }
                            }
                        }

                    }


                    val verificationIcon = when (verificationValues[verificationSelectedIndex]) {
                        VerifMethod.Aucune -> Icons.Default.Check
                        VerifMethod.Image -> Icons.Default.CameraAlt
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(generalWidth),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Text("Vérification :")
                        Box {
                            Row(
                                modifier = Modifier
                                    .clickable(onClick = {
                                        verificationExpanded = true
                                    })
                                    .border(
                                        1.dp, MaterialTheme.colorScheme.onBackground, cornerShape
                                    ), verticalAlignment = CenterVertically
                            ) {
                                Text(
                                    text = "${verificationValues[verificationSelectedIndex]}",
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = textSize,
                                )
                                Icon(
                                    verificationIcon,
                                    "Verification type icon",
                                    modifier = Modifier.size(32.dp)
                                )
                            }


                            DropdownMenu(
                                expanded = verificationExpanded,
                                onDismissRequest = { verificationExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.4f)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                verificationValues.forEachIndexed { index, s ->
                                    DropdownMenuItem(onClick = {
                                        verificationSelectedIndex = index
                                        verificationExpanded = false
                                    }, text = { Text(text = s.name) })
                                }
                            }
                        }


                    }

                    if (showDateStartPicker.value) {
                        Popup(alignment = Center) {
                            BackHandler {
                                showDateStartPicker.value = false
                            }
                            Column(
                                Modifier
                                    .background(
                                        MaterialTheme.colorScheme.background, cornerShape
                                    )
                                    .fillMaxSize()
                                    .padding(15.dp)
                            ) {

                                DatePicker(state = datePickerState.value)
                                Button(
                                    onClick = {
                                        // Convert from milli to LocalDate

                                        val instant: Instant =
                                            Instant.ofEpochMilli(datePickerState.value.selectedDateMillis!!)
                                        val localDate: LocalDate =
                                            instant.atZone(ZoneOffset.UTC).toLocalDate()

                                        eventStartDate.value = localDate
                                        showDateStartPicker.value = false
                                    }, modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Enregistrer")
                                }
                                Button(
                                    onClick = {
                                        showDateStartPicker.value = false
                                    }, modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Annuler")
                                }
                            }
                        }
                    }

                    val startMinuteText = if (startTimePickerState.value.minute < 10) {
                        "0${startTimePickerState.value.minute}"
                    } else {
                        "${startTimePickerState.value.minute}"
                    }
                    val endMinuteText = if (endTimePickerState.value.minute < 10) {
                        "0${endTimePickerState.value.minute}"
                    } else {
                        "${endTimePickerState.value.minute}"
                    }
                    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    Row(
                        modifier = Modifier.fillMaxWidth(generalWidth),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Text(
                            text = "Début", fontSize = textSize
                        )
                        Button(
                            onClick = {
                                showDateStartPicker.value = true
                            }, enabled = !(modifiying && isRecurringEvent)
                        ) {
                            Text(eventStartDate.value.format(dateFormatter))
                        }
                        Button(onClick = { showStartTimePicker = true }) {
                            Text(
                                text = "${startTimePickerState.value.hour}:${startMinuteText}",
                                fontSize = textSize
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(generalWidth),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Text(
                            text = "Heure de fin", fontSize = textSize
                        )
                        Button(onClick = { showEndTimePicker = true }) {
                            Text(
                                text = "${endTimePickerState.value.hour}:${endMinuteText}",
                                fontSize = textSize
                            )
                        }
                    }

                    if (showStartTimePicker) {
                        Popup(
                            alignment = Center,
                        ) {
                            BackHandler {
                                showStartTimePicker = false
                            }
                            Column(
                                Modifier
                                    .background(
                                        MaterialTheme.colorScheme.background, cornerShape
                                    )
                                    .padding(15.dp)
                            ) {
                                TimePicker(state = startTimePickerState.value)
                                Button(
                                    onClick = { showStartTimePicker = false },
                                    modifier = Modifier.fillMaxWidth(generalWidth)
                                ) {
                                    Text(text = "Enregistrer", fontSize = textSize)
                                }
                            }
                        }
                    }
                    if (showEndTimePicker) {
                        Popup(
                            alignment = Center
                        ) {
                            BackHandler {
                                showEndTimePicker = false
                            }
                            Column(
                                Modifier
                                    .background(
                                        MaterialTheme.colorScheme.background, cornerShape
                                    )
                                    .padding(15.dp)
                            ) {
                                TimePicker(state = endTimePickerState.value)
                                Button(
                                    onClick = { showEndTimePicker = false },
                                    modifier = Modifier.fillMaxWidth(generalWidth)
                                ) {
                                    Text(text = "Enregistrer", fontSize = textSize)
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(generalWidth),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Text(
                            "Récurrente", fontSize = textSize
                        )
                        Switch(
                            checked = isRecurringEvent,
                            onCheckedChange = { isRecurringEvent = it },
                            enabled = !modifiying
                        )
                    }

                    if (!modifiying) {
                        LaunchedEffect(eventStartDate.value) {
                            weeklyOptions.forEach {
                                it.third.value = false
                            }
                            weeklyOptions[eventStartDate.value.dayOfWeek.value - 1].third.value =
                                true
                        }
                    }

                    if (isRecurringEvent) {
                        Row(
                            modifier = Modifier.fillMaxWidth(generalWidth),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = CenterVertically
                        ) {
                            Text("Répété chaque : ")
                            TextField(
                                value = stringInterval,
                                onValueChange = {
                                    if (it == "" || it.isDigitsOnly() && it.toInt() > 0) stringInterval =
                                        it
                                },
                                enabled = !(modifiying && isRecurringEvent),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(50.dp)
                                    .padding(0.dp),
                            )
                            Box {
                                Text(
                                    text = recurringFrequencies[recurringSelectedFreq].second,
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            if (!(modifiying && isRecurringEvent)) frequenciesExpanded =
                                                true
                                        })
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground,
                                            cornerShape
                                        )
                                        .padding(10.dp),
                                    fontSize = textSize
                                )
                                DropdownMenu(
                                    expanded = frequenciesExpanded,
                                    onDismissRequest = { frequenciesExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    recurringFrequencies.forEachIndexed { index, s ->
                                        DropdownMenuItem(onClick = {
                                            recurringSelectedFreq = index
                                            frequenciesExpanded = false
                                        }, text = { Text(text = s.second) })
                                    }
                                }
                            }

                        }
                        if (recurringFrequencies[recurringSelectedFreq].first == Frequency.WEEKLY) {

                            Row(
                                modifier = Modifier.fillMaxWidth(generalWidth),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = CenterVertically
                            ) {
                                weeklyOptions.forEach { weekdayTuple ->
                                    OutlinedIconToggleButton(
                                        checked = weekdayTuple.third.value,
                                        onCheckedChange = {
                                            weekdayTuple.third.value = it
                                        },
                                        modifier = Modifier.size(30.dp),
                                        enabled = !(modifiying && isRecurringEvent),
                                    ) {
                                        Text(weekdayTuple.second, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                        if (recurringFrequencies[recurringSelectedFreq].first == Frequency.MONTHLY) {
                            var optionExpanded by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier.fillMaxWidth(generalWidth),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = CenterVertically
                            ) {
                                Box {
                                    Text(
                                        text = monthlyOptions[monthlyOptionsSelected].second,
                                        modifier = Modifier
                                            .clickable(onClick = {
                                                if (!(modifiying && isRecurringEvent)) optionExpanded =
                                                    true
                                            })
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.onBackground,
                                                cornerShape
                                            )
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        fontSize = textSize,
                                        textAlign = TextAlign.Center
                                    )
                                    DropdownMenu(
                                        expanded = optionExpanded,
                                        onDismissRequest = { optionExpanded = false },
                                        modifier = Modifier
                                            .fillMaxWidth(generalWidth)
                                            .background(MaterialTheme.colorScheme.background)
                                    ) {
                                        monthlyOptions.forEachIndexed { index, s ->
                                            DropdownMenuItem(onClick = {
                                                monthlyOptionsSelected = index
                                                optionExpanded = false
                                            }, text = { Text(text = s.second) })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(generalWidth),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = CenterVertically
                    ) {
                        Button(onClick = {
                            isOpen.value = false
                        }) { Text(text = "Cancel", fontSize = textSize) }
                        Button(onClick = {
                            if ((startTimePickerState.value.hour * 60 + startTimePickerState.value.minute) > (endTimePickerState.value.hour * 60 + endTimePickerState.value.minute)) {
                                VisualFeedbackManager.showMessage("Le temps de départ est après le temps de fin")
                                Log.d(TAG, "Provided start time is AFTER end time")
                                return@Button
                            }
                            val startTime: Instant = LocalTime.of(
                                startTimePickerState.value.hour, startTimePickerState.value.minute
                            ).atDate(eventStartDate.value) // Gets local datetime start
                                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now())) // Converts to instant
                            val endTime: Instant = LocalTime.of(
                                endTimePickerState.value.hour, endTimePickerState.value.minute
                            ).atDate(eventStartDate.value) // Gets local datetime start
                                .toInstant(ZoneId.systemDefault().rules.getOffset(Instant.now())) // Converts to instant
                            // Get AgendaItemTypes from UI
                            if (!modifiying) {
                                // New Item
                                val newItemType: AgendaItemTypes =
                                    if (isRecurringEvent) AgendaItemTypes.Recurring else AgendaItemTypes.Single

                                when (newItemType) {
                                    AgendaItemTypes.Single -> {
                                        val newAgendaItem = AgendaItem(
                                            0,
                                            newItemTitle,
                                            newItemDesc,
                                            startTime,
                                            endTime,
                                            importanceValues[importanceSelectedIndex],
                                            AgendaState.Upcoming,
                                            false,
                                            eventStartDate.value,
                                            verifMethod = verificationValues[verificationSelectedIndex]
                                        )
                                        Thread {
                                            loadProgress.floatValue = 0.3f
                                            RemoteManager.addAgendaItem(
                                                appContext,
                                                userLoginInfo.value!!,
                                                newAgendaItem,
                                                agendaItemDao
                                            )
                                            loadProgress.floatValue = 1.0f
                                            loadProgress.floatValue = 0.0f
                                        }.start()
                                    }

                                    AgendaItemTypes.Recurring -> {
                                        val frequency =
                                            recurringFrequencies[recurringSelectedFreq].first
                                        val builder = Builder()
                                        builder.interval(stringInterval.toInt())
                                        builder.frequency(frequency)
                                        when (frequency) {
                                            Frequency.MONTHLY -> {
                                                builder.monthDayList(NumberList(monthlyOptions[monthlyOptionsSelected].first))
                                            }

                                            Frequency.WEEKLY -> {
                                                val weekDayList =
                                                    WeekDayList(*weeklyOptions.filter { it.third.value }
                                                        .map { it.first }.toTypedArray())
                                                builder.dayList(weekDayList)
                                            }

                                            else -> {}
                                        }

                                        val rrule = RRule(builder.build())

                                        val newItem = RecurringItem(
                                            0,
                                            newItemTitle,
                                            newItemDesc,
                                            importanceValues[importanceSelectedIndex],
                                            duration = ((endTime.epochSecond - startTime.epochSecond) / 60).toInt(),
                                            first_occurence = startTime,
                                            rrule = rrule,
                                            verif_method = verificationValues[verificationSelectedIndex]
                                        )
                                        println(newItem.toString())
                                        Thread {
                                            loadProgress.floatValue = 0.3f
                                            RemoteManager.addRecurringItem(
                                                appContext,
                                                userLoginInfo.value!!,
                                                newItem,
                                                recurringItemDao
                                            )
                                            loadProgress.floatValue = 1.0f
                                            loadProgress.floatValue = 0.0f
                                        }.start()
                                    }
                                }
                            } else {
                                // Update item
                                when (val item = baseItem.value) {
                                    is AgendaItem -> {
                                        val updatedAgendaItem = AgendaItem(
                                            item.uid,
                                            newItemTitle,
                                            newItemDesc,
                                            startTime,
                                            endTime,
                                            importanceValues[importanceSelectedIndex],
                                            AgendaState.Upcoming,
                                            false,
                                            eventStartDate.value,
                                            verifMethod = verificationValues[verificationSelectedIndex]
                                        )
                                        val _dumpState = mutableStateOf(updatedAgendaItem.state)
                                        GlobalScope.launch(Dispatchers.IO) {
                                            RemoteManager.updateAgendaItem(
                                                appContext,
                                                loginInfo = userLoginInfo.value!!,
                                                updatedAgendaItem,
                                                agendaItemDao,
                                                _dumpState
                                            )
                                        }

                                    }

                                    is RecurringItem -> {
                                        val newItem = RecurringItem(
                                            item.uid,
                                            newItemTitle,
                                            newItemDesc,
                                            importanceValues[importanceSelectedIndex],
                                            duration = ((endTime.epochSecond - startTime.epochSecond) / 60).toInt(),
                                            first_occurence = startTime,
                                            rrule = item.rrule,
                                            verif_method = verificationValues[verificationSelectedIndex]
                                        )
                                        GlobalScope.launch(Dispatchers.IO) {
                                            RemoteManager.updateRecurringItem(
                                                appContext,
                                                userLoginInfo.value!!,
                                                newItem,
                                                recurringItemDao
                                            )
                                        }
                                    }

                                    null -> {
                                        Log.e(TAG, "No item to update")
                                    }
                                }
                            }

                            isOpen.value = false
                        }) {
                            Text(text = "Sauvegarder", fontSize = textSize)
                        }
                    }
                }
            }
        }
    }

    private var lastFocus: MutableState<Boolean>? = null

    @Composable
    fun AgendaList(
        mutableItems: State<List<AgendaItemInterface>>, deleteItemMode: MutableState<Boolean>
    ) {
        val showEditMenu = remember { mutableStateOf(false) }
        val currentEditItem: MutableState<ModifiableItem?> = remember { mutableStateOf(null) }
        if (showEditMenu.value) {
            AgendaItemEdit(
                atDay = remember { mutableStateOf(LocalDate.now()) },
                isOpen = showEditMenu,
                baseItem = currentEditItem
            )
        }

        Column(Modifier.padding(horizontal = 15.dp)) {
            LazyColumn(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                itemsIndexed(mutableItems.value) { index, _ ->
                    //Calls only on re-draws. seem to be called when items in the list change.

                    // If lists changes, agendaItem is updated, if you just use rowAgendaItem, agendaItem is not updated
                    val agendaItem: AgendaItemInterface = mutableItems.value[index]
                    agendaItem.updateState()
                    val state = remember { mutableStateOf(agendaItem.state) }
                    val visuallyChecked = remember { mutableStateOf(agendaItem.done) }
                    val focused = remember { mutableStateOf(false) }

                    // Makes sure it updates when agendaItem changes
                    LaunchedEffect(mutableItems.value) {
                        state.value = agendaItem.state
                        visuallyChecked.value = agendaItem.done
                        focused.value = false
                    }

                    AgendaItemRow(agendaItem = agendaItem,
                        state = state,
                        isVisuallyChecked = visuallyChecked,
                        onCheckChange = { newDesiredCheck ->
                            val currentItem = agendaItem
                            if (currentItem.done != newDesiredCheck) {
                                GlobalScope.launch(Dispatchers.Main) {
                                    try {
                                        NetworkHelper.checkNetwork(applicationContext)
                                        // Check if item can be completed
                                        currentItem.checkDoneWithVerifMethod(
                                            applicationContext, newDesiredCheck
                                        )
                                        // If it can, then update the agendaItem db
                                        if (currentItem.done == newDesiredCheck) {
                                            withContext(Dispatchers.IO) {
                                                val remoteManagerOk = agendaItem.updateSelfRemote(
                                                    applicationContext, state
                                                )

                                                if (remoteManagerOk) {
                                                    visuallyChecked.value = newDesiredCheck
                                                    StoredNotificationManager.updateNotificationForItems(
                                                        listOf(agendaItem), applicationContext
                                                    )
                                                    agendaItem.updateState()
                                                    state.value = agendaItem.state
                                                } else {
                                                    currentItem.done = !newDesiredCheck
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("$TAG AddAgendaItem", e.toString())
                                    }

                                }
                            }
                        },
                        isFocused = focused,
                        onFocusChange = { newFocus ->
                            if (newFocus && lastFocus != focused) {
                                lastFocus?.value = false
                                lastFocus = focused
                            }
                        },
                        deleteItemMode,
                        onLongClick = {
                            when (val itemToEdit = mutableItems.value[index]) {
                                is RecurringItemView -> {
                                    currentEditItem.value = itemToEdit.recurringItemData
                                }

                                is AgendaItem -> {
                                    currentEditItem.value = itemToEdit
                                }
                            }
                            showEditMenu.value = true
                        })
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    fun AgendaItemRow(
        agendaItem: AgendaItemInterface,
        state: MutableState<AgendaState>,
        isVisuallyChecked: MutableState<Boolean>,
        onCheckChange: (Boolean) -> Unit,
        isFocused: MutableState<Boolean>,
        onFocusChange: (Boolean) -> Unit,
        deleteItemMode: MutableState<Boolean>,
        onLongClick: () -> Unit = {},
    ) {
        // val isFocused = remember { mutableStateOf(false) }
        onFocusChange(isFocused.value)
        val padding = if (isFocused.value) 30.dp else 7.dp
        state.value = agendaItem.state
        val deleteDialogOpen = remember { mutableStateOf(false) }

        // If focused cyan
        val backgroundColor = if (!isFocused.value) state.value.backgroundColor else FocusedCyan
        val textColor = if (!isFocused.value) state.value.textColor else Black
        Box {
            // val boxHeight = this.maxHeight
            Row(
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = backgroundColor, shape = cornerShape)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
                        shape = cornerShape
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            onLongClick()
                        }, onPress = {
                            isFocused.value = !(isFocused.value)
                        })
                    }
                    .padding(start = 16.dp, top = padding, end = 7.dp, bottom = padding)
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    horizontalAlignment = CenterHorizontally,
                    modifier = Modifier.weight(0.2f, false)
                ) {
                    var startMinutes = "${agendaItem.getStartTime().minute}"
                    if (startMinutes.length == 1) startMinutes = "0$startMinutes"
                    var endMinutes = "${agendaItem.getEndTime().minute}"
                    if (endMinutes.length == 1) endMinutes = "0$endMinutes"
                    Text(
                        text = "${agendaItem.getStartTime().hour}:$startMinutes",

                        style = TextStyle(color = textColor),
                    )
                    Text(
                        text = "${agendaItem.getEndTime().hour}:$endMinutes",

                        style = TextStyle(color = textColor),
                    )
                }

                Column(
                    horizontalAlignment = CenterHorizontally,
                    modifier = Modifier.weight(1f, true),
                ) {
                    Text(text = agendaItem.title, fontSize = 30.sp, color = textColor)
                    if (isFocused.value) {
                        Text(text = agendaItem.description, color = textColor)
                    }
                }

                if (deleteItemMode.value) {
                    IconButton(modifier = Modifier.weight(0.1f, false), onClick = {
                        deleteDialogOpen.value = true

                    }) {
                        Icon(
                            imageVector = Icons.Filled.DeleteForever,
                            contentDescription = "Delete",
                            tint = textColor
                        )
                    }
                } else {
                    val imageBitmap: MutableState<ImageBitmap?> = remember {
                        mutableStateOf(null)
                    }

                    if (agendaItem.verifMethod == VerifMethod.Image && agendaItem.done) {
                        Icon(
                            Icons.Default.ImageSearch,
                            "Bouton affiche preuve",
                            tint = textColor,
                            modifier = Modifier.clickable {
                                GlobalScope.launch(Dispatchers.IO) {
                                    loadProgress.floatValue = 0.2f
                                    val image = agendaItem.getImage(ImageType.Proof)
                                    loadProgress.floatValue = 1.0f
                                    imageBitmap.value = image?.imageData?.asImageBitmap()
                                    loadProgress.floatValue = 0.0f
                                }
                            })
                        imageBitmap.value?.let {
                            Popup {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    Column(
                                        horizontalAlignment = CenterHorizontally,
                                        verticalArrangement = Arrangement.SpaceAround,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Image(it, "Image de la preuve")
                                        Button(modifier = Modifier.height(60.dp), onClick = {
                                            imageBitmap.value = null
                                        }) {
                                            Text("Retour")
                                        }
                                    }

                                }
                            }
                        }
                    }
                    Checkbox(
                        checked = isVisuallyChecked.value,
                        onCheckedChange = { onCheckChange(it) },
                        modifier = Modifier.weight(0.1f, false),
                        colors = CheckboxDefaults.colors(uncheckedColor = textColor)
                    )

                }
            }
        }
        when {

            deleteDialogOpen.value -> {
                val message = when (agendaItem.getType()) {
                    AgendaItemTypes.Single -> {
                        "Voulez-vous vraiment supprimer: ${agendaItem.title}"
                    }

                    AgendaItemTypes.Recurring -> {
                        "Ceci supprimera TOUTES les occurences de: ${agendaItem.title}"
                    }
                }
                val title = "Supression d'un événement: ${agendaItem.getType()}"
                AlertDialog(icon = {
                    Icon(Icons.Filled.Delete, contentDescription = "Trash Can Icon")
                }, title = {
                    Text(text = title)
                }, text = {
                    Text(text = message)
                }, onDismissRequest = {
                    deleteDialogOpen.value = false
                }, confirmButton = {
                    TextButton(onClick = {
                        deleteDialogOpen.value = false
                        agendaItem.deleteSelf(applicationContext)
                    }) {
                        Text("Confirmé")
                    }
                }, dismissButton = {
                    TextButton(onClick = {
                        deleteDialogOpen.value = false
                    }) {
                        Text("Annulé")
                    }
                })
            }
        }

    }

    data class MapLocationEdit(
        var shortName: MutableState<String>, var adress: MutableState<String>
    )

    @Composable
    fun MapLocationRow(
        editableMapLocation: MapLocationEdit, deleteSelf: () -> Unit
    ) {
        val confirmDeleteDialog = remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = CenterVertically
        ) {
            OutlinedTextField(value = editableMapLocation.shortName.value,
                label = { Text("Court") },
                modifier = Modifier.weight(0.2f),
                maxLines = 1,
                onValueChange = {
                    editableMapLocation.shortName.value = it
                })
            OutlinedTextField(value = editableMapLocation.adress.value,
                label = { Text("Adresse") },
                modifier = Modifier.weight(0.6f),
                maxLines = 1,
                onValueChange = {
                    editableMapLocation.adress.value = it
                })
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        confirmDeleteDialog.value = true
                    })
        }
        if (confirmDeleteDialog.value) {
            AlertDialog(
                title = { Text("Suppression de lieu") },
                onDismissRequest = { confirmDeleteDialog.value = false },
                confirmButton = {
                    Button(onClick = {
                        deleteSelf()
                        confirmDeleteDialog.value = false
                    }) {
                        Text("Supprimer", color = Color.Red, fontSize = textSize)
                    }

                },
                dismissButton = {
                    Button(onClick = {
                        confirmDeleteDialog.value = false
                    }) {
                        Text("Annuler", fontSize = textSize)
                    }
                })
        }

    }

    @Composable
    fun MapLocationList(
        listMapLocation: List<MapLocation>, isOpen: MutableState<Boolean>
    ) {
        // Makes list of editable elements to save
        val editableList = remember { mutableStateListOf<MapLocationEdit>() }

        if (editableList.isEmpty()) {
            listMapLocation.forEach { constLocation ->
                if (constLocation.userManaged) editableList.add(
                    MapLocationEdit(
                        mutableStateOf(constLocation.shortName),
                        mutableStateOf(constLocation.address ?: "")
                    )
                )
            }
        }
        Column(
            Modifier
                .padding(horizontal = 15.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Liste des lieux enregistreés",
                fontSize = 34.sp,
                modifier = Modifier.padding(10.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    itemsIndexed(editableList) { index, editableMapLocation ->
                        MapLocationRow(editableMapLocation) { editableList.removeAt(index) }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    /*Button(modifier = Modifier.weight(1f), onClick = {
                        editableList.removeAt(editableList.size - 1)
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Icon")
                    }*/
                    Button(modifier = Modifier.weight(1f), onClick = {
                        editableList.add(MapLocationEdit(mutableStateOf(""), mutableStateOf("")))
                    }) {
                        Icon(Icons.Filled.AddLocation, contentDescription = "Add Location Icon")
                    }
                }

            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    isOpen.value = false
                }) {
                    Text(text = "Annuler", fontSize = textSize)
                }
                Button(onClick = {
                    val newMapLocations = mutableListOf<MapLocation>()
                    editableList.forEach {

                        val geocoder = Geocoder(applicationContext)
                        val geocodeListener = Geocoder.GeocodeListener { addresses ->
                            val location = addresses[0]
                            val latLng = LatLng(location.latitude, location.longitude)
                            // When a geocode is found add it to list and save param.
                            newMapLocations.add(
                                MapLocation(
                                    latLng, it.shortName.value, it.adress.value
                                )
                            )
                            saveLocationList(applicationContext, newMapLocations)
                        }
                        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocationName(it.adress.value, 1, geocodeListener)
                        } else {
                            VisualFeedbackManager.showMessage("Ne peut avoir lat/lng de Geocode")
                        }

                    }
                    //saveLocationList(applicationContext, newMapLocations)
                    isOpen.value = false
                }) {
                    Text("Sauvegarder", fontSize = textSize)
                }

            }
        }
    }

    @Composable
    fun NotificationSettings(isOpen: MutableState<Boolean>) {
        Column(
            Modifier
                .padding(horizontal = 15.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            BackHandler {
                isOpen.value = false
            }
            Text(
                text = "Notification Settings", fontSize = 34.sp, modifier = Modifier.padding(10.dp)
            )
            val configManager =
                StoredNotificationManager.UserNotificationConfigPreferences(applicationContext)
            val notificationConfigs = remember {
                configManager.getAllConfigs().map {
                    Pair(
                        it, mutableStateOf(it.enabled)
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                notificationConfigs.forEachIndexed { index, config ->
                    Text(text = config.first.name, fontSize = 24.sp)
                    Switch(checked = config.second.value, onCheckedChange = {
                        config.second.value = it
                    })
                }
            }
            Button(onClick = {
                // Cancel all notifications
                StoredNotificationManager.removeAllNotifications()

                // Save new configurations
                configManager.saveConfigs(notificationConfigs.map { it.first.copy(enabled = it.second.value) })

                // Build new notifications
                StoredNotificationManager.updateAllNotifications(appContext)

                isOpen.value = false
            }) {
                Text(text = "Sauvegarder", fontSize = textSize)
            }
            Button(onClick = {
                isOpen.value = false
            }) {
                Text(text = "Annuler", fontSize = textSize)
            }
        }
    }

    @Composable
    fun SettingsMenu() {
        Column(
            modifier = Modifier.fillMaxSize(), horizontalAlignment = CenterHorizontally
        ) {
            Text(text = "Paramètres", fontSize = 34.sp, modifier = Modifier.padding(10.dp))
            Text(text = BuildConfig.VERSION_NAME, fontSize = 14.sp)
            Text(userLoginInfo.value?.name ?: "Déconnecté", fontSize = 24.sp)
            Row {
                Button(enabled = userLoginInfo.value == null, onClick = {
                    loginInfoDao.deleteTable()
                    // Login
                    lifecycleScope.launch {
                        checkLogin()
                    }
                }) {
                    Text(text = "Connexion", fontSize = textSize)
                }
                Button(enabled = userLoginInfo.value != null, onClick = {
                    loginInfoDao.deleteTable()
                    userLoginInfo.value = null
                }) {
                    Text(text = "Déconnexion", fontSize = textSize)
                }
            }

            /*Button(onClick = {
                // Storing data into SharedPreferences
                val sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE)

                // Creating an Editor object to edit(write to the file)
                val myEdit = sharedPreferences.edit()
                // Storing the key and its value as the data fetched from edittext
                myEdit.putString("state_not_done_basic", Color.Black.toString())

                // Once the changes have been made, we need to commit to apply those changes made,
                // otherwise, it will throw an error
                myEdit.apply()
            }) {
                Text("Couleur ")
            }*/
            val showPopupLocationList = remember { mutableStateOf(false) }
            val showPopupNotificationConfig = remember {
                mutableStateOf(false)
            }
            Button(onClick = {
                showPopupLocationList.value = true
            }) {
                Text("Modifier la liste de lieux", fontSize = textSize)
            }
            if (showPopupLocationList.value) {
                Popup(
                    alignment = Center, properties = PopupProperties(focusable = true)
                ) {
                    BackHandler {
                        showPopupLocationList.value = false
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(10.dp)
                    ) {
                        MapLocationList(locationList, showPopupLocationList)
                    }
                }
            }
            Button(onClick = {
                showPopupNotificationConfig.value = true
            }) {
                Text("Modifier les heures de notification", fontSize = textSize)
            }
            if (showPopupNotificationConfig.value) {
                Popup(
                    alignment = Center, properties = PopupProperties(focusable = true)
                ) {
                    BackHandler {
                        showPopupNotificationConfig.value = false
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(10.dp)
                    ) {
                        NotificationSettings(showPopupNotificationConfig)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(onClick = {
                    Thread {
                        loadProgress.floatValue = 0.1f
                        RemoteManager.getAllFastUpdate(
                            appContext,
                            userLoginInfo.value!!,
                            agendaItemDao,
                            recurringItemDao,
                            recurringItemAtDateDao
                        )
                        loadProgress.floatValue = 1.0f
                        loadProgress.floatValue = 0.0f
                    }.start()
                }) {
                    Text("Sync", fontSize = textSize)
                }
                Button(onClick = {
                    checkNewApkUpdate()
                }) {
                    Text("MAJ", fontSize = textSize)
                }
            }
        }
    }

    fun checkNewApkUpdate() {
        val outputPath = filesDir.absolutePath + "/hdp_latest_release.apk"
        CoroutineScope(Dispatchers.IO).launch {
            val newUpdate = AppUpdater.checkAndDownloadLatestRelease(
                "liperium", "hdp-app-builds", outputPath
            )
            if (newUpdate) {
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Opening Activity")
                    val file = File(outputPath)
                    val fileUri: Uri = FileProvider.getUriForFile(
                        applicationContext, "${applicationContext.packageName}.provider", file
                    )
                    Log.d(TAG, "Opening Activity | File : ${fileUri.path}")
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(
                            fileUri, "application/vnd.android.package-archive"
                        ) // Adjust MIME type as needed
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    this@MainActivity.startActivity(intent)
                }
            }
        }
    }

    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        val formattedDuration = StringBuilder()

        if (hours > 0) {
            formattedDuration.append("${hours}h ")
        }

        if (minutes > 0 || hours > 0) {
            formattedDuration.append("${minutes}m ")
        }

        formattedDuration.append("${remainingSeconds}s")

        return formattedDuration.toString()
    }

    fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            val kilometers = meters / 1000
            String.format(Locale.CANADA, "%.2f km", kilometers)
        } else {
            String.format(Locale.CANADA, "%.2f m", meters)
        }
    }

    fun checkPermission() {
        val devicePermissions = mutableListOf(
            android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            devicePermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        // Check permissions
        requestMultiplePermissionLauncher.launch(
            devicePermissions.toTypedArray()
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewAgendaItems() {
        SmartAssTheme {
            Column {
                for (item in AgendaItem.makeDumbList(
                    10, daySelector = DaySelector(remember { mutableStateOf(LocalDate.now()) })
                ).toList()) {
                    item.updateState()
                    val state = remember { mutableStateOf(item.state) }
                    val checked = remember { mutableStateOf(item.done) }
                    val focused = remember { mutableStateOf(false) }
                    val deleteItemMode = remember { mutableStateOf(false) }
                    AgendaItemRow(
                        item,
                        state = state,
                        isVisuallyChecked = checked,
                        onCheckChange = { newCheck ->
                            GlobalScope.launch {
                                item.checkDoneWithVerifMethod(applicationContext, newCheck)

                                // Checks if the relative time/state of item has changed
                                state.value = item.state
                            }

                        },
                        isFocused = focused,
                        onFocusChange = { newFocus ->
                            if (newFocus && lastFocus != focused) {
                                lastFocus?.value = false
                                lastFocus = focused
                            }
                        },
                        deleteItemMode
                    )
                }
            }
        }
    }

    companion object {
        lateinit var appContext: Context
        val loginInfoDao: LoginInfoDao
            get() = getLocalDb(appContext).loginInfoDao()
        val agendaItemDao: AgendaItemDao
            get() = getDb(appContext).agendaItemDao()
        val recurringItemDao: RecurringItemDao
            get() = getDb(appContext).recurringItemDao()
        val recurringItemAtDateDao: RecurringItemAtDateDao
            get() = getDb(appContext).recurringItemAtDateDao()
        val storedNotificationDao: StoredNotificationDao
            get() = getDb(appContext).storedNotificationDao()

        private fun getDb(context: Context): AppDatabase {
            return AppDatabase.getInstance(context)
        }

        private fun getLocalDb(context: Context): LocalDatabase {
            return LocalDatabase.getInstance(context)
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun ErrorNotificationManagerPreview() {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("This is a simple page")
            VisualFeedbackManager.Popup()
        }

        VisualFeedbackManager.showMessage("Test error popup")
    }
}
