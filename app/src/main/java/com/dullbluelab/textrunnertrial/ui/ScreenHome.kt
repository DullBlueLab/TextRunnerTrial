package com.dullbluelab.textrunnertrial.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dullbluelab.textrunnertrial.RunnerViewModel
import com.dullbluelab.textrunnertrial.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHome(
    onExecuteButtonClicked: () -> Unit,
    onClearButtonClicked: () -> Unit,
    launchLoadText: () -> Unit,
    viewModel: RunnerViewModel,
    modifier: Modifier = Modifier
){
    val uiState by viewModel.uiState.collectAsState()

    Canvas(
        modifier = modifier.fillMaxSize()
    )
    {
        viewModel.status().canvasSize = size
    }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = uiState.sourceText,
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier
                .weight(2.0f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            onValueChange = { viewModel.updateSourceText(it) }
        )
        Text(
            text = stringResource(id = R.string.title_console),
            modifier = Modifier
                .padding(8.dp).align(Alignment.CenterHorizontally)

        )
        Text(
            text = uiState.consoleText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(36.dp)
        ) {
            Button(
                modifier = Modifier,
                onClick = { launchLoadText() }
            ) {
                Text(text = stringResource(id = R.string.load_button))
            }
            Button(
                modifier = Modifier,
                onClick = { onExecuteButtonClicked() }
            ) {
                Text(text = stringResource(id = R.string.execute_button))
            }
            Button(
                modifier = Modifier,
                onClick = { onClearButtonClicked() }
            ) {
                Text(text = stringResource(id = R.string.clear_button))
            }
        }
    }
}
