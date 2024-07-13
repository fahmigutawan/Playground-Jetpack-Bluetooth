package com.example.bluetoothpractice

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow

class BluetoothHandler(
    private val context: Context,
    private val owner: LifecycleOwner,
    private val adapter: BluetoothAdapter,
    bondedDevice: SnapshotStateList<BluetoothDevice>,
    discoveredDevice: SnapshotStateList<BluetoothDevice>
) {
    val bondedDevice: SnapshotStateList<BluetoothDevice> = bondedDevice
    val discoveredDevice: SnapshotStateList<BluetoothDevice> = discoveredDevice
    val discoveryState = MutableStateFlow(BluetoothDiscoveryState.NOT_DISCOVERY_YET)
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    discoveryState.value = BluetoothDiscoveryState.ON_DICOVERY
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    discoveryState.value = BluetoothDiscoveryState.DISCOVERY_FINISH
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    device?.let { deviceNotNull ->
                        if(!discoveredDevice.any { it.address == deviceNotNull.address }){
                            discoveredDevice.add(deviceNotNull)
                        }
                    }
                }
            }
        }
    }
    private val lifecycleObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when(event){
                Lifecycle.Event.ON_RESUME -> {
                    context.registerReceiver(
                        receiver,
                        IntentFilter().apply {
                            addAction(BluetoothDevice.ACTION_FOUND)
                            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                        }
                    )
                }
                Lifecycle.Event.ON_PAUSE -> {
                    adapter.cancelDiscovery()
                    context.unregisterReceiver(receiver)
                }
                else -> {}
            }
        }
    }

    init {
        owner.lifecycle.addObserver(lifecycleObserver)
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery() {
        adapter.startDiscovery()
    }

    @RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
    fun stopDiscovery() {
        adapter.cancelDiscovery()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun updateBondedDevice() {
        if (adapter.bondedDevices != null) {
            bondedDevice.clear()
            bondedDevice.addAll(adapter.bondedDevices)
        }
    }
}

@SuppressLint("ComposableNaming")
@RequiresPermission(android.Manifest.permission.BLUETOOTH_SCAN)
@Composable
fun rememberBluetoothState(
    context: Context,
    owner: LifecycleOwner
): BluetoothHandler {
    val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    val adapter = bluetoothManager.adapter

    DisposableEffect(key1 = Unit) {
        onDispose {
            adapter.cancelDiscovery()
        }
    }

    return rememberSaveable(
        saver = mapSaver(
            save = {
                buildMap {
                    "bonded" to it.bondedDevice
                    "discovered" to it.discoveredDevice
                }
            },
            restore = {
                BluetoothHandler(
                    context,
                    owner,
                    adapter,
                    (it["bonded"] as SnapshotStateList<BluetoothDevice>?) ?: mutableStateListOf(),
                    (it["discovered"] as SnapshotStateList<BluetoothDevice>?)
                        ?: mutableStateListOf()
                )
            }
        )
    ) {
        BluetoothHandler(
            context,
            owner,
            adapter,
            mutableStateListOf(),
            mutableStateListOf()
        )
    }
}

enum class BluetoothDiscoveryState {
    NOT_DISCOVERY_YET,
    ON_DICOVERY,
    DISCOVERY_FINISH
}