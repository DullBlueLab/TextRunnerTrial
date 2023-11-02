package com.dullbluelab.textrunnertrial.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dullbluelab.textrunnertrial.R
import com.dullbluelab.textrunnertrial.RunnerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSetting(
    viewModel: RunnerViewModel,
    modifier: Modifier = Modifier,
    onDumpClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        ) {
        Text(
            text = stringResource(id = R.string.setting_title),
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.size(32.dp))
        Text(
            text = stringResource(id = R.string.label_timer_limit)
        )
        TextField(
            value = uiState.timerLimit.toString(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(200.dp).align(Alignment.End).padding(8.dp),
            onValueChange = { viewModel.updateTimerLimit(it) },
        )
        Spacer(modifier = Modifier.size(32.dp))
        Text(
            text = stringResource(id = R.string.label_loop_limit)
        )
        TextField(
            value = uiState.loopLimit.toString(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(200.dp).align(Alignment.End).padding(8.dp),
            onValueChange = { viewModel.updateLoopLimit(it) }
        )
        Spacer(modifier = Modifier.size(32.dp))
        Text(
            text = stringResource(id = R.string.label_func_limit)
        )
        TextField(
            value = uiState.functionLimit.toString(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(200.dp).align(Alignment.End).padding(8.dp),
            onValueChange = { viewModel.updateFuncLimit(it) },
        )
        Spacer(modifier = Modifier.size(64.dp))
        Text(
            text = stringResource(id = R.string.title_tool),
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.size(32.dp))
        Text(
            text = stringResource(id = R.string.text_dump)
        )
        Button(
            onClick = { onDumpClicked() },
            modifier = Modifier.align(Alignment.End).padding(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.button_dump)
            )
        }
    }
}