package com.example.tumbuh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tumbuh.data.api.ApiService
import com.example.tumbuh.ui.theme.TumbuhTheme
import com.example.tumbuh.ui.viewmodel.ApiValidationState
import com.example.tumbuh.ui.viewmodel.PlantViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TumbuhTheme {
                ApiTestScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTestScreen() {
    val context = LocalContext.current
    val viewModel: PlantViewModel = viewModel()
    val apiValidationState by viewModel.apiValidationState

    // Auto-validate on first load
    LaunchedEffect(Unit) {
        viewModel.validateApiSetup(context)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Tumbuh - API Test") },
                actions = {
                    IconButton(
                        onClick = { viewModel.validateApiSetup(context) }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌱 Tumbuh",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Plant Identifier App",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // API Status Card
            ApiStatusCard(
                validationState = apiValidationState,
                onRetry = { viewModel.validateApiSetup(context) }
            )

            // Current API Configuration
            ApiConfigCard()

            // Test Buttons
            TestActionsCard(
                onTestDemo = { viewModel.testWithDemoData() },
                onValidateApi = { viewModel.validateApiSetup(context) }
            )

            // API Instructions
            if (apiValidationState is ApiValidationState.InvalidApiKey) {
                ApiInstructionsCard(viewModel.getApiKeyInstructions())
            }
        }
    }
}

@Composable
fun ApiStatusCard(
    validationState: ApiValidationState,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "API Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (validationState) {
                is ApiValidationState.NotChecked -> {
                    StatusRow(
                        icon = Icons.Default.Refresh,
                        text = "Not checked yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is ApiValidationState.Checking -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text("Checking API connection...")
                    }
                }
                is ApiValidationState.Valid -> {
                    StatusRow(
                        icon = Icons.Default.Check,
                        text = "API connection successful",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is ApiValidationState.InvalidApiKey -> {
                    StatusRow(
                        icon = Icons.Default.Error,
                        text = "Invalid API key - Please configure",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is ApiValidationState.NetworkError -> {
                    StatusRow(
                        icon = Icons.Default.Error,
                        text = "Network connection error",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Retry")
                    }
                }
                is ApiValidationState.Error -> {
                    StatusRow(
                        icon = Icons.Default.Error,
                        text = validationState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun StatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(text, color = color)
    }
}

@Composable
fun ApiConfigCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Current Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            ConfigRow("API Key", if (ApiService.isValidApiKey(BuildConfig.PLANTNET_API_KEY)) "✓ Set" else "⚠ Not set")
            ConfigRow("Base URL", "my-api.plantnet.org")
//            ConfigRow("Project", ApiService.Projects.WORLD_FLORA)
//            ConfigRow("Default Organ", ApiService.Organs.LEAF)
        }
    }
}

@Composable
fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun TestActionsCard(
    onTestDemo: () -> Unit,
    onValidateApi: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Test Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onTestDemo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test with Demo Data")
            }

            OutlinedButton(
                onClick = onValidateApi,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Validate API Setup")
            }
        }
    }
}

@Composable
fun ApiInstructionsCard(instructions: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚠️ API Key Required",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = instructions,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
