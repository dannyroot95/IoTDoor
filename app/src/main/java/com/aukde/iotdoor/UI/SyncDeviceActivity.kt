package com.aukde.iotdoor.UI

import android.Manifest
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PatternMatcher
import android.telephony.TelephonyManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aukde.iotdoor.Adapters.WifiNetworkAdapter
import com.aukde.iotdoor.Providers.AuthenticationProvider
import com.aukde.iotdoor.Providers.UserProvider
import com.aukde.iotdoor.databinding.ActivitySyncDeviceBinding
import com.aukde.iotdoor.databinding.DialogScanWifiBinding
import com.aukde.iotdoor.databinding.DialogWifiConnectionBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.common.api.ResolvableApiException
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder

class SyncDeviceActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySyncDeviceBinding
    var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiNetworkAdapter: WifiNetworkAdapter
    private val REQUEST_CODE_PERMISSIONS = 123
    private val REQUEST_CHECK_SETTINGS = 0x1
    private var alertDialog: AlertDialog? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var dialogPasswordSSID: Dialog
    private lateinit var dialogSSID: Dialog
    private lateinit var bindingSSID : DialogScanWifiBinding
    private lateinit var bindingPasswordSSID : DialogWifiConnectionBinding
    var resultQr : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncDeviceBinding.inflate(layoutInflater)
        bindingSSID = DialogScanWifiBinding.inflate(layoutInflater)
        bindingPasswordSSID = DialogWifiConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val wifiItemClickListener = WifiItemClickListener()
        wifiNetworkAdapter = WifiNetworkAdapter(mutableListOf(), wifiItemClickListener)

        dialogSSID = Dialog(this)
        dialogSSID.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogSSID.setContentView(bindingSSID.root)
        dialogSSID.setCancelable(false)
        val window: Window = dialogSSID.window!!
        window.setLayout(950, 1400)

        dialogPasswordSSID = Dialog(this)
        dialogPasswordSSID.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogPasswordSSID.setContentView(bindingPasswordSSID.root)
        dialogPasswordSSID.setCancelable(false)
        val window2: Window = dialogPasswordSSID.window!!
        window2.setLayout(950, 950)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        recyclerView = bindingSSID.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = wifiNetworkAdapter
        // Crear una instancia de WifiItemClickListener y pasarla como listener

        binding.btnQr.setOnClickListener {
            //initScanner()
            if (isWifiEnabled()) {
                if (!isMobileDataActive()) {
                    //showPermissionExplanationIfNeeded()
                    //scanWifi()
                    initScanner()
                } else {
                    showAlertDialog("Datos móviles !", "Debes desactivar tus datos móviles")
                }
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Wifi !")
                    .setCancelable(false)
                    .setMessage("Se deben activar el WiFi y desactivar los datos móviles.")
                    .setPositiveButton("Ok") { dialog, which ->

                    }
                    .create().show()
            }
        }

        showPermissionExplanationIfNeeded()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            } else {
                val qr = result.contents.toString()
                resultQr = qr
                connectToWiFi("xFa2.22xP","Athor")
                //
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Athor v1.0")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }
    private fun scanWifi() {
        registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
        //Toast.makeText(this, "Escaneando redes ...", Toast.LENGTH_SHORT).show()
    }
    private fun saveDeviceInDatabase(qr : String){
        mDatabase.child("devices").child(qr).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val root = snapshot.child("hasRoot").value.toString()
                    if(root == "null"){
                        UserProvider().updateDeviceAndRoot(qr,"root")
                        UserProvider().updateDevice(qr,AuthenticationProvider().getId())
                        val sharedPreferences= getSharedPreferences("cache", Context.MODE_PRIVATE)
                        val editorDevice = sharedPreferences.edit()
                        val editorTypeUser = sharedPreferences.edit()
                        editorDevice.putString("keyDevice",qr)
                        editorDevice.apply()
                        editorTypeUser.putString("keyTypeUser","root")
                        editorTypeUser.apply()
                        val intent = Intent(this@SyncDeviceActivity, PasswordActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }else{
                        UserProvider().updateOnlyDevice(qr)
                        val sharedPreferences= getSharedPreferences("cache", Context.MODE_PRIVATE)
                        val editorDevice = sharedPreferences.edit()
                        editorDevice.putString("keyDevice",qr.toString())
                        editorDevice.apply()
                        val intent = Intent(this@SyncDeviceActivity, PasswordActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                }else{
                    Toast.makeText(this@SyncDeviceActivity, "No existe dispositivo!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ContextCompat.checkSelfPermission(this@SyncDeviceActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                dialogSSID.show()
                val results = wifiManager.scanResults
                val filteredResults = results
                    .filterNot { it.SSID.isBlank() }
                    .distinctBy { it.SSID }

                wifiNetworkAdapter.updateData(filteredResults)
            } else {
                Toast.makeText(this@SyncDeviceActivity, "Location permission not granted. Can't scan for WiFi networks.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkIfGpsEnabled() {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        /*
        task.addOnSuccessListener {
            //scanWifi()
            Toast.makeText(this,"xd1",Toast.LENGTH_SHORT).show()
        }

        task.addOnCompleteListener {
            Toast.makeText(this,"xd2",Toast.LENGTH_SHORT).show()
        }*/

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Toast.makeText(this, "Error al intentar resolver la configuración de ubicación.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun proceedWithFunctionality() {
        checkIfGpsEnabled()
    }

    private fun showPermissionExplanationIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si necesitas mostrar una explicación, puedes usar un AlertDialog aquí
            AlertDialog.Builder(this)
                .setTitle("Se requiere permiso de ubicación")
                .setCancelable(false)
                .setMessage("Debe aceptar todos los permisos de ubicación para sincronizar el dispositivo.")
                .setPositiveButton("Ok") { dialog, which ->
                    checkAndRequestPermissions()
                }
                .create().show()
        } else {
            checkAndRequestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    checkIfGpsEnabled()
                    //scanWifi()
                } else {
                    Toast.makeText(this, "Permission denied, can't scan WiFi.", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "Permission denied, can't scan WiFi.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded, REQUEST_CODE_PERMISSIONS)
            //proceedWithFunctionality()
        } else {
            proceedWithFunctionality()
        }
    }
    override fun onBackPressed() {
        val sharedPreferencesDevice = this.getSharedPreferences("cache", Context.MODE_PRIVATE)
        val id = sharedPreferencesDevice.getString("keyDevice", "")!!
        if(id != ""){
            finish()
        }
    }

    private fun isWifiEnabled(): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }
    private fun isMobileDataActive(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.dataState == TelephonyManager.DATA_CONNECTED
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setCancelable(false)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            if (isMobileDataActive()) {
                showAlertDialog("Conexión de datos móviles", "Tienes conexión de datos móviles activa")
            }else{
                scanWifi()
                dialog.dismiss()
                alertDialog = null
            }
        }
        alertDialog = alertDialogBuilder.create()
        alertDialog?.setOnDismissListener {
            if (!isMobileDataActive()) {
                alertDialog?.dismiss()
                alertDialog = null
            }
        }
        alertDialog?.show()
    }

    private inner class WifiItemClickListener : WifiNetworkAdapter.OnWifiItemClickListener {
        override fun onWifiItemClicked(ssid: String) {
            //val param1 = ssid
            //val param2 = "aukde123#"
            //AQUI MOSTRAR EL DIALOG PARA INTRODUCIR EL PASSWORD
            //sendGETRequest(param1, param2)
            //Toast.makeText(this@MainActivity,ssid,Toast.LENGTH_SHORT).show()
            showWifiConnectDialog(ssid)
        }
    }

    private fun showWifiConnectDialog(ssid: String) {
        // Infla el layout para el diálogo.
        dialogPasswordSSID.show()
        // Configura los elementos del diálogo.
        bindingPasswordSSID.tvWifiName.text = ssid

        // Configura el CheckBox para mostrar u ocultar la contraseña.
        bindingPasswordSSID.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                bindingPasswordSSID.etWifiPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                bindingPasswordSSID.etWifiPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            bindingPasswordSSID.etWifiPassword.setSelection(bindingPasswordSSID.etWifiPassword.text!!.length)
        }

        // Configura el botón de conectar.
        bindingPasswordSSID.btnConnect.setOnClickListener {
            val password = bindingPasswordSSID.etWifiPassword.text.toString()
            dialogPasswordSSID.dismiss() // Cierra el diálogo después de conectar.
            if(password != ""){
                Toast.makeText(this,"SSID: ${ssid} - Paasword : ${password} - QR : ${resultQr}",Toast.LENGTH_SHORT).show()
                sendGETRequest(ssid,password)
                //dialogPasswordSSID.dismiss()
                finish()
            }else{
                Toast.makeText(this,"Ingrese una contraseña",Toast.LENGTH_SHORT).show()
            }
        }

        // Configura el botón de cancelar.
        bindingPasswordSSID.btnCancel.setOnClickListener {
            dialogPasswordSSID.dismiss() // Simplemente cierra el diálogo.
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectToWiFi(pin: String, ssid:String) {
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as
                    ConnectivityManager
        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pin)
            .setSsidPattern(PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX))
            .build()
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            //.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Toast.makeText(this@SyncDeviceActivity,"Dispositivo Conectado",Toast.LENGTH_SHORT).show()
                if (!isMobileDataActive()) {
                    scanWifi()
                }else{
                    showAlertDialog("Conexión de datos móviles", "Tienes conexión de datos móviles activa")
                }

            }

            override fun onUnavailable() {
                Toast.makeText(this@SyncDeviceActivity,"Error al vincular dispositivo",Toast.LENGTH_SHORT).show()
                super.onUnavailable()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Toast.makeText(this@SyncDeviceActivity,"Fuera de rango",Toast.LENGTH_SHORT).show()
            }
        }
        connectivityManager.requestNetwork(request, networkCallback)
    }

    fun sendGETRequest(param1: String, param2: String) {
        val baseUrl = "http://192.168.4.1/"
        val encodedParam1 = URLEncoder.encode(param1, "UTF-8")
        val encodedParam2 = URLEncoder.encode(param2, "UTF-8")
        val url = "$baseUrl?param1=$encodedParam1&param2=$encodedParam2"

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                println("Respuesta del servidor: $responseBody")
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Error al realizar la solicitud: ${e.message}")
            }
        })
    }

}
