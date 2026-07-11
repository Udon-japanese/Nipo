package com.example.nipo.ui.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nipo.data.PostTag
import com.example.nipo.ui.common.NipoSwitch
import com.example.nipo.ui.common.PrimaryButton
import com.example.nipo.ui.home.HomeViewModel
import com.example.nipo.ui.theme.NeutralAccent
import com.example.nipo.ui.theme.NeutralBg
import com.example.nipo.ui.theme.NeutralMutedText
import com.example.nipo.ui.theme.SosGradientEnd
import com.example.nipo.ui.theme.TipsAccentLight
import androidx.compose.runtime.collectAsState

@Composable
fun FilterScreen(navController: NavHostController, onBack: () -> Unit) {
    val homeViewModel: HomeViewModel = viewModel(
        viewModelStoreOwner = navController.getBackStackEntry("home"),
    )
    val filterTips by homeViewModel.filterTips.collectAsState()
    val filterSos by homeViewModel.filterSos.collectAsState()
    val filterTags by homeViewModel.filterTags.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Text("←", color = NeutralAccent, style = MaterialTheme.typography.titleLarge)
            }
            Text("表示フィルター", color = NeutralAccent, style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
        ) {
            item {
                Spacer(Modifier.height(6.dp))
                ToggleRow(
                    label = "置き手紙を表示",
                    labelColor = TipsAccentLight,
                    checked = filterTips,
                    onCheckedChange = { homeViewModel.setFilterTips(it) },
                )
                Spacer(Modifier.height(14.dp))
                ToggleRow(
                    label = "困りごとを表示",
                    labelColor = SosGradientEnd,
                    checked = filterSos,
                    onCheckedChange = { homeViewModel.setFilterSos(it) },
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "置き手紙のタグで絞り込み",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeutralMutedText,
                )
                Spacer(Modifier.height(8.dp))
            }
            items(PostTag.entries) { tag ->
                val checked = tag in filterTags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .clickable { homeViewModel.toggleFilterTag(tag) }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(16.dp)
                            .border(BorderStroke(2.dp, TipsAccentLight), RoundedCornerShape(4.dp))
                            .background(if (checked) TipsAccentLight else Color.Transparent, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (checked) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                            )
                        }
                    }
                    Text(tag.displayName, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4A3A22))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            PrimaryButton(text = "適用する", onClick = onBack)
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    labelColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = labelColor,
            modifier = Modifier.weight(1f),
        )
        NipoSwitch(checked = checked, onCheckedChange = onCheckedChange, checkedColor = labelColor)
    }
}
