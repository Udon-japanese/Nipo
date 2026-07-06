package com.example.nipo.ui.postcreate

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nipo.BuildConfig
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

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

    // 近くにある「名前付きの施設」をPlaces API (New)で検索する
    suspend fun findNearbyPlaceName(latLng: LatLng, apiKey: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://places.googleapis.com/v1/places:searchNearby")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("X-Goog-Api-Key", apiKey)
                connection.setRequestProperty("X-Goog-FieldMask", "places.displayName")
                connection.doOutput = true

                val body = """
                {
                  "locationRestriction": {
                    "circle": {
                      "center": { "latitude": ${latLng.latitude}, "longitude": ${latLng.longitude} },
                      "radius": 40.0
                    }
                  },
                  "maxResultCount": 1
                }
            """.trimIndent()
                connection.outputStream.use { it.write(body.toByteArray()) }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val places = JSONObject(responseText).optJSONArray("places")
                    if (places != null && places.length() > 0) {
                        places.getJSONObject(0).getJSONObject("displayName").getString("text")
                    } else null
                } else null
            } catch (e: Exception) {
                null
            }
        }

    // タップ/ドラッグ時に呼ばれる、地名解決のメイン処理
    fun resolvePlaceName(latLng: LatLng) {
        scope.launch {
            // ① まず近くの施設名を探す（例: TRIO名駅）
            val nearbyName = findNearbyPlaceName(latLng, BuildConfig.MAPS_API_KEY)
            if (nearbyName != null) {
                selectedPlaceName = nearbyName
                return@launch
            }

            // ② 見つからなければ従来通り住所ベースにフォールバック
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
            } catch (e: Exception) {
                null
            }
        }
    }

    // ★施設名検索(イオンなど)はPlaces Autocompleteを使う
    fun search(query: String) {
        if (query.isBlank()) {
            predictions = emptyList(); return
        }
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
        val request =
            FetchPlaceRequest.builder(prediction.placeId, fields)  // ← newInstanceではなくbuilder()
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text("← 戻る") }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
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
                            .clickable { selectPrediction(prediction) }
                            .padding(12.dp)
                    )
                    Divider()
                }
            }
        }

        GoogleMap(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
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
            if (markerState.dragState == DragState.END && markerState.position != LatLng(
                    0.0,
                    0.0
                )
            ) {
                resolvePlaceName(markerState.position)
            }
        }

        OutlinedTextField(
            value = selectedPlaceName ?: "",
            onValueChange = { selectedPlaceName = it },
            label = { Text("場所の名前") },
            supportingText = { Text("分かりにくい表記の場合は、適切な場所名に変えてください") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        Button(
            onClick = { onLocationPicked(markerState.position, selectedPlaceName) },
            modifier = Modifier.padding(16.dp)
        ) { Text("この場所に決定") }
    }
}