package com.example.bluetoothpractice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.example.bluetoothpractice.ui.theme.BluetoothPracticeTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BluetoothPracticeTheme {
                val permissions = rememberMultiplePermissionsState(
                    permissions = listOf(
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                    )
                )
                val owner = LocalLifecycleOwner.current
                val context = LocalContext.current
                val bluetoothState = rememberBluetoothState(context = context, owner = owner)
                val discoveryState = bluetoothState.discoveryState.collectAsState()

                LaunchedEffect(key1 = bluetoothState.bondedDevice.size) {
                    Log.e(
                        "BONDED",
                        bluetoothState.bondedDevice.toList().toString()
                    )
                }

                LaunchedEffect(key1 = bluetoothState.discoveredDevice.size) {
                    Log.e(
                        "DISCOVERED",
                        bluetoothState.discoveredDevice.toList().toString()
                    )
                }

                LaunchedEffect(key1 = discoveryState.value) {
                    Log.e(
                        "STATE",
                        bluetoothState.discoveryState.value.name
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .padding(it),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    permissions.launchMultiplePermissionRequest()
                                }
                            ) {
                                Text(text = "REQUEST PERMISSION")
                            }

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    bluetoothState.startDiscovery()
                                }
                            ) {
                                Text(text = "DISCOVER")
                            }

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    bluetoothState.updateBondedDevice()
                                }
                            ) {
                                Text(text = "UPDATE BONDED")
                            }

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    //TODO
                                }
                            ) {
                                Text(text = "LOG")
                            }
                        }
                    }
                }
            }
        }
    }
}