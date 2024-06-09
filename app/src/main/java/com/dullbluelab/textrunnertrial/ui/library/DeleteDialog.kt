package com.dullbluelab.textrunnertrial.ui.library

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dullbluelab.textrunnertrial.R
@Composable
fun DeleteDialog(
    itemName: String,
    delete: () -> Unit,
    cancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = stringResource(id = R.string.text_delete_image) + itemName + "?"

    AlertDialog(
        text = { Text(text = message) },
        onDismissRequest = { cancel() },
        dismissButton = {
            TextButton(onClick = { cancel() }) {
                Text(text = stringResource(id = R.string.button_cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { delete() }) {
                Text(text = stringResource(id = R.string.name_delete))
            }
        },
        modifier = modifier
    )
}