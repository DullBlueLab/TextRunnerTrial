package com.dullbluelab.textrunnertrial.ui.library

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
fun NoNameAlert(
    modifier: Modifier = Modifier,
    cancel: () -> Unit,
    confirm: () -> Unit
) {
    val message = stringResource(id = R.string.text_name_alert)

    AlertDialog(
        icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null) },
        text = { Text(text = message) },
        onDismissRequest = { cancel() },
        confirmButton = {
            TextButton(onClick = { confirm() }) {
                Text(text = stringResource(id = R.string.name_ok))
            }
        },
        modifier = modifier
    )
}