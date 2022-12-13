package com.example.vmagardener.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.Notification
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vmagardener.*
import com.example.vmagardener.database.DatabaseHandler
import com.example.vmagardener.models.PlantModel
import com.example.vmagardener.utils.GetAddressFromLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.zxing.integration.android.IntentIntegrator
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_plant.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddPlantActivity : AppCompatActivity(), View.OnClickListener {

    /* Globale Variablen */

    /* Kalendervariablen. Aktuelle Zeitzone vom System */
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var cal = Calendar.getInstance()

    /* Für Datenspeicherung */
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mPlantDetails: PlantModel? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var qrScanIntegrator: IntentIntegrator? = null

    private var intvWaterJSON = ""
    private var intvFertJSON = ""
    private var intvCutJSON = ""


    private val months = arrayOf(
        "Januar",
        "Februar",
        "März",
        "April",
        "Mai",
        "Juni",
        "Juli",
        "August",
        "September",
        "Oktober",
        "November",
        "Dezember"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_plant)

        /* Die Toolbar war enabled und wurde nun auf eine eigene Toolbar gestellt */
        setSupportActionBar(toolbarAddPlant)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarAddPlant.setNavigationOnClickListener {
            onBackPressed()

        }

        createNotificationChannel()


        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this@AddPlantActivity)


        if (intent.hasExtra(MainActivity.PLANT_OBJECT_DETAILS)) {
            mPlantDetails = intent.getParcelableExtra(MainActivity.PLANT_OBJECT_DETAILS)
        }

        /* dateSetListener für Kalender vorbereiten */
        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()

        }
        /* Aktuelles Datum wird direkt am Anfang hinterlegt */
        updateDateInView()
        // Damit sind wir am editieren
        if (mPlantDetails != null) {
            supportActionBar?.title = "EDIT PLANT"
            etTitle.setText(mPlantDetails!!.name)
            etDescription.setText(mPlantDetails!!.description)
            etFertilize.setText(mPlantDetails!!.intvFertilize)
            etDate.setText(mPlantDetails!!.plantDate)
            etCut.setText(mPlantDetails!!.intvCut)
            etWater.setText(mPlantDetails!!.intvWater)
            etLocation.setText(mPlantDetails!!.location)
            mLatitude = mPlantDetails!!.latitude
            mLongitude = mPlantDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mPlantDetails!!.image)

            ivPlaceImage.setImageURI(saveImageToInternalStorage)

            buSave.text = "UPDATE"

        }

        etDate.setOnClickListener(this)
        etLocation.setOnClickListener(this)
        tvAddImage.setOnClickListener(this)
        tvADDQR.setOnClickListener(this)
        buSave.setOnClickListener(this)
        tvSelectCurrentLocation.setOnClickListener(this)
        setupScanner()


    }

    /*https://tutorialwing.com/implement-android-qr-code-scanner-using-zxing-library-in-kotlin/ */
    private fun setupScanner() {
        Log.i("QR1", "SetupScanner")
        qrScanIntegrator = IntentIntegrator(this@AddPlantActivity)
        qrScanIntegrator?.setOrientationLocked(true)
        qrScanIntegrator?.setBeepEnabled(false)
        qrScanIntegrator?.setRequestCode(QRCODE)


    }


    /* Um das Datum im Text zu updaten */
    private fun updateDateInView() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        etDate.setText(sdf.format(cal.time).toString())

    }

    /* Auswahl 1 von "Bild hinzufügen" - Foto aus Gallery */
    private fun choosePhotoFromGallery() {
        Dexter.withContext(this)
            .withPermissions(
                /* Prüfung ob wir die Berechtigung haben */
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            /* Listener wartet bis es akzeptiert wird, oder bereits vorhanden ist */
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.areAllPermissionsGranted()) {
                        val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )

                        startActivityForResult(galleryIntent, GALLERY)


                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationaleDialogForPermissions()
                }


            })
            /* Notwendig für die Ausführung */
            .onSameThread()
            .check()
    }

    /* Auswahl 2 von "Bild hinzufügen" - Foto mit Kamera*/
    private fun takePhotoWithCamera() {

        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA


            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.areAllPermissionsGranted()) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        @Suppress("DEPRECATION")
                        startActivityForResult(cameraIntent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    showRationaleDialogForPermissions()
                }

            }
            ).onSameThread()
            .check()
    }

    /* Wenn Berechtigung nicht vorliegt */
    private fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("Es liegen keine Berechtigungen vor")
            .setPositiveButton("Zu den Einstellungen") { _, _ ->
                try {
                    /* Einstellungen auf dem Gerät öffnen */
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    /* Paketname wird übergeben */
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()

                }


            }
            .setNegativeButton("Abbrechen") { dialog, _ ->
                dialog.dismiss()

            }.show()

    }


    // Der Pfad wird zurückgegeben und eine Bitmap eingegeben
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        // Schnittstelle
        val wrapper = ContextWrapper(applicationContext)

        // Kann nur mit der App zugegriffen werden. Ist der Pfadname
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        // Name des Bildes (zufällige Zeichenfolge als png)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Stream um die Datei speichern zu können
            val stream = FileOutputStream(file)
            // Komprimieren als JPEG
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    private fun isLocationEndabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        val task = mFusedLocationClient.lastLocation
        task.addOnSuccessListener {
            Log.i("FetchLocation", it.toString())
            if (it != null) {
                Log.i("Latitude", it.latitude.toString() + " " + it.longitude.toString())
                val addressTask =
                    GetAddressFromLatLng(this@AddPlantActivity, it.latitude, it.longitude)
                addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener {
                    override fun onAddressFound(address: String?) {
                        if (address != null) {
                            Log.i("Address in Function", address)
                        }
                        etLocation.setText(address)
                    }

                    override fun onError() {
                        Log.e("Get Address:", "Something went wrong")
                    }
                })
                addressTask.getAddress()

            }
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            /* Datumsauswahl */
            R.id.etDate -> {
                /* ruft updateDateInView() nach jeder Auswahl auf */
                DatePickerDialog(
                    this@AddPlantActivity, dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()


            }
            /* Auswahl eines Fotos */
            R.id.tvAddImage -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle(R.string.addPlantActivityAddPhoto)
                val chooseOne: String = getString(R.string.addPlantActivityAddPhotoOne)
                val chooseTwo: String = getString(R.string.addPlantActivityAddPhotoTwo)
                val pictureDialogItems = arrayOf(
                    chooseOne,
                    chooseTwo
                )
                pictureDialog.setItems(pictureDialogItems) { _, choose ->
                    when (choose) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoWithCamera()
                    }

                }
                pictureDialog.show()
            }
            /* QR-Code-Informationen einlesen */
            /*https://tutorialwing.com/implement-android-qr-code-scanner-using-zxing-library-in-kotlin/ */
            R.id.tvADDQR -> {
                Log.e("QR1: ", "QR-Code starten")
                qrScanIntegrator?.initiateScan()
            }
            R.id.buSave -> {
                when {
                    etTitle.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Bitte Name eingeben",
                        Toast.LENGTH_SHORT
                    ).show()
                    etDate.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Bitte Datum eingeben",
                        Toast.LENGTH_SHORT
                    ).show()

                    etLocation.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Bitte Ort eingeben",
                        Toast.LENGTH_SHORT
                    ).show()
                    etCut.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Bitte Schneidung eingeben",
                        Toast.LENGTH_SHORT
                    ).show()
                    etFertilize.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Bitte Düngung eingeben",
                        Toast.LENGTH_SHORT
                    ).show()
                    etWater.text.isNullOrEmpty() -> Toast.makeText(
                        this,
                        "Bitte Wasser eingeben",
                        Toast.LENGTH_SHORT
                    ).show()
                    else -> {
                        val plantModel = PlantModel(
                            mPlantDetails?.id ?: 0, /* Wird nicht übergeben an Datenbank */
                            etTitle.text.toString(),
                            saveImageToInternalStorage.toString(),
                            etDescription.text.toString(),
                            etDate.text.toString(),
                            etLocation.text.toString(),
                            mLatitude,
                            mLongitude,
                            etWater.text.toString(),
                            etCut.text.toString(),
                            etFertilize.text.toString()

                        )
                        Log.i("NOTF", "Aufruf SheduleNotification")
                        Log.i("NOTF - Water", plantModel.intvWater.toString())
                        Log.i("NOTF - Cut", plantModel.intvCut.toString())
                        Log.i("NOTF - Fert", plantModel.intvFertilize.toString())
                        scheduleNotification(plantModel)
                        Log.i("NOTF", "Nach SheduleNotification")
                        /* Vor dem Save wird noch geprüft ob es Datümer gibt,
                         die als Erinnerung eingespeichert werden müssen
                         plantModel enthält bereits alle notwendigen Informationen
                        */


                        val dbHandler = DatabaseHandler(this)
                        if (mPlantDetails == null) {
                            val addPlant = dbHandler.addPlant(plantModel)

                            if (addPlant > 0) {
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(
                                    this,
                                    "Pflanze erfolgreich hinzugefügt",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        } else {
                            val updatePlant = dbHandler.updatePlant(plantModel)

                            if (updatePlant > 0) {
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(
                                    this,
                                    "Pflanze erfolgreich angepasst",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }


                    }
                }
            }
            R.id.tvSelectCurrentLocation -> {
                if (!isLocationEndabled()) {
                    Toast.makeText(this, "Bitte GPS anschalten!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                            if (p0!!.areAllPermissionsGranted()) {
                                Log.i("Current Location", "Erfüllt")
                                fetchLocation()
                                Log.i("Current Location", "Nach NewLocation")


                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<PermissionRequest>?,
                            p1: PermissionToken?
                        ) {
                            showRationaleDialogForPermissions()
                        }

                    }).onSameThread()
                        .check()
                }
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        @Suppress("DEPRECATION")
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage =
                            saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image: ", "Path: $saveImageToInternalStorage")
                        /* Das Bild soll nun auch im ImageView angezeigt werden */
                        ivPlaceImage!!.setImageBitmap(selectedImageBitmap)

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddPlantActivity,
                            getString(R.string.addPlantActivityAddPhotoError),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                }
            } else if (requestCode == CAMERA) {
                val photoBitmap: Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(photoBitmap)
                Log.e("Saved Image: ", "Path: $saveImageToInternalStorage")
                /* Beispielausgabe :

                Saved Image:: Path: /data/user/0/com.example.vmagardener/app_PlantsImages/b963386c-a5e9-405f-9a85-4412252d447e.jpg

                 */


                ivPlaceImage!!.setImageBitmap(photoBitmap)

            } else if (requestCode == QRCODE) {
                Log.i("QR1", "Result finished")
                Log.i("QR1", "RESULT_CODE" + Activity.RESULT_OK.toString())
                Log.i("QR1", "REQUEST CODE $requestCode")
                Log.i("QR1", data.toString())
                Log.i("QR1", result.contents)
                if (result != null) {
                    if (result.contents == null) {
                        Toast.makeText(this, "Result not found", Toast.LENGTH_LONG).show()
                    } else {
                        try {
                            val obj = JSONObject(result.contents)
                            Log.i("QR1", obj.getString("name"))
                            Log.i("QR1", obj.getString("water"))
                            Log.i("QR1", obj.getString("fert"))
                            Log.i("QR1", obj.getString("cut"))

                            // Globale Speicherung für Notification
                            intvWaterJSON = obj.getString("water")
                            intvFertJSON = obj.getString("fert")
                            intvCutJSON = obj.getString("cut")
                            Log.i("NOTF - OnActivityResult", "intvWaterJSON $intvWaterJSON")
                            Log.i("NOTF - OnActivityResult", "intvFertJSON $intvFertJSON")
                            Log.i("NOTF - OnActivityResult", "intvCutJSON $intvCutJSON")

                            // Name der Pflanze
                            etTitle.setText(obj.getString("name"))
                            // IntervallWässerung
                            etWater.setText(calcIntv(obj.getString("water"), "water"))
                            // Intervall Düngung
                            etFertilize.setText(calcIntv(obj.getString("fert"), "fert"))
                            // Intervall Schneiden
                            etCut.setText(calcIntv(obj.getString("cut"), "cut"))

                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()

                        }

                    }
                }


            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun calcIntv(xCode: String, type: String): String {
        var calcString = ""
        if (type == "water") {
            var xCount = 0
            for (i in xCode.indices) {
                if (xCode[i] == 'X') {
                    xCount++
                }
            }
            calcString = "$xCount mal pro Woche"
        } else {
            for (i in xCode.indices) {
                Log.i("QR1", "INDEX $i")
                Log.i("QR1", xCode[i].toString())
                if (xCode[i] == 'X') {
                    Log.i("QR1", "X GEFUNDEN")
                    calcString += months[i] + "/"
                    Log.i("QR1", "NEUER STRING $calcString")

                }
            }
            // Letzter Slash kann weg
            calcString = calcString.dropLast(1)
        }

        return calcString
    }

    private fun createNotificationChannel() {
        val name = "Plant Channel"
        val desc = "Erinnerungen für die Pflanzen"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleNotification(plantModel: PlantModel) {
        val title = plantModel.name
        val intent = Intent(applicationContext, Notification::class.java)
        var message: String


        intent.putExtra(titleExtra, title)


        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //TODO Muss im Laufe des Projekts dynamisch eingerichtet werden. Jedoch zum Test statisch
        val minute = 0
        val hour = 8
        var day = 1
        val year = 2022
        var month: Int
        var time: Long = 0

        Log.i("NOTF - Schedule Cut", plantModel.intvCut.toString())
        Log.i("NOTF - Schedule Fert", plantModel.intvFertilize.toString())
        Log.i("NOTF - Schedule Water", plantModel.intvWater.toString())


        for (i in intvCutJSON.indices) {
            month = i + 1
            if (intvCutJSON[i] == 'X') {
                Log.i("NOTF - Schedule Cut", "X Gefunden")
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, hour, minute)
                time = calendar.timeInMillis
                Log.i("NOTF - Schedule Cut - Time", time.toString())
                message = "$title muss geschnitten werden"
                intent.putExtra(messageExtra, message)

                val pendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    notificationID,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }

        }
        for (i in intvFertJSON.indices) {
            day = i + 1
            month = 1
            if (intvFertJSON[i] == 'X') {
                Log.i("NOTF - Schedule Fert", "X Gefunden")
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, hour, minute)
                time = calendar.timeInMillis
                Log.i("NOTF - Schedule Fert - Time", time.toString())
                message = "$title muss gedüngt werden"
                intent.putExtra(messageExtra, message)

                val pendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    notificationID,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }
        }
        for (i in intvWaterJSON.indices) {
            month = i + 1
            if (intvWaterJSON[i] == 'X') {
                Log.i("NOTF - Schedule Water", "X Gefunden")
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, hour, minute)
                time = calendar.timeInMillis
                Log.i("NOTF - Schedule Water - Time", time.toString())
            }

        }
    }

    private fun showAlert(time: Long, title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle("Notification Scheduled")
            .setMessage(
                "Name: " + title +
                        "\nErinnerung gesetzt für : " + message

            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }


    /*https://developer.android.com/training/location/change-location-settings*/


    companion object {
        // Codes für Intent onActivityResult
        private const val GALLERY = 1
        private const val CAMERA = 2

        // QR-COde
        private const val QRCODE = 3

        // Ordnername an dem die Bilder abgespeichert werden
        private const val IMAGE_DIRECTORY = "PlantsImages"

    }

}