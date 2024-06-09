package com.dullbluelab.textrunnertrial.ui.library

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dullbluelab.textrunnertrial.R

@Composable
fun ItemScreen(
    viewModel: LibraryViewModel,
    done: () -> Unit,
    modifier: Modifier = Modifier
) {
    val itemUi by viewModel.itemUi.collectAsState()
    val context = LocalContext.current
    val textInputName =  stringResource(id = R.string.text_input_name)

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
        ) {
            Text(text = "name : ")
            TextField(
                value = itemUi.rename,
                onValueChange = { value ->
                    viewModel.updateRename(value)
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        itemUi.bitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
        ) {
            Button(onClick = { done() }) {
                Text(text = stringResource(id = R.string.button_cancel))
            }
            when (itemUi.mode) {
                "edit" -> {
                    Button(onClick = {
                        if (itemUi.rename.isEmpty()) {
                            Toast.makeText(context, textInputName, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.renameItem()
                            done()
                        }
                    }) {
                        Text(text = stringResource(id = R.string.name_submit))
                    }
                    Button(onClick = {
                        viewModel.showDeleteDialog(true)
                    }) {
                        Text(text = stringResource(id = R.string.name_delete))
                    }
                }
                "load" -> {
                    Button(onClick = {
                        if (itemUi.rename.isEmpty()) {
                            Toast.makeText(context, textInputName, Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveItem {
                                done()
                            }
                        }
                    }) {
                        Text(text = stringResource(id = R.string.name_store))
                    }
                }
            }
        }
    }
    if (itemUi.deleteDialogFlag) {
        DeleteDialog(
            itemName = itemUi.rename,
            delete = {
                viewModel.showDeleteDialog(false)
                viewModel.deleteItem()
                done()
            },
            cancel = {
                viewModel.showDeleteDialog(false)
            }
        )
    }
    if (itemUi.progressFlag) {
        val color = MaterialTheme.colorScheme.background
        val background = Color(color.red, color.green, color.blue, alpha = 0.75f)
        Box(modifier = Modifier
            .fillMaxSize()
            .background(background)) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }
    }
}
