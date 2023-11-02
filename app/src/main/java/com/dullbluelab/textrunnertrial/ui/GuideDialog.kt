package com.dullbluelab.textrunnertrial.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dullbluelab.textrunnertrial.R

@Composable
fun GuideDialog(
    onBrowse: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.wifi_24px),
                contentDescription = stringResource(id = R.string.icon_network)
            )
        },
        title = {
            Text(text = stringResource(id = R.string.title_guide))
        },
        text = {
            Text(text = stringResource(id = R.string.text_guide))
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
                onClick = { onBrowse() }
            ) {
                Text(text = stringResource(id = R.string.button_browse))
            }
        },
        modifier = modifier
    )
}
