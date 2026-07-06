package com.example.nipo.ui.postcreate

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.Locale
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapPickerScreen(
    placesClient: PlacesClient,
    onLocationPicked: (LatLng, placeName: String?) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler { onCancel() }

    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLatLng by remember { mutableStateOf(LatLng(35.1815, 136.9066)) }
    var selectedPlaceName by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    // セッショントークンは「検索開始〜場所確定」まで使い回す
    var sessionToken by remember { mutableStateOf(AutocompleteSessionToken.newInstance()) }
    val scope = rememberCoroutineScope()
    val markerState = rememberMarkerState()

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isGranted) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { currentLatLng = LatLng(it.latitude, it.longitude) }
            }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng, 16f)
    }
    LaunchedEffect(currentLatLng) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 16f)
    }

    // ★「33」のような番地だけの結果を弾く
    fun resolvePlaceName(latLng: LatLng) {
        scope.launch {
            selectedPlaceName = try {
                @Suppress("DEPRECATION")
                val addr = Geocoder(context, Locale.JAPAN)
                    .getFromLocation(latLng.latitude, latLng.longitude, 1)
                    ?.firstOrNull()
                addr?.let {
                    val feature = it.featureName
                    when {
                        feature != null && !feature.matches(Regex("^[0-9\\-]+$")) -> feature
                        !it.thoroughfare.isNullOrBlank() -> "${it.locality.orEmpty()}${it.thoroughfare}"
                        !it.subLocality.isNullOrBlank() -> "${it.locality.orEmpty()}${it.subLocality}"
                        else -> it.getAddressLine(0)
                    }
                }
            } catch (e: Exception) { null }
        }
    }

    // ★施設名検索(イオンなど)はPlaces Autocompleteを使う
    fun search(query: String) {
        if (query.isBlank()) { predictions = emptyList(); return }
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("JP")
            .setSessionToken(sessionToken)   // ← 追加
            .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { predictions = it.autocompletePredictions }
            .addOnFailureListener { predictions = emptyList() }
    }

    fun selectPrediction(prediction: AutocompletePrediction) {
        val fields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        val request = FetchPlaceRequest.builder(prediction.placeId, fields)  // ← newInstanceではなくbuilder()
            .setSessionToken(sessionToken)
            .build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                response.place.latLng?.let { latLng ->
                    markerState.position = latLng
                    selectedPlaceName = response.place.name
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 17f)
                }
                predictions = emptyList()
                searchQuery = response.place.name ?: ""
                sessionToken = AutocompleteSessionToken.newInstance()
            }
    }

    LaunchedEffect(searchQuery) {
        delay(300)
        search(searchQuery)
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text("← 戻る") }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                label = { Text("施設名・住所で検索（例: イオン）") },
                singleLine = true
            )
        }
        if (predictions.isNotEmpty()) {
            LazyColumn(Modifier.heightIn(max = 200.dp)) {
                items(predictions) { prediction ->
                    Text(
                        text = prediction.getFullText(null).toString(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {selectPrediction(prediction)}
                            .padding(12.dp)
                    )
                    Divider()
                }
            }
        }

        GoogleMap(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted),
            onMapClick = { latLng ->
                markerState.position = latLng
                resolvePlaceName(latLng)   // タップした時は直接呼ぶ
            }
        ) {
            Marker(state = markerState, draggable = true)
        }

        // ドラッグでピンを動かし終えた時だけ再取得する（検索で選んだ時は上書きしない）
        LaunchedEffect(markerState.dragState) {
            if (markerState.dragState == DragState.END && markerState.position != LatLng(0.0, 0.0)) {
                resolvePlaceName(markerState.position)
            }
        }

        selectedPlaceName?.let {
            Text("選択中: $it", modifier = Modifier.padding(8.dp))
        }

        Button(
            onClick = { onLocationPicked(markerState.position, selectedPlaceName) },
            modifier = Modifier.padding(16.dp)
        ) { Text("この場所に決定") }
    }
}