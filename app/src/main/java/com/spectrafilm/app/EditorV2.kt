/* Spektrafilm Android — fixed offline film reference gallery. GPLv3. */
package com.spectrafilm.app

import android.graphics.BitmapFactory
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** This component has no engine reference. Scrolling can only read bundled JPEGs. */
@Composable
internal fun FilmReferenceGallery(groups: List<DropdownGroup>, selectedId: String, onSelect: (String) -> Unit) {
    val ctx = LocalContext.current
    val prefs = remember(ctx) { ctx.getSharedPreferences("film_reference_browser_v2", 0) }
    var favourites by remember { mutableStateOf(prefs.getStringSet("favourites", emptySet())!!.toSet()) }
    var recent by remember { mutableStateOf(prefs.getString("recent", "")!!.split('|').filter(String::isNotBlank)) }
    var filter by remember { mutableStateOf("全部") }
    val options = remember(groups) { groups.flatMap { it.options }.distinctBy { it.id } }
    val filtered = when(filter) {
        "收藏" -> options.filter { it.id in favourites }
        "最近" -> recent.mapNotNull { id -> options.find { it.id == id } }
        "灯光" -> options.filter { StockCatalog.entry(ctx, it.id)?.balance == "tungsten" }
        "日光" -> options.filter { StockCatalog.entry(ctx, it.id)?.balance == "daylight" }
        else -> options
    }
    Text(StockCatalog.displayName(ctx, selectedId), style = MaterialTheme.typography.titleMedium)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(listOf("全部", "收藏", "最近", "日光", "灯光")) { label ->
            FilterChip(selected = filter == label, onClick = { filter = label }, label = { Text(label) }, modifier = Modifier.heightIn(min = 48.dp))
        }
    }
    if (filtered.isEmpty()) Text("这里还没有胶片，可先浏览全部并收藏。", style = MaterialTheme.typography.bodySmall)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 2.dp)) {
        items(filtered, key = { it.id }) { option ->
            val selected = option.id == selectedId
            Column(Modifier.width(140.dp)) {
                Box(Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(12.dp))
                    .border(if(selected) 3.dp else 1.dp, if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .clickable {
                        recent = rememberFilmSelection(recent, option.id)
                        prefs.edit().putString("recent", recent.joinToString("|")).apply()
                        onSelect(option.id)
                    }.semantics { this.selected = selected; role = Role.RadioButton }) {
                    val bitmap by produceState<android.graphics.Bitmap?>(null, option.id) {
                        value = withContext(Dispatchers.IO) {
                            runCatching { ctx.assets.open("film_references/${option.id}.jpg").use { BitmapFactory.decodeStream(it) } }.getOrNull()
                        }
                    }
                    bitmap?.let { Image(it.asImageBitmap(), "${option.label}，参考效果", Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), modifier = Modifier.align(Alignment.TopStart).padding(6.dp), shape = RoundedCornerShape(4.dp)) {
                        Text(if (selected) "✓ 参考效果" else "参考效果", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(4.dp))
                    }
                }
                Text(option.label, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
                Text(option.summary, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = {
                    favourites = if(option.id in favourites) favourites - option.id else favourites + option.id
                    prefs.edit().putStringSet("favourites", favourites.toSet()).apply()
                }, modifier = Modifier.heightIn(min = 48.dp)) { Text(if(option.id in favourites) "★ 已收藏" else "☆ 收藏") }
            }
        }
    }
    Text("同一张基准照片的离线引擎效果；选中后才处理当前照片。", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
internal fun QuickAdjustments(s: ParamsState) {
    Text("调色", style = MaterialTheme.typography.titleMedium)
    QuickSlider("曝光", s.exposureCompensationEv, -10f..10f, 0f, "改变进入胶片的曝光量（EV）。") { s.exposureCompensationEv = it }
    QuickSlider("对比度", s.contrast, -100f..100f, 0f, "增强或减弱明暗反差，保留现有曲线。") { s.toneCurveActive = true; s.contrast = it }
    QuickSlider("色温", s.creativeWbTemp, -100f..100f, 0f, "向左偏冷，向右偏暖；不是开尔文绝对色温。") { s.creativeWbTemp = it }
    QuickSlider("饱和度", s.saturation, -100f..100f, 0f, "调节整体色彩浓淡。") { s.saturation = it }
}

@Composable
private fun QuickSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, default: Float, help: String, onChange: (Float) -> Unit) {
    EnhancedSlider(label, value, range, onChange, default = default, tooltip = help, decimals = if(label == "曝光") 2 else 0)
    TextButton(onClick = { onChange(default) }, enabled = value != default, modifier = Modifier.heightIn(min = 48.dp)) { Text("重置${label} · 默认 $default") }
}

@Composable
internal fun EditorV2Navigation(active: Category?, quick: Boolean, tab: Int, onTab: (Int) -> Unit, onProfessional: (Category) -> Unit) {
    var darkroom by remember { mutableStateOf(false) }
    Surface(color = SpektraBrand.canvasNearBlack) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth()) {
                TextButton(onClick = { darkroom = true }, modifier = Modifier.align(Alignment.CenterEnd).heightIn(min = 48.dp)) { Text("专业暗房 ›") }
                DropdownMenu(expanded = darkroom, onDismissRequest = { darkroom = false }) {
                    Category.entries.forEach { category -> DropdownMenuItem(text = { Text(androidx.compose.ui.res.stringResource(category.labelRes)) }, onClick = { darkroom = false; onProfessional(category) }) }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth().selectableGroup()) {
                listOf("风格", "调色", "质感", "局部").forEachIndexed { index, title ->
                    TextButton(onClick = { onTab(index) }, modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics { selected = quick && tab == index && active != null; role = Role.Tab }, colors = ButtonDefaults.textButtonColors(contentColor = if(quick && tab == index && active != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)) { Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(listOf(SpectraIcons.Simulation, SpectraIcons.ToneCurve, SpectraIcons.Grain, SpectraIcons.Masks)[index], null, Modifier.size(24.dp))
                        Text(title)
                    } }
                }
            }
        }
    }
}
