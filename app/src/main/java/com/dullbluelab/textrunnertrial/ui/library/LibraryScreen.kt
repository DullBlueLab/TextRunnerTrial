package com.dullbluelab.textrunnertrial.ui.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.dullbluelab.textrunnertrial.data.DirectoryTable
import com.dullbluelab.textrunnertrial.data.ImageLibrary

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onItemClick: () -> Unit,
    onLoadClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp)
) {
    val state by viewModel.libraryUi.collectAsState()

    Column(

    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = contentPadding
        ) {
            items(
                items = state.list,
                key = { item -> item.name }
            ) { item ->
                LibraryCard(
                    item = item,
                    onClick = { clickItem ->
                        viewModel.selectItem(clickItem)
                        onItemClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
        ) {
            Button(
                onClick = { onLoadClick() }
            ) {
                Text(text = "Load")
            }
        }
    }
}

@Composable
private fun LibraryCard(
    item: ImageLibrary.Table,
    onClick: (DirectoryTable) -> Unit,
    modifier: Modifier = Modifier
) {
    val sizeText = "${item.width} x ${item.height}"

    Card(
        modifier = modifier.padding(4.dp).clickable { onClick(item.directory) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                bitmap = item.thumbnailBitmap.asImageBitmap(),
                contentDescription = "image",
                modifier = Modifier.size(100.dp)
            )
            Column(
                modifier = Modifier.weight(1f).padding(16.dp, 8.dp)
            ) {
                Text(
                    text = item.directory.name,
                    modifier = Modifier
                )
                Text(
                    text = sizeText,
                    modifier = Modifier
                )
            }
        }
    }
}