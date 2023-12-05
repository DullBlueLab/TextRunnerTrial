package com.dullbluelab.textrunnertrial.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dullbluelab.textrunnertrial.R

@Composable
fun ClearDataDialog(
    onClear: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = stringResource(id = R.string.label_warning)
            )
        },
        title = {
            Text(text = stringResource(id = R.string.clear_data_title))
        },
        text = {
            Text(text = stringResource(id = R.string.text_clear_data_dialog))
        },
        onDismissRequest = {
            onCancel()
        },
        dismissButton = {
            TextButton(
                onClick = { onCancel() }
            ) {
                Text(text = stringResource(id = R.string.button_cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onClear() }
            ) {
                Text(text = stringResource(id = R.string.clear_button))
            }
        },
        modifier = modifier
    )

}