package com.dullbluelab.textrunnertrial.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dullbluelab.textrunnertrial.RunnerViewModel

@Composable
fun ScreenExecute(
    viewModel: RunnerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Text(
        text = uiState.codeText,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    )
}