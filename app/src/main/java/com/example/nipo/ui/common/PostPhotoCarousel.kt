package com.example.nipo.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nipo.ui.theme.PhotoSlotShape

@Composable
fun PostPhotoCarousel(
    photoUrls: List<String>,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 150.dp,
) {
    if (photoUrls.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { photoUrls.size })
    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(PhotoSlotShape),
    ) { page ->
        AsyncImage(
            model = photoUrls[page],
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
        )
    }
}
