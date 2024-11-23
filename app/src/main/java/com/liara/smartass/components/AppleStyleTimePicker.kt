package com.liara.smartass.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val MAX_HOUR = 23
const val MAX_MINUTE = 59

@Composable
fun AppleStyleTimePicker(hour: MutableIntState, minute: MutableIntState) {

    TimePicker()

}

@Composable
fun TimePicker() {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    var selectedHour by remember { mutableIntStateOf(0) }
    var selectedMinute by remember { mutableIntStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.LightGray),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Picker(
            intSelection = hours,
            selectedItem = selectedHour,
            onItemSelected = { selectedHour = it }
        )
        Text(":", fontSize = 24.sp)
        Picker(
            intSelection = minutes,
            selectedItem = selectedMinute,
            onItemSelected = { selectedMinute = it }
        )
    }
}

@Composable
fun Picker(
    intSelection: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedItem) {
        listState.scrollToItem(selectedItem)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .width(60.dp)
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(intSelection) { item ->
            val isSelected = item == selectedItem
            Text(
                text = item.toString().padStart(2, '0'),
                fontSize = if (isSelected) 24.sp else 20.sp,
                color = if (isSelected) Color.Black else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .fillMaxWidth()
            )
        }
    }

    LaunchedEffect(remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }) {
        val firstVisibleItemIndex = listState.firstVisibleItemIndex
        val firstVisibleItemScrollOffset = listState.firstVisibleItemScrollOffset

        val middleItemIndex =
            firstVisibleItemIndex + (if (firstVisibleItemScrollOffset > 100) 1 else 0)
        val closestIndex = middleItemIndex.coerceIn(0, intSelection.size - 1)

        if (selectedItem != closestIndex) {
            onItemSelected(closestIndex)
        }
    }
}