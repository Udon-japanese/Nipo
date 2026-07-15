package com.example.nipo.ui.home

import android.Manifest
import android.annotation.SuppressLint
import androidx.core.content.edit
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nipo.data.GeoUtils
import com.example.nipo.data.PostTag
import com.example.nipo.ui.common.PrimaryButton
import com.example.nipo.ui.common.SecondaryButton
import com.example.nipo.ui.common.TeardropMapPin
import com.example.nipo.ui.theme.NeutralBg
import com.example.nipo.ui.theme.NeutralMutedText
import com.example.nipo.ui.theme.NeutralText
import com.example.nipo.ui.theme.SosGradientEnd
import com.example.nipo.ui.theme.style
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val DefaultCenter = LatLng(35.681236, 139.767125) // 位置情報が取得できない場合のフォールバック(東京駅)
private const val PREFS_NAME = "home_map_prefs"
private const val KEY_LAST_LAT = "last_lat"
private const val KEY_LAST_LNG = "last_lng"

private fun loadLastLatLng(context: android.content.Context): LatLng? {
    val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    if (!prefs.contains(KEY_LAST_LAT) || !prefs.contains(KEY_LAST_LNG)) return null
    return LatLng(prefs.getFloat(KEY_LAST_LAT, 0f).toDouble(), prefs.getFloat(KEY_LAST_LNG, 0f).toDouble())
}

private fun saveLastLatLng(context: android.content.Context, latLng: LatLng) {
    context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE).edit {
        putFloat(KEY_LAST_LAT, latLng.latitude.toFloat())
        putFloat(KEY_LAST_LNG, latLng.longitude.toFloat())
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onCreatePost: () -> Unit,
    onOpenPost: (String) -> Unit,
    onCreateSos: () -> Unit,
    onOpenSos: (String) -> Unit,
    onOpenFilter: () -> Unit,
    navController: NavHostController,
    placesClient: PlacesClient,
    viewModel: HomeViewModel = viewModel(),
) {
    val posts by viewModel.posts.collectAsState()
    val sosPosts by viewModel.sosPosts.collectAsState()
    val filterTips by viewModel.filterTips.collectAsState()
    val filterSos by viewModel.filterSos.collectAsState()
    val filterTags by viewModel.filterTags.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var placePredictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var placeSessionToken by remember { mutableStateOf(AutocompleteSessionToken.newInstance()) }
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPositionState = rememberCameraPositionState {
        // 前回表示した位置をキャッシュから復元しておくことで、起動直後に世界地図から
        // 現在地までスクロールする違和感のある動きを防ぐ
        position = CameraPosition.fromLatLngZoom(loadLastLatLng(context) ?: DefaultCenter, 15f)
    }
    val scope = rememberCoroutineScope()
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }
    var hasPlacedInitialCamera by remember { mutableStateOf(false) }

    fun moveCameraTo(latLng: LatLng, animate: Boolean = true) {
        saveLastLatLng(context, latLng)
        scope.launch {
            if (animate) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                    durationMs = 600,
                )
            } else {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
            }
        }
    }

    fun recenterToCurrentLocation(moveCamera: Boolean = true) {
        if (!locationPermission.status.isGranted) return
        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLatLng = LatLng(location.latitude, location.longitude)
                // 初回はキャッシュ済みの位置から近いことが多いのでアニメーションせず即座に合わせる
                if (moveCamera) moveCameraTo(currentLatLng!!, animate = hasPlacedInitialCamera)
                hasPlacedInitialCamera = true
            } else {
                // 端末に直近の位置情報キャッシュがない場合、明示的に現在地取得を試みる
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { freshLocation ->
                        freshLocation?.let {
                            currentLatLng = LatLng(it.latitude, it.longitude)
                            if (moveCamera) moveCameraTo(currentLatLng!!, animate = hasPlacedInitialCamera)
                            hasPlacedInitialCamera = true
                        }
                    }
            }
        }
    }

    // 一度きりのスナップショット読み取り。reactiveなStateFlow経由だと、使用後にnullへ
    // クリアする操作自体が再度Effectを発火させ、現在地への復帰処理を呼び直してしまうため。
    LaunchedEffect(Unit) {
        val handle = navController.currentBackStackEntry?.savedStateHandle
        val lat = handle?.get<Double>("focusLat")
        val lng = handle?.get<Double>("focusLng")
        if (lat != null && lng != null) {
            hasPlacedInitialCamera = true
            moveCameraTo(LatLng(lat, lng), animate = false)
            handle.remove<Double>("focusLat")
            handle.remove<Double>("focusLng")
            // 距離フィルタ用の現在地は取得するが、カメラは投稿の位置を優先して動かさない
            recenterToCurrentLocation(moveCamera = false)
        } else {
            recenterToCurrentLocation(moveCamera = true)
        }
    }

    val nearbySos = remember(sosPosts, currentLatLng, filterSos) {
        if (!filterSos) return@remember emptyList()
        val center = currentLatLng ?: return@remember emptyList()
        sosPosts.filter { sos ->
            val geo = sos.location ?: return@filter false
            GeoUtils.distanceMeters(center.latitude, center.longitude, geo.latitude, geo.longitude) <= 500
        }
    }

    val visiblePosts = remember(posts, filterTips, filterTags) {
        if (!filterTips) return@remember emptyList()
        posts.filter { post ->
            val tag = runCatching { PostTag.valueOf(post.label) }.getOrNull()
            tag == null || tag in filterTags
        }
    }

    fun searchPlaces(query: String) {
        if (query.isBlank()) {
            placePredictions = emptyList()
            return
        }
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setCountries("JP")
            .setSessionToken(placeSessionToken)
            .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { placePredictions = it.autocompletePredictions }
            .addOnFailureListener { placePredictions = emptyList() }
    }

    fun selectPlacePrediction(prediction: AutocompletePrediction) {
        val fields = listOf(Place.Field.LAT_LNG, Place.Field.NAME)
        val request = FetchPlaceRequest.builder(prediction.placeId, fields)
            .setSessionToken(placeSessionToken)
            .build()
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                response.place.latLng?.let { moveCameraTo(it) }
                placePredictions = emptyList()
                searchQuery = ""
                placeSessionToken = AutocompleteSessionToken.newInstance()
            }
    }

    LaunchedEffect(searchQuery) {
        delay(300)
        searchPlaces(searchQuery)
    }

    Column(Modifier.fillMaxSize().background(NeutralBg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = NeutralMutedText, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text("施設名で検索", color = NeutralMutedText, style = MaterialTheme.typography.bodySmall)
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = NeutralText),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(color = Color.White, shape = RoundedCornerShape(12.dp)) {
                IconButton(onClick = onOpenFilter, modifier = Modifier.size(42.dp)) {
                    FilterIcon(tint = NeutralText)
                }
            }
        }
        if (placePredictions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
            ) {
                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    items(placePredictions) { prediction ->
                        Text(
                            text = prediction.getFullText(null).toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectPlacePrediction(prediction) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                        )
                        HorizontalDivider(color = NeutralBg)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted),
            ) {
                visiblePosts.forEach { post ->
                    val geo = post.geo ?: return@forEach
                    key(post.id) {
                        val tag = runCatching { PostTag.valueOf(post.label) }.getOrNull()
                        val markerState = remember(post.id, geo.latitude, geo.longitude) {
                            com.google.maps.android.compose.MarkerState(position = LatLng(geo.latitude, geo.longitude))
                        }
                        MarkerComposable(
                            state = markerState,
                            onClick = { onOpenPost(post.id); true },
                        ) {
                            TeardropMapPin(color = tag?.style?.bg ?: MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                nearbySos.forEach { sos ->
                    val geo = sos.location ?: return@forEach
                    key("sos_${sos.id}") {
                        val markerState = remember(sos.id, geo.latitude, geo.longitude) {
                            com.google.maps.android.compose.MarkerState(position = LatLng(geo.latitude, geo.longitude))
                        }
                        MarkerComposable(
                            state = markerState,
                            onClick = { onOpenSos(sos.id); true },
                        ) {
                            PulsingSosPin(color = SosGradientEnd)
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { recenterToCurrentLocation() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .size(44.dp),
                containerColor = Color.White,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "現在地に戻る", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            PrimaryButton(text = "置き手紙", onClick = onCreatePost, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(14.dp))
            SecondaryButton(text = "困りごと", onClick = onCreateSos, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FilterIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(18.dp)) {
        val lineYs = listOf(size.height * 0.15f, size.height * 0.5f, size.height * 0.85f)
        val knobXs = listOf(size.width * 0.65f, size.width * 0.35f, size.width * 0.55f)
        val strokeWidth = size.height * 0.09f
        lineYs.forEach { y ->
            drawLine(
                color = tint,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
        lineYs.forEachIndexed { index, y ->
            drawCircle(color = tint, radius = size.height * 0.12f, center = androidx.compose.ui.geometry.Offset(knobXs[index], y))
        }
    }
}
